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
import static org.junit.Assert.assertSame;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.example.viewbinding.databinding.MergeWithIncludeBinding;
import com.example.viewbinding.databinding.SimpleBinding;
import org.junit.Test;

public final class MergeWithIncludeTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final LayoutInflater inflater = LayoutInflater.from(context);

    @Test public void works() {
        ViewGroup parent = new LinearLayout(context);
        MergeWithIncludeBinding binding = MergeWithIncludeBinding.inflate(inflater, parent);

        SimpleBinding simpleBinding = binding.simple;
        assertNotNull(simpleBinding);
        View requiredView = parent.findViewById(R.id.simple);
        assertSame(requiredView, simpleBinding.getRoot());
    }
}
