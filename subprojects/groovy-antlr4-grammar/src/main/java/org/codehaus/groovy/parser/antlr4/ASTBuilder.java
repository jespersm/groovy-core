/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.parser.antlr4;

import groovy.lang.Closure;
import groovy.lang.IntRange;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.antlr.EnumHelper;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.parser.antlr4.util.StringUtil;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.syntax.Numbers;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.*;

@SuppressWarnings("ALL")
public class ASTBuilder {

    public ASTBuilder(final SourceUnit sourceUnit, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.sourceUnit = sourceUnit;
        this.moduleNode = new ModuleNode(sourceUnit);

        String text = this.readSourceCode(sourceUnit);

        if (log.isLoggable(Level.FINE)) {
            this.logTokens(text);
        }

        GroovyLexer lexer = new GroovyLexer(new ANTLRInputStream(text));
        GroovyParser parser = new GroovyParser(new CommonTokenStream(lexer));

        this.setUpErrorListener(parser);

        this.startParsing(parser);
    }

    private void startParsing(GroovyParser parser) {
        GroovyParser.CompilationUnitContext tree = parser.compilationUnit();

        if (log.isLoggable(Level.FINE)) {
            this.logTreeStr(tree);
        }

        try {
            DefaultGroovyMethods.each(tree.importStatement(), new MethodClosure(this, "parseImportStatement"));
            DefaultGroovyMethods.each(tree.children, new Closure<ClassNode>(this, this) {
                public ClassNode doCall(ParseTree it) {
                    if (it instanceof GroovyParser.EnumDeclarationContext)
                        parseEnumDeclaration((GroovyParser.EnumDeclarationContext)it);
                    else if (it instanceof GroovyParser.ClassDeclarationContext)
                        return parseClassDeclaration((GroovyParser.ClassDeclarationContext)it);
                    else if (it instanceof GroovyParser.PackageDefinitionContext)
                        parsePackageDefinition((GroovyParser.PackageDefinitionContext)it);
                    return null;
                }
            });
            for (GroovyParser.ScriptPartContext part : tree.scriptPart()) {
                if (part.statement() != null) {
                    unpackStatement(moduleNode, parseStatement(part.statement()));
                } else {
                    moduleNode.addMethod(parseScriptMethod(part.methodDeclaration()));
                }
            }
        } catch (CompilationFailedException ignored) {
            // Compilation failed.
        }
    }

    public void parseImportStatement(@NotNull GroovyParser.ImportStatementContext ctx) {
        ImportNode node;
        List<TerminalNode> qualifiedClassName = new ArrayList<TerminalNode>(ctx.IDENTIFIER());
        boolean isStar = ctx.MULT() != null;
        boolean isStatic = ctx.KW_STATIC() != null;
        String alias = (ctx.KW_AS() != null) ? DefaultGroovyMethods.pop(qualifiedClassName).getText() : null;
        List<AnnotationNode> annotations = parseAnnotations(ctx.annotationClause());

        if (isStar) {
            if (isStatic) {
                // import is like "import static foo.Bar.*"
                // packageName is actually a className in this case
                ClassNode type = ClassHelper.make(DefaultGroovyMethods.join(qualifiedClassName, "."));
                moduleNode.addStaticStarImport(DefaultGroovyMethods.last(qualifiedClassName).getText(), type, annotations);
            } else {
                // import is like "import foo.*"
                moduleNode.addStarImport(DefaultGroovyMethods.join(qualifiedClassName, ".") + ".", annotations);
            }
            node = DefaultGroovyMethods.last(moduleNode.getStarImports());
            if (alias != null) throw new GroovyBugError(
                "imports like 'import foo.* as Bar' are not " +
                    "supported and should be caught by the grammar");
        } else {
            if (isStatic) {
                // import is like "import static foo.Bar.method"
                // packageName is really class name in this case
                String fieldName = DefaultGroovyMethods.pop(qualifiedClassName).getText();
                ClassNode type = ClassHelper.make(DefaultGroovyMethods.join(qualifiedClassName, "."));
                moduleNode.addStaticImport(type, fieldName, alias != null ? alias : fieldName, annotations);
            } else {
                // import is like "import foo.Bar"
                ClassNode type = ClassHelper.make(DefaultGroovyMethods.join(qualifiedClassName, "."));
                if (alias == null) {
                    alias = DefaultGroovyMethods.last(qualifiedClassName).getText();
                }
                moduleNode.addImport(alias, type, annotations);
            }
            node = DefaultGroovyMethods.last(moduleNode.getImports());
        }
        setupNodeLocation(node, ctx);
    }

    public void parsePackageDefinition(@NotNull GroovyParser.PackageDefinitionContext ctx) {
        moduleNode.setPackageName(DefaultGroovyMethods.join(ctx.IDENTIFIER(), ".") + ".");
        attachAnnotations(moduleNode.getPackage(), ctx.annotationClause());
        setupNodeLocation(moduleNode.getPackage(), ctx);
    }

    private void unpackStatement(ModuleNode destination, Statement stmt) {
        if (stmt instanceof DeclarationList) {
            for (DeclarationExpression decl : ((DeclarationList)stmt).declarations) {
                destination.addStatement(setupNodeLocation(new ExpressionStatement(decl), decl));
            }
        } else {
            destination.addStatement(stmt);
        }
    }

