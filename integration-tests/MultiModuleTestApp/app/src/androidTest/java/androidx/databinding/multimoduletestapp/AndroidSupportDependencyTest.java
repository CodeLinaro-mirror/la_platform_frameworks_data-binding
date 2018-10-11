/*
 * Copyright (C) 2018 The Android Open Source Project
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
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.multimoduletestapp.databinding.AndroidSupportDependantLayoutBinding;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AndroidSupportDependencyTest {
    @Test
    public void testSupportBindingAdapter() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                View view = inflater.inflate(R.layout.android_support_dependant_layout, null);
                // force override tag
                view.setTag("layout/android_support_dependant_layout_0");
                AndroidSupportDependantLayoutBinding binding = DataBindingUtil.bind(view);
                binding.setCardCustomCompat(true);
                binding.executePendingBindings();
                assertEquals(binding.cardView.getUseCompatPadding(), true);
                binding.setCardCustomCompat(false);
                binding.executePendingBindings();
                assertEquals(binding.cardView.getUseCompatPadding(), false);
            }
        });
    }
}
