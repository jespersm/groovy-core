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
package org.codehaus.groovy.parser.antlr4

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.parser.antlr4.util.ASTComparatorCategory
import org.codehaus.groovy.parser.antlr4.util.ASTWriter
import org.codehaus.groovy.syntax.Token
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Logger

class MainTest extends Specification {
    private Logger log = Logger.getLogger(MainTest.class.getName());
    public static final String DEFAULT_RESOURCES_PATH = 'subprojects/groovy-antlr4-grammar/src/test/resources';
    public static final String RESOURCES_PATH = new File(DEFAULT_RESOURCES_PATH).exists() ? DEFAULT_RESOURCES_PATH : 'src/test/resources';



	@Unroll
    def "test ast builder for #path"() {
        def filename = path;

        setup:
        def file = new File("$RESOURCES_PATH/$path")
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)
        config = config.is(_) ? ASTComparatorCategory.DEFAULT_CONFIGURATION : config

        expect:
        moduleNodeNew
        moduleNodeOld
        ASTComparatorCategory.apply(config) {
            assert moduleNodeOld == moduleNodeOld2
        }
        and:
        ASTWriter.astToString(moduleNodeNew) == ASTWriter.astToString(moduleNodeOld2)
        and:
        ASTComparatorCategory.apply(config) {
            assert moduleNodeNew == moduleNodeOld, "Fail in $path"
        }

