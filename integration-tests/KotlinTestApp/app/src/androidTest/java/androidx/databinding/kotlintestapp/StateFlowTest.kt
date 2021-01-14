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

import androidx.databinding.kotlintestapp.databinding.StateFlowBinding
import androidx.databinding.kotlintestapp.vo.StateFlowContainer
import androidx.databinding.kotlintestapp.vo.StateFlowViewModel
import androidx.test.runner.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class StateFlowTest {

    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val rule = BindingActivityRule<StateFlowBinding>(R.layout.state_flow)

    private val stateFlow = MutableStateFlow("")
    private val stateFlowHolder = MutableStateFlow(stateFlow)
    private val stateFlowContainer = StateFlowContainer()
    private val stateFlowViewModel = StateFlowViewModel()

    @Test
    fun testStateFlow() {
        setupView()

        assertEquals("", rule.binding.textView1.text.toString())
        assertEquals("", rule.binding.included.textView1.text.toString())
        assertEquals("", rule.binding.textView2.text.toString())
        assertEquals("", rule.binding.included.textView2.text.toString())

        val stateFlowExpected = "Hello world!"
        stateFlow.value = stateFlowExpected
        val containerExpected = "Hi there!"
        stateFlowContainer.stateFlow.value = containerExpected
        rule.executePendingBindings()

        assertEquals(stateFlowExpected, rule.binding.textView1.text.toString())
        assertEquals(stateFlowExpected, rule.binding.included.textView1.text.toString())
        assertEquals(containerExpected, rule.binding.textView2.text.toString())
        assertEquals(containerExpected, rule.binding.included.textView2.text.toString())
    }

    @Test
    fun noLifecycleOwner() {
        setupView(includeLifecycle = false)

        assertEquals("", rule.binding.textView1.text.toString())
        assertEquals("", rule.binding.included.textView1.text.toString())
        assertEquals("", rule.binding.textView2.text.toString())
        assertEquals("", rule.binding.included.textView2.text.toString())

        stateFlow.value = "Hello world!"
        stateFlowContainer.stateFlow.value = "Hi there!"
        rule.executePendingBindings()

        assertEquals("", rule.binding.textView1.text.toString())
        assertEquals("", rule.binding.included.textView1.text.toString())
        assertEquals("", rule.binding.textView2.text.toString())
        assertEquals("", rule.binding.included.textView2.text.toString())
    }

    @Test
    fun testMutableStateFlow() {
        setupView()

        assertEquals("", rule.binding.textView2.text.toString())
        assertEquals("", rule.binding.included.textView2.text.toString())

        val expected = "EditText changed"
        rule.runOnUiThread {
            rule.binding.editText1.setText(expected)
        }
        rule.executePendingBindings()

        assertEquals(expected, rule.binding.textView2.text.toString())
        assertEquals(expected, rule.binding.included.textView2.text.toString())
        assertEquals(expected, stateFlowContainer.stateFlow.value)
    }

    @Test
    fun stateFlowChanged() {
        setupView()

        stateFlow.value = "foo"
        rule.executePendingBindings()
        assertEquals("foo", rule.binding.textView1.text)

        rule.runOnUiThread {
            val anotherStateFlow = MutableStateFlow("new value")
            rule.binding.stateFlow = anotherStateFlow
            rule.executePendingBindings()
        }

        assertEquals("new value", rule.binding.textView1.text)
    }

    @Test
    fun cleanLifecycle() {
        setupView()
        stateFlow.value = "foo"
        rule.executePendingBindings()
        assertEquals("foo", rule.binding.textView1.text)
        rule.runOnUiThread {
            rule.binding.lifecycleOwner = null
        }
        stateFlow.value = "bar"
        rule.executePendingBindings()
        // value shouldn't change because we don't have a lifecycle
        assertEquals("foo", rule.binding.textView1.text)
        // now set the lifecycle again, it should receover
        rule.runOnUiThread {
            rule.binding.lifecycleOwner = rule.activity
        }
        rule.executePendingBindings()
        assertEquals("bar", rule.binding.textView1.text)
    }

    @Test
    fun stateFlowOfStateFlowChanged() {
        setupView()

        stateFlow.value = "foo"
        rule.executePendingBindings()
        assertEquals("foo", rule.binding.textView3.text)

        stateFlow.value = "bar"
        rule.executePendingBindings()
        assertEquals("bar", rule.binding.textView3.text)

        rule.runOnUiThread {
            val anotherStateFlow = MutableStateFlow("new value")
            stateFlowHolder.value = anotherStateFlow
            rule.executePendingBindings()
        }

        assertEquals("new value", rule.binding.textView3.text)
    }

    @Test
    fun testStateInOperator() {
        setupView()
        assertEquals("Hi there", rule.binding.textView4.text.toString())
    }

    private fun setupView(includeLifecycle: Boolean = true) {
        rule.runOnUiThread {
            rule.binding.apply {
                if (includeLifecycle) {
                    lifecycleOwner = rule.activity
                }
                stateFlow = this@StateFlowTest.stateFlow
                stateFlowOfStateFlow = this@StateFlowTest.stateFlowHolder
                stateFlowContainer = this@StateFlowTest.stateFlowContainer
                stateFlowViewModel = this@StateFlowTest.stateFlowViewModel
            }
        }

        rule.executePendingBindings()
    }
}
