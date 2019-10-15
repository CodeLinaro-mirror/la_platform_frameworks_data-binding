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

package com.example.viewbinding;

import static org.junit.Assert.assertSame;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.View;
import com.example.viewbinding.databinding.IncludeReplaceIdBinding;
import com.example.viewbinding.databinding.RootWithIdBinding;
import org.junit.Test;

public final class IncludeReplaceIdTest {
    private final Context context = InstrumentationRegistry.getTargetContext();
    private final LayoutInflater inflater = LayoutInflater.from(context);

    @Test public void inflate() {
        IncludeReplaceIdBinding binding = IncludeReplaceIdBinding.inflate(inflater);
        View bindingChild = binding.getRoot().getChildAt(0);
        RootWithIdBinding rootWithIdBinding = binding.replacedId;
        assertSame(bindingChild, rootWithIdBinding.getRoot());
    }
}
