/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.databinding.compilationTest

import android.databinding.tool.processing.ErrorMessages
import android.databinding.tool.processing.ScopedException
import com.google.common.base.Joiner
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.PrefixFileFilter
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.nio.charset.StandardCharsets

@RunWith(JUnit4::class)
class SimpleCompilationTest : DataBindingCompilationTestCase() {

    @Test
    fun listTasks() {
        loadApp()
        val result = invokeGradleTasks(project, "tasks")
        assertTrue("Empty project tasks failed", result.isBuildSuccessful)
    }

    @Test
    fun testEmptyCompilation() {
        loadApp()
        val result = invokeGradleTasks(project, "assembleDebug")
        assertTrue("Basic compile failed", result.isBuildSuccessful)
    }

    @Test
    fun testMultipleConfigs() {
        loadApp()
        copyTestData(
            "layout/basic_layout.xml",
            "app/src/main/res/layout/main.xml"
        )
        copyTestData(
            "layout/basic_layout.xml",
            "app/src/main/res/layout-sw100dp/main.xml"
        )
        val result = invokeGradleTasks(project, "assembleDebug")
        assertTrue(result.isBuildSuccessful)
        val debugOut = File(
            projectRoot,
            "app/build/intermediates/incremental/mergeDebugResources/stripped.dir"
        )
        val layoutFiles = FileUtils.listFiles(
            debugOut, NameFileFilter("main.xml"),
            PrefixFileFilter("layout")
        )
        assertTrue("Unexpected generated layout count", layoutFiles.size > 1)
        for (layout: File in layoutFiles) {
            val contents = FileUtils.readFileToString(layout, StandardCharsets.UTF_8)
            if (layout.parent.contains("sw100")) {
                assertTrue(
                    "File has wrong tag:" + layout.path,
                    contents.indexOf("android:tag=\"layout-sw100dp/main_0\"") > 0
                )
            } else {
                assertTrue(
                    "File has wrong tag:" + layout.path + "\n" + contents,
                    contents.indexOf("android:tag=\"layout/main_0\"") > 0
                )
            }
        }
    }

    @Test
    fun testBadSyntax() {
        singleFileErrorTest(
            "layout/layout_with_bad_syntax.xml",
            "app/src/main/res/layout/broken.xml",
            "myVar.length())",
            String.format(
                ErrorMessages.SYNTAX_ERROR,
                "extraneous input ')' expecting {<EOF>, ',', '.', '::', '[', '+', '-', " +
                        "'*', '/', '%', '<<', '>>>', '>>', '<=', '>=', '>', '<', " +
                        "'instanceof', '==', '!=', '&', '^', '|', '&&', '||', '?', '??'}"
            )
        )
    }

    @Test
    fun testBrokenSyntax() {
        singleFileErrorTest(
            "layout/layout_with_completely_broken_syntax.xml",
            "app/src/main/res/layout/broken.xml",
            "new String()", String.format(
                ErrorMessages.SYNTAX_ERROR,
                "mismatched input 'String' expecting {<EOF>, ',', '.', '::', '[', '+', " +
                        "'-', '*', '/', '%', '<<', '>>>', '>>', '<=', '>=', '>', '<', " +
                        "'instanceof', '==', '!=', '&', '^', '|', '&&', '||', '?', '??'}"
            )
        )
    }

    @Test
    fun testUndefinedVariable() {
        singleFileErrorTest(
            "layout/undefined_variable_binding.xml",
            "app/src/main/res/layout/broken.xml", "myVariable", String.format(
                ErrorMessages.UNDEFINED_VARIABLE, "myVariable"
            )
        )
    }

    @Test
    fun testInvalidSetterBinding() {
        singleFileErrorTest(
            "layout/invalid_setter_binding.xml",
            "app/src/main/res/layout/invalid_setter.xml", "myVariable", String.format(
                ErrorMessages.CANNOT_FIND_SETTER_CALL, "android.widget.TextView",
                "android:textx",
                String::class.java.canonicalName
            )
        )
    }

