/*
 * Copyright (C) 2019 The Android Open Source Project
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

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

/**
 * The data binding compiler generates different Impl code based on whether
 * `AppCompatResources` is found in the classpath or not.
 */
@RunWith(Parameterized::class)
class AppCompatResourcesTest(private val addAppCompatDependency: Boolean)
    : BaseCompilationTest(true) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Add support dependency: {0}")
        fun useSupportDependency() = listOf(true, false)
    }

    @Before
    fun setUp() {
        if (addAppCompatDependency) {
            prepareApp(toMap(
              KEY_DEPENDENCIES, "implementation 'androidx.appcompat:appcompat:1.0.0'"
            ))
        } else {
            prepareProject()
        }
        copyResourceTo("/layout/layout_with_drawable.xml",
          "/app/src/main/res/layout/layout_with_drawable.xml")
        copyResourceTo("/drawable/thumbs_up.png",
          "/app/src/main/res/drawable/thumbs_up.png")

        val result = runGradle("assembleDebug")
        assertThat(result.error, result.resultCode, `is`(0))
    }

    @Test
    fun expectedCodeGenerated() {
        val bindingImpl = findFile("LayoutWithDrawableBindingImpl.java")!!

        val expectedCode = if (addAppCompatDependency) {
            "androidx.appcompat.content.res.AppCompatResources.getDrawable(mboundView1.getContext(), R.drawable.thumbs_up)"
        } else {
            "getDrawableFromResource(mboundView1, R.drawable.thumbs_up)"
        }

        assertThat(bindingImpl.readText(Charsets.UTF_8), containsString(expectedCode))
    }

    private fun findFile(fileName: String): File?
      = testFolder.walkBottomUp().firstOrNull { it.name == fileName }

}