    private void unpackStatement(BlockStatement destination, Statement stmt) {
        if (stmt instanceof DeclarationList) {
            for (DeclarationExpression decl : ((DeclarationList)stmt).declarations) {
                destination.addStatement(setupNodeLocation(new ExpressionStatement(decl), decl));
            }
        } else {
            destination.addStatement(stmt);
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    private MethodNode parseMethod(ClassNode classNode, GroovyParser.MethodDeclarationContext ctx, Closure<MethodNode> createMethodNode) {
        //noinspection GroovyAssignabilityCheck
        final Iterator<Object> iterator = parseModifiers(ctx.memberModifier(), Opcodes.ACC_PUBLIC).iterator();
        int modifiers = ((Integer)(iterator.hasNext() ? iterator.next() : null));
        boolean hasVisibilityModifier = ((Boolean)(iterator.hasNext() ? iterator.next() : null));

        innerClassesDefinedInMethod.add(new ArrayList());
        Statement statement = asBoolean(ctx.methodBody())
                ? parseStatement(ctx.methodBody().blockStatement())
                : null;
        List<InnerClassNode> innerClassesDeclared = innerClassesDefinedInMethod.pop();

        Parameter[] params = parseParameters(ctx.argumentDeclarationList());

        ClassNode returnType = asBoolean(ctx.typeDeclaration())
                ? parseTypeDeclaration(ctx.typeDeclaration())
                : asBoolean(ctx.genericClassNameExpression())
                ? parseExpression(ctx.genericClassNameExpression())
                : ClassHelper.OBJECT_TYPE;

        ClassNode[] exceptions = parseThrowsClause(ctx.throwsClause());


        String methodName = (null != ctx.IDENTIFIER()) ? ctx.IDENTIFIER().getText() : parseString(ctx.STRING());

        MethodNode methodNode = createMethodNode.call(classNode, ctx, methodName, modifiers, returnType, params, exceptions, statement, innerClassesDeclared);

        setupNodeLocation(methodNode, ctx);
        attachAnnotations(methodNode, ctx.annotationClause());
        methodNode.setSyntheticPublic(!hasVisibilityModifier);
        return methodNode;
    }


    @SuppressWarnings("GroovyUnusedDeclaration")
    public MethodNode parseScriptMethod(final GroovyParser.MethodDeclarationContext ctx) {

        return parseMethod(null, ctx, new Closure<MethodNode>(this, this) {
                                                public MethodNode doCall(ClassNode classNode, GroovyParser.MethodDeclarationContext ctx, String methodName, int modifiers, ClassNode returnType, Parameter[] params, ClassNode[] exceptions, Statement statement, List<InnerClassNode> innerClassesDeclared) {

                                                    final MethodNode methodNode = new MethodNode(methodName, modifiers, returnType, params, exceptions, statement);
                                                    methodNode.setGenericsTypes(parseGenericDeclaration(ctx.genericDeclarationList()));
                                                    methodNode.setAnnotationDefault(true);

                                                    return methodNode;
                                                }
                                      }
        );
    }

    public void parseEnumDeclaration(@NotNull GroovyParser.EnumDeclarationContext ctx) {
        List list = asBoolean(ctx.implementsClause())
                    ? collect(ctx.implementsClause().genericClassNameExpression(), new Closure<ClassNode>(this, this) {
            public ClassNode doCall(GroovyParser.GenericClassNameExpressionContext it) {return parseExpression(it);}
        }) : new ArrayList();
        ClassNode[] interfaces = (ClassNode[])list.toArray(new ClassNode[list.size()]);
        final ClassNode classNode = EnumHelper.makeEnumNode(ctx.IDENTIFIER().getText(), Modifier.PUBLIC, interfaces, null);// FIXME merge with class declaration.
        setupNodeLocation(classNode, ctx);
        attachAnnotations(classNode, ctx.annotationClause());
        moduleNode.addClass(classNode);

        classNode.setModifiers(parseClassModifiers(ctx.classModifier()) | Opcodes.ACC_ENUM | Opcodes.ACC_FINAL);
        classNode.setSyntheticPublic((classNode.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0);
        classNode.setModifiers(classNode.getModifiers() & ~Opcodes.ACC_SYNTHETIC);// FIXME Magic with synthetic modifier.

        List<TerminalNode> enumConstants = collect(DefaultGroovyMethods.grep(ctx.enumMember(), new Closure<TerminalNode>(this, this) {
            public TerminalNode doCall(GroovyParser.EnumMemberContext e) {return e.IDENTIFIER();}

        }), new Closure<TerminalNode>(this, this) {
            public TerminalNode doCall(GroovyParser.EnumMemberContext it) {return it.IDENTIFIER();}
        });
        List<GroovyParser.ClassMemberContext> classMembers = collect(DefaultGroovyMethods.grep(ctx.enumMember(), new Closure<GroovyParser.ClassMemberContext>(this, this) {
            public GroovyParser.ClassMemberContext doCall(GroovyParser.EnumMemberContext e) {return e.classMember();}

        }), new Closure<GroovyParser.ClassMemberContext>(this, this) {
            public GroovyParser.ClassMemberContext doCall(GroovyParser.EnumMemberContext it) {return it.classMember();}
        });
        DefaultGroovyMethods.each(enumConstants, new Closure<FieldNode>(this, this) {
            public FieldNode doCall(TerminalNode it) {
                return setupNodeLocation(EnumHelper.addEnumConstant(classNode, it.getText(), null), it.getSymbol());
            }
        });
        parseMembers(classNode, classMembers);
    }

    public ClassNode parseClassDeclaration(@NotNull final GroovyParser.ClassDeclarationContext ctx) {
        ClassNode classNode;
        final ClassNode parentClass = asBoolean(classes) ? classes.peek() : null;
        if (parentClass != null) {
            String string = parentClass.getName() + "$" + String.valueOf(ctx.IDENTIFIER());
            classNode = new InnerClassNode(parentClass, string, Modifier.PUBLIC, ClassHelper.OBJECT_TYPE);
        } else {
            final String name = moduleNode.getPackageName();
            classNode = new ClassNode((name != null && asBoolean(name) ? name : "") + String.valueOf(ctx.IDENTIFIER()), Modifier.PUBLIC, ClassHelper.OBJECT_TYPE);
        }


        setupNodeLocation(classNode, ctx);
        attachAnnotations(classNode, ctx.annotationClause());
        moduleNode.addClass(classNode);
        if (asBoolean(ctx.extendsClause()))
            (classNode).setSuperClass(parseExpression(ctx.extendsClause().genericClassNameExpression()));
        if (asBoolean(ctx.implementsClause()))
            (classNode).setInterfaces(DefaultGroovyMethods.asType(collect(ctx.implementsClause().genericClassNameExpression(), new Closure<ClassNode>(this, this) {
                public ClassNode doCall(GroovyParser.GenericClassNameExpressionContext it) {return parseExpression(it);}
            }), ClassNode[].class));

        (classNode).setGenericsTypes(parseGenericDeclaration(ctx.genericDeclarationList()));
        (classNode).setUsingGenerics((classNode.getGenericsTypes() != null && classNode.getGenericsTypes().length != 0) || (classNode).getSuperClass().isUsingGenerics() || DefaultGroovyMethods.any(classNode.getInterfaces(), new Closure<Boolean>(this, this) {
            public Boolean doCall(ClassNode it) {return it.isUsingGenerics();}
        }));
        classNode.setModifiers(parseClassModifiers(ctx.classModifier()) | (asBoolean(ctx.KW_INTERFACE())
                                                                           ? Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT
                                                                           : 0));
        classNode.setSyntheticPublic((classNode.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0);
        classNode.setModifiers(classNode.getModifiers() & ~Opcodes.ACC_SYNTHETIC);// FIXME Magic with synthetic modifier.

        if (asBoolean(ctx.AT())) {
            classNode.addInterface(ClassHelper.Annotation_TYPE);
            classNode.setModifiers(classNode.getModifiers() | Opcodes.ACC_ANNOTATION);
        }


        classes.add(classNode);
        parseMembers(classNode, ctx.classBody().classMember());
        classes.pop();

        if (classNode.isInterface()) { // FIXME why interface has null mixin
            try {
                // FIXME Hack with visibility.
                Field field = classNode.getClass().getDeclaredField("mixins");
                field.setAccessible(true);
                field.set(classNode, null);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return classNode;
    }

    public void parseMembers(ClassNode classNode, List<GroovyParser.ClassMemberContext> ctx) {
        for (GroovyParser.ClassMemberContext member : ctx) {
            ParseTree memberContext = DefaultGroovyMethods.last(member.children);

            ASTNode memberNode = null;
            if (memberContext instanceof GroovyParser.ClassDeclarationContext)
                memberNode = parseClassDeclaration(DefaultGroovyMethods.asType(memberContext, GroovyParser.ClassDeclarationContext.class));
            else if (memberContext instanceof GroovyParser.EnumDeclarationContext)
                parseEnumDeclaration(DefaultGroovyMethods.asType(memberContext, GroovyParser.EnumDeclarationContext.class));
            else if (memberContext instanceof GroovyParser.ConstructorDeclarationContext)
                memberNode = parseMember(classNode, (GroovyParser.ConstructorDeclarationContext)memberContext);
            else if (memberContext instanceof GroovyParser.MethodDeclarationContext)
                memberNode = parseMember(classNode, (GroovyParser.MethodDeclarationContext)memberContext);
            else if (memberContext instanceof GroovyParser.FieldDeclarationContext)
                memberNode = parseMember(classNode, (GroovyParser.FieldDeclarationContext)memberContext);
            else if (memberContext instanceof GroovyParser.ObjectInitializerContext)
                parseMember(classNode, (GroovyParser.ObjectInitializerContext)memberContext);
            else if (memberContext instanceof GroovyParser.ClassInitializerContext)
                parseMember(classNode, (GroovyParser.ClassInitializerContext)memberContext);
            else
                assert false : "Unknown class member type.";
            if (asBoolean(memberNode)) setupNodeLocation(memberNode, member);
            if (member.getChildCount() > 1) {
                assert memberNode != null;
                for (int i = 0; i < member.children.size() - 2; i++) {
                    ParseTree annotationCtx = member.children.get(i);
                    assert annotationCtx instanceof GroovyParser.AnnotationClauseContext;
                    ((AnnotatedNode)memberNode).addAnnotation(parseAnnotation((GroovyParser.AnnotationClauseContext)annotationCtx));
                }

            }

        }

    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public AnnotatedNode parseMember(ClassNode classNode, GroovyParser.MethodDeclarationContext ctx) {
        return parseMethod(classNode, ctx, new Closure<MethodNode>(this, this) {
                    public MethodNode doCall(ClassNode classNode, GroovyParser.MethodDeclarationContext ctx, String methodName, int modifiers, ClassNode returnType, Parameter[] params, ClassNode[] exceptions, Statement statement, List<InnerClassNode> innerClassesDeclared) {
                        modifiers |= classNode.isInterface() ? Opcodes.ACC_ABSTRACT : 0;

                        if (ctx.KW_DEFAULT() != null) {
                            statement = new ExpressionStatement(parseExpression(ctx.annotationParameter()));
                        }

                        final MethodNode methodNode = classNode.addMethod(methodName, modifiers, returnType, params, exceptions, statement);
                        methodNode.setGenericsTypes(parseGenericDeclaration(ctx.genericDeclarationList()));
                        DefaultGroovyMethods.each(innerClassesDeclared, new Closure<MethodNode>(this, this) {
                            public MethodNode doCall(InnerClassNode it) {
                                it.setEnclosingMethod(methodNode);
                                return methodNode;
                            }
                        });

                        if (ctx.KW_DEFAULT() != null) {
                            methodNode.setAnnotationDefault(true);
                        }


                        return methodNode;
                    }
                }
        );
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public AnnotatedNode parseMember(ClassNode classNode, GroovyParser.FieldDeclarationContext ctx) {
        //noinspection GroovyAssignabilityCheck
        final Iterator<Object> iterator = parseModifiers(ctx.memberModifier()).iterator();
        int modifiers = ((Integer)(iterator.hasNext() ? iterator.next() : null));
        boolean hasVisibilityModifier = ((Boolean)(iterator.hasNext() ? iterator.next() : null));

        modifiers |= classNode.isInterface() ? Opcodes.ACC_STATIC | Opcodes.ACC_FINAL : 0;


        AnnotatedNode node = null;
        List<GroovyParser.SingleDeclarationContext> variables = ctx.singleDeclaration();
        for (GroovyParser.SingleDeclarationContext variableCtx : variables) {
            GroovyParser.ExpressionContext initExprContext = variableCtx.expression();
            Expression initialierExpression = asBoolean(initExprContext)
                ? parseExpression(initExprContext)
                : null;
            ClassNode typeDeclaration = asBoolean(ctx.genericClassNameExpression())
                ? parseExpression(ctx.genericClassNameExpression())
                : ClassHelper.OBJECT_TYPE;
            Expression initialValue = classNode.isInterface() && !typeDeclaration.equals(ClassHelper.OBJECT_TYPE)
                ? new ConstantExpression(initialExpressionForType(typeDeclaration))
                : initialierExpression;
            if (classNode.isInterface() || hasVisibilityModifier) {
                modifiers |= classNode.isInterface() ? Opcodes.ACC_PUBLIC : 0;

                FieldNode field = classNode.addField(variableCtx.IDENTIFIER().getText(), modifiers, typeDeclaration, initialValue);
                attachAnnotations(field, ctx.annotationClause());
                node = setupNodeLocation(field, variables.size() == 1 ? ctx : variableCtx);
            } else {// no visibility specified. Generate property node.
                Integer propertyModifier = modifiers | Opcodes.ACC_PUBLIC;
                PropertyNode propertyNode = classNode.addProperty(variableCtx.IDENTIFIER().getText(), propertyModifier, typeDeclaration, initialValue, null, null);
                propertyNode.getField().setModifiers(modifiers | Opcodes.ACC_PRIVATE);
                propertyNode.getField().setSynthetic(!classNode.isInterface());
                node = setupNodeLocation(propertyNode.getField(), variables.size() == 1 ? ctx : variableCtx);
                attachAnnotations(propertyNode.getField(), ctx.annotationClause());
                setupNodeLocation(propertyNode, variables.size() == 1 ? ctx : variableCtx);
            }
        }
        return node;
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public void parseMember(ClassNode classNode, GroovyParser.ClassInitializerContext ctx) {
        unpackStatement((BlockStatement)getOrCreateClinitMethod(classNode).getCode(), parseStatement(ctx.blockStatement()));
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public void parseMember(ClassNode classNode, GroovyParser.ObjectInitializerContext ctx) {
        BlockStatement statement = new BlockStatement();
        unpackStatement(statement, parseStatement(ctx.blockStatement()));
        classNode.addObjectInitializerStatements(statement);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public AnnotatedNode parseMember(ClassNode classNode, GroovyParser.ConstructorDeclarationContext ctx) {
        int modifiers = asBoolean(ctx.VISIBILITY_MODIFIER())
                        ? parseVisibilityModifiers(ctx.VISIBILITY_MODIFIER())
                        : Opcodes.ACC_PUBLIC;

        ClassNode[] exceptions = parseThrowsClause(ctx.throwsClause());
        this.innerClassesDefinedInMethod.add(new ArrayList());
        final ConstructorNode constructorNode = classNode.addConstructor(modifiers, parseParameters(ctx.argumentDeclarationList()), exceptions, parseStatement(DefaultGroovyMethods.asType(ctx.blockStatement(), GroovyParser.BlockStatementContext.class)));
        DefaultGroovyMethods.each(this.innerClassesDefinedInMethod.pop(), new Closure<ConstructorNode>(null, null) {
            public ConstructorNode doCall(InnerClassNode it) {
                it.setEnclosingMethod(constructorNode);
                return constructorNode;
            }
        });
        setupNodeLocation(constructorNode, ctx);
        constructorNode.setSyntheticPublic(ctx.VISIBILITY_MODIFIER() == null);
        return constructorNode;
    }

    private static class DeclarationList extends Statement{
        List<DeclarationExpression> declarations;

        DeclarationList(List<DeclarationExpression> declarations) {
            this.declarations = declarations;
        }
    }

    public Statement parseStatement(GroovyParser.StatementContext ctx) {
        if (ctx instanceof GroovyParser.ForColonStatementContext)
            parseStatement((GroovyParser.ForColonStatementContext)ctx);
        if (ctx instanceof GroovyParser.IfStatementContext)
            return parseStatement((GroovyParser.IfStatementContext)ctx);
        if (ctx instanceof GroovyParser.NewArrayStatementContext)
            return parseStatement((GroovyParser.NewArrayStatementContext)ctx);
        if (ctx instanceof GroovyParser.TryCatchFinallyStatementContext)
            return parseStatement((GroovyParser.TryCatchFinallyStatementContext)ctx);
        if (ctx instanceof GroovyParser.ThrowStatementContext)
            return parseStatement((GroovyParser.ThrowStatementContext)ctx);
        if (ctx instanceof GroovyParser.ClassicForStatementContext)
            return parseStatement((GroovyParser.ClassicForStatementContext)ctx);
        if (ctx instanceof GroovyParser.DeclarationStatementContext)
            return parseStatement((GroovyParser.DeclarationStatementContext)ctx);
        if (ctx instanceof GroovyParser.ReturnStatementContext)
            return parseStatement((GroovyParser.ReturnStatementContext)ctx);
        if (ctx instanceof GroovyParser.ExpressionStatementContext)
            return parseStatement((GroovyParser.ExpressionStatementContext)ctx);
        if (ctx instanceof GroovyParser.ForInStatementContext)
            return parseStatement((GroovyParser.ForInStatementContext)ctx);
        if (ctx instanceof GroovyParser.ForColonStatementContext)
            return parseStatement((GroovyParser.ForColonStatementContext)ctx);
        if (ctx instanceof GroovyParser.SwitchStatementContext)
            return parseStatement((GroovyParser.SwitchStatementContext)ctx);
        if (ctx instanceof GroovyParser.WhileStatementContext)
            return parseStatement((GroovyParser.WhileStatementContext)ctx);
        if (ctx instanceof GroovyParser.ControlStatementContext)
            return parseStatement((GroovyParser.ControlStatementContext)ctx);
        if (ctx instanceof GroovyParser.CommandExpressionStatementContext)
            return parseStatement((GroovyParser.CommandExpressionStatementContext)ctx);
        if (ctx instanceof GroovyParser.NewInstanceStatementContext)
            return parseStatement((GroovyParser.NewInstanceStatementContext)ctx);
        if (ctx instanceof GroovyParser.AssertStatementContext)
            return parseStatement((GroovyParser.AssertStatementContext)ctx);
        throw new RuntimeException("Unsupported statement type! " + ctx.getText());
    }

    public Statement parseStatement(GroovyParser.BlockStatementContext ctx) {
        final BlockStatement statement = new BlockStatement();
        if (!asBoolean(ctx)) return statement;

        DefaultGroovyMethods.each(ctx.statement(), new Closure<Object>(null, null) {
            public void doCall(GroovyParser.StatementContext it) {
                unpackStatement(statement, parseStatement(it));
            }
        });
        return setupNodeLocation(statement, ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.ExpressionStatementContext ctx) {
        return setupNodeLocation(new ExpressionStatement(parseExpression(ctx.expression())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.IfStatementContext ctx) {
        Statement trueBranch = parse(ctx.statementBlock(0));
        Statement falseBranch = asBoolean(ctx.KW_ELSE())
                                ? parse(ctx.statementBlock(1))
                                : EmptyStatement.INSTANCE;
        BooleanExpression expression = new BooleanExpression(parseExpression(ctx.expression()));
        return setupNodeLocation(new IfStatement(expression, trueBranch, falseBranch), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.WhileStatementContext ctx) {
        return setupNodeLocation(new WhileStatement(new BooleanExpression(parseExpression(ctx.expression())), parse(ctx.statementBlock())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.ClassicForStatementContext ctx) {
        ClosureListExpression expression = new ClosureListExpression();

        Boolean captureNext = false;
        for (ParseTree c : ctx.children) {
            // FIXME terrible logic.
            Boolean isSemicolon = c instanceof TerminalNode && (((TerminalNode)c).getSymbol().getText().equals(";") || ((TerminalNode)c).getSymbol().getText().equals("(") || ((TerminalNode)c).getSymbol().getText().equals(")"));
            if (captureNext && isSemicolon) expression.addExpression(EmptyExpression.INSTANCE);
            else if (captureNext && c instanceof GroovyParser.ExpressionContext)
                expression.addExpression(parseExpression((GroovyParser.ExpressionContext)c));
            captureNext = isSemicolon;
        }


        Parameter parameter = ForStatement.FOR_LOOP_DUMMY;
        return setupNodeLocation(new ForStatement(parameter, expression, parse(ctx.statementBlock())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.ForInStatementContext ctx) {
        Parameter parameter = new Parameter(parseTypeDeclaration(ctx.typeDeclaration()), ctx.IDENTIFIER().getText());
        parameter = setupNodeLocation(parameter, ctx.IDENTIFIER().getSymbol());

        return setupNodeLocation(new ForStatement(parameter, parseExpression(ctx.expression()), parse(ctx.statementBlock())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.ForColonStatementContext ctx) {
        if (!asBoolean(ctx.typeDeclaration()))
            throw new RuntimeException("Classic for statement require type to be declared.");
        Parameter parameter = new Parameter(parseTypeDeclaration(ctx.typeDeclaration()), ctx.IDENTIFIER().getText());
        parameter = setupNodeLocation(parameter, ctx.IDENTIFIER().getSymbol());

        return setupNodeLocation(new ForStatement(parameter, parseExpression(ctx.expression()), parse(ctx.statementBlock())), ctx);
    }

    public Statement parse(GroovyParser.StatementBlockContext ctx) {
        if (asBoolean(ctx.statement()))
            return setupNodeLocation(parseStatement(ctx.statement()), ctx.statement());
        else return parseStatement(ctx.blockStatement());
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.SwitchStatementContext ctx) {
        List<CaseStatement> caseStatements = new ArrayList<CaseStatement>();
        for (GroovyParser.CaseStatementContext caseStmt : ctx.caseStatement()) {
            BlockStatement stmt = new BlockStatement();// #BSC
            for (GroovyParser.StatementContext st : caseStmt.statement()) {
                unpackStatement (stmt, parseStatement(st));
            }
            caseStatements.add(setupNodeLocation(new CaseStatement(parseExpression(caseStmt.expression()), stmt), caseStmt.KW_CASE().getSymbol()));// There only 'case' kw was highlighted in parser old version.
        }


        Statement defaultStatement;
        if (asBoolean(ctx.KW_DEFAULT())) {
            defaultStatement = new BlockStatement();// #BSC
            for (GroovyParser.StatementContext stmt : ctx.statement())
                unpackStatement((BlockStatement)defaultStatement,parseStatement(stmt));
        } else defaultStatement = EmptyStatement.INSTANCE;// TODO Refactor empty stataements and expressions.

        return new SwitchStatement(parseExpression(ctx.expression()), caseStatements, defaultStatement);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.DeclarationStatementContext ctx) {
        List<DeclarationExpression> declarations = parseDeclaration(ctx.declarationRule());
        return setupNodeLocation(new DeclarationList(declarations), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.NewArrayStatementContext ctx) {
        return setupNodeLocation(new ExpressionStatement(parse(ctx.newArrayRule())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.NewInstanceStatementContext ctx) {
        return setupNodeLocation(new ExpressionStatement(parse(ctx.newInstanceRule())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.ControlStatementContext ctx) {
        // TODO check validity. Labeling support.
        // Fake inspection result should be suppressed.
        //noinspection GroovyConditionalWithIdenticalBranches
        return setupNodeLocation(asBoolean(ctx.KW_BREAK())
                                 ? new BreakStatement()
                                 : new ContinueStatement(), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.ReturnStatementContext ctx) {
        GroovyParser.ExpressionContext expression = ctx.expression();
        return setupNodeLocation(new ReturnStatement(asBoolean(expression)
                                                     ? parseExpression(expression)
                                                     : EmptyExpression.INSTANCE), ctx);
    }


    @SuppressWarnings("GroovyUnusedDeclaration")
    public Statement parseStatement(GroovyParser.AssertStatementContext ctx) {
        Expression conditionExpression = parseExpression(ctx.expression(0));
        BooleanExpression booleanConditionExpression =
            conditionExpression instanceof BooleanExpression
                ?
                    (BooleanExpression)conditionExpression
                :
                    new BooleanExpression(conditionExpression);

        if (ctx.expression().size() == 1) {
            return setupNodeLocation(new AssertStatement(booleanConditionExpression), ctx);
        } else {
            Expression errorMessage = parseExpression(ctx.expression(1));
            return setupNodeLocation(new AssertStatement(booleanConditionExpression, errorMessage), ctx);
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.ThrowStatementContext ctx) {
        return setupNodeLocation(new ThrowStatement(parseExpression(ctx.expression())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.TryCatchFinallyStatementContext ctx) {
        Object finallyStatement;

        GroovyParser.BlockStatementContext finallyBlockStatement = ctx.finallyBlock() != null ? ctx.finallyBlock().blockStatement() : null;
        if (finallyBlockStatement != null) {
            BlockStatement fbs = new BlockStatement();
            unpackStatement(fbs, parseStatement(finallyBlockStatement));
            finallyStatement = setupNodeLocation(fbs, finallyBlockStatement);

        } else finallyStatement = EmptyStatement.INSTANCE;

        final TryCatchStatement statement = new TryCatchStatement(parseStatement(DefaultGroovyMethods.asType(ctx.tryBlock().blockStatement(), GroovyParser.BlockStatementContext.class)), (Statement)finallyStatement);
        DefaultGroovyMethods.each(ctx.catchBlock(), new Closure<List<GroovyParser.ClassNameExpressionContext>>(null, null) {
            public List<GroovyParser.ClassNameExpressionContext> doCall(GroovyParser.CatchBlockContext it) {
                final Statement catchBlock = parseStatement(DefaultGroovyMethods.asType(it.blockStatement(), GroovyParser.BlockStatementContext.class));
                final String var = it.IDENTIFIER().getText();

                List<GroovyParser.ClassNameExpressionContext> classNameExpression = it.classNameExpression();
                if (!asBoolean(classNameExpression))
                    statement.addCatch(setupNodeLocation(new CatchStatement(new Parameter(ClassHelper.OBJECT_TYPE, var), catchBlock), it));
                else {
                    DefaultGroovyMethods.each(classNameExpression, new Closure<Object>(null, null) {
                        public void doCall(GroovyParser.ClassNameExpressionContext it) {
                            statement.addCatch(setupNodeLocation(new CatchStatement(new Parameter(parseExpression(DefaultGroovyMethods.asType(it, GroovyParser.ClassNameExpressionContext.class)), var), catchBlock), it));
                        }
                    });
                }
                return null;
            }
        });
        return statement;
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Statement parseStatement(GroovyParser.CommandExpressionStatementContext ctx) {
        Expression expression = null;
        List<List<ParseTree>> list = DefaultGroovyMethods.collate(ctx.cmdExpressionRule().children, 2);
        for (List<ParseTree> c : list) {
            final Iterator<ParseTree> iterator = c.iterator();
            ParseTree c1 = iterator.hasNext() ? iterator.next() : null;
            ParseTree c0 = iterator.hasNext() ? iterator.next() : null;

            if (c.size() == 1) expression = new PropertyExpression(expression, c1.getText());
            else {
                assert c0 instanceof GroovyParser.ArgumentListContext;
                if (c1 instanceof TerminalNode) {
                    expression = new MethodCallExpression(expression, ((TerminalNode)c1).getText(), createArgumentList((GroovyParser.ArgumentListContext)c0));
                    ((MethodCallExpression)expression).setImplicitThis(false);
                } else if (c1 instanceof GroovyParser.PathExpressionContext) {
                    String methodName;
                    boolean implicitThis;
                    ArrayList<Object> objects = parsePathExpression((GroovyParser.PathExpressionContext)c1);
                    expression = (Expression)objects.get(0);
                    methodName = (String)objects.get(1);
                    implicitThis = (Boolean)objects.get(2);

                    expression = new MethodCallExpression(expression, methodName, createArgumentList((GroovyParser.ArgumentListContext)c0));
                    ((MethodCallExpression)expression).setImplicitThis(implicitThis);
                }

            }

        }


        return setupNodeLocation(new ExpressionStatement(expression), ctx);
    }

    /**
     * Parse path expression.
     *
     * @param ctx
     * @return tuple of 3 values: Expression, String methodName and boolean implicitThis flag.
     */
    public ArrayList<Object> parsePathExpression(GroovyParser.PathExpressionContext ctx) {
        Expression expression;
        List<TerminalNode> identifiers = ctx.IDENTIFIER();
        switch (identifiers.size()) {
        case 1:
            expression = VariableExpression.THIS_EXPRESSION;
            break;
        case 2:
            expression = new VariableExpression(identifiers.get(0).getText());
            break;
        default:
            expression = DefaultGroovyMethods.inject(identifiers.subList(1, identifiers.size() - 1), new VariableExpression(identifiers.get(0).getText()), new Closure<PropertyExpression>(null, null) {
                public PropertyExpression doCall(Expression expr, Object prop) {
                    return new PropertyExpression(expr, ((TerminalNode)prop).getText());
                }

            });
            log.info(expression.getText());
            break;
        }
        return new ArrayList<Object>(Arrays.asList(expression, DefaultGroovyMethods.last(identifiers).getSymbol().getText(), identifiers.size() == 1));
    }

    public Expression parseExpression(GroovyParser.ExpressionContext ctx) {
        if (ctx instanceof GroovyParser.ParenthesisExpressionContext)
            return parseExpression((GroovyParser.ParenthesisExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.ConstantIntegerExpressionContext)
            return parseExpression((GroovyParser.ConstantIntegerExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.PostfixExpressionContext)
            return parseExpression((GroovyParser.PostfixExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.ClosureExpressionContext)
            return parseExpression((GroovyParser.ClosureExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.AssignmentExpressionContext)
            return parseExpression((GroovyParser.AssignmentExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.ConstantDecimalExpressionContext)
            return parseExpression((GroovyParser.ConstantDecimalExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.TernaryExpressionContext)
            return parseExpression((GroovyParser.TernaryExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.MethodCallExpressionContext)
            return parseExpression((GroovyParser.MethodCallExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.CastExpressionContext)
            return parseExpression((GroovyParser.CastExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.DeclarationExpressionContext)
            return parseExpression((GroovyParser.DeclarationExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.ElvisExpressionContext)
            return parseExpression((GroovyParser.ElvisExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.BinaryExpressionContext)
            return parseExpression((GroovyParser.BinaryExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.NullExpressionContext)
            return parseExpression((GroovyParser.NullExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.ListConstructorContext)
            return parseExpression((GroovyParser.ListConstructorContext)ctx);
        else if (ctx instanceof GroovyParser.PrefixExpressionContext)
            return parseExpression((GroovyParser.PrefixExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.ConstantExpressionContext)
            return parseExpression((GroovyParser.ConstantExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.NewArrayExpressionContext)
            return parseExpression((GroovyParser.NewArrayExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.FieldAccessExpressionContext)
            return parseExpression((GroovyParser.FieldAccessExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.VariableExpressionContext)
            return parseExpression((GroovyParser.VariableExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.NewInstanceExpressionContext)
            return parseExpression((GroovyParser.NewInstanceExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.BoolExpressionContext)
            return parseExpression((GroovyParser.BoolExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.CallExpressionContext)
            return parseExpression((GroovyParser.CallExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.ConstructorCallExpressionContext)
            return parseExpression((GroovyParser.ConstructorCallExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.UnaryExpressionContext)
            return parseExpression((GroovyParser.UnaryExpressionContext)ctx);
        else if (ctx instanceof GroovyParser.MapConstructorContext)
            return parseExpression((GroovyParser.MapConstructorContext)ctx);
        else if (ctx instanceof GroovyParser.GstringExpressionContext)
            return parseExpression((GroovyParser.GstringExpressionContext)ctx);
        if (ctx instanceof GroovyParser.IndexExpressionContext)
            return parseExpression((GroovyParser.IndexExpressionContext)ctx);
        if (ctx instanceof GroovyParser.SpreadExpressionContext)
            return parseExpression((GroovyParser.SpreadExpressionContext)ctx);

        throw new RuntimeException("Unsupported expression type! " + String.valueOf(ctx));
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.NewArrayExpressionContext ctx) {
        return parse(ctx.newArrayRule());
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.NewInstanceExpressionContext ctx) {
        return parse(ctx.newInstanceRule());
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.ParenthesisExpressionContext ctx) {
        return parseExpression(ctx.expression());
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.ListConstructorContext ctx) {
        ListExpression expression = new ListExpression(collect(ctx.expression(), new MethodClosure(this, "parseExpression")));
        return setupNodeLocation(expression, ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.MapConstructorContext ctx) {
        final List collect = collect(ctx.mapEntry(), new MethodClosure(this, "parseExpression"));
        return setupNodeLocation(new MapExpression(asBoolean(collect)
                                                   ? collect
                                                   : new ArrayList()), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public MapEntryExpression parseExpression(GroovyParser.MapEntryContext ctx) {
        Expression keyExpr;
        Expression valueExpr;
        List<GroovyParser.ExpressionContext> expressions = ctx.expression();
        if (expressions.size() == 1) {
            valueExpr = parseExpression(expressions.get(0));
            if (asBoolean(ctx.MULT())) {
                // This is really a spread map entry.
                // This is an odd construct, SpreadMapExpression does not extend MapExpression, so we workaround
                keyExpr = setupNodeLocation(new SpreadMapExpression(valueExpr), ctx);
            } else {
                if (asBoolean(ctx.STRING())) {
                    keyExpr = new ConstantExpression(parseString(ctx.STRING()));
                } else if (asBoolean(ctx.selectorName())) {
                    keyExpr = new ConstantExpression(ctx.selectorName().getText());
                } else if (asBoolean(ctx.gstring())) {
                    keyExpr = parseExpression(ctx.gstring());
                } else if (asBoolean(ctx.INTEGER())) {
                    keyExpr = parseInteger(ctx.INTEGER().getText(), ctx);
                } else if (asBoolean(ctx.DECIMAL())) {
                    keyExpr = parseDecimal(ctx.DECIMAL().getText(), ctx);
                } else {
                    throw new RuntimeException("Unsupported map key type! " + String.valueOf(ctx));
                }
            }
        } else {
            keyExpr = parseExpression(expressions.get(0));
            valueExpr = parseExpression(expressions.get(1));
        }

        return setupNodeLocation(new MapEntryExpression(keyExpr, valueExpr), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public Expression parseExpression(GroovyParser.ClosureExpressionContext ctx) {
        return parseExpression(ctx.closureExpressionRule());
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public Expression parseExpression(GroovyParser.ClosureExpressionRuleContext ctx) {
        final Parameter[] parameters1 = parseParameters(ctx.argumentDeclarationList());
        Parameter[] parameters = asBoolean(ctx.argumentDeclarationList()) ? (
                asBoolean(parameters1)
                        ? parameters1
                        : null) : (new Parameter[0]);

        Statement statement = parseStatement(DefaultGroovyMethods.asType(ctx.blockStatement(), GroovyParser.BlockStatementContext.class));
        return setupNodeLocation(new ClosureExpression(parameters, statement), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public Expression parseExpression(GroovyParser.BinaryExpressionContext ctx) {
        TerminalNode c = DefaultGroovyMethods.asType(ctx.getChild(1), TerminalNode.class);
        int i = 1;
        for (ParseTree next = ctx.getChild(i + 1); next instanceof TerminalNode && ((TerminalNode)next).getSymbol().getType() == GroovyParser.GT; next = ctx.getChild(i + 1))
            i++;
        org.codehaus.groovy.syntax.Token op = createToken(c, i);
        Object expression;
        Expression left = parseExpression(ctx.expression(0));
        Expression right = null;// Will be initialized later, in switch. We should handle as and instanceof creating
        // ClassExpression for given IDENTIFIERS. So, switch should fall through.
        //noinspection GroovyFallthrough
        switch (op.getType()) {
        case Types.RANGE_OPERATOR:
            right = parseExpression(ctx.expression(1));
            expression = new RangeExpression(left, right, !op.getText().endsWith("<"));
            break;
        case Types.KEYWORD_AS:
            ClassNode classNode = setupNodeLocation(parseExpression(ctx.genericClassNameExpression()), ctx.genericClassNameExpression());
            expression = CastExpression.asExpression(classNode, left);
            break;
        case Types.KEYWORD_INSTANCEOF:
            ClassNode rhClass = setupNodeLocation(parseExpression(ctx.genericClassNameExpression()), ctx.genericClassNameExpression());
            right = new ClassExpression(rhClass);
        default:
            if (!asBoolean(right)) right = parseExpression(ctx.expression(1));
            expression = new BinaryExpression(left, op, right);
            break;
        }

        ((Expression)expression).setColumnNumber(op.getStartColumn());
        ((Expression)expression).setLastColumnNumber(op.getStartColumn() + op.getText().length());
        ((Expression)expression).setLineNumber(op.getStartLine());
        ((Expression)expression).setLastLineNumber(op.getStartLine());
        return ((Expression)(expression));
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.CastExpressionContext ctx) {
        Expression left = parseExpression(ctx.expression());
        ClassNode classNode = setupNodeLocation(parseExpression(ctx.genericClassNameExpression()), ctx.genericClassNameExpression());
        CastExpression expression = new CastExpression(classNode, left);
        return setupNodeLocation(expression, ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.TernaryExpressionContext ctx) {
        BooleanExpression boolExpr = new BooleanExpression(parseExpression(ctx.expression(0)));
        Expression trueExpr = parseExpression(ctx.expression(1));
        Expression falseExpr = parseExpression(ctx.expression(2));
        return setupNodeLocation(new TernaryExpression(boolExpr, trueExpr, falseExpr), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.ElvisExpressionContext ctx) {
        Expression baseExpr = parseExpression(ctx.expression(0));
        Expression falseExpr = parseExpression(ctx.expression(1));
        return setupNodeLocation(new ElvisOperatorExpression(baseExpr, falseExpr), ctx);
    }

    protected Expression unaryMinusExpression(GroovyParser.ExpressionContext ctx) {
        // if we are a number literal then let's just parse it
        // as the negation operator on MIN_INT causes rounding to a long
        if (ctx instanceof GroovyParser.ConstantDecimalExpressionContext) {
            return parseDecimal('-' + ((GroovyParser.ConstantDecimalExpressionContext)ctx).DECIMAL().getText(), ctx);
        } else if (ctx instanceof GroovyParser.ConstantIntegerExpressionContext) {
            return parseInteger('-' + ((GroovyParser.ConstantIntegerExpressionContext)ctx).INTEGER().getText(), ctx);
        } else {
            return new UnaryMinusExpression(parseExpression(ctx));
        }
    }

    protected Expression unaryPlusExpression(GroovyParser.ExpressionContext ctx) {
        if (ctx instanceof GroovyParser.ConstantDecimalExpressionContext || ctx instanceof GroovyParser.ConstantIntegerExpressionContext) {
            return parseExpression(ctx);
        } else {
            return new UnaryPlusExpression(parseExpression(ctx));
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.UnaryExpressionContext ctx) {
        Object node = null;
        TerminalNode op = DefaultGroovyMethods.asType(ctx.getChild(0), TerminalNode.class);
        if (DefaultGroovyMethods.isCase("-", op.getText())) {
            node = unaryMinusExpression(ctx.expression());
        } else if (DefaultGroovyMethods.isCase("+", op.getText())) {
            node = unaryPlusExpression(ctx.expression());
        } else if (DefaultGroovyMethods.isCase("!", op.getText())) {
            node = new NotExpression(parseExpression(ctx.expression()));
        } else if (DefaultGroovyMethods.isCase("~", op.getText())) {
            node = new BitwiseNegationExpression(parseExpression(ctx.expression()));
        } else {
            assert false : "There is no " + op.getText() + " handler.";
        }

        ((Expression)node).setColumnNumber(op.getSymbol().getCharPositionInLine() + 1);
        ((Expression)node).setLineNumber(op.getSymbol().getLine());
        ((Expression)node).setLastLineNumber(op.getSymbol().getLine());
        ((Expression)node).setLastColumnNumber(op.getSymbol().getCharPositionInLine() + 1 + op.getText().length());
        return ((Expression)(node));
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public SpreadExpression parseExpression(GroovyParser.SpreadExpressionContext ctx) {
        SpreadExpression expression = new SpreadExpression(parseExpression(ctx.expression()));
        return setupNodeLocation(expression, ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public Expression parseExpression(GroovyParser.AnnotationParameterContext ctx) {
        if (ctx instanceof GroovyParser.AnnotationParamArrayExpressionContext) {
            GroovyParser.AnnotationParamArrayExpressionContext c = DefaultGroovyMethods.asType(ctx, GroovyParser.AnnotationParamArrayExpressionContext.class);
            return setupNodeLocation(new ListExpression(collect(c.annotationParameter(), new Closure<Expression>(null, null) {
                public Expression doCall(GroovyParser.AnnotationParameterContext it) {return parseExpression(it);}
            })), c);
        } else if (ctx instanceof GroovyParser.AnnotationParamBoolExpressionContext) {
            return parseExpression((GroovyParser.AnnotationParamBoolExpressionContext)ctx);
        } else if (ctx instanceof GroovyParser.AnnotationParamClassExpressionContext) {
            return setupNodeLocation(new ClassExpression(parseExpression((DefaultGroovyMethods.asType(ctx, GroovyParser.AnnotationParamClassExpressionContext.class)).genericClassNameExpression())), ctx);
        } else if (ctx instanceof GroovyParser.AnnotationParamDecimalExpressionContext) {
            return parseExpression((GroovyParser.AnnotationParamDecimalExpressionContext)ctx);
        } else if (ctx instanceof GroovyParser.AnnotationParamIntegerExpressionContext) {
            return parseExpression((GroovyParser.AnnotationParamIntegerExpressionContext)ctx);
        } else if (ctx instanceof GroovyParser.AnnotationParamNullExpressionContext) {
            return parseExpression((GroovyParser.AnnotationParamNullExpressionContext)ctx);
        } else if (ctx instanceof GroovyParser.AnnotationParamPathExpressionContext) {
            GroovyParser.AnnotationParamPathExpressionContext c = DefaultGroovyMethods.asType(ctx, GroovyParser.AnnotationParamPathExpressionContext.class);
            return collectPathExpression(c.pathExpression());
        } else if (ctx instanceof GroovyParser.AnnotationParamStringExpressionContext) {
            return parseExpression((GroovyParser.AnnotationParamStringExpressionContext)ctx);
        }
        throw new CompilationFailedException(CompilePhase.PARSING.getPhaseNumber(), this.sourceUnit, new IllegalStateException(String.valueOf(ctx) + " is prohibited inside annotations."));
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.VariableExpressionContext ctx) {
        return setupNodeLocation(new VariableExpression(ctx.IDENTIFIER().getText()), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.FieldAccessExpressionContext ctx) {
        TerminalNode op = DefaultGroovyMethods.asType(ctx.getChild(1), TerminalNode.class);
        Expression left = parseExpression(ctx.expression());

        GroovyParser.SelectorNameContext fieldName = ctx.selectorName();
        Expression right = fieldName != null ? new ConstantExpression(fieldName.getText()) :
            (ctx.STRING() != null ? parseConstantStringToken(ctx.STRING().getSymbol()) : parseExpression(ctx.gstring())
        );
        Expression node = null;
        switch (op.getSymbol().getType()) {
            case GroovyParser.ATTR_DOT:
                node = new AttributeExpression(left, right);
                break;
            case GroovyParser.MEMBER_POINTER:
                node = new MethodPointerExpression(left, right);
                break;
            case GroovyParser.SAFE_DOT:
                node = new PropertyExpression(left, right, true);
                break;
            case GroovyParser.STAR_DOT:
                node = new PropertyExpression(left, right, true /* For backwards compatibility! */);
                ((PropertyExpression)node).setSpreadSafe(true);
                break;
            default:
                // Normal dot
                node = new PropertyExpression(left, right, false);
                break;
        }
        return setupNodeLocation(node, ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public PrefixExpression parseExpression(GroovyParser.PrefixExpressionContext ctx) {
        return setupNodeLocation(new PrefixExpression(createToken(DefaultGroovyMethods.asType(ctx.getChild(0), TerminalNode.class)), parseExpression(ctx.expression())), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public PostfixExpression parseExpression(GroovyParser.PostfixExpressionContext ctx) {
        return setupNodeLocation(new PostfixExpression(parseExpression(ctx.expression()), createToken(DefaultGroovyMethods.asType(ctx.getChild(1), TerminalNode.class))), ctx);
    }

    public ConstantExpression parseDecimal(String text, ParserRuleContext ctx) {
        return setupNodeLocation(new ConstantExpression(Numbers.parseDecimal(text), !text.startsWith("-")), ctx);// Why 10 is int but -10 is Integer?
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public ConstantExpression parseExpression(GroovyParser.AnnotationParamDecimalExpressionContext ctx) {
        return parseDecimal(ctx.DECIMAL().getText(), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public ConstantExpression parseExpression(GroovyParser.ConstantDecimalExpressionContext ctx) {
        return parseDecimal(ctx.DECIMAL().getText(), ctx);
    }

    public ConstantExpression parseInteger(String text, ParserRuleContext ctx) {
        return setupNodeLocation(new ConstantExpression(Numbers.parseInteger(text), !text.startsWith("-")), ctx);//Why 10 is int but -10 is Integer?
    }

    public ConstantExpression parseInteger(String text, Token ctx) {
        return setupNodeLocation(new ConstantExpression(Numbers.parseInteger(text), !text.startsWith("-")), ctx);//Why 10 is int but -10 is Integer?
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public ConstantExpression parseExpression(GroovyParser.ConstantIntegerExpressionContext ctx) {
        return parseInteger(ctx.INTEGER().getText(), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public ConstantExpression parseExpression(GroovyParser.AnnotationParamIntegerExpressionContext ctx) {
        return parseInteger(ctx.INTEGER().getText(), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public ConstantExpression parseExpression(GroovyParser.BoolExpressionContext ctx) {
        return setupNodeLocation(new ConstantExpression(!asBoolean(ctx.KW_FALSE()), true), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public ConstantExpression parseExpression(GroovyParser.AnnotationParamBoolExpressionContext ctx) {
        return setupNodeLocation(new ConstantExpression(!asBoolean(ctx.KW_FALSE()), true), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public ConstantExpression cleanConstantStringLiteral(String text) {
        Boolean isSlashy = text.startsWith("/");

        if (text.startsWith("'''") || text.startsWith("\"\"\"")) {
            text = StringUtil.removeCR(text); // remove CR in the multiline string

            text = text.length() == 6 ? "" : text.substring(3, text.length() - 3);
        } else if (text.startsWith("'") || text.startsWith("/") || text.startsWith("\"")) {
            text = text.length() == 2 ? "" : text.substring(1, text.length() - 1);
        }

        //handle escapes.
        if (isSlashy) {
            text = StringUtil.replaceHexEscapes(text);
            text = text.replace("\\/", "/");
        } else {
            text = StringUtil.replaceEscapes(text);
        }

        return new ConstantExpression(text, true);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public ConstantExpression parseConstantString(ParserRuleContext ctx) {
        return setupNodeLocation(cleanConstantStringLiteral(ctx.getText()), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public ConstantExpression parseConstantStringToken(org.antlr.v4.runtime.Token token) {
        return setupNodeLocation(cleanConstantStringLiteral(token.getText()), token);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public ConstantExpression parseExpression(GroovyParser.ConstantExpressionContext ctx) {
        return parseConstantString(ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public ConstantExpression parseExpression(GroovyParser.AnnotationParamStringExpressionContext ctx) {
        return parseConstantString(ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    public Expression parseExpression(GroovyParser.GstringExpressionContext ctx) {
        return parseExpression(ctx.gstring());
    }

    public Expression parseExpression(GroovyParser.GstringContext ctx) {
        Closure<String> clearStart = new Closure<String>(null, null) {
            public String doCall(String it) {
                if (it.startsWith("\"\"\"")) {
                    it = StringUtil.removeCR(it);

                    it = it.substring(2); // translate leading """ to "
                }

                it = StringUtil.replaceEscapes(it);

                return (it.length() == 2)
                        ? ""
                        : DefaultGroovyMethods.getAt(it, new IntRange(true, 1, -2));
            }

        };
        final Closure<String> clearPart = new Closure<String>(null, null) {
            public String doCall(String it) {
                it = StringUtil.removeCR(it);
                it = StringUtil.replaceEscapes(it);

                return it.length() == 1
                        ? ""
                        : DefaultGroovyMethods.getAt(it, new IntRange(true, 0, -2));
            }

        };
        Closure<String> clearEnd = new Closure<String>(null, null) {
            public String doCall(String it) {
                if (it.endsWith("\"\"\"")) {
                    it = StringUtil.removeCR(it);

                    it = DefaultGroovyMethods.getAt(it, new IntRange(true, 0, -3)); // translate tailing """ to "
                }

                it = StringUtil.replaceEscapes(it);

                return (it.length() == 1)
                        ? ""
                        : DefaultGroovyMethods.getAt(it, new IntRange(true, 0, -2));
            }

        };
        Collection<String> strings = DefaultGroovyMethods.plus(DefaultGroovyMethods.plus(new ArrayList<String>(Arrays.asList(clearStart.call(ctx.GSTRING_START().getText()))), collect(ctx.GSTRING_PART(), new Closure<String>(null, null) {
            public String doCall(TerminalNode it) {return clearPart.call(it.getText());}
        })), new ArrayList<String>(Arrays.asList(clearEnd.call(ctx.GSTRING_END().getText()))));
        final List<Expression> expressions = new ArrayList<Expression>();

        final List<ParseTree> children = ctx.children;
        DefaultGroovyMethods.eachWithIndex(children, new Closure<Collection>(null, null) {
            public Collection doCall(Object it, Integer i) {
                if (!(it instanceof GroovyParser.GstringExpressionBodyContext)) {
                    return expressions;
                }

                GroovyParser.GstringExpressionBodyContext gstringExpressionBodyContext = (GroovyParser.GstringExpressionBodyContext) it;

                if (asBoolean(gstringExpressionBodyContext.gstringPathExpression())) {
                    expressions.add(collectPathExpression(gstringExpressionBodyContext.gstringPathExpression()));
                    return expressions;
                } else if (asBoolean(gstringExpressionBodyContext.closureExpressionRule())) {
                    GroovyParser.ClosureExpressionRuleContext closureExpressionRule = gstringExpressionBodyContext.closureExpressionRule();
                    Expression expression = parseExpression(closureExpressionRule);

                    if (!asBoolean(closureExpressionRule.CLOSURE_ARG_SEPARATOR())) {

                        MethodCallExpression methodCallExpression = new MethodCallExpression(expression, "call", new ArgumentListExpression());
                        copyNodeLocation(expression, methodCallExpression);

                        expressions.add(methodCallExpression);
                        return expressions;
                    }

                    expressions.add(expression);
                    return expressions;
                } else {
                    if (asBoolean(gstringExpressionBodyContext.expression())) {
                        // We can guarantee, that it will be at least fallback ExpressionContext multimethod overloading, that can handle such situation.
                        //noinspection GroovyAssignabilityCheck
                        expressions.add(parseExpression(gstringExpressionBodyContext.expression()));
                        return expressions;
                    } else { // handle empty expression e.g. "GString ${}"
                        expressions.add(new ConstantExpression(null));
                        return expressions;
                    }
                }

            }

        });
        GStringExpression gstringNode = new GStringExpression(ctx.getText(), collect(strings, new Closure<ConstantExpression>(null, null) {
            public ConstantExpression doCall(String it) {return new ConstantExpression(it);}
        }), expressions);
        return setupNodeLocation(gstringNode, ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.NullExpressionContext ctx) {
        return setupNodeLocation(new ConstantExpression(null), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.AnnotationParamNullExpressionContext ctx) {
        return setupNodeLocation(new ConstantExpression(null), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.AssignmentExpressionContext ctx) {
        Expression left = parseExpression(ctx.expression(0));// TODO reference to AntlrParserPlugin line 2304 for error handling.
        Expression right = parseExpression(ctx.expression(1));
        return setupNodeLocation(new BinaryExpression(left, createToken(DefaultGroovyMethods.asType(ctx.getChild(1), TerminalNode.class)), right), ctx);
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.DeclarationExpressionContext ctx) {
        List<?> declarations = parseDeclaration(ctx.declarationRule());

        if (declarations.size() == 1) {
            return setupNodeLocation((Expression) declarations.get(0), ctx);
        } else {
            return new ClosureListExpression((List<Expression>)declarations);
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public Expression parseExpression(GroovyParser.CallExpressionContext ctx) {

        Object methodNode;
        //FIXME in log a, b; a is treated as path expression and became a method call instead of variable
        if (!asBoolean(ctx.LPAREN()) && ctx.closureExpressionRule().size() == 0)
            return collectPathExpression(ctx.pathExpression());

        // Collect closure's in argumentList expression.
        final Expression argumentListExpression = createArgumentList(ctx.argumentList());
        DefaultGroovyMethods.each(ctx.closureExpressionRule(), new Closure<Object>(null, null) {
            public Object doCall(GroovyParser.ClosureExpressionRuleContext it) {return DefaultGroovyMethods.invokeMethod(argumentListExpression, "addExpression", new Object[]{ parseExpression(it) });}
        });

        //noinspection GroovyAssignabilityCheck
        List<Object> iterator = parsePathExpression(ctx.pathExpression());
        Expression expression = (Expression)iterator.get(0);
        String methodName = (String)iterator.get(1);
        boolean implicitThis = (Boolean)iterator.get(2);

        if (implicitThis && VariableExpression.THIS_EXPRESSION.getText().equals(methodName)) {
            // Actually a constructor call
            ConstructorCallExpression call = new ConstructorCallExpression(ClassNode.THIS, argumentListExpression);
            return setupNodeLocation(call, ctx);
//        } else if (implicitThis && VariableExpression.SUPER_EXPRESSION.getText().equals(methodName)) {
//            // Use this once path expression is refac'ed
//            // Actually a constructor call
//            ConstructorCallExpression call = new ConstructorCallExpression(ClassNode.SUPER, argumentListExpression);
//            return setupNodeLocation(call, ctx);
        }
        // OK, just a normal call
        methodNode = new MethodCallExpression(expression, methodName, argumentListExpression);
        ((MethodCallExpression)methodNode).setImplicitThis(implicitThis);
        return (Expression)methodNode;
    }

    public Expression collectPathExpression(GroovyParser.PathExpressionContext ctx) {
        List<TerminalNode> identifiers = ctx.IDENTIFIER();
        switch (identifiers.size()) {
        case 1:
            return new VariableExpression(identifiers.get(0).getText());
        default:
            Expression inject = DefaultGroovyMethods.inject(identifiers.subList(1, identifiers.size()), new VariableExpression(identifiers.get(0).getText()), new Closure<PropertyExpression>(null, null) {
                public PropertyExpression doCall(Object val, Object prop) {
                    return new PropertyExpression(DefaultGroovyMethods.asType(val, Expression.class), new ConstantExpression(((TerminalNode)prop).getText()));
                }

            });
            return inject;
        }
    }

    public Expression collectPathExpression(GroovyParser.GstringPathExpressionContext ctx) {
        if (!asBoolean(ctx.GSTRING_PATH_PART()))
            return new VariableExpression(ctx.IDENTIFIER().getText());
        else {
            Expression inj = DefaultGroovyMethods.inject(ctx.GSTRING_PATH_PART(), new VariableExpression(ctx.IDENTIFIER().getText()), new Closure<PropertyExpression>(null, null) {
                public PropertyExpression doCall(Object val, Object prop) {
                    return new PropertyExpression(DefaultGroovyMethods.asType(val, Expression.class), new ConstantExpression(DefaultGroovyMethods.getAt(((TerminalNode)prop).getText(), new IntRange(true, 1, -1))));
                }

            });
            return inj;
        }

    }

    @SuppressWarnings("GroovyUnusedDeclaration") public BinaryExpression parseExpression(GroovyParser.IndexExpressionContext ctx) {
        // parse the lhs
        Expression leftExpression = parseExpression(ctx.expression(0));
        int expressionCount = ctx.expression().size();
        List<Expression> expressions = new LinkedList<Expression>();
        Expression rightExpression = null;

        // parse the indices
        for (int i = 1; i < expressionCount; ++i) {
            expressions.add(parseExpression(ctx.expression(i)));
        }
        if (expressionCount == 2) {
            // If only one index, treat as single expression
            rightExpression = expressions.get(0);
            // unless it's a spread operator...
            if (rightExpression instanceof SpreadExpression) {
                ListExpression wrapped = new ListExpression();
                wrapped.addExpression(rightExpression);
                rightExpression = setupNodeLocation(wrapped, ctx.expression(1));
            }
        } else {
            // Otherwise, setup as list expression
            ListExpression listExpression = new ListExpression(expressions);
            listExpression.setWrapped(true);
            rightExpression = listExpression;
            // if nonempty, set location info for index list
            if (expressionCount > 2) {
                Token start = ctx.expression(1).getStart();
                Token stop = ctx.expression(expressionCount - 1).getStart();
                listExpression.setLineNumber(start.getLine());
                listExpression.setColumnNumber(start.getCharPositionInLine() + 1);
                listExpression.setLastLineNumber(stop.getLine());
                listExpression.setLastColumnNumber(stop.getCharPositionInLine() + 1 + stop.getText().length());
            }
        }
        BinaryExpression binaryExpression = new BinaryExpression(leftExpression, createToken(ctx.LBRACK(), 1), rightExpression);
        return setupNodeLocation(binaryExpression, ctx);
    }


    @SuppressWarnings("GroovyUnusedDeclaration") public MethodCallExpression parseExpression(GroovyParser.MethodCallExpressionContext ctx) {
        GroovyParser.SelectorNameContext methodSelector = ctx.selectorName();
        Expression method = methodSelector != null ? new ConstantExpression(methodSelector.getText()) : (
            ctx.STRING() != null ? parseConstantStringToken(ctx.STRING().getSymbol()) : parseExpression(ctx.gstring())
        );
        Expression argumentListExpression = createArgumentList(ctx.argumentList());
        MethodCallExpression expression = new MethodCallExpression(parseExpression(ctx.expression()), method, argumentListExpression);
        expression.setImplicitThis(false);
        TerminalNode op = DefaultGroovyMethods.asType(ctx.getChild(1), TerminalNode.class);
        expression.setSpreadSafe(op.getSymbol().getType() == GroovyParser.STAR_DOT);
        expression.setSafe(op.getSymbol().getType() == GroovyParser.SAFE_DOT);
        return expression;
    }

    @SuppressWarnings("GroovyUnusedDeclaration") public ConstructorCallExpression parseExpression(GroovyParser.ConstructorCallExpressionContext ctx) {
        Expression argumentListExpression = createArgumentList(ctx.argumentList());
        ConstructorCallExpression expression = new ConstructorCallExpression(ClassNode.SUPER, argumentListExpression);
        return setupNodeLocation(expression, ctx);
    }

    public ClassNode parseExpression(GroovyParser.ClassNameExpressionContext ctx) {
        return setupNodeLocation(ClassHelper.make(DefaultGroovyMethods.join(ctx.IDENTIFIER(), ".")), ctx);
    }

    public ClassNode parseExpression(GroovyParser.GenericClassNameExpressionContext ctx) {
        ClassNode classNode = parseExpression(ctx.classNameExpression());

        if (asBoolean(ctx.LBRACK())) classNode = classNode.makeArray();
        classNode.setGenericsTypes(parseGenericList(ctx.genericList()));
        return setupNodeLocation(classNode, ctx);
    }

    public GenericsType[] parseGenericList(GroovyParser.GenericListContext ctx) {
        if (ctx == null)
            return null;
        List<GenericsType> collect = collect(ctx.genericListElement(), new Closure<GenericsType>(null, null) {
            public GenericsType doCall(GroovyParser.GenericListElementContext it) {
                if (it instanceof GroovyParser.GenericsConcreteElementContext)
                    return setupNodeLocation(new GenericsType(parseExpression(((GroovyParser.GenericsConcreteElementContext)it).genericClassNameExpression())), it);
                else {
                    assert it instanceof GroovyParser.GenericsWildcardElementContext;
                    GroovyParser.GenericsWildcardElementContext gwec = (GroovyParser.GenericsWildcardElementContext)it;
                    ClassNode baseType = ClassHelper.makeWithoutCaching("?");
                    ClassNode[] upperBounds = null;
                    ClassNode lowerBound = null;
                    if (asBoolean(gwec.KW_EXTENDS())) {
                        ClassNode classNode = parseExpression(gwec.genericClassNameExpression());
                        upperBounds = new ClassNode[]{ classNode };
                    } else if (asBoolean(gwec.KW_SUPER()))
                        lowerBound = parseExpression(gwec.genericClassNameExpression());

                    GenericsType type = new GenericsType(baseType, upperBounds, lowerBound);
                    type.setWildcard(true);
                    type.setName("?");
                    return setupNodeLocation(type, it);
                }

            }
        });
        return collect.toArray(new GenericsType[collect.size()]);
    }

    public GenericsType[] parseGenericDeclaration(GroovyParser.GenericDeclarationListContext ctx) {
        if (ctx == null)
            return null;
        List<GenericsType> genericTypes = collect(ctx.genericsDeclarationElement(), new Closure<GenericsType>(null, null) {
            public GenericsType doCall(GroovyParser.GenericsDeclarationElementContext it) {
                ClassNode classNode = parseExpression(it.genericClassNameExpression(0));
                ClassNode[] upperBounds = null;
                if (asBoolean(it.KW_EXTENDS())) {
                    List<GroovyParser.GenericClassNameExpressionContext> genericClassNameExpressionContexts = DefaultGroovyMethods.toList(it.genericClassNameExpression());
                    upperBounds = DefaultGroovyMethods.asType(collect(genericClassNameExpressionContexts.subList(1, genericClassNameExpressionContexts.size()), new MethodClosure(ASTBuilder.this, "parseExpression")), ClassNode[].class);
                }
                GenericsType type = new GenericsType(classNode, upperBounds, null);
                return setupNodeLocation(type, it);
            }
        });
        return  genericTypes.toArray(new GenericsType[genericTypes.size()]);
    }

    public List<DeclarationExpression> parseDeclaration(GroovyParser.DeclarationRuleContext ctx) {
        ClassNode type = parseTypeDeclaration(ctx.typeDeclaration());
        List<GroovyParser.SingleDeclarationContext> variables = ctx.singleDeclaration();
        List<DeclarationExpression> declarations = new LinkedList<DeclarationExpression>();

        if (asBoolean(ctx.tupleDeclaration())) {
            DeclarationExpression declarationExpression = parseTupleDeclaration(ctx.tupleDeclaration());

            declarations.add(declarationExpression);

            return declarations;
        }

        for (GroovyParser.SingleDeclarationContext variableCtx : variables) {
            VariableExpression left = new VariableExpression(variableCtx.IDENTIFIER().getText(), type);

            org.codehaus.groovy.syntax.Token token;
            if (asBoolean(variableCtx.ASSIGN())) {
                token = createGroovyToken(variableCtx.ASSIGN().getSymbol(), Types.ASSIGN);
            } else {
                int line = variableCtx.start.getLine();
                int col = -1; //ASSIGN TOKEN DOES NOT APPEAR, SO COL IS -1. IF NO ERROR OCCURS, THE ORIGINAL CODE CAN BE REMOVED IN THE FURTURE: variableCtx.getStart().getCharPositionInLine() + 1; // FIXME Why assignment token location is it's first occurrence.

                token = new org.codehaus.groovy.syntax.Token(Types.ASSIGN, "=", line, col);
            }

            GroovyParser.ExpressionContext initialValueCtx = variableCtx.expression();
            Expression initialValue = initialValueCtx != null ? parseExpression(variableCtx.expression()) : setupNodeLocation(new EmptyExpression(),ctx);
            DeclarationExpression expression = new DeclarationExpression(left, token, initialValue);
            attachAnnotations(expression, ctx.annotationClause());
            declarations.add(setupNodeLocation(expression, variableCtx));
        }

        int declarationsSize = declarations.size();
        if (declarationsSize == 1) {
            setupNodeLocation(declarations.get(0), ctx);
        } else if (declarationsSize > 0) {
            DeclarationExpression declarationExpression = declarations.get(0);
            // Tweak start of first declaration
            declarationExpression.setLineNumber(ctx.getStart().getLine());
            declarationExpression.setColumnNumber(ctx.getStart().getCharPositionInLine() + 1);
        }

        return declarations;
    }

    private org.codehaus.groovy.syntax.Token createGroovyToken(Token token, int type) {
        if (null == token) {
            throw new IllegalArgumentException("token should not be null");
        }

        return new org.codehaus.groovy.syntax.Token(type, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    private DeclarationExpression parseTupleDeclaration(GroovyParser.TupleDeclarationContext ctx) {
        // tuple must have an initial value.
        if (null == ctx.expression()) {
            throw new RuntimeException("tuple declaration must have an initial value.");
        }

        List<Expression> variables = new LinkedList<Expression>();

        for (GroovyParser.TupleVariableDeclarationContext tupleVariableDeclarationContext : ctx.tupleVariableDeclaration()) {
            ClassNode type = asBoolean(tupleVariableDeclarationContext.genericClassNameExpression())
                                ? setupNodeLocation(parseExpression(tupleVariableDeclarationContext.genericClassNameExpression()), ctx)
                                : ClassHelper.OBJECT_TYPE;

            variables.add(new VariableExpression(tupleVariableDeclarationContext.IDENTIFIER().getText(), type));
        }

        ArgumentListExpression argumentListExpression = new ArgumentListExpression(variables);
        Token assignToken = ctx.ASSIGN().getSymbol();
        org.codehaus.groovy.syntax.Token token = createGroovyToken(assignToken, Types.ASSIGN);

        Expression initialValue = (ctx != null) ? parseExpression(ctx.expression())
                                                : setupNodeLocation(new EmptyExpression(),ctx);

        DeclarationExpression declarationExpression  = new DeclarationExpression(argumentListExpression, token, initialValue);

        return setupNodeLocation(declarationExpression, ctx);
    }

    @SuppressWarnings("UnnecessaryQualifiedReference")
    private Expression createArgumentList(GroovyParser.ArgumentListContext ctx) {
        final List<MapEntryExpression> mapArgs = new ArrayList<MapEntryExpression>();
        final List<Expression> expressions = new ArrayList<Expression>();
        if (ctx != null) {
            DefaultGroovyMethods.each(ctx.children, new Closure<Collection<? extends Expression>>(null, null) {
                public Collection<? extends Expression> doCall(ParseTree it) {
                    if (it instanceof GroovyParser.ArgumentContext) {
                        if (asBoolean(((GroovyParser.ArgumentContext)it).mapEntry())) {
                            mapArgs.add(parseExpression(((GroovyParser.ArgumentContext) it).mapEntry()));
                            return mapArgs;
                        } else {
                            expressions.add(parseExpression(((GroovyParser.ArgumentContext) it).expression()));
                            return expressions;
                        }
                    } else if (it instanceof GroovyParser.ClosureExpressionRuleContext) {
                        expressions.add(parseExpression((GroovyParser.ClosureExpressionRuleContext) it));
                        return expressions;
                    }
                    return null;
                }
            });
        }
        if (asBoolean(expressions)) {
            if (asBoolean(mapArgs))
                expressions.add(0, new MapExpression(mapArgs));
            return new ArgumentListExpression(expressions);
        } else {
            if (asBoolean(mapArgs))
                return new TupleExpression(new NamedArgumentListExpression(mapArgs));
            else return new ArgumentListExpression();
        }

    }

    public void attachAnnotations(AnnotatedNode node, List<GroovyParser.AnnotationClauseContext> ctxs) {
        for (GroovyParser.AnnotationClauseContext ctx : ctxs) {
            AnnotationNode annotation = parseAnnotation(ctx);
            node.addAnnotation(annotation);
        }

    }

    public List<AnnotationNode> parseAnnotations(List<GroovyParser.AnnotationClauseContext> ctxs) {
        return collect(ctxs, new Closure<AnnotationNode>(null, null) {
            public AnnotationNode doCall(GroovyParser.AnnotationClauseContext it) {return parseAnnotation(it);}
        });
    }

    public AnnotationNode parseAnnotation(GroovyParser.AnnotationClauseContext ctx) {
        AnnotationNode node = new AnnotationNode(parseExpression(ctx.genericClassNameExpression()));
        if (asBoolean(ctx.annotationElement()))
            node.addMember("value", parseAnnotationElement(ctx.annotationElement()));
        else {
            for (GroovyParser.AnnotationElementPairContext pair : ctx.annotationElementPair()) {
                node.addMember(pair.IDENTIFIER().getText(), parseAnnotationElement(pair.annotationElement()));
            }

        }


        return setupNodeLocation(node, ctx);
    }

    public Expression parseAnnotationElement(GroovyParser.AnnotationElementContext ctx) {
        GroovyParser.AnnotationClauseContext annotationClause = ctx.annotationClause();
        if (asBoolean(annotationClause))
            return setupNodeLocation(new AnnotationConstantExpression(parseAnnotation(annotationClause)), annotationClause);
        else return parseExpression(ctx.annotationParameter());
    }

    public ClassNode[] parseThrowsClause(GroovyParser.ThrowsClauseContext ctx) {
        List list = asBoolean(ctx)
                    ? collect(ctx.classNameExpression(), new Closure<ClassNode>(null, null) {
            public ClassNode doCall(GroovyParser.ClassNameExpressionContext it) {return parseExpression(it);}
        })
                    : new ArrayList();
        return (ClassNode[])list.toArray(new ClassNode[list.size()]);
    }

    /**
     * @param node
     * @param cardinality Used for handling GT ">" operator, which can be repeated to give bitwise shifts >> or >>>
     * @return
     */
    public org.codehaus.groovy.syntax.Token createToken(TerminalNode node, int cardinality) {
        String text = multiply(node.getText(), cardinality);
        return new org.codehaus.groovy.syntax.Token(node.getText().equals("..<") || node.getText().equals("..")
                                                    ? Types.RANGE_OPERATOR
                                                    : Types.lookup(text, Types.ANY), text, node.getSymbol().getLine(), node.getSymbol().getCharPositionInLine() + 1);
    }

    /**
     * @param node
     * @param cardinality Used for handling GT ">" operator, which can be repeated to give bitwise shifts >> or >>>
     * @return
     */
    public org.codehaus.groovy.syntax.Token createToken(TerminalNode node) {
        return createToken(node, 1);
    }

    public ClassNode parseTypeDeclaration(GroovyParser.TypeDeclarationContext ctx) {
        return !asBoolean(ctx) || ctx.KW_DEF() != null
               ? ClassHelper.OBJECT_TYPE
               : setupNodeLocation(parseExpression(ctx.genericClassNameExpression()), ctx);
    }

    public ArrayExpression parse(GroovyParser.NewArrayRuleContext ctx) {
        List<Expression> collect = collect(ctx.INTEGER(), new Closure<Expression>(null, null) {
            public Expression doCall(TerminalNode it) {return parseInteger(it.getText(), it.getSymbol());}
        });
        ArrayExpression expression = new ArrayExpression(parseExpression(ctx.classNameExpression()), new ArrayList<Expression>(), collect);
        return setupNodeLocation(expression, ctx);
    }

    public ConstructorCallExpression parse(GroovyParser.NewInstanceRuleContext ctx) {
        ClassNode creatingClass = asBoolean(ctx.genericClassNameExpression())
                                  ? parseExpression(ctx.genericClassNameExpression())
                                  : parseExpression(ctx.classNameExpression());
        if (asBoolean(ctx.LT())) creatingClass.setGenericsTypes(new GenericsType[0]);

        ConstructorCallExpression expression;
        if (!asBoolean(ctx.classBody())) {
            expression = setupNodeLocation(new ConstructorCallExpression(creatingClass, createArgumentList(ctx.argumentList())), ctx);
        } else {
            ClassNode outer = this.classes.peek();
            InnerClassNode classNode = new InnerClassNode(outer, outer.getName() + "$" + String.valueOf((this.anonymousClassesCount = ++this.anonymousClassesCount)), Opcodes.ACC_PUBLIC, ClassHelper.make(creatingClass.getName()));
            expression = setupNodeLocation(new ConstructorCallExpression(classNode, createArgumentList(ctx.argumentList())), ctx);
            expression.setUsingAnonymousInnerClass(true);
            classNode.setAnonymous(true);
            DefaultGroovyMethods.last(this.innerClassesDefinedInMethod).add(classNode);
            this.moduleNode.addClass(classNode);
            this.classes.add(classNode);
            this.parseMembers(classNode, ctx.classBody().classMember());
            this.classes.pop();
        }

        return expression;
    }

    public Parameter[] parseParameters(GroovyParser.ArgumentDeclarationListContext ctx) {
        List<Parameter> parameterList = ctx == null || ctx.argumentDeclaration() == null ?
            new ArrayList<Parameter>(0) :
            collect(ctx.argumentDeclaration(), new Closure<Parameter>(null, null) {
                public Parameter doCall(GroovyParser.ArgumentDeclarationContext it) {
                    Parameter parameter = new Parameter(parseTypeDeclaration(it.typeDeclaration()), it.IDENTIFIER().getText());
                    attachAnnotations(parameter, it.annotationClause());
                    if (asBoolean(it.expression()))
                        parameter.setInitialExpression(parseExpression(it.expression()));
                    return setupNodeLocation(parameter, it);
                }
            });
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    public MethodNode getOrCreateClinitMethod(ClassNode classNode) {
        MethodNode methodNode = DefaultGroovyMethods.find(classNode.getMethods(), new Closure<Boolean>(null, null) {
            public Boolean doCall(MethodNode it) {return it.getName().equals("<clinit>");}
        });
        if (!asBoolean(methodNode)) {
            methodNode = new MethodNode("<clinit>", Opcodes.ACC_STATIC, ClassHelper.VOID_TYPE, new Parameter[0], new ClassNode[0], new BlockStatement());
            methodNode.setSynthetic(true);
            classNode.addMethod(methodNode);
        }

        return methodNode;
    }

    private <T extends ASTNode> void copyNodeLocation(T srcNode, T destNode) {
        if (null == srcNode || null == destNode) {
            throw new IllegalArgumentException("srcNode[" + srcNode + "] and destNode[" + destNode + "] should not be null");
        }

        destNode.setLineNumber(srcNode.getLineNumber());
        destNode.setColumnNumber(srcNode.getColumnNumber());
        destNode.setLastLineNumber(srcNode.getLastLineNumber());
        destNode.setLastColumnNumber(srcNode.getLastColumnNumber());
    }

    /**
     * Sets location(lineNumber, colNumber, lastLineNumber, lastColumnNumber) for node using standard context information.
     * Note: this method is implemented to be closed over ASTNode. It returns same node as it received in arguments.
     *
     * @param astNode Node to be modified.
     * @param ctx     Context from which information is obtained.
     * @return Modified astNode.
     */
    public <T extends ASTNode> T setupNodeLocation(T astNode, ParserRuleContext ctx) {
        astNode.setLineNumber(ctx.getStart().getLine());
        astNode.setColumnNumber(ctx.getStart().getCharPositionInLine() + 1);
        astNode.setLastLineNumber(ctx.getStop().getLine());
        astNode.setLastColumnNumber(ctx.getStop().getCharPositionInLine() + 1 + ctx.getStop().getText().length());
//        System.err.println(astNode.getClass().getSimpleName() + " at " + astNode.getLineNumber() + ":" + astNode.getColumnNumber());
        return astNode;
    }

    public <T extends ASTNode> T setupNodeLocation(T astNode, Token token) {
        astNode.setLineNumber(token.getLine());
        astNode.setColumnNumber(token.getCharPositionInLine() + 1);
        astNode.setLastLineNumber(token.getLine());
        astNode.setLastColumnNumber(token.getCharPositionInLine() + 1 + token.getText().length());
//        System.err.println(astNode.getClass().getSimpleName() + " at " + astNode.getLineNumber() + ":" + astNode.getColumnNumber());
        return astNode;
    }

    public <T extends ASTNode> T setupNodeLocation(T astNode, ASTNode source) {
        astNode.setLineNumber(source.getLineNumber());
        astNode.setColumnNumber(source.getColumnNumber());
        astNode.setLastLineNumber(source.getLastLineNumber());
        astNode.setLastColumnNumber(source.getLastColumnNumber());
        return astNode;
    }

    public int parseClassModifiers(@NotNull List<GroovyParser.ClassModifierContext> ctxs) {
        List<TerminalNode> visibilityModifiers = new ArrayList<TerminalNode>();
        int modifiers = 0;
        for (int i = 0; i < ctxs.size(); i++) {
            for (Object ctx : ctxs.get(i).children) {
                ParseTree child = null;
                if (ctx instanceof List) {
                    List list = (List)ctx;
                    assert list.size() == 1;
                    child = (ParseTree)list.get(0);
                }
                else
                    child = (ParseTree)ctx;

                assert child instanceof TerminalNode;
                switch (((TerminalNode)child).getSymbol().getType()) {
                case GroovyLexer.VISIBILITY_MODIFIER:
                    visibilityModifiers.add((TerminalNode)child);
                    break;
                case GroovyLexer.KW_STATIC:
                    modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_STATIC, (TerminalNode)child);
                    break;
                case GroovyLexer.KW_ABSTRACT:
                    modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_ABSTRACT, (TerminalNode)child);
                    break;
                case GroovyLexer.KW_FINAL:
                    modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_FINAL, (TerminalNode)child);
                    break;
                case GroovyLexer.KW_STRICTFP:
                    modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_STRICT, (TerminalNode)child);
                    break;
                }
            }
        }

        if (asBoolean(visibilityModifiers))
            modifiers |= parseVisibilityModifiers(visibilityModifiers, 0);
        else modifiers |= Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC;
        return modifiers;
    }

    public int checkModifierDuplication(int modifier, int opcode, TerminalNode node) {
        if ((modifier & opcode) == 0) return modifier | opcode;
        else {
            Token symbol = node.getSymbol();

            Integer line = symbol.getLine();
            Integer col = symbol.getCharPositionInLine() + 1;
            sourceUnit.addError(new SyntaxException("Cannot repeat modifier: " + symbol.getText() + " at line: " + String.valueOf(line) + " column: " + String.valueOf(col) + ". File: " + sourceUnit.getName(), line, col));
            return modifier;
        }

    }

    /**
     * Traverse through modifiers, and combine them in one int value. Raise an error if there is multiple occurrences of same modifier.
     *
     * @param ctxList                   modifiers list.
     * @param defaultVisibilityModifier Default visibility modifier. Can be null. Applied if providen, and no visibility modifier exists in the ctxList.
     * @return tuple of int modifier and boolean flag, signalising visibility modifiers presence(true if there is visibility modifier in list, false otherwise).
     * @see #checkModifierDuplication(int, int, org.antlr.v4.runtime.tree.TerminalNode)
     */
    public ArrayList<Object> parseModifiers(List<GroovyParser.MemberModifierContext> ctxList, Integer defaultVisibilityModifier) {
        int modifiers = 0;
        boolean hasVisibilityModifier = false;
        for (GroovyParser.MemberModifierContext it : ctxList) {
            TerminalNode child = (DefaultGroovyMethods.asType(it.getChild(0), TerminalNode.class));
            switch (child.getSymbol().getType()) {
            case GroovyLexer.KW_STATIC:
                modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_STATIC, child);
                break;
            case GroovyLexer.KW_ABSTRACT:
                modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_ABSTRACT, child);
                break;
            case GroovyLexer.KW_FINAL:
                modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_FINAL, child);
                break;
            case GroovyLexer.KW_NATIVE:
                modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_NATIVE, child);
                break;
            case GroovyLexer.KW_SYNCHRONIZED:
                modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_SYNCHRONIZED, child);
                break;
            case GroovyLexer.KW_TRANSIENT:
                modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_TRANSIENT, child);
                break;
            case GroovyLexer.KW_VOLATILE:
                modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_VOLATILE, child);
                break;
            case GroovyLexer.VISIBILITY_MODIFIER:
                modifiers |= parseVisibilityModifiers(child);
                hasVisibilityModifier = true;
                break;
            }
        }
        if (!hasVisibilityModifier && defaultVisibilityModifier != null) modifiers |= defaultVisibilityModifier;

        return new ArrayList<Object>(Arrays.asList(modifiers, hasVisibilityModifier));
    }

    /**
     * Traverse through modifiers, and combine them in one int value. Raise an error if there is multiple occurrences of same modifier.
     *
     * @param ctxList                   modifiers list.
     * @param defaultVisibilityModifier Default visibility modifier. Can be null. Applied if providen, and no visibility modifier exists in the ctxList.
     * @return tuple of int modifier and boolean flag, signalising visibility modifiers presence(true if there is visibility modifier in list, false otherwise).
     * @see #checkModifierDuplication(int, int, org.antlr.v4.runtime.tree.TerminalNode)
     */
    public ArrayList<Object> parseModifiers(List<GroovyParser.MemberModifierContext> ctxList) {
        return parseModifiers(ctxList, null);
    }

    public void reportError(String text, int line, int col) {
        sourceUnit.addError(new SyntaxException(text, line, col));
    }

    public int parseVisibilityModifiers(TerminalNode modifier) {
        assert modifier.getSymbol().getType() == GroovyLexer.VISIBILITY_MODIFIER;
        if (DefaultGroovyMethods.isCase("public", modifier.getSymbol().getText()))
            return Opcodes.ACC_PUBLIC;
        else if (DefaultGroovyMethods.isCase("private", modifier.getSymbol().getText()))
            return Opcodes.ACC_PRIVATE;
        else if (DefaultGroovyMethods.isCase("protected", modifier.getSymbol().getText()))
            return Opcodes.ACC_PROTECTED;
        else
            throw new AssertionError(modifier.getSymbol().getText() + " is not a valid visibility modifier!");
    }

    public int parseVisibilityModifiers(List<TerminalNode> modifiers, int defaultValue) {
        if (! asBoolean(modifiers)) return defaultValue;

        if (modifiers.size() > 1) {
            Token modifier = modifiers.get(1).getSymbol();

            Integer line = modifier.getLine();
            Integer col = modifier.getCharPositionInLine() + 1;

            reportError("Cannot specify modifier: " + modifier.getText() + " when access scope has already been defined at line: " + String.valueOf(line) + " column: " + String.valueOf(col) + ". File: " + sourceUnit.getName(), line, col);
        }


        return parseVisibilityModifiers(modifiers.get(0));
    }

    /**
     * Method for construct string from string literal handling empty strings.
     *
     * @param node
     * @return
     */
    public String parseString(TerminalNode node) {
        String t = node.getText();
        return asBoolean(t) ? DefaultGroovyMethods.getAt(t, new IntRange(true, 1, -2)) : t;
    }

    public Object initialExpressionForType(ClassNode type) {
        if (ClassHelper.int_TYPE.equals(type))
            return 0;
        else if (ClassHelper.long_TYPE.equals(type))
            return 0L;
        else if (ClassHelper.double_TYPE.equals(type))
            return 0.0;
        else if (ClassHelper.float_TYPE.equals(type))
            return 0f;
        else if (ClassHelper.boolean_TYPE.equals(type))
            return Boolean.FALSE;
        else if (ClassHelper.short_TYPE.equals(type))
            return (short)0;
        else if (ClassHelper.byte_TYPE.equals(type))
            return (byte)0;
        else if (ClassHelper.char_TYPE.equals(type))
            return (char)0;
        else return null;
    }

    private String readSourceCode(SourceUnit sourceUnit) {
        String text = null;
        try {
            text = DefaultGroovyMethods.getText(
                    new BufferedReader(
                            sourceUnit.getSource().getReader()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred when reading source code.", e);
        }

        return text;
    }


    private void setUpErrorListener(GroovyParser parser) {
        parser.removeErrorListeners();
        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(
                    @NotNull Recognizer<?, ?> recognizer,
                    Object offendingSymbol, int line, int charPositionInLine,
                    @NotNull String msg, RecognitionException e) {
                sourceUnit.getErrorCollector().addFatalError(new SyntaxErrorMessage(new SyntaxException(msg, line, charPositionInLine+1), sourceUnit));
            }

            @Override
            public void reportAmbiguity(@NotNull Parser recognizer, @NotNull DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, @NotNull ATNConfigSet configs) {
                log.fine("Ambiguity at " + startIndex + " - " + stopIndex);
            }

            @Override
            public void reportAttemptingFullContext(
                    @NotNull Parser recognizer,
                    @NotNull DFA dfa, int startIndex, int stopIndex,
                    BitSet conflictingAlts, @NotNull ATNConfigSet configs) {
                log.fine("Attempting Full Context at " + startIndex + " - " + stopIndex);
            }

            @Override
            public void reportContextSensitivity(
                    @NotNull Parser recognizer,
                    @NotNull DFA dfa, int startIndex, int stopIndex, int prediction, @NotNull ATNConfigSet configs) {
                log.fine("Context Sensitivity at " + startIndex + " - " + stopIndex);
            }
        });
    }

    private void logTreeStr(GroovyParser.CompilationUnitContext tree) {
        final StringBuilder s = new StringBuilder();
        new ParseTreeWalker().walk(new ParseTreeListener() {
            @Override
            public void visitTerminal(@NotNull TerminalNode node) {
                s.append(multiply(".\t", indent));
                s.append(String.valueOf(node));
                s.append("\n");
            }

            @Override
            public void visitErrorNode(@NotNull ErrorNode node) {
            }

            @Override
            public void enterEveryRule(@NotNull final ParserRuleContext ctx) {
                s.append(multiply(".\t", indent));
                s.append(GroovyParser.ruleNames[ctx.getRuleIndex()] + ": {");
                s.append("\n");
                indent = indent++;
            }

            @Override
            public void exitEveryRule(@NotNull ParserRuleContext ctx) {
                indent = indent--;
                s.append(multiply(".\t", indent));
                s.append("}");
                s.append("\n");
            }

            public int getIndent() {
                return indent;
            }

            public void setIndent(int indent) {
                this.indent = indent;
            }

            private int indent;
        }, tree);

        log.fine((multiply("=", 60)) + "\n" + String.valueOf(s) + "\n" + (multiply("=", 60)));
    }

    private void logTokens(String text) {
        final GroovyLexer lexer = new GroovyLexer(new ANTLRInputStream(text));
        log.fine(multiply("=", 60) + "\n" + text + "\n" + multiply("=", 60));
        log.fine("\nLexer TOKENS:\n\t" + DefaultGroovyMethods.join(collect(lexer.getAllTokens(), new Closure<String>(this, this) {
            public String doCall(Token it) { return String.valueOf(it.getLine()) + ", " + String.valueOf(it.getStartIndex()) + ":" + String.valueOf(it.getStopIndex()) + " " + GroovyLexer.tokenNames[it.getType()] + " " + it.getText(); }
        }), "\n\t") + multiply("=", 60));
    }

    public ModuleNode getModuleNode() {
        return moduleNode;
    }

    public void setModuleNode(ModuleNode moduleNode) {
        this.moduleNode = moduleNode;
    }

    private ModuleNode moduleNode;
    private SourceUnit sourceUnit;
    private ClassLoader classLoader;
    private ASTBuilder instance;
    private Stack<ClassNode> classes = new Stack<ClassNode>();
    private Stack<List<InnerClassNode>> innerClassesDefinedInMethod = new Stack<List<InnerClassNode>>();
    private int anonymousClassesCount = 0;
    private Logger log = Logger.getLogger(ASTBuilder.class.getName());
}
