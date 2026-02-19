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
package android.databinding.cli

import android.databinding.AndroidDataBinding
import java.io.File
import java.util.zip.ZipFile
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProcessXmlTest {
  @Suppress("MemberVisibilityCanPrivate") @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun processXml() {
    processXmlTest(false)
  }

  @Test
  fun processXml_zipOutput() {
    processXmlTest(true)
  }

  private fun processXmlTest(useZip: Boolean) {
    val out = tempFolder.root
    val resOut = File(out, "resOut")
    val infoOut = File(out, "infoOut")
    val options =
      ProcessXmlOptions().apply {
        appId = "foo.baz"
        resInput = File("src/test-data/base")
        resOutput = resOut
        layoutInfoOutput = infoOut
        setZipLayoutInfo(useZip)
      }
    AndroidDataBinding.doRun(options)
    assertThat(File(resOut, "layout/activity.xml").exists(), `is`(true))
    assertThat(File(infoOut, "activity-layout.xml").exists(), `is`(!useZip))
    val layoutInfoZip = File(infoOut, "layout-info.zip")
    assertThat(layoutInfoZip.exists(), `is`(useZip))
    if (useZip) {
      assertThat(ZipFile(layoutInfoZip).hasFile("activity-layout.xml"), `is`(true))
    }
  }
}
