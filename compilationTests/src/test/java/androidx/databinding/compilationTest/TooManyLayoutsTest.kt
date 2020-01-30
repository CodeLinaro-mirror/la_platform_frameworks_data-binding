/*
 * Copyright (C) 2018 The Android Open Source Project
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

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Stress test that creates many layouts and hopes that we don't choke compiler
 */
@RunWith(JUnit4::class)
class TooManyLayoutsTest : BaseCompilationTest() {
    @Test
    fun tooManyLayouts() {
        prepareProject()
        // 3000 is a random number that does not hit the # of static references limit but still fairly large
        // for any reasonable project
        (0 until 3000).forEach {
            copyResourceTo("/layout/basic_layout.xml",
                    "/app/src/main/res/layout/layout_$it.xml")
        }
        val result = runGradle("assembleDebug")
        assertEquals(result.error, 0, result.resultCode.toLong())
    }

    @Test
    fun tooManyModules() {
        val moduleCount = 5
        // we can actually support more layouts per module but it causes a timeout in the build
        // server because test takes a long time.
        val layoutsPerModule = 25
        val appDeps = (0 until moduleCount).map {
            "implementation project(':module$it')"
        }.joinToString(System.lineSeparator())
        val settings = (0 until moduleCount).map {
            "include ':module$it'"
        }.joinToString(System.lineSeparator())
        prepareApp(toMap(KEY_DEPENDENCIES, appDeps,
                KEY_SETTINGS_INCLUDES, "include ':app'\n$settings"))
        (0 until moduleCount).forEach {
            prepareModule("module$it", "com.example.module$it", toMap(KEY_DEPENDENCIES,
                ""))
            (0 until layoutsPerModule).forEach { layoutId ->
                copyResourceTo("/layout/basic_layout.xml",
                        "/module$it/src/main/res/layout/module${it}_${layoutId}_layout.xml")
            }

        }
        val result = runGradle("assembleDebug")
        assertEquals(result.error, 0, result.resultCode)
    }

    @Test
    fun tooManyModules_dependingOnEachother() {
        val moduleCount = 5
        val layoutsPerModule = 25
        val appDeps = (0 until moduleCount).map {
            "implementation project(':module0')"
        }.joinToString(System.lineSeparator())
        val settings = (0 until moduleCount).map {
            "include ':module$it'"
        }.joinToString(System.lineSeparator())
        prepareApp(toMap(KEY_DEPENDENCIES, appDeps,
                KEY_SETTINGS_INCLUDES, "include ':app'\n$settings"))
        (0 until moduleCount).forEach {
            val deps = if (it == 0) {
                ""
            } else {
                "implementation project(':module${it-1}')"
            }
            prepareModule("module$it", "com.example.module$it", toMap(KEY_DEPENDENCIES,
                    deps)) // add dependency ?
            (0 until layoutsPerModule).forEach { layoutId ->
                copyResourceTo("/layout/basic_layout.xml",
                        "/module$it/src/main/res/layout/module${it}_${layoutId}_layout.xml")
            }

        }
        val result = runGradle("assembleDebug")
        assertEquals(result.error, 0, result.resultCode)
    }
}