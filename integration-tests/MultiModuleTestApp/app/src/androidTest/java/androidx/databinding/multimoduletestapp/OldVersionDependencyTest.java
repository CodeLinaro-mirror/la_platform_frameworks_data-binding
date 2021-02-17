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

package androidx.databinding.multimoduletestapp;

import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.example.agp331.databinding.LayoutFrom331Binding;
import com.example.android.support.lib.databinding.LayoutFrom31Binding;

import org.junit.Test;
import org.junit.runner.RunWith;
import static junit.framework.Assert.assertEquals;

/**
 * Tries to run a binding layout that was compiled with AGP 3_1_2
 */
@RunWith(AndroidJUnit4.class)
public class OldVersionDependencyTest {
    @Test
    public void loadPreCompiledBinding_AGP_3_1_4() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                LayoutFrom31Binding binding = DataBindingUtil.inflate(inflater,
                        com.example.android.support.lib.R.layout.layout_from_3_1, null, false);
                binding.setBar("bar");
                binding.setFoo(3);
                binding.executePendingBindings();
                assertEquals(binding.text.getText().toString(), "bar 3");
            }
        });
    }

    @Test
    public void loadPreCompiledBinding_AGP_3_3_1() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                LayoutFrom331Binding binding = DataBindingUtil.inflate(inflater,
                        com.example.agp331.R.layout.layout_from_3_3_1, null, false);
                binding.setBar("bar");
                binding.setFoo(3);
                binding.executePendingBindings();
                assertEquals(binding.text.getText().toString(), "bar 3");
            }
        });
    }
}
