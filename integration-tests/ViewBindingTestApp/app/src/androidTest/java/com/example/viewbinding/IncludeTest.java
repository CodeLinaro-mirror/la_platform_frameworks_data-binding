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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import android.content.Context;
import android.content.res.Configuration;
import androidx.test.platform.app.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.viewbinding.databinding.IncludeBinding;
import com.example.viewbinding.databinding.SimpleBinding;
import org.junit.Test;

public final class IncludeTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test public void bindPortrait() {
        LayoutInflater inflater = LayoutInflater.from(context);

        View root = inflater.inflate(R.layout.include, null, false);
        IncludeBinding binding = IncludeBinding.bind(root);

        SimpleBinding requiredBinding = binding.simpleRequired;
        assertNotNull(requiredBinding);
        View requiredView = root.findViewById(R.id.simple_required);
        assertSame(requiredView, requiredBinding.getRoot());

        SimpleBinding optionalBinding = binding.simpleOptional;
        assertNotNull(optionalBinding);
        View optionalView = root.findViewById(R.id.simple_optional);
        assertSame(optionalView, optionalBinding.getRoot());
    }

    @Test public void bindLandscape() {
        Configuration newConfig = new Configuration(context.getResources().getConfiguration());
        newConfig.orientation = Configuration.ORIENTATION_LANDSCAPE;
        Context landscapeContext = context.createConfigurationContext(newConfig);
        LayoutInflater inflater = LayoutInflater.from(landscapeContext);

        View root = inflater.inflate(R.layout.include, null, false);
        IncludeBinding binding = IncludeBinding.bind(root);

        SimpleBinding requiredBinding = binding.simpleRequired;
        assertNotNull(requiredBinding);
        View requiredView = root.findViewById(R.id.simple_required);
        assertSame(requiredView, requiredBinding.getRoot());

        SimpleBinding optionalBinding = binding.simpleOptional;
        assertNull(optionalBinding);
    }
}
