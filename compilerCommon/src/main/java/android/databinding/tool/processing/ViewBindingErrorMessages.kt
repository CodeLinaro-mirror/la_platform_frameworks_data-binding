/*
 * Copyright (C) 2021 The Android Open Source Project
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

package android.databinding.tool.processing

import android.databinding.tool.store.LayoutFileParser.VIEW_BINDING_TYPE_ATTR

/**
 * Error messages for ViewBinding code generator.
 */
object ViewBindingErrorMessages {
    fun viewBindingTypeInIncludeTag(
        layoutFileName: String,
        includeTagId: String
    ): String = """
        Cannot use '$VIEW_BINDING_TYPE_ATTR' in <include> tags.

        <include id='$includeTagId'> in layout '$layoutFileName' will be ignored, as ViewBinding
        always uses the type from the included target.
    """.trimIndent()

    fun inconsistentViewBindingType(
        layoutFileName: String,
        bindingTypes: List<String>,
        bindingTargetId: String,
    ): String = """
        <View $VIEW_BINDING_TYPE_ATTR='$bindingTargetId'> is not defined consistently in '$layoutFileName'

        Types declared across layouts: ${bindingTypes.joinToString(", ")}
    """.trimIndent()
}
