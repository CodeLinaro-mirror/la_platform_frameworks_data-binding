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

package com.example.android.dynamicapp.app;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;

import static org.hamcrest.CoreMatchers.is;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.dynamicapp.app.databinding.BaseFeatureActivityBinding;
import com.example.android.dynamicapp.app.vo.ObservablePojo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BaseFeatureActivityTest {
    @Rule
    public ActivityTestRule<BaseFeatureActivity> rule =
            new ActivityTestRule<>(BaseFeatureActivity.class);

    BaseFeatureActivityBinding mBinding;
    @Before
    public void getBinding() {
        mBinding = rule.getActivity().getBinding();
    }

    @Test
    public void testSimple() {
        ObservablePojo pojo = new ObservablePojo();
        pojo.field.set("foo");
        pojo.liveData.postValue("bar");
        mBinding.setModel(pojo);
        sync();
        assertThat(mBinding.field.getText().toString(), is("foo"));
        assertThat(mBinding.liveData.getText().toString(), is("bar"));
        pojo.setBindable("baz");
        sync();
        assertThat(mBinding.bindable.getText().toString(), is("baz"));
    }

    private void sync() {
        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBinding.executePendingBindings();
                }
            });
        } catch (Throwable throwable) {
            throw new AssertionError(throwable);
        }
    }
}