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

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.example.viewbinding.test.databinding.SimpleInTestModuleBinding;
import org.junit.Test;

public final class SimpleInTestModuleTest {
    private final Context context = InstrumentationRegistry.getTargetContext();
    private final LayoutInflater inflater = LayoutInflater.from(context);

    @Test public void inflateWithParent() {
        SimpleInTestModuleBinding binding = SimpleInTestModuleBinding.inflate(inflater);
        assertNotNull(binding);
    }
}
