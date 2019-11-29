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
import static org.junit.Assert.fail;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.example.viewbinding.databinding.SimpleBinding;
import org.junit.Test;

public final class SimpleTest {
    private final Context context = InstrumentationRegistry.getTargetContext();
    private final LayoutInflater inflater = LayoutInflater.from(context);

    @Test public void missingUserIdError() {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.simple, null, false);
        root.removeViewAt(0);

        try {
            SimpleBinding.bind(root);
            fail();
        } catch (NullPointerException e) {
            assertEquals(
                "Missing required view with ID: com.example.viewbinding:id/one", e.getMessage());
        }
    }

    @Test public void missingAndroidIdError() {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.simple, null, false);
        root.removeViewAt(1);

        try {
            SimpleBinding.bind(root);
            fail();
        } catch (NullPointerException e) {
            assertEquals("Missing required view with ID: android:id/text2", e.getMessage());
        }
    }
}