    @Test
    fun testCallbackArgumentCountMismatch() {
        singleFileErrorTest(
            "layout/layout_with_missing_callback_args.xml",
            "app/src/main/res/layout/broken.xml",
            "(seekBar, progress) -> obj.length()", String.format(
                ErrorMessages.CALLBACK_ARGUMENT_COUNT_MISMATCH,
                "androidx.databinding.adapters.SeekBarBindingAdapter.OnProgressChanged",
                "onProgressChanged", 3, 2
            )
        )
    }

    @Test
    fun testDuplicateCallbackArgument() {
        singleFileErrorTest(
            "layout/layout_with_duplicate_callback_identifier.xml",
            "app/src/main/res/layout/broken.xml",
            "(seekBar, progress, progress) -> obj.length()", String.format(
                ErrorMessages.DUPLICATE_CALLBACK_ARGUMENT,
                "progress"
            )
        )
    }

    @Test
    fun testInvalidVariableType() {
        singleFileErrorTest(
            "layout/invalid_variable_type.xml",
            "app/src/main/res/layout/invalid_variable.xml",
            "myVariable",
            String.format(ErrorMessages.CANNOT_RESOLVE_TYPE, "myVariable")
        )
    }

    @Test
    fun testConflictWithVariableName() {
        singleFileWarningTest(
            "layout/layout_with_same_name_for_var_and_callback.xml",
            "app/src/main/res/layout/broken.xml", String.format(
                ErrorMessages.CALLBACK_VARIABLE_NAME_CLASH,
                "myVar", "String", "myVar"
            )
        )
    }

    @Test
    fun testMultipleExceptionsInDifferentFiles() {
        loadApp()
        copyTestData(
            "layout/undefined_variable_binding.xml",
            "app/src/main/res/layout/broken.xml"
        )
        copyTestData(
            "layout/invalid_setter_binding.xml",
            "app/src/main/res/layout/invalid_setter.xml"
        )
        val result = assembleDebug()
        Assert.assertNotEquals(result.output, 0, result.resultCode.toLong())
        val bindingExceptions = result.bindingExceptions
        Assert.assertEquals(result.error, 2, bindingExceptions.size.toLong())
        val broken = File(projectRoot, "/app/src/main/res/layout/broken.xml")
        val invalidSetter = File(projectRoot, "/app/src/main/res/layout/invalid_setter.xml")
        for (exception in bindingExceptions) {
            val report = exception.scopedErrorReport
            val errorFile = requireErrorFile(report)
            var message: String? = null
            var expectedErrorFile: String? = null
            when (errorFile.canonicalPath) {
                broken.canonicalPath -> {
                    message = String.format(ErrorMessages.UNDEFINED_VARIABLE, "myVariable")
                    expectedErrorFile = "/app/src/main/res/layout/broken.xml"
                }
                invalidSetter.canonicalPath -> {
                    message = String.format(
                        ErrorMessages.CANNOT_FIND_SETTER_CALL,
                        "android.widget.TextView", "android:textx", String::class.java.canonicalName
                    )
                    expectedErrorFile = "/app/src/main/res/layout/invalid_setter.xml"
                }
                else -> {
                    Assert.fail("unexpected exception " + exception.bareMessage)
                }
            }
            Assert.assertEquals(1, report.locations.size.toLong())
            val loc = report.locations[0]
            val extract = extract(expectedErrorFile!!, loc)
            Assert.assertEquals("myVariable", extract)
            Assert.assertEquals(message, exception.bareMessage)
        }
    }

    @Test
    fun testSingleModule() {
        loadApp(
            mapOf(
                KEY_DEPENDENCIES to "implementation project(':module1')",
                KEY_SETTINGS_INCLUDES to "include ':app'\ninclude ':module1'"
            )
        )
        loadModule("module1", mapOf(KEY_MANIFEST_PACKAGE to "com.example.module1"))
        copyTestData("layout/basic_layout.xml", "module1/src/main/res/layout/module_layout.xml")
        copyTestData("layout/basic_layout.xml", "app/src/main/res/layout/app_layout.xml")

        val result: CompilationResult = assembleDebug()
        assertEquals(result.error, 0, result.resultCode.toLong())
    }

