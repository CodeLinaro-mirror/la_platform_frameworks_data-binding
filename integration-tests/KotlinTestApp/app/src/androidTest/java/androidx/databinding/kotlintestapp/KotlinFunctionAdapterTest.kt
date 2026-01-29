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

package androidx.databinding.kotlintestapp

import androidx.databinding.kotlintestapp.databinding.FunctionAdapterBinding
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinFunctionAdapterTest {
    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val rule = BindingActivityRule<FunctionAdapterBinding>(R.layout.function_adapter)

    @Test
    fun methodReference() {
        val model = FunctionAdapterBindingModel()
        rule.runOnUiThread {
            rule.binding.model = model
        }
        rule.executePendingBindings()
        assertThat(model.referenceMethodCallCnt, `is`(1))
    }

    @Test
    fun lambdaMethod() {
        val model = FunctionAdapterBindingModel()
        rule.runOnUiThread {
            rule.binding.model = model
        }
        rule.executePendingBindings()
        assertThat(model.lambdaMethodCallCnt, `is`(1))
    }

    @Test
    fun methodRefenceWithParameter() {
        val model = FunctionAdapterBindingModel()
        rule.runOnUiThread {
            rule.binding.model = model
        }
        rule.executePendingBindings()
        assertThat(model.longClickCallbackCount, `is`(0))
        onView(withId(R.id.textView1)).perform(longClick())
        Espresso.onIdle()
        assertThat(model.longClickCallbackCount, `is`(1))
    }

    @Test
    fun lambdaFunction() {
        val model = FunctionAdapterBindingModel()
        rule.runOnUiThread {
            rule.binding.model = model
        }
        rule.executePendingBindings()
        assertThat(model.longClickCallbackCount, `is`(0))
        onView(withId(R.id.textView2)).perform(longClick())
        Espresso.onIdle()
        assertThat(model.longClickCallbackCount, `is`(1))
    }
}
