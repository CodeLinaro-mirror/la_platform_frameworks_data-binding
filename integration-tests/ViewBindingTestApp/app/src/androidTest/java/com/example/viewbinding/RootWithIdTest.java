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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.View;
import com.example.viewbinding.databinding.RootWithIdBinding;
import org.junit.Test;

public final class RootWithIdTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final LayoutInflater inflater = LayoutInflater.from(context);

    @Test public void inflate() {
        RootWithIdBinding binding = RootWithIdBinding.inflate(inflater);
        View rootView = binding.rootId;
        assertSame(rootView, binding.getRoot());
    }

    @Test public void bindNullThrows() {
        // Improper generated logic might rely on the non-zero binding count and
        // assume a rootView.findViewById call was implicitly performing a null
        // check. But since the only binding is the root view which should be a
        // simple cast, ensure that null is not allowed to propagate.
        try {
            RootWithIdBinding.bind(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("rootView", e.getMessage());
        }
    }
}
