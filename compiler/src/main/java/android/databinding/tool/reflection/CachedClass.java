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
package android.databinding.tool.reflection;

import com.android.annotations.Nullable;

/**
 * A simple data structure that avoids searching the same class again and again.
 * Especially useful since things like isObservable might look for classes
 * (e.g. LiveData) repeatedly that are not mandatory.
 */
public abstract class CachedClass {
    private boolean mSearched = false;
    @Nullable
    private ModelClass klass;

    @Nullable
    public ModelClass get() {
        if (!mSearched) {
            klass = find();
            mSearched = true;
        }
        return klass;
    }

    @Nullable
    abstract ModelClass find();
}
