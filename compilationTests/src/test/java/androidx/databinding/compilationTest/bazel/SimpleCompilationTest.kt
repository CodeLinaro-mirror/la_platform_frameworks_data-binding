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
package androidx.databinding.compilationTest.bazel

import android.databinding.tool.processing.ErrorMessages
import android.databinding.tool.processing.ScopedErrorReport
import android.databinding.tool.processing.ScopedException
import android.databinding.tool.store.Location
import androidx.databinding.compilationTest.copyResourceToFile
import com.google.common.base.Joiner
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.PrefixFileFilter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.io.IOException
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
        copyResource(
            "/layout/basic_layout.xml",
            "app/src/main/res/layout/main.xml"
        )
        copyResource(
            "/layout/basic_layout.xml",
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
            "/layout/layout_with_bad_syntax.xml",
            "/app/src/main/res/layout/broken.xml",
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
            "/layout/layout_with_completely_broken_syntax.xml",
            "/app/src/main/res/layout/broken.xml",
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
            "/layout/undefined_variable_binding.xml",
            "/app/src/main/res/layout/broken.xml", "myVariable", String.format(
                ErrorMessages.UNDEFINED_VARIABLE, "myVariable"
            )
        )
    }

    @Test
    fun testInvalidSetterBinding() {
        singleFileErrorTest(
            "/layout/invalid_setter_binding.xml",
            "/app/src/main/res/layout/invalid_setter.xml", "myVariable", String.format(
                ErrorMessages.CANNOT_FIND_SETTER_CALL, "android.widget.TextView",
                "android:textx",
                String::class.java.canonicalName
            )
        )
    }

    @Test
    fun testCallbackArgumentCountMismatch() {
        singleFileErrorTest(
            "/layout/layout_with_missing_callback_args.xml",
            "/app/src/main/res/layout/broken.xml",
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
            "/layout/layout_with_duplicate_callback_identifier.xml",
            "/app/src/main/res/layout/broken.xml",
            "(seekBar, progress, progress) -> obj.length()", String.format(
                ErrorMessages.DUPLICATE_CALLBACK_ARGUMENT,
                "progress"
            )
        )
    }

    @Test
    fun testInvalidVariableType() {
        singleFileErrorTest(
            "/layout/invalid_variable_type.xml",
            "/app/src/main/res/layout/invalid_variable.xml",
            "myVariable",
            String.format(ErrorMessages.CANNOT_RESOLVE_TYPE, "myVariable")
        )
    }

    @Test
    fun testConflictWithVariableName() {
        singleFileWarningTest(
            "/layout/layout_with_same_name_for_var_and_callback.xml",
            "/app/src/main/res/layout/broken.xml", String.format(
                ErrorMessages.CALLBACK_VARIABLE_NAME_CLASH,
                "myVar", "String", "myVar"
            )
        )
    }

    private fun singleFileWarningTest(
        resource: String, targetFile: String,
        expectedMessage: String
    ) {
        loadApp()
        copyResourceToFile(resource, File(projectRoot, targetFile))
        val result = invokeTasks(listOf("assembleDebug"))
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
    ) : ScopedException {
        loadApp()
        copyResourceToFile(resource, File(projectRoot, targetFile))
        val result = invokeTasks(listOf("assembleDebug"))

        assertFalse(result.isBuildSuccessful)
        val scopedException = result.bindingException
        val report = scopedException.scopedErrorReport
        assertNotNull(report)
        assertEquals(1, report.locations.size.toLong())

        val loc = report.locations[0]
        if (expectedExtract != null) {
            val extract: String = extract(File(projectRoot, targetFile), loc)
            Assert.assertEquals(scopedException.message, expectedExtract, extract)
        }
        val errorFile: File = requireErrorFile(report, projectRoot)
        Assert.assertEquals(
            File(projectRoot, targetFile).canonicalFile,
            errorFile.canonicalFile
        )
        if (errorMessage != null) {
            Assert.assertEquals(errorMessage, scopedException.bareMessage)
        }
        return scopedException
    }

    /**
     * Extracts the text in the given location from the file at the given application path.
     *
     * @param file The file to be extracted from.
     * @param location  The location to extract
     * @return The string that is contained in the given location
     * @throws IOException If file is invalid.
     */
    private fun extract(file: File, location: Location): String {
        Assert.assertTrue(file.exists())
        val result = StringBuilder()
        val lines = file.readLines(StandardCharsets.UTF_8)
        for (i in location.startLine..location.endLine) {
            if (i > location.startLine) {
                result.append("\n")
            }
            val line = lines[i]
            var start = 0
            if (i == location.startLine) {
                start = location.startOffset
            }
            var end = line.length - 1 // inclusive
            if (i == location.endLine) {
                end = location.endOffset
            }
            result.append(line.substring(start, end + 1))
        }
        return result.toString()
    }

    /**
     * Finds the error file referenced in the given error report.
     * Handles possibly relative paths.
     *
     * Throws an assertion exception if the error file reported cannot be found.
     */
    private fun requireErrorFile(report: ScopedErrorReport, projectRoot: File): File {
        val path = report.filePath
        Assert.assertNotNull(path)
        var file = File(path)
        if (file.exists()) {
            return file
        }
        // might be relative, try in test project folder
        file = File(projectRoot, path)
        Assert.assertTrue("required error file is missing in " + file.absolutePath, file.exists())
        return file
    }
}
