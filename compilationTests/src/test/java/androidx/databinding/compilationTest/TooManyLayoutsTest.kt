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

import com.android.testutils.AssumeUtil
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Stress test that creates many layouts and hopes that we don't choke compiler
 */
@RunWith(JUnit4::class)
class TooManyLayoutsTest : DataBindingCompilationTestCase() {

    @Before
    fun setUp() {
        // This test takes a long time to run and also has high variance in running time.
        // We disable it on Windows to reduce build latency (b/301266550) and avoid potential
        // timeouts (b/300720819).
        // We probably don't need to re-enable this test later as having it run on other platforms
        // is probably enough.
        AssumeUtil.assumeNotWindows()
    }

    @Test
    fun tooManyLayouts() {
        loadApp()
        // 3000 is a random number that does not hit the # of static references limit but still fairly large
        // for any reasonable project
        (0 until 3000).forEach {
            copyTestData(
                "layout/basic_layout.xml",
                "app/src/main/res/layout/layout_$it.xml"
            )
        }
        val result = assembleDebug()
        assertWithMessage(result.error).that(result.resultCode).isEqualTo(0)
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
        loadApp(
            mapOf(
                KEY_DEPENDENCIES to appDeps,
                KEY_SETTINGS_INCLUDES to "include ':app'\n$settings"
            )
        )
        (0 until moduleCount).forEach {
            loadModule("module$it", mapOf(KEY_NAMESPACE to "com.example.module$it"))
            (0 until layoutsPerModule).forEach { layoutId ->
                copyTestData(
                    "layout/basic_layout.xml",
                    "module$it/src/main/res/layout/module${it}_${layoutId}_layout.xml"
                )
            }

        }
        val result = assembleDebug()
        assertWithMessage(result.error).that(result.resultCode).isEqualTo(0)
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
        loadApp(
            mapOf(
                KEY_DEPENDENCIES to appDeps,
                KEY_SETTINGS_INCLUDES to "include ':app'\n$settings"
            )
        )
        (0 until moduleCount).forEach {
            val deps = if (it == 0) {
                ""
            } else {
                "implementation project(':module${it - 1}')"
            }
            loadModule(
                "module$it",
                mapOf(
                    KEY_NAMESPACE to "com.example.module$it",
                    KEY_DEPENDENCIES to deps
                )
            ) // add dependency ?
            (0 until layoutsPerModule).forEach { layoutId ->
                copyTestData(
                    "layout/basic_layout.xml",
                    "module$it/src/main/res/layout/module${it}_${layoutId}_layout.xml"
                )
            }

        }
        val result = assembleDebug()
        assertWithMessage(result.error).that(result.resultCode).isEqualTo(0)
    }
}
