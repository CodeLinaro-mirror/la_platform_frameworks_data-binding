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

import com.example.android.databinding.v1.V1Pojo;
import com.example.android.databinding.v1.databinding.V1LayoutBinding;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class V1DependencyTest {
    @Test
    public void testSupportBindingAdapter() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(
                        InstrumentationRegistry.getTargetContext());
                V1Pojo v1Pojo = new V1Pojo();

                v1Pojo.name = "John";
                v1Pojo.lastName = "Doe";
                v1Pojo.age = 19;
                V1LayoutBinding binding = V1LayoutBinding.inflate(inflater);
                binding.setObj(v1Pojo);
                binding.executePendingBindings();
                assertEquals("John Doe", binding.fullName.getText() );
                assertEquals("19", binding.age.getText());
            }
        });
    }
}
