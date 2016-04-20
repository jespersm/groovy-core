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

import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.parser.antlr4.util.ASTComparatorCategory
import org.codehaus.groovy.parser.antlr4.util.ASTWriter
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
        "Closure_Call_Issue40.groovy" | _
        "CommandExpression_issue41.groovy" | _
        "Switch-Case_issue36.groovy" | addIgnore(CaseStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "ScriptSupport.groovy" | addIgnore([FieldNode, PropertyNode], ASTComparatorCategory.LOCATION_IGNORE_LIST)

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
//FIXME        "chap09/Listing_09_18_SynchronizedCustomLock.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_19_ReadWriteLock.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_20_AutoClone.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_21_AutoCloneCopyConstructor.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_22_AutoExternalize.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_23_TimedInterrupt.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/Listing_09_24_ThreadInterrupt.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME        "chap09/Listing_09_25_ConditionalInterrupt.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
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
//FIXME        "chap09/snippet0902_autoCloneDefault.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME        "chap09/snippet0902_autoCloneSerialization.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME        "chap09/snippet0902_autoExternalize.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_fieldEquivalent.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_mapCreation.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "chap09/snippet0902_noisySetDelegateByHand.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
//FIXME        "chap09/snippet0902_noisySetInheritance.txt" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
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
//FIXME        "chap09/src/test/groovy/regina/CompiledAtASTTransformationTest.groovy" | addIgnore(AssertStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
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

    }


    /*
	@Unroll
    def "test invalid class modifiers #path"() {
        expect:
        def file = new File("$RESOURCES_PATH/$path")

        def errorCollectorNew = new Main(Configuration.NEW).process(file).context.errorCollector
        def errorCollectorOld = new Main(Configuration.OLD).process(file).context.errorCollector
        def errorCollectorOld2 = new Main(Configuration.OLD).process(file).context.errorCollector


        def cl = { ErrorCollector errorCollector, int it -> def s = new StringWriter(); errorCollector.getError(it).write(new PrintWriter(s)); s.toString() }
        def errOld1 = (0..<errorCollectorOld.errorCount).collect cl.curry(errorCollectorOld)
        def errOld2 = (0..<errorCollectorOld2.errorCount).collect cl.curry(errorCollectorOld2)
        def errNew = (0..<errorCollectorNew.errorCount).collect cl.curry(errorCollectorNew)

        assert errOld1 == errOld2
        assert errOld1 == errNew

        where:
        path | output
        "ClassModifiersInvalid_Issue1_2.groovy" | _
        "ClassModifiersInvalid_Issue2_2.groovy" | _
    }
    */

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

