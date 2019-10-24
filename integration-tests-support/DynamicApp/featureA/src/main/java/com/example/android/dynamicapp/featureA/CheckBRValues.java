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

package com.example.android.dynamicapp.featureA;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * When we have androidTest for features, we should do it there
 */
public class CheckBRValues {
    private List<Comparison> mComparisonList = new ArrayList<>();
    public void addComparison(Comparison comparison) {
        mComparisonList.add(comparison);
    }

    public String checkAll() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Comparison comparison : mComparisonList) {
            check(comparison, stringBuilder);
        }
        return stringBuilder.toString();
    }

    public boolean check(Comparison comparison, @Nullable StringBuilder stringBuilder) {
        int value1 = loadBRValue(comparison.package1, comparison.variableName);
        int value2 = loadBRValue(comparison.package2, comparison.variableName);
        if (stringBuilder != null) {
            stringBuilder.append(comparison);
            stringBuilder.append("\n");
            stringBuilder.append(value1 + " vs " + value2);
            stringBuilder.append("\n");
        }
        return value1 == value2;
    }
    public boolean check(Comparison comparison) {
        return check(comparison, null);
    }

    private static int loadBRValue(String pkg, String name) {
        try {
            Class<?> aClass = Class.forName(pkg);
            Object value = aClass.getField(name).get(aClass);
            return (int) value;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static class Comparison {
        public final String variableName;
        public final String package1;
        public final String package2;

        public Comparison(String variableName, String package1, String package2) {
            this.variableName = variableName;
            this.package1 = package1;
            this.package2 = package2;
        }

        @Override
        public String toString() {
            return "Comparison{" +
                    "variableName='" + variableName + '\'' +
                    ", package1='" + package1 + '\'' +
                    ", package2='" + package2 + '\'' +
                    '}';
        }
    }
}
