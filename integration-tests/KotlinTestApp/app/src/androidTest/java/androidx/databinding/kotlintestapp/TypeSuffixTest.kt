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
package androidx.databinding.kotlintestapp

import androidx.databinding.kotlintestapp.databinding.TypeSuffixBinding
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TypeSuffixTest {
    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val rule = BindingActivityRule<TypeSuffixBinding>(R.layout.type_suffix)

    @Test
    fun test() {
        rule.runOnUiThread {
            rule.binding.enabled = true
            rule.executePendingBindings()
            assertEquals("500", rule.binding.longValueView.text)
            assertEquals("200", rule.binding.floatValueView.text)
            assertEquals("4", rule.binding.binaryValueView.text)
            assertEquals("255", rule.binding.hexValueView.text)

            rule.binding.enabled = false
            rule.executePendingBindings()
            assertEquals("0", rule.binding.longValueView.text)
            assertEquals("100", rule.binding.floatValueView.text)
            assertEquals("2", rule.binding.binaryValueView.text)
            assertEquals("160", rule.binding.hexValueView.text)
        }
    }
}
