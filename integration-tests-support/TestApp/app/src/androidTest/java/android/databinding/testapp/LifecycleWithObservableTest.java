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
package android.databinding.testapp;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.databinding.testapp.databinding.BindableObservablesBinding;
import android.databinding.testapp.vo.ViewModel;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LifecycleWithObservableTest extends BaseDataBinderTest<BindableObservablesBinding> {

    @Rule
    public final IdlingResourceRule idlingResource = new IdlingResourceRule() {
        @Override
        ViewDataBinding getBinding() {
            return getBinder();
        }
    };
    public LifecycleWithObservableTest() {
        super(BindableObservablesBinding.class);
    }

    @Test
    public void testLifecycle() {
        final FakeLifecycleOwner fakeLifecycleOwner = new FakeLifecycleOwner();
        final ViewModel viewModel = new ViewModel();
        ObservableField<String> field = viewModel.getFieldObservable();
        initBinder(new Runnable() {
            @Override
            public void run() {
                getBinder().setModel(viewModel);
                getBinder().setLifecycleOwner(fakeLifecycleOwner);
            }
        });
        field.set("initial");
        onView(withId(R.id.view2)).check(matches(withText("initial")));
        fakeLifecycleOwner.stop();
        field.set("value 1");
        onView(withId(R.id.view2)).check(matches(withText("initial")));
        fakeLifecycleOwner.start();
        onView(withId(R.id.view2)).check(matches(withText("value 1")));
        field.set("value 2");
        onView(withId(R.id.view2)).check(matches(withText("value 2")));
    }

    class FakeLifecycleOwner implements LifecycleOwner {
        final LifecycleRegistry registry = new LifecycleRegistry(this);
        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return registry;
        }

        void start() {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
                }
            });
        }

        void stop() {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
                }
            });
        }
    }
}
