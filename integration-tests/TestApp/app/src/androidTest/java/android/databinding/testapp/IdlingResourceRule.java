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

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class IdlingResourceRule extends TestWatcher {

    private final IdlingResource mIdlingResource = new DataBindingIdlingResource() {
        @Override
        ViewDataBinding getBinding() {
            return IdlingResourceRule.this.getBinding();
        }
    };

    @Override
    protected void starting(Description description) {
        super.starting(description);
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Override
    protected void finished(Description description) {
        super.finished(description);
        IdlingRegistry.getInstance().unregister(mIdlingResource);
    }

    abstract ViewDataBinding getBinding();

    private abstract static class DataBindingIdlingResource implements IdlingResource {
        private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);
        private final String mId = "data-binding-" + ID_COUNTER.incrementAndGet();
        private final List<ResourceCallback> mObservers = new CopyOnWriteArrayList<>();
        // holds whether isIdle is called and the result was false. We track this to avoid calling
        // onTransitionToIdle callbacks if Espresso never thought we were idle in the first place.
        private boolean mWasNotIdle = false;

        @Override
        public String getName() {
            return mId;
        }

        @Override
        public boolean isIdleNow() {
            boolean idle = doCheckIdle();
            if (idle) {
                if (mWasNotIdle) {
                    invokeCallbacks();
                }
                mWasNotIdle = false;
            } else {
                mWasNotIdle = true;
                getBinding().getRoot().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isIdleNow();
                    }
                }, 16);
            }
            return idle;
        }

        private boolean doCheckIdle() {
            boolean hasPending = getBinding().hasPendingBindings();
            if (hasPending) {
                // data binding wont process them unless it is in a good lifecycle so check
                // that as well before deciding
                final LifecycleOwner owner = getBinding().getLifecycleOwner();
                if (owner == null || owner.getLifecycle().getCurrentState()
                        .isAtLeast(Lifecycle.State.STARTED)) {
                    // waiting won't help, data binding won't bind.
                    return false;
                }
            }
            return true;
        }

        private void invokeCallbacks() {
            for (ResourceCallback callback : mObservers) {
                callback.onTransitionToIdle();
            }
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            mObservers.add(callback);
        }
        abstract ViewDataBinding getBinding();
    }
}
