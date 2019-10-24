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

package com.example.android.dynamicapp.featureA;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Debug;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.dynamicapp.featureA.databinding.ActivityMainABinding;
import com.example.android.dynamicapp.app.vo.ObservablePojo;

public class FeatureAActivity extends AppCompatActivity {
    private static final String BASE_BR = "com.example.android.dynamicapp.app.BR";
    private static final String FEATURE_A_BR = "com.example.android.dynamicapp.featureA.BR";
    private static final String FEATURE_B_BR = "com.example.android.dynamicapp.featureB.BR";
    private ActivityMainABinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.waitForDebugger();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_a);
        ObservablePojo pojo = new ObservablePojo();
        pojo.field.set("foo");
        pojo.liveData.setValue("bar");
        pojo.setBindable("baz");
        mBinding.setModel(pojo);
        CheckBRValues checkBRValues = new CheckBRValues();
        checkBRValues.addComparison(new CheckBRValues.Comparison(
                "bindable",
                BASE_BR,
                FEATURE_A_BR
        ));
        checkBRValues.addComparison(new CheckBRValues.Comparison(
                "sharedBindableName",
                BASE_BR,
                FEATURE_A_BR
        ));
        checkBRValues.addComparison(new CheckBRValues.Comparison(
                "sharedBindableName",
                BASE_BR,
                FEATURE_B_BR
        ));
        pojo.setBindable(checkBRValues.checkAll());
    }

    public void checkBRValues() {
        StringBuilder result = new StringBuilder();

    }
}
