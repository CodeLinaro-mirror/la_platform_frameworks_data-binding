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

package androidx.databinding;

import android.annotation.TargetApi;

import androidx.annotation.RestrictTo;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import android.content.res.ColorStateList;
import androidx.databinding.CallbackRegistry.NotifierCallback;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import androidx.viewbinding.ViewBinding;

import androidx.databinding.library.R;
import androidx.databinding.ObservableReference;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class WeakListener<T> extends WeakReference<ViewDataBinding> {
    private final ObservableReference<T> mObservable;
    protected final int mLocalFieldId;
    private T mTarget;

    public WeakListener(
            ViewDataBinding binder,
            int localFieldId,
            ObservableReference<T> observable,
            ReferenceQueue<ViewDataBinding> referenceQueue
    ) {
        super(binder, referenceQueue);
        mLocalFieldId = localFieldId;
        mObservable = observable;
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        mObservable.setLifecycleOwner(lifecycleOwner);
    }

    public void setTarget(T object) {
        unregister();
        mTarget = object;
        if (mTarget != null) {
            mObservable.addListener(mTarget);
        }
    }

    public boolean unregister() {
        boolean unregistered = false;
        if (mTarget != null) {
            mObservable.removeListener(mTarget);
            unregistered = true;
        }
        mTarget = null;
        return unregistered;
    }

    public T getTarget() {
        return mTarget;
    }

    @Nullable
    protected ViewDataBinding getBinder() {
        ViewDataBinding binder = get();
        if (binder == null) {
            unregister(); // The binder is dead
        }
        return binder;
    }
}
