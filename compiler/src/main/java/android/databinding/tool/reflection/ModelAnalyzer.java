/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.databinding.tool.Context;
import android.databinding.tool.LibTypes;
import android.databinding.tool.util.L;
import android.databinding.tool.util.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is the base class for several implementations of something that
 * acts like a ClassLoader. Different implementations work with the Annotation
 * Processor, ClassLoader, and an Android Studio plugin.
 */
public abstract class ModelAnalyzer {
    public static final String GENERATED_ANNOTATION = "javax.annotation.Generated";

    private static final String MAP_CLASS_NAME = "java.util.Map";

    private static final String STRING_CLASS_NAME = "java.lang.String";

    private static final String OBJECT_CLASS_NAME = "java.lang.Object";

    private static final String VIEW_STUB_CLASS_NAME = "android.view.ViewStub";

    private List<ModelClass> mListTypes;
    private CachedClass mMapType = new CachedClass() {
        @Override
        ModelClass find() {
            return loadClassErasure(MAP_CLASS_NAME);
        }
    };
    private CachedClass mStringType = new CachedClass() {
        @Override
        ModelClass find() {
            return findClass(STRING_CLASS_NAME, null);
        }
    };
    private CachedClass mObjectType = new CachedClass() {

        @Override
        ModelClass find() {
            return findClass(OBJECT_CLASS_NAME, null);

        }
    };
    private CachedClass mObservableType = new CachedClass() {
        @Override
        ModelClass find() {
            return findClass(libTypes.getObservable(), null);
        }
    };
    private CachedClass mObservableListType = new CachedClass() {
        @Override
        ModelClass find() {
            return loadClassErasure(libTypes.getObservableList());
        }
    };
    private CachedClass mObservableMapType = new CachedClass() {

        @Override
        ModelClass find() {
            return loadClassErasure(libTypes.getObservableMap());
        }
    };
    private CachedClass mLiveDataType = new CachedClass() {
        @Override
        ModelClass find() {
            return loadClassErasure(libTypes.getLiveData());
        }
    };
    private CachedClass mMutableLiveDataType = new CachedClass() {
        @Override
        ModelClass find() {
            return loadClassErasure(libTypes.getMutableLiveData());
        }
    };
    private List<ModelClass> mObservableFieldTypes;
    private CachedClass mViewBindingType = new CachedClass() {
        @Override
        ModelClass find() {
            return findClass(libTypes.getViewDataBinding(), null);
        }
    };
    private CachedClass mViewStubType = new CachedClass() {
        @Override
        ModelClass find() {
            return findClass(VIEW_STUB_CLASS_NAME, null);
        }
    };
    private CachedClass mViewStubProxyType = new CachedClass() {
        @Override
        ModelClass find() {
            return findClass(libTypes.getViewStubProxy(), null);
        }
    };

    public final LibTypes libTypes;

    /**
     * If it is present, we annotate generated classes with @Generated.
     */
    private Boolean mHasGeneratedAnnotation;

    private final Map<String, InjectedClass> mInjectedClasses =
            new HashMap<String, InjectedClass>();

    protected ModelAnalyzer(LibTypes libTypes) {
        this.libTypes = libTypes;
    }

    public ModelClass findCommonParentOf(ModelClass modelClass1, ModelClass modelClass2) {
        return findCommonParentOf(modelClass1, modelClass2, true);
    }

    public ModelClass findCommonParentOf(ModelClass modelClass1, ModelClass modelClass2,
            boolean failOnError) {
        ModelClass curr = modelClass1;
        while (curr != null && !curr.isAssignableFrom(modelClass2)) {
            curr = curr.getSuperclass();
        }
        if (curr == null) {
            if (modelClass1.isObject() && modelClass2.isInterface()) {
                return modelClass1;
            } else if (modelClass2.isObject() && modelClass1.isInterface()) {
                return modelClass2;
            }

            ModelClass primitive1 = modelClass1.unbox();
            ModelClass primitive2 = modelClass2.unbox();
            if (!modelClass1.equals(primitive1) || !modelClass2.equals(primitive2)) {
                return findCommonParentOf(primitive1, primitive2, failOnError);
            }
        }
        if (failOnError) {
            Preconditions.checkNotNull(curr,
                    "must be able to find a common parent for " + modelClass1 + " and "
                            + modelClass2);
        }
        return curr;
    }

    public abstract ModelClass loadPrimitive(String className);

    public static ModelAnalyzer getInstance() {
        return Context.getModelAnalyzer();
    }

