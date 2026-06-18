/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Configuration;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.viewbinding.databinding.ViewBindingTypeLayoutBinding;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ViewBindingTypeTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void bindPortrait() {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewBindingTypeLayoutBinding.inflate(
            inflater,
            null,
            false
        );
        // binding successfully without crashing is the test
    }

    @Test
    public void bindLandscape() {
        Configuration newConfig = new Configuration(context.getResources().getConfiguration());
        newConfig.orientation = Configuration.ORIENTATION_LANDSCAPE;
        Context landscapeContext = context.createConfigurationContext(newConfig);
        LayoutInflater inflater = LayoutInflater.from(landscapeContext);
        ViewBindingTypeLayoutBinding.inflate(
                inflater,
                null,
                false
        );
        // binding successfully without crashing is the test
    }

    @Test
    public void checkTypes() {
        Map<String, Class<?>> fieldTypes = new HashMap<>();
        for(Field field : ViewBindingTypeLayoutBinding.class.getDeclaredFields()) {
            fieldTypes.put(field.getName(), field.getType());
        }
        assertEquals(
                TextView.class,
                fieldTypes.get("typeMatchesOtherLayout")
        );
        assertEquals(
                ViewGroup.class,
                fieldTypes.get("onlyInLandscape")
        );
        assertEquals(
                ViewGroup.class,
                fieldTypes.get("onlyInPortrait")
        );
        assertEquals(
                ImageView.class,
                fieldTypes.get("imageButtonAsImageView")
        );
    }
}
