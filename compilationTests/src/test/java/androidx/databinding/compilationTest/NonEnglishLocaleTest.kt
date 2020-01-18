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
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.AssertionError
import java.util.Locale

@RunWith(JUnit4::class)
class NonEnglishLocaleTest : BaseCompilationTest() {
    @Test
    fun turkishICapitalization() {
        prepareProject()
        copyResourceTo("/layout/turkish_i_capitalization.xml",
                "/app/src/main/res/layout/i_turkish_capitalization.xml")
        val result = runGradle("assembleDebug", "-Duser.language=tr", "-Duser.region=TR")
        assertThat(result.error, result.resultCode, `is`(0))
        assertDoesNotContainTurkishChars("ITurkishCapitalizationBinding.java")
        assertDoesNotContainTurkishChars("ITurkishCapitalizationBindingImpl.java")
    }

    @Test
    fun turkishICapitalization_twoWayBinding() {
        prepareProject()
        copyResourceTo("/layout/turkish_i_capitalization_two_way_binding.xml",
                "/app/src/main/res/layout/i_turkish_capitalization.xml")
        val result = runGradle("assembleDebug", "-Duser.language=tr", "-Duser.region=TR")
        assertThat(result.error, result.resultCode, `is`(0))
        assertDoesNotContainTurkishChars("ITurkishCapitalizationBinding.java")
        assertDoesNotContainTurkishChars("ITurkishCapitalizationBindingImpl.java")
    }

    private fun assertDoesNotContainTurkishChars(fileName:String) {
        val bindingJava = testFolder.walkBottomUp().firstOrNull {
            it.name == fileName
        } ?: throw AssertionError("cannot find $fileName")
        // assert that it does not use turkish i
        assertThat(bindingJava.readText(Charsets.UTF_8), not(containsString(CAPITAL_I)))
        assertThat(bindingJava.readText(Charsets.UTF_8), not(containsString(LOWERCASE_I)))
    }

    companion object {
        // İ
        private val CAPITAL_I = "i".toUpperCase(Locale.forLanguageTag("tr-TR"))
        // ı
        private val LOWERCASE_I = "I".toLowerCase(Locale.forLanguageTag("tr-TR"))
    }
}