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

import androidx.databinding.kotlintestapp.databinding.TwoWayBinding
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TwoWayBindingTest {
    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val rule = BindingActivityRule<TwoWayBinding>(R.layout.two_way)

    @Test
    fun editText() {
        val model = TwoWayBindingModel()
        model.username.set("foo")
        rule.runOnUiThread {
            rule.binding.model = model
        }
        rule.executePendingBindings()
        onView(withId(R.id.userNameInput)).check(matches(withText("foo")))
        onView(withId(R.id.userNameInput)).perform(ViewActions.clearText())
        onView(withId(R.id.userNameInput)).perform(ViewActions.typeText("bar"))
        rule.executePendingBindings()
        assertThat(model.username.get(), `is`("bar"))
    }
}
