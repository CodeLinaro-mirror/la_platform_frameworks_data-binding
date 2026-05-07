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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.example.viewbinding.databinding.MergeBinding;
import org.junit.Test;

public final class MergeTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final LayoutInflater inflater = LayoutInflater.from(context);

    @Test public void inflateWithParent() {
        ViewGroup parent = new LinearLayout(context);
        MergeBinding binding = MergeBinding.inflate(inflater, parent);

        assertSame(binding.getRoot(), parent);

        assertEquals(2, parent.getChildCount());
        assertSame(binding.one, parent.getChildAt(0));
        assertSame(binding.two, parent.getChildAt(1));
    }

    @Test public void inflateWithNullInflaterThrows() {
        ViewGroup parent = new LinearLayout(context);
        try {
            MergeBinding.inflate(null, parent);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test public void inflateWithNullParentThrows() {
        try {
            MergeBinding.inflate(inflater, null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test public void bind() {
        ViewGroup parent = new FrameLayout(context);
        inflater.inflate(R.layout.merge, parent, true);
        MergeBinding binding = MergeBinding.bind(parent);

        assertSame(parent, binding.getRoot());
        assertSame(parent.getChildAt(0), binding.one);
        assertSame(parent.getChildAt(1), binding.two);
    }

    @Test public void bindNullThrows() {
        try {
            MergeBinding.bind(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }
}
