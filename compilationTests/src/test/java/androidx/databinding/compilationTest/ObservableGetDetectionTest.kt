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

import org.apache.commons.lang3.StringEscapeUtils
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ObservableGetDetectionTest(
        private val type: String,
        private val resolvedType: String, // e..g if it is ObservableInt, resolvedType is Int
        private val getter: String,
        private val constructor: String
) : BaseCompilationTest() {
    @Test
    fun detectGetterCallsOnObservables() {
        // this used to be disallowed on user code since 3.6 but causes issues w/ two way binding
        // which generates the same code. Now we instead support it but IDE will still show an
        // error to discourage developers.
        prepareProject()
        // add an adapter so that it is settable on TextView
        writeFile("/app/src/main/java/com/example/MyAdapter.java",
                """
                    package com.example;
                    import androidx.databinding.*;
                    import android.widget.TextView;
                    public class MyAdapter {
                        @BindingAdapter("android:text")
                        public static void mySet(TextView textView, $resolvedType value) {
                        }
                    }
                """.trimIndent())
        writeFile("/app/src/main/res/layout/observable_get.xml",
                """
                    <layout xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:bind="http://schemas.android.com/apk/res-auto">
                    <data>
                        <variable name="myVariable" type="${type.escapeXml()}"/>
                    </data>
                    <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@{myVariable.$getter}"/>
                </layout>
                """.trimIndent())
        val result = runGradle("assembleDebug")
        assertThat(result.error, result.resultCode, `is`(0))
    }

    @Test
    fun nestedObservable() {
        prepareProject()
        writeFile("/app/src/main/java/com/example/MyClass.java",
                """
                    package com.example;
                    import androidx.databinding.*;
                    public class MyClass {
                        public final $type value = $constructor;
                    }
                """.trimIndent())
        writeFile("/app/src/main/res/layout/observable_get.xml",
                """
                    <layout xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:bind="http://schemas.android.com/apk/res-auto">
                    <data>
                        <variable name="myVariable" type="com.example.MyClass"/>
                    </data>
                    <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@{``+myVariable.value}"/>
                </layout>
                """.trimIndent())
        val result = runGradle("assembleDebug")
        assertThat(result.error, result.resultCode, `is`(0))
    }

    @Test
    fun twoWayNested() {
        prepareProject()

        writeFile("/app/src/main/java/com/example/MyClass.java",
                """
                    package com.example;
                    import androidx.databinding.*;
                    public class MyClass {
                        public final $type value = $constructor;
                        @InverseMethod("fromString")
                        public static String convertToString($resolvedType value) {
                            throw new RuntimeException("");
                        }
                        public static $resolvedType fromString(String value) {
                            throw new RuntimeException("");
                        }
                    }
                """.trimIndent())
        writeFile("/app/src/main/res/layout/observable_get.xml",
                """
                    <layout xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:bind="http://schemas.android.com/apk/res-auto">
                    <data>
                        <import type="com.example.MyClass"/>
                        <variable name="myVariable" type="androidx.databinding.ObservableField&lt;MyClass>"/>
                    </data>
                    <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@={MyClass.convertToString(myVariable.value)}"/>
                </layout>
                """.trimIndent())
        val result = runGradle("assembleDebug")
        assertThat(result.error, result.resultCode, `is`(0))
    }

    private fun String.escapeXml() = StringEscapeUtils.escapeXml11(this)

    companion object {
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun params() = arrayOf(
                arrayOf("androidx.databinding.ObservableByte", "byte"),
                arrayOf("androidx.databinding.ObservableBoolean", "boolean"),
                arrayOf("androidx.databinding.ObservableChar", "char"),
                arrayOf("androidx.databinding.ObservableShort", "short"),
                arrayOf("androidx.databinding.ObservableInt", "int"),
                arrayOf("androidx.databinding.ObservableLong", "long"),
                arrayOf("androidx.databinding.ObservableFloat", "float"),
                arrayOf("androidx.databinding.ObservableDouble", "double"),
                arrayOf("androidx.databinding.ObservableField<String>", "String")
        ).map {
            arrayOf(it[0], it[1], "get()", "new ${it[0]}()")
        } + arrayOf(
                arrayOf(
                        "androidx.lifecycle.MutableLiveData<String>",
                        "String",
                        "getValue()",
                        "new androidx.lifecycle.MutableLiveData<String>()"
                )
        ) + arrayOf(
                arrayOf(
                        "kotlinx.coroutines.flow.MutableStateFlow<String>",
                        "String",
                        "getValue()",
                        "kotlinx.coroutines.flow.StateFlowKt.MutableStateFlow(\"\")"
                )
        )
    }
}