    /**
     * Takes a raw className (potentially w/ generics and arrays) and expands definitions using
     * the import statements.
     * <p>
     * For instance, this allows user to define variables
     * <variable type="User" name="user"/>
     * if they previously imported User.
     * <import name="com.example.User"/>
     */
    public String applyImports(String className, Map<String, String> imports) {
        className = className.trim();
        int numDimensions = 0;
        String generic = null;
        // handle array
        while (className.endsWith("[]")) {
            numDimensions++;
            className = className.substring(0, className.length() - 2);
        }
        // handle generics
        final int lastCharIndex = className.length() - 1;
        if ('>' == className.charAt(lastCharIndex)) {
            // has generic.
            int open = className.indexOf('<');
            if (open == -1) {
                L.e("un-matching generic syntax for %s", className);
                return className;
            }
            generic = applyImports(className.substring(open + 1, lastCharIndex), imports);
            className = className.substring(0, open);
        }
        int dotIndex = className.indexOf('.');
        final String qualifier;
        final String rest;
        if (dotIndex == -1) {
            qualifier = className;
            rest = null;
        } else {
            qualifier = className.substring(0, dotIndex);
            rest = className.substring(dotIndex); // includes dot
        }
        final String expandedQualifier = imports.get(qualifier);
        String result;
        if (expandedQualifier != null) {
            result = rest == null ? expandedQualifier : expandedQualifier + rest;
        } else {
            result = className; // no change
        }
        // now append back dimension and generics
        if (generic != null) {
            result = result + "<" + applyImports(generic, imports) + ">";
        }
        while (numDimensions-- > 0) {
            result = result + "[]";
        }
        return result;
    }

    public String getDefaultValue(String className) {
        if ("int".equals(className)) {
            return "0";
        }
        if ("short".equals(className)) {
            return "0";
        }
        if ("long".equals(className)) {
            return "0L";
        }
        if ("float".equals(className)) {
            return "0f";
        }
        if ("double".equals(className)) {
            return "0.0";
        }
        if ("boolean".equals(className)) {
            return "false";
        }
        if ("char".equals(className)) {
            return "'\\u0000'";
        }
        if ("byte".equals(className)) {
            return "0";
        }
        return "null";
    }

    public final ModelClass findClass(String className, ImportBag imports) {
        if (mInjectedClasses.containsKey(className)) {
            return mInjectedClasses.get(className);
        }
        return findClassInternal(className, imports);
    }

    public abstract ModelClass findClassInternal(String className, ImportBag importBag);

    public abstract ModelClass findClass(Class classType);

    public abstract TypeUtil createTypeUtil();

    public ModelClass injectClass(InjectedClass injectedClass) {
        mInjectedClasses.put(injectedClass.getCanonicalName(), injectedClass);
        return injectedClass;
    }

    List<ModelClass> getListTypes() {
        if (mListTypes == null) {
            mListTypes = libTypes.getListClassNames()
                    .stream()
                    .map(this::loadClassErasure)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return mListTypes;
    }

    public ModelClass getMapType() {
        return mMapType.get();
    }

    ModelClass getStringType() {
        return mStringType.get();
    }

    ModelClass getObjectType() {
        return mObjectType.get();
    }

    ModelClass getObservableType() {
        return mObservableType.get();
    }

    ModelClass getObservableListType() {
        return mObservableListType.get();
    }

    ModelClass getObservableMapType() {
        return mObservableMapType.get();
    }

    ModelClass getLiveDataType() {
        return mLiveDataType.get();
    }

    ModelClass getMutableLiveDataType() {
        return mMutableLiveDataType.get();
    }

    ModelClass getViewDataBindingType() {
        ModelClass result = mViewBindingType.get();
        Preconditions.checkNotNull(result, "Cannot find %s class. Something is wrong "
                + "in the classpath, please submit a bug report", libTypes.getViewDataBinding());
        return result;
    }

    public ModelClass getViewStubProxyType() {
        return mViewStubProxyType.get();
    }

    protected List<ModelClass> getObservableFieldTypes() {
        if (mObservableFieldTypes == null) {
            mObservableFieldTypes = libTypes.getObservableFields()
                        .stream()
                        .map(this::loadClassErasure)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        }
        return mObservableFieldTypes;
    }

    ModelClass getViewStubType() {
        return mViewStubType.get();
    }

    private ModelClass loadClassErasure(String className) {
        ModelClass modelClass = findClass(className, null);
        if (modelClass == null) {
            return null;
        } else {
            return modelClass.erasure();
        }
    }

    public final boolean hasGeneratedAnnotation() {
        if (mHasGeneratedAnnotation == null) {
            mHasGeneratedAnnotation = findGeneratedAnnotation();
        }
        return mHasGeneratedAnnotation;
    }

    protected abstract boolean findGeneratedAnnotation();

}
