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

import android.databinding.tool.processing.ErrorMessages
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ObservableGetDetectionTest(
        private val type: String,
        private val getter: String
) : BaseCompilationTest(true) {
    @Test
    fun detectGetterCallsOnObservables() {
        prepareProject()
        writeFile("/app/src/main/res/layout/observable_get.xml",
                """
                    <layout xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:bind="http://schemas.android.com/apk/res-auto">
                    <data>
                        <variable name="myVariable" type="$type"/>
                    </data>
                    <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@{myVariable.$getter}"/>
                </layout>
                """.trimIndent())
        val result = runGradle("assembleDebug")
        assertThat(result.error, result.resultCode, not(0))
        val expected = ErrorMessages.GETTER_ON_OBSERVABLE.format("myVariable.$getter")
        assertTrue(result.error, result.bindingExceptions.any {
            it.createHumanReadableMessage().contains(expected)
        })
    }

    companion object {
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun params() = arrayOf(
                "ObservableByte",
                "ObservableBoolean",
                "ObservableChar",
                "ObservableShort",
                "ObservableInt",
                "ObservableLong",
                "ObservableFloat",
                "ObservableDouble",
                "ObservableField&lt;String>",
                "ObservableParcelable").map {
            arrayOf("androidx.databinding.$it", "get()")
        } + arrayOf(
                arrayOf("androidx.lifecycle.LiveData&lt;String>", "getValue()")
        )
    }
}