/*
 * Copyright (C) 2020 The Android Open Source Project
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

package androidx.databinding.viewbinding.testapp

import android.view.LayoutInflater
import androidx.databinding.viewbinding.testapp.databinding.ViewBindingIncludingDataBindingLayoutBinding
import androidx.test.annotation.UiThreadTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewBindingIncludesDataBindingTest {
    @Test
    @UiThreadTest
    fun viewBindingIncludesDataBinding() {
        val binding = ViewBindingIncludingDataBindingLayoutBinding.inflate(
                LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext)
        )
        assertThat(binding.dataBinding1, notNullValue())
        val model = Model("a content")
        binding.dataBinding1.apply {
            this.model = model
            executePendingBindings()
            assertThat(widgetInDataBinding2.text.toString(), `is`("a content"))
        }
    }
}
