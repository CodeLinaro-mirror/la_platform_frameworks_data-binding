/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.androidx.databinding.multimoduletestapp;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.multimoduletestapp.R;
import androidx.databinding.multimoduletestapp.databinding.ActivityMainBinding;
import androidx.databinding.testlibrary2.TestObservable;
import androidx.databinding.testlibrary2.databinding.Layout2Binding;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class GeneratedLayoutTest {
    @Test
    public void testBindToDefault() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                View view = inflater.inflate(R.layout.library_layout, null);
                // force override tag
                view.setTag("layout/library_layout_0");
                ViewDataBinding bind = DataBindingUtil.bind(view);
                assertEquals("IndependentLibraryBindingImpl",
                        bind.getClass().getSimpleName());
            }
        });
    }

    @Test
    public void testBindToSw600() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                View view = inflater.inflate(R.layout.library_layout, null);
                // force override tag
                view.setTag("layout-sw600dp-land/library_layout_0");
                ViewDataBinding bind = DataBindingUtil.bind(view);
                assertEquals("IndependentLibraryBindingSw600dpLandImpl",
                        bind.getClass().getSimpleName());
            }
        });
    }

    @Test
    public void testInheritedLayout() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                View view = inflater.inflate(R.layout.layout2, null);
                Layout2Binding binding = DataBindingUtil.bind(view);
                TestObservable testObservable = new TestObservable();
                testObservable.setCat("foo");
                binding.setVar(testObservable);
                binding.executePendingBindings();
                TextView inherited = (TextView) view.findViewById(R.id.inherited_text);
                assertEquals("foo", inherited.getText());
            }
        });
    }

    @Test
    public void testOverriddenAdapters() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                View view = inflater.inflate(R.layout.activity_main, null);
                ActivityMainBinding binding = DataBindingUtil.bind(view);
                binding.setFoo("xx");
                binding.executePendingBindings();
                TextView module = view.findViewById(R.id.overriddenModulePropText);
                assertEquals("app-module: xx", module.getText());

                TextView library = view.findViewById(R.id.overriddenLibraryPropText);
                assertEquals("app-library: xx", library.getText());
            }
        });
    }
}
