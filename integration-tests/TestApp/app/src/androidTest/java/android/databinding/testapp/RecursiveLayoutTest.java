/*
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.databinding.testapp;

import android.databinding.testapp.databinding.RecursiveLayoutBinding;
import android.databinding.testapp.vo.RecursiveClass;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class RecursiveLayoutTest extends BaseDataBinderTest<RecursiveLayoutBinding> {
    public RecursiveLayoutTest() {
        super(RecursiveLayoutBinding.class);
    }

    @Test
    @UiThreadTest
    public void runRecursiveTest() {
        initBinder();
        RecursiveClass recursiveClass = new RecursiveClass();
        recursiveClass.text = "foo";

        mBinder.setRecursiveClass(recursiveClass);
        mBinder.executePendingBindings();
        assertThat(
                mBinder.recursiveClassText.getText().toString(),
                is("foo")
        );
        assertThat(
                mBinder.recursiveClassViaAdapterText.getText().toString(),
                is("foo foo")
        );
    }
}
