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

import android.databinding.tool.processing.ScopedErrorReport
import android.databinding.tool.store.Location
import com.android.testutils.TestUtils
import com.android.tools.analytics.Environment
import com.android.tools.analytics.EnvironmentFakes
import com.android.tools.idea.gradle.project.build.invoker.GradleBuildInvoker
import com.android.tools.idea.testing.AndroidGradleTestCase
import com.android.tools.idea.testing.TestProjectPaths
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.util.io.FileUtil.toSystemDependentName
import com.intellij.util.io.createDirectories
import com.intellij.util.io.readText
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.regex.Pattern

private const val TEST_DEPENDENCIES = "implementation 'androidx.fragment:fragment:+'"
private const val DEFAULT_SETTINGS_GRADLE = "include ':app'"

private const val TEST_DATA_PATH = "tools/data-binding/compilationTests/testData"

private val pattern: Pattern = Pattern.compile("!@\\{([A-Za-z0-9_-]*)}")

const val KEY_MANIFEST_PACKAGE = "PACKAGE"
const val KEY_DEPENDENCIES = "DEPENDENCIES"
const val KEY_SETTINGS_INCLUDES = "SETTINGS_INCLUDES"
const val DEFAULT_APP_PACKAGE = "com.android.databinding.compilationTest.test"

abstract class DataBindingCompilationTestCase : AndroidGradleTestCase() {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        EnvironmentFakes.setSingleProperty(
            Environment.EnvironmentVariable.ANDROID_PREFS_ROOT.key,
            temporaryFolder.newFolder().absolutePath
        )
    }

    @After
    fun restoreSystemProperty() {
        EnvironmentFakes.setSystemEnvironment()
    }

    protected fun loadApp() {
        loadApp(emptyMap())
    }

    protected fun loadApp(appReplacements: Map<String, String>) {
        loadProject(TestProjectPaths.DATA_BINDING_COMPILATION)
        projectRoot.toPath().resolve("app/src/main").createDirectories()
        val replacements = appendTestReplacements(appReplacements)
        copyTestDataWithReplacement(
            "AndroidManifest.xml",
            "app/src/main/AndroidManifest.xml",
            replacements
        )
        copyTestDataWithReplacement(
            "app_build.gradle",
            "app/build.gradle",
            replacements
        )
        copyTestDataWithReplacement(
            "settings.gradle",
             "settings.gradle",
            replacements
        )
    }

    protected fun loadModule(moduleName: String, moduleReplacements: Map<String, String>) {
        val replacements = appendTestReplacements(moduleReplacements)
        copyTestDataWithReplacement(
            "AndroidManifest.xml",
            "${moduleName}/src/main/AndroidManifest.xml",
            replacements
        )
        copyTestDataWithReplacement(
            "module_build.gradle",
            "${moduleName}/build.gradle",
            replacements
        )
    }

    protected fun assembleDebug() = invokeTasks(listOf("assembleDebug"))

    protected fun invokeTasks(tasks: List<String>, args: List<String> = emptyList()): CompilationResult {
        val request =
            GradleBuildInvoker.Request(
                project,
                File(toSystemDependentName(project.basePath!!)),
                tasks
            )
        val outBuilder = StringBuilder()
        val errBuilder = StringBuilder()
        request.taskListener = object : ExternalSystemTaskNotificationListenerAdapter() {
            override fun onTaskOutput(id: ExternalSystemTaskId, text: String, stdOut: Boolean) {
                if (stdOut) {
                    outBuilder.append(text)
                } else {
                    errBuilder.append(text)
                }
            }
        }
        request.setCommandLineArguments(listOf("--offline") + args)
        val result = invokeGradle(project) { gradleInvoker ->
            gradleInvoker.executeTasks(request)
        }
        return CompilationResult(
            if (result.isBuildSuccessful) 0 else 1,
            outBuilder.toString(),
            errBuilder.toString()
        )
    }

    protected val projectRoot: File
        get() = File(toSystemDependentName(project.basePath!!))

    /**
     * Copies the file in the testData directory to the target directory.
     *
     * [source] and [target] are relative to testData and projectRoot respectively.
     */
    protected fun copyTestData(source: String, target: String) {
        val sourcePath = TestUtils.resolveWorkspacePath(TEST_DATA_PATH).resolve(source)
        val targetPath = File(projectRoot, target).toPath()
        targetPath.parent.createDirectories()
        Files.copy(sourcePath, targetPath)
    }

    protected fun copyTestDataWithReplacement(
        source: String,
        target: String,
        replacements: Map<String, String> = emptyMap()
    ) {
        val sourcePath = TestUtils.resolveWorkspacePath(TEST_DATA_PATH).resolve(source)
        val targetPath = File(projectRoot, target)
        val contents = sourcePath.readText()
        val out = StringBuilder(contents.length)
        val matcher = pattern.matcher(contents)
        var location = 0
        while (matcher.find()) {
            val start = matcher.start()
            if (start > location) {
                out.append(contents, location, start)
            }
            val key = matcher.group(1)
            val replacement = replacements[key]
            if (replacement != null) {
                out.append(replacement)
            }
            location = matcher.end()
        }
        if (location < contents.length) {
            out.append(contents, location, contents.length)
        }
        targetPath.parentFile.mkdirs()
        targetPath.writeText(out.toString(), StandardCharsets.UTF_8)
    }

    /**
     * Custom logic that replaces or appends to the replacement values
     * depending on the key.
     *
     * If key is [KEY_DEPENDENCIES], then append the replacement.
     * If key is [KEY_MANIFEST_PACKAGE] or [KEY_SETTINGS_INCLUDES],
     *   then put if value doesn't already exist.
     */
    private fun appendTestReplacements(
        map: Map<String, String>
    ): Map<String, String> {
        val mutableMap = map.toMutableMap()
        if (mutableMap.containsKey(KEY_DEPENDENCIES)) {
            mutableMap[KEY_DEPENDENCIES] += "\n$TEST_DEPENDENCIES"
        } else {
            mutableMap[KEY_DEPENDENCIES] = TEST_DEPENDENCIES
        }
        if (!mutableMap.containsKey(KEY_MANIFEST_PACKAGE)) {
            mutableMap[KEY_MANIFEST_PACKAGE] = DEFAULT_APP_PACKAGE
        }
        if (!mutableMap.containsKey(KEY_SETTINGS_INCLUDES)) {
            mutableMap[KEY_SETTINGS_INCLUDES] = DEFAULT_SETTINGS_GRADLE
        }
        return mutableMap
    }

    /**
     * Finds the error file referenced in the given error report.
     * Handles possibly relative paths.
     *
     * Throws an assertion exception if the error file reported cannot be found.
     */
    protected fun requireErrorFile(report: ScopedErrorReport): File {
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

    /**
     * Extracts the text in the given location from the file at the given application path.
     *
     * @param relativePath the relative path of the file to be extracted from
     * @param location  The location to extract
     * @return The string that is contained in the given location
     * @throws IOException If file is invalid.
     */
    protected fun extract(relativePath: String, location: Location): String {
        val file = File(projectRoot, relativePath)
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

    protected fun writeFile(path: String, contents: String) {
        val targetFile = File(projectRoot, path)
        targetFile.parentFile.mkdirs()
        targetFile.writeText(contents, StandardCharsets.UTF_8)
    }
}
