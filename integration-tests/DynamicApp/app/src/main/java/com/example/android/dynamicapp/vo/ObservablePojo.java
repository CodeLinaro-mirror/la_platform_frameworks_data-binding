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

package com.example.android.dynamicapp.app.vo;

import androidx.lifecycle.MutableLiveData;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.example.android.dynamicapp.app.BR;

public class ObservablePojo extends BaseObservable {
    public final ObservableField<String> field = new ObservableField<>();
    public final MutableLiveData<String> liveData = new MutableLiveData<>();
    @Bindable
    private String mBindable;

    public String getBindable() {
        return mBindable;
    }

    public void setBindable(String bindable) {
        mBindable = bindable;
        notifyPropertyChanged(BR.bindable);
    }
}