    @Test
    fun testConflictingIds() {
        loadApp()
        copyTestData(
            "layout/duplicate_ids.xml",
            "app/src/main/res/layout/duplicate_ids.xml"
        )
        val result = assembleDebug()
        val exceptions = result.bindingExceptions
        assertMessages(
            exceptions, String.format(
                ErrorMessages.DUPLICATE_VIEW_OR_INCLUDE_ID,
                "TextView", "@+id/shared_id"
            ), String.format(
                ErrorMessages.DUPLICATE_VIEW_OR_INCLUDE_ID,
                "TextView", "@+id/shared_id"
            )
        )
    }

    @Test
    fun testConflictingIds_include() {
        loadApp()
        copyTestData("layout/basic_layout.xml", "app/src/main/res/layout/basic_layout.xml")
        copyTestData(
            "layout/duplicate_include_ids.xml",
            "app/src/main/res/layout/duplicate_include_ids.xml"
        )
        val result = assembleDebug()
        val exceptions = result.bindingExceptions
        assertMessages(
            exceptions, String.format(
                ErrorMessages.DUPLICATE_VIEW_OR_INCLUDE_ID,
                "include", "@+id/shared_id"
            ), String.format(
                ErrorMessages.DUPLICATE_VIEW_OR_INCLUDE_ID,
                "include", "@+id/shared_id"
            )
        )
    }

    @Test
    fun testConflictingIds_includeVsView() {
        loadApp()
        copyTestData("layout/basic_layout.xml", "app/src/main/res/layout/basic_layout.xml")
        copyTestData(
            "layout/duplicate_include_vs_view_ids.xml",
            "app/src/main/res/layout/duplicate_include_vs_view_ids.xml"
        )
        val result = assembleDebug()
        val exceptions = result.bindingExceptions
        assertMessages(
            exceptions, String.format(
                ErrorMessages.DUPLICATE_VIEW_OR_INCLUDE_ID,
                "TextView", "@+id/shared_id"
            ), String.format(
                ErrorMessages.DUPLICATE_VIEW_OR_INCLUDE_ID,
                "include", "@+id/shared_id"
            )
        )
    }

    // TODO: reenable this test once it works.
    @Ignore
    fun testModuleDependencyChange() {
        loadApp(
            mapOf(
                KEY_DEPENDENCIES to "implementation project(':module1')",
                KEY_SETTINGS_INCLUDES to "include ':app'\ninclude ':module1'"
            )
        )
        loadModule(
            "module1", mapOf(
                KEY_DEPENDENCIES to "implementation 'com.android.support:appcompat-v7:23.1.1'",
                KEY_MANIFEST_PACKAGE to "com.example.module1"
            )
        )
        copyTestData("layout/basic_layout.xml", "module1/src/main/res/layout/module_layout.xml")
        copyTestData("layout/basic_layout.xml", "app/src/main/res/layout/app_layout.xml")
        var result = assembleDebug()
        Assert.assertEquals(result.error, 0, result.resultCode.toLong())
        copyTestDataWithReplacement("module_build.gradle", "module1/build.gradle")
        result = assembleDebug()
        Assert.assertEquals(result.error, 0, result.resultCode.toLong())
    }

