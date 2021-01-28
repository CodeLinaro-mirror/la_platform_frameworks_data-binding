/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.databinding.compilationTest;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

import static org.junit.Assert.assertNotEquals;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
@RunWith(JUnit4.class)
public class InverseMethodTest extends DataBindingCompilationTestCase {
    @Test
    public void testInverseMethodWrongParameterType() throws Throwable {
        testErrorForMethod("InverseMethod_WrongParameterType", 23, "Could not find inverse " +
                "method: public static int wrongParameterType(java.lang.String)");
    }

    @Test
    public void testInverseMethodNoArg() throws Throwable {
        testErrorForMethod("InverseMethod_NoArg", 23, "@InverseMethods must have at least one " +
                "parameter.");
    }

    @Test
    public void testInverseMethodNoInverse() throws Throwable {
        testErrorForMethod("InverseMethod_NoInverse", 23, "@InverseMethod must supply a value " +
                "containing the name of the method to call when going from View value to bound " +
                "value");
    }

    @Test
    public void testInverseMethodNonPublic() throws Throwable {
        testErrorForMethod("InverseMethod_NonPublic", 23, "@InverseMethods must be associated " +
                "with a public method");
    }

    @Test
    public void testInverseMethodNonPublicInverse() throws Throwable {
        testErrorForMethod("InverseMethod_NonPublicInverse", 23, "InverseMethod must be declared " +
                "public 'notPublicIntToString(int)'");
    }

    private void testErrorForMethod(String className, int lineNumber, String expectedError)
            throws Throwable {
        loadApp();
        copyTestData(
                "androidx/databinding/compilationTests/badJava/" + className + ".java",
                "app/src/main/java/androidx/databinding/compilationTest/badJava/"
                + className
                + ".java");
        CompilationResult result = invokeTasks(Lists.newArrayList("assembleDebug"),
                                               Lists.newArrayList("--stacktrace"));
        assertNotEquals(0, result.resultCode);
        String error = getErrorLine(result.error);
        assertNotNull("Couldn't find error in \n" + result.error, error);
        File errorFile = new File(getProjectRoot(),
                                  "app/src/main/java/androidx/databinding/compilationTest/badJava/"
                                  +
                                  className
                                  + ".java");
        assertEquals(errorFile.getCanonicalPath() + ":" + lineNumber + ": error: " + expectedError,
                     error);
    }

    private static String getErrorLine(String err) {
        String[] lines = err.split("\n");
        for (String line : lines) {
            if (line.contains("error:")) {
                return line.trim();
            }
        }
        return null;
    }
}
