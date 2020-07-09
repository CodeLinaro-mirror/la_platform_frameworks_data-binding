/*
 * Copyright (C) 2020 The Android Open Source Project
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
package android.databinding.testapp;

import android.content.Context;
import android.databinding.testapp.databinding.TwoWayCurrentTabTagBinding;
import android.databinding.testapp.vo.TwoWayBindingObject;
import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TabHost.TabSpec;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ConstantConditions")
@RunWith(AndroidJUnit4.class)
public class TabHostCurrentTabTagTest extends BaseDataBinderTest<TwoWayCurrentTabTagBinding> {

    TwoWayBindingObject mBindingObject;

    public TabHostCurrentTabTagTest() {
        super(TwoWayCurrentTabTagBinding.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initBinder(new Runnable() {
            @Override
            public void run() {
                Context context = getBinder().getRoot().getContext();
                mBindingObject = new TwoWayBindingObject(context);
                getBinder().setObj(mBindingObject);
                getBinder().executePendingBindings();
            }
        });
    }

    @Test
    public void testTabHostCurrentTabTag() throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBinder.tabhost.setup();
                TabSpec tab1 = mBinder.tabhost.newTabSpec("Tab1");
                TabSpec tab2 = mBinder.tabhost.newTabSpec("Tab2");

                tab1.setIndicator("tab1");
                tab1.setContent(R.id.foo);
                tab2.setIndicator("tab2");
                tab2.setContent(R.id.bar);
                mBinder.tabhost.addTab(tab1);
                mBinder.tabhost.addTab(tab2);
                mBinder.tabhost.setCurrentTab(1);
            }
        });

        waitWhile(new TestCondition() {
            @Override
            public boolean testValue() {
                return mBindingObject.currentTabTag.get() == null;
            }
        });

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals("Tab2", mBinder.tabhost.getCurrentTabTag());
                assertEquals("Tab2", mBindingObject.currentTabTag.get());
                mBinder.tabhost.setCurrentTab(0);
            }
        });

        waitWhile(new TestCondition() {
            @Override
            public boolean testValue() {
                return "Tab2".equals(mBindingObject.currentTabTag.get());
            }
        });

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals("Tab1", mBinder.tabhost.getCurrentTabTag());
                assertEquals("Tab1", mBindingObject.currentTabTag.get());
                mBindingObject.currentTabTag.set("Tab2");
            }
        });

        waitWhile(new TestCondition() {
            @Override
            public boolean testValue() {
                return "Tab1".equals(mBinder.tabhost.getCurrentTabTag());
            }
        });

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals("Tab2", mBinder.tabhost.getCurrentTabTag());
                assertEquals("Tab2", mBindingObject.currentTabTag.get());
            }
        });
    }

    private void waitWhile(final TestCondition check) throws Throwable {
        final long timeout = SystemClock.uptimeMillis() + 500;
        final boolean[] val = new boolean[1];
        do {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    val[0] = check.testValue();
                }
            });
            Thread.sleep(1);
        } while (SystemClock.uptimeMillis() < timeout && val[0]);
    }

    private interface TestCondition {
        boolean testValue();
    }
}