    @Test
    fun testTwoLevelDependency() {
        loadApp(
            mapOf(
                KEY_DEPENDENCIES to "implementation project(':module1')",
                KEY_SETTINGS_INCLUDES to
                        "include ':app'\ninclude ':module1'\n"
                        + "include ':module2'"
            )
        )
        loadModule(
            "module1",
            mapOf(
                KEY_DEPENDENCIES to "implementation project(':module2')",
                KEY_MANIFEST_PACKAGE to "com.example.module1"
            )
        )
        loadModule("module2", mapOf(KEY_MANIFEST_PACKAGE to "com.example.module2"))
        copyTestData(
            "layout/basic_layout.xml",
            "module2/src/main/res/layout/module2_layout.xml"
        )
        copyTestData(
            "layout/basic_layout.xml",
            "module1/src/main/res/layout/module1_layout.xml"
        )
        copyTestData("layout/basic_layout.xml", "app/src/main/res/layout/app_layout.xml")
        val result = assembleDebug()
        Assert.assertEquals(result.error, 0, result.resultCode.toLong())
    }

    @Test
    fun testIncludeInMerge() {
        loadApp()
        copyTestData("layout/merge_include.xml", "app/src/main/res/layout/merge_include.xml")
        val result = assembleDebug()
        Assert.assertNotEquals(0, result.resultCode.toLong())
        val errors = ScopedException.extractErrors(result.error)
        Assert.assertEquals(result.error, 1, errors.size.toLong())
        val ex = errors[0]
        val report = ex.scopedErrorReport
        val errorFile = requireErrorFile(report)
        Assert.assertEquals(
            File(projectRoot, "/app/src/main/res/layout/merge_include.xml").canonicalFile,
            errorFile.canonicalFile
        )
        Assert.assertEquals(ErrorMessages.INCLUDE_INSIDE_MERGE, ex.bareMessage)
    }

    @Test
    fun testAssignTwoWayEvent() {
        loadApp()
        copyTestData(
            "layout/layout_with_two_way_event_attribute.xml",
            "app/src/main/res/layout/layout_with_two_way_event_attribute.xml"
        )
        val result: CompilationResult = assembleDebug()
        Assert.assertNotEquals(0, result.resultCode.toLong())
        val errors = ScopedException.extractErrors(result.error)
        Assert.assertEquals(result.error, 1, errors.size.toLong())
        val ex = errors[0]
        val report = ex.scopedErrorReport
        val errorFile = requireErrorFile(report)
        Assert.assertEquals(
            File(
                projectRoot,
                "/app/src/main/res/layout/layout_with_two_way_event_attribute.xml"
            ).canonicalFile,
            errorFile.canonicalFile
        )
        Assert.assertEquals(
            String.format(ErrorMessages.TWO_WAY_EVENT_ATTRIBUTE, "android:textAttrChanged"),
            ex.bareMessage
        )
    }

    @Test
    fun testDependantDoesNotExist() {
        loadApp()
        copyTestData(
            "layout/layout_with_dependency.xml",
            "app/src/main/res/layout/layout_with_dependency.xml"
        )
        copyTestData(
            "androidx/databinding/compilationTests/badJava/ObservableNoDependent.java",
            "app/src/main/java/androidx/databinding/compilationTest/badJava/MyObservable.java"
        )
        val result: CompilationResult = assembleDebug()
        Assert.assertNotEquals(0, result.resultCode.toLong())
        val errors = ScopedException.extractErrors(result.error)
        Assert.assertEquals(result.error, 1, errors.size.toLong())
        val ex = errors[0]
        val report = ex.scopedErrorReport
        val errorFile = requireErrorFile(report)
        Assert.assertEquals(
            File(
                projectRoot,
                "/app/src/main/res/layout/layout_with_dependency.xml"
            ).canonicalFile,
            errorFile.canonicalFile
        )
        Assert.assertEquals(
            ("Could not find dependent property 'notExist' referenced in " +
                    "@Bindable annotation on " +
                    "androidx.databinding.compilationTest.badJava.MyObservable.getField"),
            ex.bareMessage
        )
    }

