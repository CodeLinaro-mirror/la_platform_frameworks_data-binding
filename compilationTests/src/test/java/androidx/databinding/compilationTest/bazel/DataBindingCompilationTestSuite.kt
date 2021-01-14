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
package androidx.databinding.compilationTest.bazel

import androidx.databinding.compilationTest.AppCompatResourcesTest
import androidx.databinding.compilationTest.InverseMethodTest
import androidx.databinding.compilationTest.MultiLayoutVerificationTest
import androidx.databinding.compilationTest.NonEnglishLocaleTest
import androidx.databinding.compilationTest.ObservableGetDetectionTest
import androidx.databinding.compilationTest.RecursiveObservableTest
import androidx.databinding.compilationTest.SimpleCompilationTest
import androidx.databinding.compilationTest.TooManyLayoutsTest
import com.android.testutils.JarTestSuiteRunner
import com.android.tools.tests.IdeaTestSuiteBase
import org.junit.runner.RunWith

@RunWith(JarTestSuiteRunner::class)
@JarTestSuiteRunner.ExcludeClasses(
    DataBindingCompilationTestSuite::class,
    //TODO(b/173134729): remove the tests below when they are migrated to bazel.
    AppCompatResourcesTest::class,
    InverseMethodTest::class,
    MultiLayoutVerificationTest::class,
    NonEnglishLocaleTest::class,
    ObservableGetDetectionTest::class,
    RecursiveObservableTest::class,
    SimpleCompilationTest::class,
    TooManyLayoutsTest::class
)
class DataBindingCompilationTestSuite: IdeaTestSuiteBase() {
    companion object {
        init {
            unzipIntoOfflineMavenRepo("tools/adt/idea/android/test_deps.zip")
            unzipIntoOfflineMavenRepo("tools/base/build-system/studio_repo.zip")
            unzipIntoOfflineMavenRepo("tools/data-binding/data_binding_runtime.zip")
        }
    }
}
