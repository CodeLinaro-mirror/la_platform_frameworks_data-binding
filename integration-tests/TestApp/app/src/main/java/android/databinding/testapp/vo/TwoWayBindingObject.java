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
package android.databinding.testapp.vo;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import androidx.databinding.InverseMethod;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableArrayMap;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableByte;
import androidx.databinding.ObservableChar;
import androidx.databinding.ObservableDouble;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableFloat;
import androidx.databinding.ObservableInt;
import androidx.databinding.ObservableLong;
import androidx.databinding.ObservableShort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TwoWayBindingObject {
    private static final String[] VALUES = {
            "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
    };
    public final ListAdapter adapter;
    public final SpinnerAdapter spinnerAdapter;
    public final ObservableInt selectedItemPosition = new ObservableInt();
    public final ObservableLong date = new ObservableLong(System.currentTimeMillis());
    public final ObservableBoolean checked = new ObservableBoolean();
    public final ObservableInt number = new ObservableInt(1);
    public final ObservableFloat rating = new ObservableFloat(1);
    public final ObservableInt progress = new ObservableInt(1);
    public final ObservableInt currentTab = new ObservableInt();
    public final ObservableField<String> text = new ObservableField<>();
    public final ObservableInt hour = new ObservableInt();
    public final ObservableInt minute = new ObservableInt();
    public final ObservableInt year = new ObservableInt(1972);
    public final ObservableInt month = new ObservableInt(9);
    public final ObservableInt day = new ObservableInt(21);
    public final ObservableArrayList<Integer> list = new ObservableArrayList<>();
    public final ObservableArrayMap<String, Integer> map = new ObservableArrayMap<>();
    public final ObservableField<int[]> array = new ObservableField<>();
    public final ObservableField<CharSequence> editText = new ObservableField<>();
    public final ObservableBoolean booleanField = new ObservableBoolean();
    public final ObservableByte byteField = new ObservableByte();
    public final ObservableShort shortField = new ObservableShort();
    public final ObservableInt intField = new ObservableInt();
    public final ObservableLong longField = new ObservableLong();
    public final ObservableFloat floatField = new ObservableFloat();
    public final ObservableDouble doubleField = new ObservableDouble();
    public final ObservableChar charField = new ObservableChar();
    public final ObservableField<List<String>> stringList =
            new ObservableField<List<String>>(new ArrayList<String>());
    public final ObservableField<String> pigLatin = new ObservableField<String>();
    public final ObservableField<int[]> anotherArray = new ObservableField<int[]>();
    public final ObservableField<NestedObservableHolder> nested = new ObservableField<>();
    public int text1Changes;
    public int text2Changes;
    public CountDownLatch textLatch;
    public String textField = "Hello";
    public static String staticField = "World";

    public TwoWayBindingObject(Context context) {
        this.adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, VALUES);
        int[] arr = new int[10];
        for (int i = 0; i < 10; i++) {
            list.add(i);
            arr[i] = i + 1;
        }
        array.set(arr);
        anotherArray.set(arr);
        for (int i = 0; i < VALUES.length; i++) {
            map.put(VALUES[i], i + 1);
            stringList.get().add(VALUES[i]);
        }
        this.spinnerAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, VALUES);
    }

    public void textChanged1(CharSequence s, int start, int before, int count) {
        text1Changes++;
        textLatch.countDown();
    }

    public void textChanged2(CharSequence s, int start, int before, int count) {
        text2Changes++;
        textLatch.countDown();
    }

    @InverseMethod("convertStringToInt")
    public String convertFromInt(int value) {
        return String.valueOf(value);
    }

    public int convertStringToInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @InverseMethod("convertStringToFloat")
    public static String convertFromFloat(float value) {
        return String.valueOf(value);
    }

    public static float convertStringToFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @InverseMethod("convertStringToList")
    public <T> String convertFromList(List<T> values) {
        return convertFromStringList((List<String>) values);
    }

    public <U> List<U> convertStringToList(String value) {
        // Yeah, I know this sucks, but it is only a test of generics.
        return (List<U>) convertStringToStringList(value);
    }

    @InverseMethod("convertStringToIntArray")
    public String convertFromIntArray(int[] values) {
        if (values == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(values[i]);
        }
        return sb.toString();
    }

    @InverseMethod("convertFromIntArray")
    public int[] convertStringToIntArray(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        String[] strings = value.split("[,]");
        int[] values = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            values[i] = Integer.parseInt(strings[i]);
        }
        return values;
    }

    @InverseMethod("convertStringToStringList")
    public String convertFromStringList(List<String> values) {
        if (values == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    public List<String> convertStringToStringList(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        return Arrays.asList(value.split("[,]"));
    }

    @InverseMethod("fromPigLatin")
    public String toPigLatin(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        return string.substring(1) + string.charAt(0) + "ay";
    }

    public String fromPigLatin(String string) {
        if (string == null || string.length() < 3 || !string.endsWith("ay")) {
            return string;
        }
        return string.charAt(string.length() - 3) + string.substring(0, string.length() - 3);
    }

    public boolean validate(CharSequence text) {
        return text != null && text.toString().contains("a");
    }

    public void doSomething(String text) {
        intField.set(text == null ? 0 : text.length());
    }

    public static class NestedObservableHolder {
        public ObservableInt observableInt = new ObservableInt(0);
        public ObservableField<Integer> observableField = new ObservableField<>(0);
    }
}
