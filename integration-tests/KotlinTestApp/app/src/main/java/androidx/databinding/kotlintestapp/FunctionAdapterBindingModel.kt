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

import android.view.View

class FunctionAdapterBindingModel {
    var referenceMethodCallCnt = 0
    var lambdaMethodCallCnt = 0
    var longClickCallbackCount = 0
    var someValue: String = "val"

    fun referencedMethod() {
        referenceMethodCallCnt++
    }

    fun lambdaMethod() {
        lambdaMethodCallCnt++
    }

    fun longClickCallback(view: View): Boolean {
        longClickCallbackCount++
        return true
    }
}
