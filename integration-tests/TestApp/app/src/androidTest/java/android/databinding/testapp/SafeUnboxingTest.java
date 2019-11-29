/*
 * Copyright (C) 2016 The Android Open Source Project
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


import android.databinding.testapp.databinding.SafeUnboxingBinding;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.MutableLiveData;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SafeUnboxingTest extends BaseDataBinderTest<SafeUnboxingBinding> {
    public SafeUnboxingTest() {
        super(SafeUnboxingBinding.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initBinder();
    }

    @Test
    @UiThreadTest
    public void testBitShift() {
        mBinder.executePendingBindings();
        assertThat(mBinder.bitShift.intField, is(0));
        mBinder.setAnInt(3);
        mBinder.executePendingBindings();
        assertThat(mBinder.bitShift.intField, is(3 << 3));
    }

    @Test
    @UiThreadTest
    public void testArrayAccess() {
        mBinder.executePendingBindings();
        assertThat(mBinder.arrayAccess.intField, is(0));

        mBinder.setAnInt(3);
        mBinder.executePendingBindings();
        assertThat(mBinder.arrayAccess.intField, is(0));

        mBinder.setAnArray(new int[]{2, 3, 4, 5});
        mBinder.executePendingBindings();
        assertThat(mBinder.arrayAccess.intField, is(5));

        mBinder.setAnInt(7);
        mBinder.executePendingBindings();
        assertThat(mBinder.arrayAccess.intField, is(0));

        mBinder.setAnInt(null);
        mBinder.executePendingBindings();
        assertThat(mBinder.arrayAccess.intField, is(2));
    }

    @Test
    @UiThreadTest
    public void testIntEq() {
        mBinder.executePendingBindings();
        assertThat(mBinder.ternary.intField, is(2));
        mBinder.setAnInt(1);
        mBinder.executePendingBindings();
        assertThat(mBinder.ternary.intField, is(3));
    }

    @Test
    @UiThreadTest
    public void testBooleanTernary() {
        mBinder.executePendingBindings();
        assertThat(mBinder.ternary2.intField, is(3));
        mBinder.setABoolean(true);
        mBinder.executePendingBindings();
        assertThat(mBinder.ternary2.intField, is(2));
    }

    @Test
    @UiThreadTest
    public void testSetter1() {
        mBinder.executePendingBindings();
        assertThat(mBinder.setter1.intField, is(0));
        assertThat(mBinder.setter1.integerField, is(nullValue()));
        mBinder.setAnInt(3);
        mBinder.executePendingBindings();
        assertThat(mBinder.setter1.intField, is(3));
        assertThat(mBinder.setter1.integerField, is(3));
    }

    @Test
    @UiThreadTest
    public void testAdapter() {
        mBinder.executePendingBindings();
        assertThat(mBinder.checkBox.isChecked(), is(false));
        mBinder.setABoolean(true);
        mBinder.executePendingBindings();
        assertThat(mBinder.checkBox.isChecked(), is(true));
    }

    @Test
    @UiThreadTest
    public void testNullableEquality() {
        mBinder.setAnInt(null);
        mBinder.executePendingBindings();
        assertEquals(Boolean.TRUE, mBinder.nullableEquality.booleanField);
        assertEquals(Boolean.FALSE, mBinder.nullableInequality.booleanField);
    }

    @Test
    @UiThreadTest
    public void testNullableEquality2() {
        mBinder.setAnInt(3);
        mBinder.executePendingBindings();
        assertEquals(Boolean.FALSE, mBinder.nullableEquality.booleanField);
        assertEquals(Boolean.TRUE, mBinder.nullableInequality.booleanField);
    }

    @Test
    @UiThreadTest
    public void ternary() {
        mBinder.setLifecycleOwner(getActivity());
        MutableLiveData<Boolean> liveData1 = new MutableLiveData<>();
        MutableLiveData<Boolean> liveData2 = new MutableLiveData<>();
        mBinder.setLiveData1(liveData1);
        mBinder.setLiveData2(liveData2);
        List<Boolean> options = Arrays.asList(null, true, false);
        for(Boolean value1 : options) {
            for(Boolean value2 : options) {
                liveData1.setValue(value1);
                liveData2.setValue(value2);
                mBinder.executePendingBindings();
                boolean v1 = value1 == null ? false : value1;
                boolean v2 = value2 == null ? false : value2;
                assertEquals(value1 + " && " + value2,v1 && v2, mBinder.ternaryAnd.booleanField);
                assertEquals(value1 + " || " + value2,v1 || v2, mBinder.ternaryOr.booleanField);
            }
        }
    }
}
