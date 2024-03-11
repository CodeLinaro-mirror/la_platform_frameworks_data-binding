/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.databinding.testapp.databinding.LeakTestBinding;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;

import androidx.lifecycle.MutableLiveData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LeakTest {
    @Rule
    public final ActivityTestRule<TestActivity> rule = new ActivityTestRule<>(TestActivity.class);
    private WeakReference<LeakTestBinding> mWeakReference = new WeakReference<>(null);
    private final MutableLiveData<String> mLiveData = new MutableLiveData<>();

    private TestActivity getActivity() {
        return rule.getActivity();
    }

    @Before
    public void setUp() throws Exception {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LeakTestBinding binding = LeakTestBinding.inflate(
                            getActivity().getLayoutInflater());
                    getActivity().setContentView(binding.getRoot());
                    mWeakReference = new WeakReference<>(binding);
                    binding.setName("hello world");
                    binding.setLiveData(mLiveData);
                    binding.setLifecycleOwner(getActivity());
                    binding.executePendingBindings();
                }
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    @Test
    @UiThreadTest
    public void testBindingLeak() throws Throwable {
        // assertNull(mWeakReference.get()); prevents GC, probably for passing it as param.
        // ¯\_(ツ)_/¯
        //noinspection SimplifiableJUnitAssertion
        assertTrue("test sanity", mWeakReference.get() != null);
        removeDataBindingView();
        forceGC();
        assertNull(mWeakReference.get());
    }

    @Test
    @UiThreadTest
    public void testLiveDataCallback() throws Throwable {
        //noinspection SimplifiableJUnitAssertion
        assertTrue("test sanity", mWeakReference.get() != null);
        removeDataBindingView();
        forceGC();
        mLiveData.setValue("world"); // should not crash with NPE;
    }

    private void removeDataBindingView() throws Throwable {
        getActivity().setContentView(new FrameLayout(getActivity()));
    }

    private void forceGC() {
        // Use a random index in the list to detect the garbage collection each time because
        // .get() may accidentally trigger a strong reference during collection.
        ArrayList<WeakReference<byte[]>> leak = new ArrayList<>();
        do {
            WeakReference<byte[]> arr = new WeakReference<>(new byte[100]);
            leak.add(arr);
        } while (leak.get((int) (Math.random() * leak.size())).get() != null);
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();

        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
    }

    // Test to ensure that when the View is detached that it doesn't rebind
    // the dirty Views. The rebind should happen only after the root view is
    // reattached.
    @Test
    public void testNoChangeWhenDetached() throws Throwable {
        final LeakTestBinding binding = mWeakReference.get();
        final AnimationWatcher watcher = new AnimationWatcher();

        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setContentView(new FrameLayout(getActivity()));
                binding.setName("goodbye world");
                getActivity().getWindow().getDecorView().postOnAnimation(watcher);
            }
        });

        watcher.waitForAnimationThread();

        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals("hello world", binding.textView.getText().toString());
                getActivity().setContentView(binding.getRoot());
                getActivity().getWindow().getDecorView().postOnAnimation(watcher);
            }
        });

        watcher.waitForAnimationThread();

        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertEquals("goodbye world", binding.textView.getText().toString());
            }
        });
    }

    private static class AnimationWatcher implements Runnable {
        private boolean mWaiting = true;

        void waitForAnimationThread() throws InterruptedException {
            synchronized (this) {
                while (mWaiting) {
                    this.wait();
                }
                mWaiting = true;
            }
        }


        @Override
        public void run() {
            synchronized (this) {
                mWaiting = false;
                this.notifyAll();
            }
        }
    }
}