    @Test
    fun testDependantNotBindable() {
        loadApp()
        copyTestData(
            "layout/layout_with_dependency.xml",
            "app/src/main/res/layout/layout_with_dependency.xml"
        )
        copyTestData(
            "androidx/databinding/compilationTests/badJava/ObservableNotBindableDependent.java",
            "app/src/main/java/androidx/databinding/compilationTest/badJava/MyObservable.java"
        )
        val result: CompilationResult = assembleDebug()
        Assert.assertNotEquals(0, result.resultCode.toLong())
        val errors = ScopedException.extractErrors(result.error)
        Assert.assertEquals(result.error, 1, errors.size.toLong())
        val ex = errors[0]
        val report = ex.scopedErrorReport
        val errorFile = requireErrorFile(report)
        Assert.assertEquals(
            File(
                projectRoot,
                "/app/src/main/res/layout/layout_with_dependency.xml"
            ).canonicalFile,
            errorFile.canonicalFile
        )
        Assert.assertEquals(
            ("The dependent property 'otherField' referenced in " +
                    "@Bindable annotation on " +
                    "androidx.databinding.compilationTest.badJava.MyObservable.getField " +
                    "must be annotated with @Bindable"), ex.bareMessage
        )
    }

    @Test
    fun testDependantField() {
        loadApp()
        copyTestData(
            "layout/layout_with_dependency.xml",
            "app/src/main/res/layout/layout_with_dependency.xml"
        )
        copyTestData(
            "androidx/databinding/compilationTests/badJava/ObservableFieldDependent.java",
            "app/src/main/java/androidx/databinding/compilationTest/badJava/MyObservable.java"
        )
        val result: CompilationResult = assembleDebug()
        Assert.assertNotEquals(0, result.resultCode.toLong())
        val errors = ScopedException.extractErrors(result.error)
        Assert.assertEquals(result.error, 1, errors.size.toLong())
        val ex = errors[0]
        val report = ex.scopedErrorReport
        val errorFile = requireErrorFile(report)
        Assert.assertTrue(errorFile.exists())
        Assert.assertEquals(
            File(
                projectRoot,
                "/app/src/main/res/layout/layout_with_dependency.xml"
            ).canonicalFile,
            errorFile.canonicalFile
        )
        Assert.assertEquals(
            ("Bindable annotation with property names is only supported on methods. " +
                    "Field 'androidx.databinding.compilationTest.badJava.MyObservable.field' has " +
                    "@Bindable(\"otherField\")"), ex.bareMessage
        )
    }

    private fun assertMessages(
        exceptions: List<ScopedException>,
        vararg messages: String
    ) {
        val actual = exceptions
            .map { obj: ScopedException -> obj.bareMessage }
            .toList()
        MatcherAssert.assertThat(actual, CoreMatchers.hasItems(*messages))
        MatcherAssert.assertThat(actual.size, CoreMatchers.`is`(messages.size))
    }

    private fun singleFileWarningTest(
        resource: String,
        targetFile: String,
        expectedMessage: String
    ) {
        loadApp()
        copyTestData(resource, targetFile)
        val result = assembleDebug()
        val warnings = result.bindingWarnings
        var found = false
        for (warning in warnings) {
            found = found or warning.contains(expectedMessage)
        }
        Assert.assertTrue(Joiner.on("\n").join(warnings), found)
    }

    private fun singleFileErrorTest(
        resource: String,
        targetFile: String,
        expectedExtract: String?,
        errorMessage: String?
    ): ScopedException {
        loadApp()
        copyTestData(resource, targetFile)
        val result = assembleDebug()
        assertFalse(result.isBuildSuccessful)
        val scopedException = result.bindingException
        val report = scopedException.scopedErrorReport
        assertNotNull(report)
        assertEquals(1, report.locations.size.toLong())

        val loc = report.locations[0]
        if (expectedExtract != null) {
            val extract: String = extract(targetFile, loc)
            Assert.assertEquals(scopedException.message, expectedExtract, extract)
        }
        val errorFile: File = requireErrorFile(report)
        Assert.assertEquals(
            File(projectRoot, targetFile).canonicalFile,
            errorFile.canonicalFile
        )
        if (errorMessage != null) {
            Assert.assertEquals(errorMessage, scopedException.bareMessage)
        }
        return scopedException
    }
}