        where:
        path | config
        "Annotations_Issue30_1.groovy" | _
        "Annotations_Issue30_2.groovy" | _
        "ArrayType_Issue44_1.groovy" | _
        "AssignmentOps_Issue23_1.groovy" | _
        "ClassConstructorBug_Issue13_1.groovy" | _
        "ClassInitializers_Issue_20_1.groovy" | _
        "ClassMembers_Issue3_1.groovy" | _
        "ClassMembers_Issue3_2.groovy" | _
        "ClassModifiers_Issue_2.groovy" | _
        "ClassProperty_Issue4_1.groovy" | _
        "Closure_Issue21_1.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "Enums_Issue43_1.groovy" | _
        "ExceptionHandling_Issue27_1.groovy" | _
        "ExplicitConstructor.groovy" | _
        "Extendsimplements_Issue25_1.groovy" | _
        "FieldAccessAndMethodCalls_Issue37_1.groovy" | _
        'FieldAccessAndMethodCalls_Issue37_2.groovy' | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "FieldInitializersAndDefaultMethods_Issue49_1.groovy" | _
        "Generics_Issue26_1.groovy" | addIgnore(GenericsType, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "GStrings_Issue41_1.groovy" | _
        "ImportRecognition_Issue6_1.groovy" | _
        "ImportRecognition_Issue6_2.groovy" | _
        "InnerClasses_Issue48_1.groovy" | _
        "ListsAndMaps_Issue22_1.groovy" | _
        "Literals_Numbers_Issue36_1.groovy" | _
        'Literals_Other_Issue36_4.groovy' | _
        "Literals_HexOctNumbers_Issue36_2.groovy" | _
        "Literals_Strings_Issue36_3.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "MapParameters_Issue55.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "MemberAccess_Issue14_1.groovy" | _
        "MethodBody_Issue7_1.groovy" | _
        "MethodCall_Issue15_1.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "New_Issue47_1.groovy" | _
        "Operators_Issue9_1.groovy" | _
        "Binary_and_Unary_Operators.groovy" | _
        "ParenthesisExpression_Issue24_1.groovy" | _
        "Script_Issue50_1.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "Statements_Issue17_1.groovy" | addIgnore([IfStatement, ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "Statements_Issue58_1.groovy" | addIgnore([IfStatement, ForStatement, ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "SubscriptOperator.groovy" | _
        "AnnotationDeclaration.groovy" | _
        "TernaryAndElvis_Issue57.groovy" | _
        "TernaryAndElvis_01.groovy" | _
        "TestClass1.groovy" | _
        "ThrowDeclarations_Issue_28_1.groovy" | _
        "Assert_Statements.groovy" | _
        "Unicode_Identifiers.groovy" | _
        "ClassMembers_String_Method_Name.groovy" | _
        "ScriptPart_String_Method_Name.groovy" | _
        "Multiline_GString.groovy" | _
        "Unescape_String_Literals_Issue7.groovy" | _
        "GString-closure-and-expression_issue12.groovy" | _
        "Slashy_Strings.groovy" | _
        "Expression_Precedence.groovy" | _
        "Expression_Span_Rows.groovy" | _
        "Tuples_issue13.groovy" | _
        "Dollar_Slashy_Strings.groovy" | _
        "Dollar_Slashy_GStrings.groovy" | _
        "SyntheticPublic_issue19.groovy" | _
        "Traits_issue21.groovy" | _
        "EmptyScript.groovy" | _
        "SemiColonScript.groovy" | _
        "Enums_issue31.groovy" | _
        "CallExpression_issue33_1.groovy" | _
        "CallExpression_issue33_2.groovy" | addIgnore(Parameter, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "CallExpression_issue33_3.groovy" | addIgnore([Parameter, IfStatement, ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "CallExpression_issue33_4.groovy" | _
        "Closure_Call_Issue40.groovy" | _
        "CommandExpression_issue41.groovy" | _
        "SynchronizedStatement.groovy" | _
        "VarArg.groovy" | _
        "Join_Line_Escape_issue46.groovy" | _
        "Enums_Inner.groovy" | _
        "Interface.groovy" | _
        "ClassMembers_Issue3_3.groovy" | _
        "FieldAccess_1.groovy" | _
        "BreakAndContinue.groovy" | _
        "Switch-Case_issue36.groovy" | addIgnore(CaseStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "ScriptSupport.groovy" | addIgnore([FieldNode, PropertyNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)

    }


    @Unroll
    def "test grails-core-3 for #path"() {
        def filename = path;

        setup:
        def file = new File("$RESOURCES_PATH/grails-core-3/$path")
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)
        config = config.is(_) ? ASTComparatorCategory.DEFAULT_CONFIGURATION : config

        expect:
        moduleNodeNew
        moduleNodeOld
        ASTComparatorCategory.apply(config) {
            assert moduleNodeOld == moduleNodeOld2
        }
        and:
        ASTWriter.astToString(moduleNodeNew) == ASTWriter.astToString(moduleNodeOld2)
        and:
        ASTComparatorCategory.apply(config) {
            assert moduleNodeNew == moduleNodeOld, "Fail in $path"
        }

        where:
        path | config
        "buildSrc/src/main/groovy/org/grails/gradle/GrailsBuildPlugin.groovy" | _

        "grails-async/src/main/groovy/grails/async/DelegateAsync.groovy" | _
        "grails-async/src/main/groovy/grails/async/Promise.groovy" | _
        "grails-async/src/main/groovy/grails/async/PromiseFactory.groovy" | _
        "grails-async/src/main/groovy/grails/async/PromiseList.groovy" | _
        "grails-async/src/main/groovy/grails/async/PromiseMap.groovy" | _
        "grails-async/src/main/groovy/grails/async/Promises.groovy" | _
        "grails-async/src/main/groovy/grails/async/decorator/PromiseDecorator.groovy" | _
        "grails-async/src/main/groovy/grails/async/decorator/PromiseDecoratorLookupStrategy.groovy" | _
        "grails-async/src/main/groovy/grails/async/decorator/PromiseDecoratorProvider.groovy" | _
        "grails-async/src/main/groovy/grails/async/factory/AbstractPromiseFactory.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/decorator/PromiseDecorator.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/decorator/PromiseDecoratorLookupStrategy.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/decorator/PromiseDecoratorProvider.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/AbstractPromiseFactory.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/BoundPromise.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/SynchronousPromise.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/SynchronousPromiseFactory.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/gpars/GparsPromise.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/gpars/GparsPromiseFactory.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/gpars/LoggingPoolFactory.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/reactor/ReactorPromise.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/factory/reactor/ReactorPromiseFactory.groovy" | _
        "grails-async/src/main/groovy/org/grails/async/transform/internal/DelegateAsyncUtils.groovy" | _
        "grails-async/src/test/groovy/grails/async/DelegateAsyncSpec.groovy" | _
        "grails-async/src/test/groovy/grails/async/PromiseListSpec.groovy" | addIgnore(ThrowStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-async/src/test/groovy/grails/async/PromiseMapSpec.groovy" | addIgnore(ThrowStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-async/src/test/groovy/grails/async/PromiseSpec.groovy" | _
        "grails-async/src/test/groovy/grails/async/ReactorPromiseFactorySpec.groovy" | _
        "grails-async/src/test/groovy/grails/async/SynchronousPromiseFactorySpec.groovy" | _

//        "grails-bootstrap/src/main/groovy/grails/build/proxy/SystemPropertiesAuthenticator.groovy" | _
        "grails-bootstrap/SystemPropertiesAuthenticator.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/codegen/model/Model.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/codegen/model/ModelBuilder.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/config/ConfigMap.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/io/IOUtils.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/io/ResourceUtils.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/io/support/SystemOutErrCapturer.groovy" | addIgnore(MethodNode, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-bootstrap/src/main/groovy/grails/io/support/SystemStreamsRedirector.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/plugins/GrailsVersionUtils.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/plugins/VersionComparator.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/util/BuildSettings.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/util/CosineSimilarity.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/util/Described.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/util/Metadata.groovy" | _
        "grails-bootstrap/src/main/groovy/grails/util/Named.groovy" | _
        "grails-bootstrap/src/main/groovy/org/codehaus/groovy/grails/io/support/GrailsResourceUtils.groovy" | _
        "grails-bootstrap/src/main/groovy/org/codehaus/groovy/grails/io/support/Resource.groovy" | _
        "grails-bootstrap/src/main/groovy/org/codehaus/groovy/grails/plugins/GrailsPluginInfo.groovy" | _
//        "grails-bootstrap/src/main/groovy/org/grails/build/parsing/ScriptNameResolver.groovy" | _
        "grails-bootstrap/ScriptNameResolver.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/config/CodeGenConfig.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/config/NavigableMap.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/exceptions/ExceptionUtils.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/CodeSnippetPrinter.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/DefaultStackTracePrinter.groovy" | addIgnore(ReturnStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-bootstrap/src/main/groovy/org/grails/exceptions/reporting/StackTracePrinter.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/io/support/ByteArrayResource.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/io/support/DevNullPrintStream.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/io/support/FactoriesLoaderSupport.groovy" | _
        "grails-bootstrap/src/main/groovy/org/grails/io/support/MainClassFinder.groovy" | addIgnore(ReturnStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-bootstrap/src/main/groovy/org/grails/io/watch/FileExtensionFileChangeListener.groovy" | _
//        "grails-bootstrap/src/test/groovy/grails/build/logging/GrailsConsoleSpec.groovy" | _
        "grails-bootstrap/GrailsConsoleSpec.groovy" | _
        "grails-bootstrap/src/test/groovy/grails/config/ConfigMapSpec.groovy" | _
        "grails-bootstrap/src/test/groovy/grails/config/GrailsConfigSpec.groovy" | _
        "grails-bootstrap/src/test/groovy/grails/io/IOUtilsSpec.groovy" | _
        "grails-bootstrap/src/test/groovy/grails/util/EnvironmentTests.groovy" | _
        "grails-bootstrap/src/test/groovy/org/codehaus/groovy/grails/cli/parsing/CommandLineParserSpec.groovy" | _

        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/Base64CodecExtensionMethods.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/DigestUtils.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/HexCodecExtensionMethods.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/MD5BytesCodecExtensionMethods.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/MD5CodecExtensionMethods.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA1BytesCodecExtensionMethods.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA1CodecExtensionMethods.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA256BytesCodecExtensionMethods.groovy" | _
        "grails-codecs/src/main/groovy/org/grails/plugins/codecs/SHA256CodecExtensionMethods.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/Base64CodecTests.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/HexCodecTests.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/MD5BytesCodecTests.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/MD5CodecTests.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/SHA1BytesCodecTests.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/SHA1CodecTests.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/SHA256BytesCodec.groovy" | _
        "grails-codecs/src/test/groovy/org/grails/web/codecs/SHA256CodecTests.groovy" | _

        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/beans/factory/GenericBeanFactoryAccessor.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/AbstractGrailsClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/AbstractInjectableGrailsClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/AnnotationDomainClassArtefactHandler.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/ArtefactHandlerAdapter.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/BootstrapArtefactHandler.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/ClassPropertyFetcher.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/ControllerArtefactHandler.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsApplication.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsBootstrapClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsControllerClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsDomainClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsDomainClassProperty.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsServiceClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsTagLibClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/DefaultGrailsUrlMappingsClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/ExternalGrailsDomainClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsBootstrapClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/InjectableGrailsClass.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/cfg/GrailsConfig.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/spring/DefaultRuntimeSpringConfiguration.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/spring/GrailsRuntimeConfigurator.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/spring/GrailsWebApplicationContext.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/commons/spring/RuntimeSpringConfiguration.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/AbstractArtefactTypeAstTransformation.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/AllArtefactClassInjector.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/AnnotatedClassInjector.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/ClassInjector.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/DefaultGrailsDomainClassInjector.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/EntityASTTransformation.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsAwareClassLoader.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsAwareInjectionOperation.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/core/io/DefaultResourceLocator.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/domain/GrailsDomainClassCleaner.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/exceptions/GrailsConfigurationException.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/exceptions/GrailsDomainException.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/exceptions/GrailsException.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/exceptions/GrailsRuntimeException.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/io/support/GrailsIOUtils.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/io/support/IOUtils.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/lifecycle/ShutdownOperations.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/orm/support/TransactionManagerAware.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/plugins/AbstractGrailsPluginManager.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/plugins/DefaultGrailsPluginManager.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/plugins/DomainClassPluginSupport.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/plugins/GrailsVersionUtils.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/plugins/support/aware/ClassLoaderAware.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/plugins/support/aware/GrailsConfigurationAware.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/support/ClassEditor.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/support/PersistenceContextInterceptor.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/support/SoftThreadLocalMap.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/support/proxy/EntityProxyHandler.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/support/proxy/ProxyHandler.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/validation/AbstractVetoingConstraint.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/validation/DefaultConstraintEvaluator.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/validation/GrailsDomainClassValidator.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/validation/VetoingConstraint.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/validation/exceptions/ConstraintException.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/binding/GrailsWebDataBinder.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/context/GrailsConfigUtils.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/context/ServletContextHolder.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/errors/GrailsExceptionResolver.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/mapping/CachingLinkGenerator.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/metaclass/ForwardMethod.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/metaclass/RenderDynamicMethod.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/DelegatingApplicationAttributes.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/GrailsFlashScope.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/GrailsUrlPathHelper.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/HttpHeaders.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/WrappedResponseHolder.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/mvc/GrailsDispatcherServlet.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/mvc/GrailsHttpSession.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/mvc/GrailsParameterMap.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/mvc/GrailsWebRequest.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/mvc/RedirectEventListener.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/mvc/exceptions/ControllerExecutionException.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/servlet/mvc/exceptions/GrailsMVCException.groovy" | _
        "grails-compat/src/main/groovy/org/codehaus/groovy/grails/web/util/WebUtils.groovy" | _
        "grails-compat/src/main/groovy/org/grails/databinding/SimpleDataBinder.groovy" | _
        "grails-compat/src/main/groovy/org/grails/databinding/SimpleMapDataBindingSource.groovy" | _

        "grails-console/src/main/groovy/grails/ui/command/GrailsApplicationContextCommandRunner.groovy" | _
        "grails-console/src/main/groovy/grails/ui/console/GrailsSwingConsole.groovy" | _
        "grails-console/src/main/groovy/grails/ui/console/support/GroovyConsoleApplicationContext.groovy" | _
        "grails-console/src/main/groovy/grails/ui/console/support/GroovyConsoleWebApplicationContext.groovy" | _
        "grails-console/src/main/groovy/grails/ui/script/GrailsApplicationScriptRunner.groovy" | _
        "grails-console/src/main/groovy/grails/ui/shell/GrailsShell.groovy" | _
        "grails-console/src/main/groovy/grails/ui/shell/support/GroovyshApplicationContext.groovy" | _
        "grails-console/src/main/groovy/grails/ui/shell/support/GroovyshWebApplicationContext.groovy" | _
        "grails-console/src/main/groovy/grails/ui/support/DevelopmentGrailsApplication.groovy" | _
        "grails-console/src/main/groovy/grails/ui/support/DevelopmentWebApplicationContext.groovy" | _

        "grails-core/src/main/groovy/grails/beans/util/LazyBeanMap.groovy" | _
        "grails-core/src/main/groovy/grails/boot/GrailsApp.groovy" | _
        "grails-core/src/main/groovy/grails/boot/GrailsAppBuilder.groovy" | _
        "grails-core/src/main/groovy/grails/boot/GrailsPluginApplication.groovy" | _
        "grails-core/src/main/groovy/grails/boot/config/GrailsApplicationContextLoader.groovy" | _
        "grails-core/src/main/groovy/grails/boot/config/GrailsApplicationPostProcessor.groovy" | _
        "grails-core/src/main/groovy/grails/boot/config/GrailsAutoConfiguration.groovy" | _
        "grails-core/src/main/groovy/grails/boot/config/tools/ProfilingGrailsApplicationPostProcessor.groovy" | _
        "grails-core/src/main/groovy/grails/boot/config/tools/SettingsFile.groovy" | _
        "grails-core/src/main/groovy/grails/compiler/DelegatingMethod.groovy" | _
        "grails-core/src/main/groovy/grails/compiler/GrailsCompileStatic.groovy" | _
        "grails-core/src/main/groovy/grails/compiler/GrailsTypeChecked.groovy" | _
        "grails-core/src/main/groovy/grails/compiler/ast/GlobalClassInjector.groovy" | _
        "grails-core/src/main/groovy/grails/compiler/ast/GlobalClassInjectorAdapter.groovy" | _
        "grails-core/src/main/groovy/grails/config/Config.groovy" | _
        "grails-core/src/main/groovy/grails/config/ConfigProperties.groovy" | _
        "grails-core/src/main/groovy/grails/config/Settings.groovy" | _
        "grails-core/src/main/groovy/grails/core/GrailsApplicationClass.groovy" | _
        "grails-core/src/main/groovy/grails/core/GrailsApplicationLifeCycle.groovy" | _
        "grails-core/src/main/groovy/grails/core/GrailsApplicationLifeCycleAdapter.groovy" | _
        "grails-core/src/main/groovy/grails/core/events/ArtefactAdditionEvent.groovy" | _
        "grails-core/src/main/groovy/grails/dev/Support.groovy" | _
        "grails-core/src/main/groovy/grails/dev/commands/ApplicationCommand.groovy" | _
        "grails-core/src/main/groovy/grails/dev/commands/ApplicationContextCommandRegistry.groovy" | _
        "grails-core/src/main/groovy/grails/dev/commands/ExecutionContext.groovy" | _
        "grails-core/src/main/groovy/grails/persistence/support/PersistenceContextInterceptorExecutor.groovy" | _
        "grails-core/src/main/groovy/grails/plugins/Plugin.groovy" | _
        "grails-core/src/main/groovy/grails/plugins/PluginManagerLoader.groovy" | _
        "grails-core/src/main/groovy/grails/transaction/GrailsTransactionTemplate.groovy" | _
        "grails-core/src/main/groovy/grails/transaction/Rollback.groovy" | _
        "grails-core/src/main/groovy/grails/util/GrailsArrayUtils.groovy" | _
        "grails-core/src/main/groovy/grails/util/GrailsStringUtils.groovy" | _
        "grails-core/src/main/groovy/grails/util/MixinTargetAware.groovy" | _
        "grails-core/src/main/groovy/grails/util/TypeConvertingMap.groovy" | _
        "grails-core/src/main/groovy/grails/validation/ValidationErrors.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/ApplicationAttributes.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/ArtefactHandler.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/ArtefactInfo.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/DomainClassArtefactHandler.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsApplication.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsClass.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsClassUtils.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsControllerClass.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsDomainClass.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsDomainClassProperty.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsDomainConfigurationUtil.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/commons/GrailsMetaClassUtils.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/ASTErrorsHelper.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/ASTValidationErrorsHelper.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/AbstractGrailsArtefactTransformer.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/AstTransformer.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsASTUtils.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsArtefactClassInjector.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsDomainClassInjector.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/plugins/GrailsPlugin.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/plugins/GrailsPluginManager.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/plugins/PluginManagerAware.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/plugins/support/aware/GrailsApplicationAware.groovy" | _
        "grails-core/src/main/groovy/org/codehaus/groovy/grails/validation/ConstraintsEvaluator.groovy" | _
        "grails-core/src/main/groovy/org/grails/boot/internal/JavaCompiler.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/CriteriaTypeCheckingExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/DomainMappingTypeCheckingExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/DynamicFinderTypeCheckingExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/HttpServletRequestTypeCheckingExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/RelationshipManagementMethodTypeCheckingExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/ValidateableTypeCheckingExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/WhereQueryTypeCheckingExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/injection/ApplicationClassInjector.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/injection/EnhancesTraitTransformation.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/injection/GlobalGrailsClassInjectorTransformation.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/injection/GlobalImportTransformation.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/injection/GroovyEclipseCompilationHelper.groovy" | _
        "grails-core/src/main/groovy/org/grails/compiler/injection/TraitInjectionSupport.groovy" | addIgnore([Parameter, IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-core/src/main/groovy/org/grails/config/NavigableMapPropertySource.groovy" | _
        "grails-core/src/main/groovy/org/grails/config/PrefixedMapPropertySource.groovy" | _
        "grails-core/src/main/groovy/org/grails/config/yaml/YamlPropertySourceLoader.groovy" | addIgnore([Parameter, IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-core/src/main/groovy/org/grails/core/artefact/ApplicationArtefactHandler.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/cfg/GroovyConfigPropertySourceLoader.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/exceptions/DefaultErrorsPrinter.groovy" | addIgnore([Parameter, IfStatement, ExpressionStatement, ContinueStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-core/src/main/groovy/org/grails/core/io/CachingPathMatchingResourcePatternResolver.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/io/GrailsResource.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/io/support/GrailsFactoriesLoader.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/legacy/LegacyGrailsApplication.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/legacy/LegacyGrailsDomainClass.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/metaclass/MetaClassEnhancer.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/support/GrailsApplicationDiscoveryStrategy.groovy" | _
        "grails-core/src/main/groovy/org/grails/core/util/IncludeExcludeSupport.groovy" | _
        "grails-core/src/main/groovy/org/grails/dev/support/DevelopmentShutdownHook.groovy" | _
        "grails-core/src/main/groovy/org/grails/plugins/CoreGrailsPlugin.groovy" | _
        "grails-core/src/main/groovy/org/grails/plugins/support/WatchPattern.groovy" | addIgnore([IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-core/src/main/groovy/org/grails/spring/beans/factory/HotSwappableTargetSourceFactoryBean.groovy" | _
        "grails-core/src/main/groovy/org/grails/spring/context/ApplicationContextExtension.groovy" | _
        "grails-core/src/main/groovy/org/grails/spring/context/support/MapBasedSmartPropertyOverrideConfigurer.groovy" | _
        "grails-core/src/main/groovy/org/grails/transaction/transform/RollbackTransform.groovy" | _
        "grails-core/src/main/groovy/org/grails/transaction/transform/TransactionalTransform.groovy" | _
        "grails-core/src/main/groovy/org/grails/validation/ConstraintEvalUtils.groovy" | _
        "grails-core/src/test/groovy/grails/artefact/EnhancesSpec.groovy" | _
        "grails-core/src/test/groovy/grails/config/ConfigPropertiesSpec.groovy" | _
        "grails-core/src/test/groovy/grails/transaction/TransactionalTransformSpec.groovy" | _
        "grails-core/src/test/groovy/grails/util/GrailsMetaClassUtilsSpec.groovy" | addIgnore([PackageNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-core/src/test/groovy/grails/web/CamelCaseUrlConverterSpec.groovy" | _
        "grails-core/src/test/groovy/grails/web/HyphenatedUrlConverterSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/commons/GrailsArrayUtilsSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/commons/GrailsStringUtilsSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/commons/cfg/GrailsPlaceHolderConfigurerCorePluginRuntimeSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/commons/cfg/GrailsPlaceholderConfigurerSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/compiler/injection/ASTValidationErrorsHelperSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/compiler/injection/ApiDelegateSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsArtefactTransformerSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/core/io/ResourceLocatorSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/exceptions/StackTraceFiltererSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/exceptions/StackTracePrinterSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/plugins/BinaryPluginSpec.groovy" | _
        "grails-core/src/test/groovy/org/codehaus/groovy/grails/plugins/support/WatchPatternParserSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/compiler/injection/ArtefactTypeAstTransformationSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/compiler/injection/DefaultDomainClassInjectorSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/compiler/injection/GlobalGrailsClassInjectorTransformationSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/config/NavigableMapPropertySourceSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/config/PropertySourcesConfigSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/config/YamlPropertySourceLoaderSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/core/DefaultGrailsControllerClassSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/plugins/GrailsPluginTests.groovy" | _
        "grails-core/src/test/groovy/org/grails/spring/context/ApplicationContextExtensionSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/transaction/ChainedTransactionManagerPostProcessorSpec.groovy" | _
        "grails-core/src/test/groovy/org/grails/util/TypeConvertingMapTests.groovy" | _

        "grails-databinding/src/main/groovy/grails/databinding/SimpleDataBinder.groovy" | addIgnore([Parameter, ExpressionStatement, IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-databinding/src/main/groovy/grails/databinding/SimpleMapDataBindingSource.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/BindUsing.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/BindingFormat.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/ClosureValueConverter.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/IndexedPropertyReferenceDescriptor.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/AbstractStructuredDateBindingEditor.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/CurrencyValueConverter.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/DateConversionHelper.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/FormattedDateValueConverter.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredCalendarBindingEditor.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredDateBindingEditor.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/StructuredSqlDateBindingEditor.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/converters/TimeZoneConverter.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/xml/GPathResultCollectionDataBindingSource.groovy" | _
        "grails-databinding/src/main/groovy/org/grails/databinding/xml/GPathResultMap.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/BindUsingSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/BindingErrorSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/BindingFormatSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/BindingListenerSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/CollectionBindingSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/CustomTypeConverterSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/IncludeExcludeBindingSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderEnumBindingSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderEnumValueConverterSpec.groovy" | addIgnore([FieldNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-databinding/src/test/groovy/grails/databinding/SimpleDataBinderSpec.groovy" | _
        "grails-databinding/src/test/groovy/grails/databinding/XMLBindingSpec.groovy" | _
        "grails-databinding/src/test/groovy/org/grails/databinding/compiler/BindingFormatCompilationErrorsSpec.groovy" | _
        "grails-databinding/src/test/groovy/org/grails/databinding/converters/CurrencyConversionSpec.groovy" | _
        "grails-databinding/src/test/groovy/org/grails/databinding/converters/DateConversionHelperSpec.groovy" | _
        "grails-databinding/src/test/groovy/org/grails/databinding/xml/GPathCollectionDataBindingSourceSpec.groovy" | _
        "grails-databinding/src/test/groovy/org/grails/databinding/xml/GPathResultMapSpec.groovy" | _

        "grails-docs/src/main/groovy/grails/doc/DocEngine.groovy" | addIgnore([ExpressionStatement, IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/DocPublisher.groovy" | addIgnore([ExpressionStatement, IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/LegacyDocMigrator.groovy" | addIgnore([ExpressionStatement, Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/PdfBuilder.groovy" | _
        "grails-docs/src/main/groovy/grails/doc/ant/DocPublisherTask.groovy" | addIgnore([IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/filters/HeaderFilter.groovy" | _
        "grails-docs/src/main/groovy/grails/doc/filters/LinkTestFilter.groovy" | addIgnore([IfStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/filters/ListFilter.groovy" | _
        "grails-docs/src/main/groovy/grails/doc/gradle/MigrateLegacyDocs.groovy" | addIgnore([Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/gradle/PublishGuide.groovy" | addIgnore([Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/gradle/PublishPdf.groovy" | _
        "grails-docs/src/main/groovy/grails/doc/internal/FileResourceChecker.groovy" | _
        "grails-docs/src/main/groovy/grails/doc/internal/LegacyTocStrategy.groovy" | addIgnore([Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/internal/UserGuideNode.groovy" | _
        "grails-docs/src/main/groovy/grails/doc/internal/YamlTocStrategy.groovy" | addIgnore([Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/macros/GspTagSourceMacro.groovy" | addIgnore([Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-docs/src/main/groovy/grails/doc/macros/HiddenMacro.groovy" | _
        "grails-docs/src/test/groovy/grails/doc/internal/LegacyTocStrategySpec.groovy" | _
        "grails-docs/src/test/groovy/grails/doc/internal/StringEscapeCategoryTests.groovy" | _
        "grails-docs/src/test/groovy/grails/doc/internal/YamlTocStrategySpec.groovy" | _
        "grails-docs/src/test/groovy/grails/doc/macros/GspTagSourceMacroTest.groovy" | addIgnore([FieldNode, PropertyNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "grails-encoder/src/main/groovy/org/grails/buffer/StreamCharBufferMetaUtils.groovy" | addIgnore([Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/main/groovy/org/grails/encoder/CodecMetaClassSupport.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/main/groovy/org/grails/encoder/impl/HTMLCodecFactory.groovy" | addIgnore([FieldNode, PropertyNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/main/groovy/org/grails/encoder/impl/JSONCodecFactory.groovy" | _
        "grails-encoder/src/main/groovy/org/grails/encoder/impl/JavaScriptCodec.groovy" | addIgnore([ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/main/groovy/org/grails/encoder/impl/StandaloneCodecLookup.groovy" | addIgnore([Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/main/groovy/org/grails/encoder/impl/URLCodecFactory.groovy" | _
        "grails-encoder/src/test/groovy/org/grails/buffer/StreamCharBufferGroovyTests.groovy" | _
        "grails-encoder/src/test/groovy/org/grails/charsequences/CharSequencesSpec.groovy" | addIgnore([ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/test/groovy/org/grails/encoder/ChainedEncodersSpec.groovy" | addIgnore([ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/test/groovy/org/grails/encoder/impl/BasicCodecLookupSpec.groovy" | _
        "grails-encoder/src/test/groovy/org/grails/encoder/impl/HTMLEncoderSpec.groovy" | addIgnore([ExpressionStatement, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-encoder/src/test/groovy/org/grails/encoder/impl/JavaScriptCodecTests.groovy" | _

        "grails-gradle-model/src/main/groovy/org/grails/gradle/plugin/model/DefaultGrailsClasspath.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-model/src/main/groovy/org/grails/gradle/plugin/model/GrailsClasspath.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/agent/AgentTasksEnhancer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/commands/ApplicationContextCommandTask.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsExtension.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
//tested separately in "test GrailsGradlePlugin for #path"       "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsPluginGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/IntegrationTestGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/PluginDefiner.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/doc/GrailsDocGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/doc/PublishGuideTask.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/model/GrailsClasspathToolingModelBuilder.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/GrailsProfileGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/GrailsProfilePublishGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/profiles/tasks/ProfileCompilerTask.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/publishing/GrailsCentralPublishGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/publishing/GrailsPublishExtension.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/run/FindMainClassTask.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/run/GrailsRunTask.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/util/SourceSets.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/watch/GrailsWatchPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/watch/WatchConfig.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/GrailsWebGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/gsp/GroovyPageCompileTask.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/web/gsp/GroovyPagePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)


        "grails-gsp/src/main/groovy/org/grails/gsp/GroovyPagesMetaUtils.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gsp/src/main/groovy/org/grails/gsp/compiler/GroovyPageCompiler.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-gsp/src/test/groovy/org/grails/gsp/GroovyPagesTemplateEngineTests.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-logging/src/test/groovy/org/grails/compiler/logging/LoggingTransformerSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/grails/artefact/AsyncController.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/grails/async/services/PersistenceContextPromiseDecorator.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/grails/async/services/TransactionalPromiseDecorator.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/grails/async/web/AsyncGrailsWebRequest.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/grails/compiler/traits/AsyncControllerTraitInjector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/org/grails/async/transform/internal/DefaultDelegateAsyncTransactionalMethodTransformer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/org/grails/compiler/web/async/TransactionalAsyncTransformUtils.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/ControllersAsyncGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/GrailsAsyncContext.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/WebRequestPromiseDecorator.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/WebRequestPromiseDecoratorLookupStrategy.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/main/groovy/org/grails/plugins/web/async/mvc/AsyncActionResultTransformer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-async/src/test/groovy/grails/async/services/AsyncTransactionalServiceSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-codecs/src/main/groovy/org/grails/plugins/CodecsGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-codecs/src/main/groovy/org/grails/plugins/codecs/URLCodec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/HTMLCodecTests.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/HTMLJSCodecSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/JSONEncoderSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-codecs/src/test/groovy/org/grails/web/codecs/URLCodecTests.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/grails/artefact/Controller.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, CaseStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/AllowedMethodsHelper.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/RequestForwarder.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/ResponseRedirector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
//tested separately        "grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/ResponseRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/grails/compiler/traits/ControllerTraitInjector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/grails/web/Controller.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/controllers/ControllersGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/controllers/DefaultControllerExceptionHandlerMetaData.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/controllers/metaclass/ForwardMethod.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/context/BootStrapClassRunner.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/mvc/InvalidResponseHandler.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/org/grails/plugins/web/servlet/mvc/ValidResponseHandler.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/test/groovy/grails/artefact/controller/support/AllowedMethodsHelperSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/test/groovy/org/codehaus/groovy/grails/compiler/web/ControllerActionTransformerClosureActionOverridingSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/test/groovy/org/codehaus/groovy/grails/compiler/web/ControllerActionTransformerCompilationErrorsSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/test/groovy/org/codehaus/groovy/grails/compiler/web/ControllerActionTransformerSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/main/groovy/grails/web/JSONBuilder.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/main/groovy/org/grails/plugins/converters/ConvertersGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
//tested separately        "grails-plugin-converters/src/main/groovy/org/grails/web/converters/AbstractParsingParameterCreationListener.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/main/groovy/org/grails/web/converters/ConfigurableConverter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/main/groovy/org/grails/web/converters/ConvertersExtension.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/main/groovy/org/grails/web/converters/IncludeExcludeConverter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/main/groovy/org/grails/web/converters/configuration/configtest.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/test/groovy/grails/converters/ParsingNullJsonValuesSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/test/groovy/org/grails/compiler/web/converters/ConvertersDomainTransformerSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/test/groovy/org/grails/plugins/converters/api/ConvertersApiSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/test/groovy/org/grails/web/converters/ConverterUtilSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/test/groovy/org/grails/web/converters/marshaller/json/DomainClassMarshallerSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/test/groovy/org/grails/web/converters/marshaller/json/ValidationErrorsMarshallerSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, AssertStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "grails-plugin-databinding/src/main/groovy/org/grails/databinding/converters/web/LocaleAwareBigDecimalConverter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-databinding/src/main/groovy/org/grails/databinding/converters/web/LocaleAwareNumberConverter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-databinding/src/main/groovy/org/grails/plugins/databinding/DataBindingGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/DataSourceGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/DataSourceUtils.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/EmbeddedDatabaseShutdownHook.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-datasource/src/main/groovy/org/grails/plugins/datasource/TomcatJDBCPoolMBeanExporter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/main/groovy/grails/artefact/DomainClass.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/main/groovy/grails/compiler/traits/DomainClassTraitInjector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/DomainClassGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/DomainClassPluginSupport.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/support/GormApiSupport.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/main/groovy/org/grails/plugins/domain/support/GrailsDomainClassCleaner.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/test/groovy/org/codehaus/groovy/grails/domain/CircularBidirectionalMapBySpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/test/groovy/org/codehaus/groovy/grails/domain/DomainClassTraitSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-domain-class/src/test/groovy/org/codehaus/groovy/grails/domain/EntityTransformIncludesGormApiSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/main/groovy/grails/events/Events.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/main/groovy/org/grails/events/ClosureEventConsumer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/main/groovy/org/grails/events/reactor/GrailsReactorConfigurationReader.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/main/groovy/org/grails/events/spring/SpringEventTranslator.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/main/groovy/org/grails/plugins/events/EventBusGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/test/groovy/grails/events/EventsTraitSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/test/groovy/grails/events/SpringEventTranslatorSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-events/src/test/groovy/org/grails/events/reactor/GrailsReactorConfigurationReaderSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/ast/groovy/grails/compiler/traits/ControllerTagLibraryTraitInjector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/GrailsLayoutViewResolverPostProcessor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/GroovyPagesGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/ApplicationTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/CountryTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/FormTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/FormatTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/JavascriptTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/PluginTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/UrlMappingTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-gsp/src/main/groovy/org/grails/plugins/web/taglib/ValidationTagLib.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-i18n/src/main/groovy/org/grails/plugins/i18n/I18nGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/main/groovy/grails/artefact/Interceptor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/main/groovy/grails/compiler/traits/InterceptorTraitInjector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/main/groovy/grails/interceptors/Matcher.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/GrailsInterceptorHandlerInterceptorAdapter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/InterceptorsGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/main/groovy/org/grails/plugins/web/interceptors/UrlMappingMatcher.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/test/groovy/grails/artefact/GrailsInterceptorHandlerInterceptorAdapterSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/test/groovy/grails/artefact/InterceptorSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-interceptors/src/test/groovy/org/grails/plugins/web/interceptors/UrlMappingMatcherSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/grails/web/mime/AcceptHeaderParser.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/api/MimeTypesApiSupport.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/FormatInterceptor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/MimeTypesFactoryBean.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/plugins/web/mime/MimeTypesGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/DefaultAcceptHeaderParser.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/DefaultMimeTypeResolver.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/HttpServletRequestExtension.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/main/groovy/org/grails/web/mime/HttpServletResponseExtension.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/test/groovy/org/codehaus/groovy/grails/plugins/web/api/RequestAndResponseMimeTypesApiSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/test/groovy/org/codehaus/groovy/grails/web/mime/AcceptHeaderParserTests.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-mimetypes/src/test/groovy/org/codehaus/groovy/grails/web/mime/MimeUtilitySpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/compiler/traits/RestResponderTraitInjector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/Link.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/Linkable.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/Resource.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/RestfulController.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractIncludeExcludeRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractRenderContext.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/AbstractRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/ContainerRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/RenderContext.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/Renderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/RendererRegistry.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/atom/AtomCollectionRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/atom/AtomRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/errors/AbstractVndErrorRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/errors/VndErrorJsonRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/errors/VndErrorXmlRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalJsonCollectionRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalJsonRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalXmlCollectionRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/hal/HalXmlRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/json/JsonCollectionRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/json/JsonRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/util/AbstractLinkingRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/xml/XmlCollectionRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/grails/rest/render/xml/XmlRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/plugin/RestResponderGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/DefaultRendererRegistry.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/ServletRenderContext.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/html/DefaultHtmlRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/json/DefaultJsonRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/render/xml/DefaultXmlRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/transform/LinkableTransform.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/transform/ResourceTransform.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/DefaultRendererRegistrySpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/VndErrorRenderingSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/hal/HalJsonRendererSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/html/HtmlRendererSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/render/json/JsonRendererSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/transform/LinkableTransformSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-rest/src/test/groovy/org/grails/plugins/web/rest/transform/ResourceTransformSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-services/src/main/groovy/grails/artefact/Service.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-services/src/main/groovy/grails/compiler/traits/ServiceTraitInjector.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-services/src/main/groovy/org/grails/plugins/services/ServiceBeanAliasPostProcessor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-services/src/main/groovy/org/grails/plugins/services/ServicesGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/TestMixinTargetAware.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/domain/DomainClassUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/domain/MockCascadingDomainClassValidator.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/integration/Integration.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/integration/IntegrationTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/services/ServiceUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/support/GrailsUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/support/TestMixinRegistrar.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/support/TestMixinRegistry.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/web/ControllerUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/web/GroovyPageUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/web/InterceptorUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/web/UrlMappingsUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/webflow/WebFlowUnitTestMixin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/mixin/webflow/WebFlowUnitTestSupport.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/ControllerTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/CoreBeansTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/DefaultSharedRuntimeConfigurer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/DirtiesRuntime.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/DomainClassTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/FreshRuntime.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/GrailsApplicationTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/GroovyPageTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/InterceptorTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/MetaClassCleanerTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/SharedRuntime.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/SharedRuntimeConfigurer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestEvent.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestEventInterceptor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestPluginUsage.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntime.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeJunitAdapter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeSettings.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/TestRuntimeUtil.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/grails/test/runtime/WebFlowTestPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/org/grails/compiler/injection/test/IntegrationTestMixinTransformation.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/org/grails/test/context/junit4/GrailsJunit4ClassRunner.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/main/groovy/org/grails/test/mixin/support/DefaultTestMixinRegistrar.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/test/groovy/grails/test/mixin/MetaClassCleanupSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/test/groovy/grails/test/mixin/TestForSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/test/groovy/grails/test/mixin/TestMixinSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/test/groovy/grails/test/mixin/integration/compiler/IntegrationTestMixinCompilationErrorsSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-testing/src/test/groovy/grails/test/runtime/TestRuntimeFactorySpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-url-mappings/src/main/groovy/org/grails/plugins/web/mapping/UrlMappingsGrailsPlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-validation/src/main/groovy/grails/validation/Validateable.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-validation/src/main/groovy/org/grails/web/plugins/support/ValidationSupport.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-validation/src/test/groovy/grails/validation/DefaultASTValidateableHelperSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "grails-shell/src/main/groovy/org/grails/cli/GrailsCli.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/boot/GrailsDependencyVersions.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/boot/GrailsTestCompilerAutoConfiguration.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/boot/SpringInvoker.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/ClasspathBuildAction.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/GradleAsyncInvoker.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/GradleInvoker.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/GradleUtil.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/cache/CachedGradleOperation.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/cache/ListReadingCachedGradleOperation.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/cache/MapReadingCachedGradleOperation.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/commands/GradleCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/commands/GradleTaskCommandAdapter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/gradle/commands/ReadGradleTasks.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/AllClassCompleter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/ClassNameCompleter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/ClosureCompleter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/DomainClassCompleter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/EscapingFileNameCompletor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/RegexCompletor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/SimpleOrFileNameCompletor.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/interactive/completers/TestsCompleter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/AbstractProfile.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/AbstractStep.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/Command.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/CommandArgument.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/CommandDescription.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/CommandException.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/DefaultFeature.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/Feature.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/FileSystemProfile.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/MultiStepCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/ProfileCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/ProfileRepository.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/ProfileRepositoryAware.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/ProjectCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/ProjectContextAware.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/ResourceProfile.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/Step.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/codegen/ModelBuilder.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/ArgumentCompletingCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/ClosureExecutingCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/CommandCompleter.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/CommandRegistry.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreateAppCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreatePluginCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/CreateProfileCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/DefaultMultiStepCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/HelpCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/ListProfilesCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/OpenCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/ProfileInfoCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/events/CommandEvents.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME "[org.grails.cli.profile.commands.events.EventStorage][class org.codehaus.groovy.ast.ClassNode][objectInitializerStatements]:: [org.codehaus.groovy.ast.stmt.BlockStatement@25d1604e[org.codehaus.groovy.ast.stmt.BlockStatement@6c99ed76[org.codehaus.groovy.ast.stmt.ExpressionStatement@7de5e9d5[expression:org.codehaus.groovy.ast.expr.ListExpression@43104479[]]]]] != []"        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/events/EventStorage.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ApplicationContextCommandFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ClasspathCommandResourceResolver.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/CommandFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/CommandResourceResolver.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/FileSystemCommandResourceResolver.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/GroovyScriptCommandFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME "97: no viable alternative"        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ResourceResolvingCommandFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/ServiceCommandFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/factory/YamlCommandFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME "[class org.codehaus.groovy.ast.ModuleNode][mainClassName]:: org.grails.cli.profile.commands.io.FileSystemInteraction != org.grails.cli.profile.commands.io.FileSystemInteraction$CopySpec"        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/FileSystemInteraction.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/FileSystemInteractionImpl.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/io/ServerInteraction.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/script/GroovyScriptCommand.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/script/GroovyScriptCommandTransform.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/SimpleTemplate.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateException.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/commands/templates/TemplateRendererImpl.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/git/GitProfileRepository.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/repository/AbstractJarProfileRepository.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/repository/MavenProfileRepository.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/repository/StaticJarProfileRepository.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/steps/DefaultStepFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/steps/ExecuteStep.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/steps/GradleStep.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/steps/MkdirStep.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/steps/RenderStep.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/steps/StepFactory.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/steps/StepRegistry.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/main/groovy/org/grails/cli/profile/support/ArtefactVariableResolver.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/test/groovy/org/grails/cli/interactive/completers/RegexCompletorSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/test/groovy/org/grails/cli/profile/ResourceProfileSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/test/groovy/org/grails/cli/profile/commands/CommandRegistrySpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/test/groovy/org/grails/cli/profile/commands/CommandScriptTransformSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/test/groovy/org/grails/cli/profile/repository/MavenRepositorySpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/test/groovy/org/grails/cli/profile/steps/StepRegistrySpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-shell/src/test/resources/profiles-repository/profiles/web/commands/TestGroovy.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-spring/src/main/groovy/grails/spring/DynamicElementReader.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/main/groovy/org/grails/taglib/NamespacedTagDispatcher.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/main/groovy/org/grails/taglib/TagLibraryMetaUtils.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/main/groovy/org/grails/taglib/TemplateNamespacedTagDispatcher.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/main/groovy/org/grails/taglib/encoder/OutputEncodingSettings.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/main/groovy/org/grails/taglib/encoder/WithCodecHelper.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/test/groovy/org/grails/taglib/GroovyPageAttributesTests.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/test/groovy/org/grails/taglib/GroovyPageTagWriterSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-taglib/src/test/groovy/org/grails/taglib/encoder/WithCodecHelperSpec.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, ReturnStatement, ForStatement, CaseStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)


    }

    @Unroll
    def "test separately for #path"() { // "and: ASTWriter.astToString(moduleNodeNew) == ASTWriter.astToString(moduleNodeOld2)" will cause java.lang.OutOfMemoryError
        def filename = path;

        setup:
        def file = new File("$RESOURCES_PATH/grails-core-3/$path")
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)
        config = config.is(_) ? ASTComparatorCategory.DEFAULT_CONFIGURATION : config
        def moduleNodeNewAstStr = moduleNodeNew ? ASTWriter.astToString(moduleNodeNew) : null;
        def moduleNodeOld2AstStr = moduleNodeOld2 ? ASTWriter.astToString(moduleNodeOld2) : null;
        def astStrCompareResult = moduleNodeNewAstStr && moduleNodeOld2AstStr && (moduleNodeNewAstStr == moduleNodeOld2AstStr)

        expect:
        moduleNodeNew
        moduleNodeOld
        ASTComparatorCategory.apply(config) {
            assert moduleNodeOld == moduleNodeOld2
        }
        and:
        astStrCompareResult
        and:
        ASTComparatorCategory.apply(config) {
            assert moduleNodeNew == moduleNodeOld, "Fail in $path"
        }
        where:
        path | config
        "grails-gradle-plugin/src/main/groovy/org/grails/gradle/plugin/core/GrailsGradlePlugin.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode, GenericsType], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-controllers/src/main/groovy/grails/artefact/controller/support/ResponseRenderer.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "grails-plugin-converters/src/main/groovy/org/grails/web/converters/AbstractParsingParameterCreationListener.groovy" | addIgnore([Parameter, IfStatement, ThrowStatement, ExpressionStatement, FieldNode, PropertyNode, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)

    }


    @Unroll
    def "test Groovy in Action 2nd Edition for #path"() {
        def filename = path;

        setup:
        def file = new File("$RESOURCES_PATH/GroovyInAction2/$path")
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)
        config = config.is(_) ? ASTComparatorCategory.DEFAULT_CONFIGURATION : config

        expect:
        moduleNodeNew
        moduleNodeOld
        ASTComparatorCategory.apply(config) {
            assert moduleNodeOld == moduleNodeOld2
        }
        and:
        ASTWriter.astToString(moduleNodeNew) == ASTWriter.astToString(moduleNodeOld2)
        and:
        ASTComparatorCategory.apply(config) {
            assert moduleNodeNew == moduleNodeOld, "Fail in $path"
        }

        where:
        path | config
        "appD/Listing_D_01_GStrings.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "appD/Listing_D_02_Lists.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "appD/Listing_D_03_Closures.groovy" | addIgnore([AssertStatement, Parameter], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "appD/Listing_D_04_Regex.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "appD/Listing_D_05_GPath.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap01/Listing_01_01_Gold.groovy" | addIgnore([AssertStatement, ReturnStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap01/snippet0101_customers.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap01/snippet0101_fileLineNumbers.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap01/snippet0101_printPackageNames.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap01/snippet0101_printPackageNamesGpath.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap01/snippet0102_printGroovyWebSiteCount.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap01/snippet0103_googleIpAdr.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap02/Book.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/Listing_02_01_Assertions.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/Listing_02_03_BookScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/Listing_02_04_BookBean.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/Listing_02_05_ImmutableBook.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/Listing_02_06_Grab.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/Listing_02_07_Clinks.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/Listing_02_08_ControlStructures.groovy" | addIgnore([AssertStatement, WhileStatement, ForStatement, BreakStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0201_comments.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0202_failing_assert.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0203_clinks_java.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0203_gstring.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0203_int_usage.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0203_map_usage.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0203_range_usage.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0203_roman.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0204_evaluate_jdk7_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0204_evaluate_jdk8_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap02/snippet0204_failing_typechecked.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap03/extra_escaped_characters_table36.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/extra_method_operators_table34.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/extra_numeric_literals_table32.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/extra_numerical_coercion_table310.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/extra_optional_typing_table33.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/extra_primitive_values_table31.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_01_PrimitiveMethodsObjectOperators.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_02_ListMapCast.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_03_DefiningOperators.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_04_DefiningGStrings.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_05_StringOperations.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_06_RegexGStrings.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_07_RegularExpressions.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_08_EachMatch.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_09_PatternReuse.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_10_PatternsClassification.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/Listing_03_11_NumberMethodsGDK.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0301_autoboxing.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0304_GString_internals.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0304_stringbuffer.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0305_matcher_each_group.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0305_matcher_groups.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0305_matcher_parallel_assignment.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0305_matcher_plain.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap03/snippet0306_GDK_methods_for_numbers.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap04/extra_EnumRange.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/extra_ListCast.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/extra_ListTable.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/extra_Map_as.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/extra_Map_group.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/extra_MaxMinSum.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/extra_SplitList.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_01_range_declarations.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_02_ranges_are_objects.groovy" | addIgnore([AssertStatement, ThrowStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_03_custom_ranges.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_04_list_declarations.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_05_list_subscript_operator.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_06_list_add_remove.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_07_lists_control_structures.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_08_list_content_manipulation.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_09_list_other_methods.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_10_list_quicksort.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_11_list_mapreduce.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_12_map_declarations.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_13_map_accessors.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_14_map_query_methods.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_15_map_iteration.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_16_map_content.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/Listing_04_17_map_example.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/snippet0402_ListAsSet.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/snippet0402_ListRemoveNulls.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/snippet0402_ListStreams_jdk8_plus.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/snippet0403_Map_Ctor_Expression.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/snippet0403_Map_Ctor_Unquoted.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/snippet0403_Map_MapReduce.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap04/snippet0403_Map_String_accessors.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap05/extra_Closure_delegate.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/extra_Closure_myWith.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/extra_ClosureProperty.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_01_closure_simple_declaration.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_02_simple_method_closure.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_03_multi_method_closure.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_04_closure_all_declarations.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_05_simple_closure_calling.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_06_calling_closures.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_07_simple_currying.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_08_logging_curry_example.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_09_closure_scope.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_10_closure_accumulator.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/Listing_05_11_visitor_pattern.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0501_envelope.groovy.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0504_closure_default_params.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0504_closure_isCase.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0504_closure_paramcount.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0505_map_with.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0505_scoping.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0506_closure_return.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0507_closure_composition.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0508_memoize.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap05/snippet0509_trampoline.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap06/extra_if_return.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/extra_in_operator.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/extra_switch_return.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_01_groovy_truth.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_02_assignment_bug.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_03_if_then_else.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_04_conditional_operator.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_05_switch_basic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_06_switch_advanced.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_07_assert_host.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_08_while.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_09_for.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_10_break_continue.groovy" | addIgnore([AssertStatement, ContinueStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/Listing_06_11_exception_example.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0602_bad_file_read.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0602_bad_file_read_with_message.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0602_failing_assert.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0603_each_loop_iterate.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0603_file_iterate_lines.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0603_for_loop_iterate.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0603_null_iterate.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0603_object_iterate.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0603_regex_iterate_match.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap06/snippet0604_multicatch.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap07/business/Vendor.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_01_Declaring_Variables.groovy" | addIgnore([AssertStatement, MethodNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_02_TypeBreaking_Assignment.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_03_Referencing_Fields.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_04_Overriding_Field_Access.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_05_Declaring_Methods.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_06_Declaring_Parameters.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_07_Parameter_Usages.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_08_Safe_Dereferencing.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_09_Instantiation.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_10_Instantiation_Named.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_11_Classes.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_13_Import.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_14_Import_As_BugFix.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_15_Import_As_NameClash.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_16_Multimethods.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_17_MultiEquals.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_18_Traits.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_19_Declaring_Beans.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_20_Calling_Beans.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_21_Calling_Beans_Advanced.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_22_Property_Methods.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_23_Expando.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/Listing_07_24_GPath.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/snippet0703_Implicit_Closure_To_SAM.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/snippet0705_Spread_List.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/snippet0705_Spread_Map.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/snippet0705_Spread_Range.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/thirdparty/MathLib.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap07/thirdparty2/MathLib.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap08/custom/Custom.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/custom/useCustom.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/failing_Listing_08_15_EMC_static.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/failing_Listing_08_16_EMC_super.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/groovy/runtime/metaclass/custom/CustomMetaClass.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_01_method_missing.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_02_mini_gorm.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_03_property_missing.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_04_bin_property.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_05_closure_dynamic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_06_property_method.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_07_MetaClass_jdk7_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_07_MetaClass_jdk8_plus.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_08_ProxyMetaClass.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_09_Expando.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_10_EMC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_11_EMC_Groovy_Class.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_12_EMC_Groovy_Object.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_13_EMC_Java_Object.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_14_EMC_Builder.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_15_EMC_static.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_16_EMC_super.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_17_EMC_hooks.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_18_Existing_Categories.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_19_Marshal.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_20_MarshalCategory.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_21_Test_Mixin.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_22_Sieve_Mixin.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_23_Millimeter.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_24_create_factory.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_25_fake_assign.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_26_restore_emc.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap08/Listing_08_27_intercept_cache_invoke.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap09/Listing_09_01_ToStringDetective.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_02_ToStringSleuth.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_03_EqualsAndHashCode.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_04_TupleConstructor.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_05_Lazy.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_06_IndexedProperty.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_07_InheritConstructors.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_08_Sortable.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_09_Builder.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_10_Canonical.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_11_Immutable.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_12_Delegate.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_13_Singleton.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_14_Memoized.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_15_TailRecursive.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_16_Log.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_17_Synchronized.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_18_SynchronizedCustomLock.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_19_ReadWriteLock.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_20_AutoClone.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_21_AutoCloneCopyConstructor.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_22_AutoExternalize.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_23_TimedInterrupt.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_24_ThreadInterrupt.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_25_ConditionalInterrupt.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_26_Field.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_27_BaseScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_28_AstByHand.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_29_AstByHandWithUtils.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_30_AstBuildFromSpec.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_31_AstBuildFromString.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_32_AstBuildFromStringMixed.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_33_AstBuildFromCode.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_34_GreeterMainTransform.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_35_GreeterMainTransform2.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_38_AstTesting1.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_39_AstTesting2.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_40_AstTesting3.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_41_AstTesting4.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_42_AstTesting5.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/settings.gradle" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_autoCloneDefault.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_autoCloneSerialization.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_autoExternalize.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_fieldEquivalent.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_mapCreation.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_noisySetDelegateByHand.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_noisySetInheritance.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_nonTailCallReverseList.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_readWriteByHand.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_readWriteLock.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_singletonByHand.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_toStringEquivalent.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0903_greeterExpanded.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0903_greeterScript.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0903_localMain.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0903_localMainTransformation.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0905_GetCompiledTimeScript.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/src/main/groovy/regina/CompiledAtASTTransformation.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/src/test/groovy/regina/CompiledAtASTTransformationTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap10/extra1004_RuntimeGroovyDispatch.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_01_Duck.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_02_failing_Typo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_03_ClassTC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_04_OneMethodTC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_05_CompileTimeTypo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_06_MethodNameTypo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_07_MethodArgsFlipped.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_08_InvalidAssignments.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_09_AssignmentsWithCoercion.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_10_DefField.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_11_InPlaceList.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_12_Generics.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_13_ListStyleCtorRuntime.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_14_ListStyleCtorTC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_15_MapStyleCtorBad.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_16_ListStyleCtor.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_17_ListStyleCtorFixed.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_18_CodeAsData.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_19_ClosuresBadReturnType.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_20_UserValidation.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_21_UserValidationTC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_22_UserValidation_ExplicitTypes.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_23_UserValidation_SAM.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_24_UserValidation_ClosureParams.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_25_UserValidation_DSL.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_26_UserValidation_DelegatesTo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_27_UserValidation_DelegatesToTarget.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_28_Category.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_29_EMC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_30_Builder.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_31_MixedTypeChecking.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_32_Skip.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_33_FlowTyping.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_34_FlowTypingOk.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_35_LUB.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_36_Condition.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_37_ClosureSharedVar.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_38_LubError.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_39_LubOk.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_40_FibBench.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_42_StaticCompileDispatch.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_43_MonkeyPatching.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_44_BookingDSL.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_45_MultiValidation.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_46_RobotExtension.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/Listing_10_47_SQLExtension.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/snippet1003_GroovyGreeter.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/snippet1005_RobotMainTC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/snippet1005_SqlMainTC.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap10/User.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap11/Listing_11_03_MarkupBuilderPlain.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_04_NodeBuilder.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_05_NodeBuilderLogic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_06_MarkupBuilderLogic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_07_MarkupBuilderHtml.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_08_StreamingMarkupBuilderLogic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_10_PW_SwingBuilder.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_11_Swing_Widgets.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_12_Swing_Layout.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_13_Table_Demo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_14_Binding.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_15_Plotter.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_16_Groovyfx.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_17_CalorieCounterBuilderSupport.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_18_CalorieCounterFactoryBuilderSupport.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/Listing_11_19_CalorieCounterByHand.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/snippet1103_MarkupWithHyphen.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/snippet1106_AntBuilderIf.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap11/snippet1107_Printer.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap12/Listing_12_01_info_jdk6_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_01_info_jdk7_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_01_info_jdk8_plus.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_02_properties.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_03_File_Iteration.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_04_Filesystem.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_05_Traversal.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_06_File_Read.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_07_File_Write.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_08_Writer_LeftShift.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_09_File_Transform_jdk7_plus.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_10_File_ObjectStreams.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_11_Temp_Dir.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_12_Threads.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_13_Processes_UnixCommands.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_14_Processes_ZipUnzip.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_15_SimpleTemplateEngine.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_16_GroovletExample.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_17_HelloWorldGroovlet.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_19_InspectGroovlet.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_20_HiLowGame.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/Listing_12_22_TemplateGroovlet.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/snippet1201_SlowTyping.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/snippet1201_UseCategory.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap12/snippet1202_base64.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap13/extra_NeoGremlinGraph.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/layering/AthleteApplication.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/layering/AthleteDAO.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/layering/DataAccessObject.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/layering/DbHelper.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_01_Connecting.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_02_ConnectingDataSource.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_03_Creating.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_05_Inserting.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_06_Reading.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_07_Updating.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_08_Delete.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_09_Transactions.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_10_Batching.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_11_Paging.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_12_Metadata.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_13_MoreMetadata.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_14_NamedOrdinal.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_15_StoredProcBasic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_16_StoredProcParam.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_17_StoredProcInOut.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_18_DataSetBasics.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_19_DataSetFiltering.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_20_DataSetViews.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_25_AthleteAppMain.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_26_AthleteAppTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_27_MongoAthletes.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_28_NeoAthletes.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/Listing_13_29_NeoGremlin.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/snippet1301_ConnectingWithGrab.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/snippet1301_ConnectingWithInstance.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/snippet1301_ConnectingWithMap.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/snippet1301_ReadEachRow.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/snippet1301_ReadEachRowList.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/snippet1301_ReadQuery.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/snippet1301_ReadRows.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/util/DbUtil.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/util/MarathonRelationships.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap13/util/Neo4jUtil.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap14/Listing_14_02_DOM.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_03_DOM_Category.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_04_XmlParser.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_05_XmlSlurper.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_06_SAX.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_07_StAX.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_08_XmlBoiler.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_09_XmlStreamer.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_10_StreamedHtml.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_11_UpdateDomCategory.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_12_UpdateParser.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_13_UpdateSlurper.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_14_XPath.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_16_XPathTemplate.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_17_JsonParser.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_18_JsonBuilder.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_19_JsonBuilderLogic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/Listing_14_20_JsonOutputAthlete.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap14/UpdateChecker.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap15/Listing_15_01_RSS_bbcnews.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_02_ATOM_devworks.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_03_REST_jira_url.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_04_REST_jira_httpb_get.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_05_REST_currency_httpb_get.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_06_REST_currency_httpb_post.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_07_REST_currency_jaxrs.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_08_REST_currency_jaxrs_proxy.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_09_XMLRPC_echo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_10_XMLRPC_jira.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_11_SOAP_wsdl.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_12_SOAP11_currency_url.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_13_SOAP12_currency_httpb.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_14_SOAP11_currency_wslite.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap15/Listing_15_15_SOAP12_currency_wslite.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap16/Listing_16_01_HelloIntegration.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_03_MultilineScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_04_UsingEval.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_05_Binding.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_06_BindingTwoWay.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_07_ClassInScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_08_Payment_calculator.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_09_MethodsInBinding.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/Listing_16_12_BeanToString.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/shapes/Circle.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/shapes/MaxAreaInfo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/spring/groovy/Circle.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap16/spring/groovy/MaxAreaInfo.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap17/automation/src/main/groovy/Calculator.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/automation/src/test/groovy/CalculatorTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/cobertura/src/main/groovy/BiggestPairCalc.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/cobertura/src/main/groovy/BiggestPairCalcFixed.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/cobertura/src/test/groovy/BiggestPairCalcTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Converter.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Counter.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME        "chap17/extra_ParameterizedTestNG.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/extra_TestNG.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Farm.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_01_Celsius.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_02_CounterTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_03_HashMapTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_04_GroovyTestSuite.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_05_AllTestSuite.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_06_DataDrivenJUnitTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_07_PropertyBased.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_08_Balancer.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_09_BalancerStub.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_10_BalancerMock.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_11_LoggingCounterTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_12_JUnitPerf.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_13_SpockSimple.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_14_SpockMock.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_15_SpockMockWildcards.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_16_SpockMockClosureChecks.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Listing_17_17_SpockDataDriven.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/LoggingCounter.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/MovieTheater.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/Purchase.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/snippet1701_JUnit4.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap17/snippet1704_listPropertyCheck.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap18/Listing_18_01_ConcurrentSquares.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_02_ConcurrentSquaresTransparent.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_03_ConcurrentSquaresTransitive.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_04_MapFilterReduce.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_05_SquaresMapReduce.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_06_Dataflow.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_07_DataflowStreams.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_08_Actors.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_09_ActorsLifecycle.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_10_ActorsMessageAware.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_11_Agent.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_13_YahooForkJoin.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_14_YahooMapReduce.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/Listing_18_15_YahooDataflow.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/snippet1801_startThread.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/snippet1803_java_parallel_streams_jdk8_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/snippet1804_deadlock.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/snippet1804_nondeterministic.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap18/YahooService.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap19/FetchOptions.groovy" | addIgnore([AssertStatement, ConstructorNode] , ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/FetchOptionsBuilder.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_06_Binding.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_29_OrderDSL.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_30_WhenIfControlStructure.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_31_Until_failing_.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_32_UntilControlStructure.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_39_GivenWhenThen.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_43_FetchOptionsScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_44_RubyStyleNewify.groovy" | addIgnore([AssertStatement, Token], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_45_PythonStyleNewify.groovy" | addIgnore([AssertStatement, Token], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_46_Terms.groovy" | addIgnore([AssertStatement, Token], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_48_No_IO.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_49_ArithmeticShell.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_50_TimedInterrupt.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_51_SystemExitGuard.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Listing_19_53_QueryCustomizer.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/Query.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/extra_FetchOptions_traditional.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v01/Listing_19_01_SelfContainedScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_04_MainSimple.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_05_MainGroovyShell.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_07_MainBinding.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_08_MainDirectionConstants.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_09_MainDirectionsSpread.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_10_MainImplicitMethod.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_12_MainBaseScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_13_MainImportCustomizer.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_14_MainCustomBaseScriptClass.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_16_MainMethodClosure.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/Listing_19_19_MainLowerCase.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/integration/CaseRobotBaseScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/integration/CustomBinding.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/integration/RobotBaseScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/model/Direction.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/model/Robot.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v02/snippet1901_MainFileRunner.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/Listing_19_27_SimpleCommandChain.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/Listing_19_40_Robot_With.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/integration/DistanceCategory.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/integration/RobotBaseScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/integration/SuperBotBaseScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/model/Direction.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/model/Distance.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/model/Duration.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/model/Robot.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/model/Speed.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/model/SuperBot.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/v03/model/Unit.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/xform/BusinessLogicScript.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/xform/CustomControlStructure.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/xform/Listing_19_36_WhenTransformation.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/xform/WhenUntilTransform.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/xform/extra_WhenTransformationWorksWithoutBraces.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap19/xform/snippet1906_WhenUntilXform_Structure.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

        "chap20/Listing_20_01_Grapes_for_twitter_urls.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap20/Listing_20_02_Scriptom_Windows_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap20/Listing_20_03_ActivX_Windows_only.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap20/Listing_20_10_SquaringMapValue.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap20/Listing_20_11_Synchronized.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap20/Listing_20_12_DbC_invariants.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)

    }



    @Unroll
    def "test comments for #path"() {
        def filename = path;

        setup:
        def file = new File("$RESOURCES_PATH/$path")
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)
        config = config.is(_) ? ASTComparatorCategory.DEFAULT_CONFIGURATION : config
        List<ClassNode> classes = new LinkedList(moduleNodeNew.classes).sort { c1, c2 -> c1.name <=> c2.name }

        expect:
        moduleNodeNew
        moduleNodeOld
        ASTComparatorCategory.apply(config) {
            assert moduleNodeOld == moduleNodeOld2
        }
        and:
        ASTWriter.astToString(moduleNodeNew) == ASTWriter.astToString(moduleNodeOld2)
        and:
        ASTComparatorCategory.apply(config) {
            assert moduleNodeNew == moduleNodeOld, "Fail in $path"
        }
        and:
        classes[0].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')            == '/** * test class Comments */'
        and:
        classes[0].fields[0].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**     * test Comments.SOME_VAR     */'
        and:
        classes[0].fields[1].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**     * test Comments.SOME_VAR2     */'
        and:
        classes[0].declaredConstructors[0].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**     * test Comments.constructor1     */'
        and:
        classes[0].methods[0].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**     * test Comments.m1     */'
        and:
        classes[0].methods[1].nodeMetaData[ASTBuilder.DOC_COMMENT] == null
        and:
        classes[0].methods[2].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**     * test Comments.m3     */'

        and:
        classes[1].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')            == '/**     * test class InnerClazz     */'
        and:
        classes[1].fields[0].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * test InnerClazz.SOME_VAR3         */'
        and:
        classes[1].fields[1].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * test InnerClazz.SOME_VAR4         */'
        and:
        classes[1].methods[0].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**         * test Comments.m4         */'
        and:
        classes[1].methods[1].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**         * test Comments.m5         */'

        and:
        classes[2].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')            == '/**     * test class InnerEnum     */'
        and:
        classes[2].fields[0].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * InnerEnum.NEW         */'
        and:
        classes[2].fields[1].nodeMetaData[ASTBuilder.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * InnerEnum.OLD         */'

        where:
        path | config
        "Comments.groovy" | _

    }


    @Unroll
    def "test by evaluating script: #path"() {
        def filename = path;

        setup:
        def file = new File("$RESOURCES_PATH/$path")
        def gsh = createGroovyShell(compilerConfiguration)


        expect:
        assertScript(gsh, file);

        where:
        path | compilerConfiguration
        "Assert_issue9.groovy" | CompilerConfiguration.DEFAULT
        "CallExpression_issue33_5.groovy" | CompilerConfiguration.DEFAULT
    }


    @Unroll
    def "test invalid files #path"() {
        when:
            def file = new File("$RESOURCES_PATH/$path")
        then:
            ! canLoad(file, Configuration.NEW) && ! canLoad(file, Configuration.OLD)
        where:
            path | output
            "Statement_Errors_1.groovy" | _
            "Statement_Errors_2.groovy" | _
            "Statement_Errors_3.groovy" | _
            "Statement_Errors_4.groovy" | _
            "Statement_Errors_5.groovy" | _
            "Statement_Errors_6.groovy" | _
            "Statement_Errors_7.groovy" | _
            "Statement_Errors_8.groovy" | _
            "Statement_Errors_9.groovy" | _
            "Statement_Errors_10.groovy" | _
            "ClassModifiersInvalid_Issue1_2.groovy" | _
            "ClassModifiersInvalid_Issue2_2.groovy" | _

    }


    def addIgnore(Class aClass, ArrayList<String> ignore, Map<Class, List<String>> c = null) {
        c = c ?: ASTComparatorCategory.DEFAULT_CONFIGURATION.clone() as Map<Class, List<String>>;
        c[aClass].addAll(ignore)
        c
    }

    def addIgnore(Collection<Class> aClass, ArrayList<String> ignore, Map<Class, List<String>> c = null) {
        c = c ?: ASTComparatorCategory.DEFAULT_CONFIGURATION.clone() as Map<Class, List<String>>;
        aClass.each { c[it].addAll(ignore) }
        c
    }

    boolean canLoad(File file, Configuration config) {
        def module = new Main(config).process(file)
        return module != null && ! module.context.errorCollector.hasErrors()
    }

    def createGroovyShell(CompilerConfiguration c) {
        CompilerConfiguration configuration = new CompilerConfiguration(c)
        configuration.pluginFactory = new Antlrv4PluginFactory()

        return new GroovyShell(configuration);
    }

    def assertScript(gsh, file) {
        def content = file.text;
        try {
            gsh.evaluate(content);

            log.info("Evaluated $file")

            return true;
        } catch (Throwable t) {
            log.info("Failed $file: ${t.getMessage()}");

            return false;
        }
    }
}
