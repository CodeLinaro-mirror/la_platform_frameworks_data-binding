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

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData

// helpers for GenericInterfaceTest

// a sample generic interface accessed from layout
interface Generic<T> {
    fun getValue(): T
}

// implementation of it
class GenericImpl(val data: String) : Generic<String> {
    override fun getValue() = data
}

data class GenericWithOutput<out T>(val data: T?, val txt:String)

// model with nested generic data
class GenericModel {
    val observable = ObservableField<List<Generic<*>>>()
    val liveData = MutableLiveData<List<Generic<*>>>()
}

// a generic class with non-generic field. see: b/139738910
class GenericModelWithNonGenericField<T> {
    val specifiedGenericField = MutableLiveData<Int>()
}

// binding adapter to test results
@BindingAdapter("genericList")
fun TextView.genericList(items: List<Generic<*>>?) {
    text = items?.joinToString(",") {
        it.getValue().toString()
    }
}

@BindingAdapter("genericList2")
fun TextView.genericList2(items: List<Generic<*>>?) {
    text = items?.joinToString("-") {
        it.getValue().toString()
    }
}
