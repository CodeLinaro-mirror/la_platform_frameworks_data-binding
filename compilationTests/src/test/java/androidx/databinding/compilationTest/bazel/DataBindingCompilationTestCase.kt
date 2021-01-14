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

import androidx.databinding.compilationTest.BaseCompilationTest.DEFAULT_APP_PACKAGE
import androidx.databinding.compilationTest.BaseCompilationTest.KEY_DEPENDENCIES
import androidx.databinding.compilationTest.BaseCompilationTest.KEY_MANIFEST_PACKAGE
import androidx.databinding.compilationTest.CompilationResult
import androidx.databinding.compilationTest.copyResourceToFile
import androidx.databinding.compilationTest.copyResourceWithReplacement
import com.android.tools.idea.gradle.project.build.invoker.GradleBuildInvoker
import com.android.tools.idea.testing.AndroidGradleTestCase
import com.android.tools.idea.testing.TestProjectPaths
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.util.io.FileUtil.toSystemDependentName
import com.intellij.util.io.createDirectories
import java.io.File

abstract class DataBindingCompilationTestCase : AndroidGradleTestCase() {

    override fun patchPreparedProject(
        projectRoot: File,
        gradleVersion: String?,
        gradlePluginVersion: String?,
        kotlinVersion: String?,
        vararg localRepos: File?
    ) {
        super.patchPreparedProject(
            projectRoot,
            gradleVersion,
            gradlePluginVersion,
            kotlinVersion,
            *localRepos
        )
        projectRoot.toPath().resolve("app/src/main").createDirectories()
        copyResourceWithReplacement(
            "/AndroidManifest.xml",
            File(projectRoot, "app/src/main/AndroidManifest.xml"),
            mapOf(KEY_MANIFEST_PACKAGE to DEFAULT_APP_PACKAGE)
        )
        copyResourceWithReplacement(
            "/app_build.gradle",
            File(projectRoot, "app/build.gradle"),
            mapOf(KEY_DEPENDENCIES to "implementation 'androidx.appcompat:appcompat:+'\n" +
                          "implementation 'androidx.constraintlayout:constraintlayout:+'")
        )
    }

    protected fun loadApp() {
        loadProject(TestProjectPaths.DATA_BINDING_COMPILATION)
    }

    protected fun invokeTasks(tasks: List<String>): CompilationResult {
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
        request.setCommandLineArguments(listOf("--offline"))
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
     * Copies the resource to the file relative to project root.
     */
    fun copyResource(name: String, relativePath: String) {
        val targetFile = File(projectRoot, relativePath)
        copyResourceToFile(name, targetFile)
    }
}
