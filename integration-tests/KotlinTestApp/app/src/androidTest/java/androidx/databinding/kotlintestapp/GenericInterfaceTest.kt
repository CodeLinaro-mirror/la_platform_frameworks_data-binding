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
package androidx.databinding.kotlintestapp

import android.view.View
import androidx.databinding.kotlintestapp.databinding.GenericInterfaceBinding
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenericInterfaceTest {
    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val rule = BindingActivityRule<GenericInterfaceBinding>(R.layout.generic_interface)

    @Test
    fun test() {
        rule.runOnUiThread {
            rule.binding.model = GenericModel().apply {
                observable.set(listOf(GenericImpl("a"), GenericImpl("b")))
                liveData.value = listOf(GenericImpl("c"), GenericImpl("d"))
            }
            rule.binding.unspecifiedGenericModel = GenericModelWithNonGenericField<Any>().also {
                it.specifiedGenericField.value = View.INVISIBLE
            }
            rule.binding.genericWithOut = GenericWithOutput(
                data = "foo",
                txt = "bar"
            )
            rule.executePendingBindings()
        }
        onView(withId(R.id.textObservable))
            .check(matches(withText("a,b")))
        onView(withId(R.id.textLiveData))
            .check(matches(withText("c-d")))
        onView(withId(R.id.unspecifiedGenericBinding))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))
        onView(withId(R.id.genericWithOutTxt))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.genericWithOutTxt))
            .check(matches(withText("bar")))
    }
}