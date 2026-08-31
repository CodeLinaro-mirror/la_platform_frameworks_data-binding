/*
 * Copyright (C) 2026 The Android Open Source Project
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

package android.databinding.annotationprocessor

import android.databinding.tool.util.GenerationalClassUtil
import android.databinding.tool.util.LoggedErrorException
import java.io.File
import java.io.ObjectOutputStream
import java.net.URL
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GenerationalClassUtilTest {
  @Rule @JvmField val tmpFolder = TemporaryFolder()

  @Test
  fun testWriteAndLoadLayoutIntermediate() {
    val inputDir = tmpFolder.newFolder("input")
    val outputDir = tmpFolder.newFolder("output")

    val util = GenerationalClassUtil(inputDir = inputDir, outputDir = outputDir)

    val intermediate = ProcessExpressions.IntermediateV2()
    intermediate.addEntry("main.xml", "<layout/>")
    util.write("com.example.pkg", GenerationalClassUtil.ExtensionFilter.LAYOUT, intermediate)

    val writtenFile = File(outputDir, "com.example.pkg-layoutinfo.bin")
    assertThat(writtenFile.exists(), `is`(true))
    writtenFile.copyTo(File(inputDir, writtenFile.name))

    val loaded = util.load(GenerationalClassUtil.ExtensionFilter.LAYOUT, ProcessExpressions.Intermediate::class.java)
    assertThat(loaded.size, `is`(1))
  }

  @Test
  fun testWriteAndLoadBRIntermediate() {
    val inputDir = tmpFolder.newFolder("input")
    val outputDir = tmpFolder.newFolder("output")

    val util = GenerationalClassUtil(inputDir = inputDir, outputDir = outputDir)

    val intermediate = ProcessBindable.IntermediateV1("com.example.pkg")
    intermediate.addProperty("com.example.pkg.User", "firstName")
    util.write("com.example.pkg", GenerationalClassUtil.ExtensionFilter.BR, intermediate)

    val writtenFile = File(outputDir, "com.example.pkg-br.bin")
    assertThat(writtenFile.exists(), `is`(true))
    writtenFile.copyTo(File(inputDir, writtenFile.name))

    val loaded = util.load(GenerationalClassUtil.ExtensionFilter.BR, ProcessBindable.Intermediate::class.java)
    assertThat(loaded.size, `is`(1))
    assertThat(loaded[0].`package`, `is`("com.example.pkg"))
  }

  @Test
  fun testRejectUnauthorizedClassDeserialization() {
    val inputDir = tmpFolder.newFolder("input")
    val maliciousFile = File(inputDir, "malicious-br.bin")

    // java.net.URL is Serializable but NOT in the filter allowlist
    maliciousFile.outputStream().use { ObjectOutputStream(it).use { oos -> oos.writeObject(URL("http://example.com")) } }

    val util = GenerationalClassUtil(inputDir = inputDir, outputDir = null)
    // Deserialization filter rejects java.net.URL, so deserialization fails and L.e logs error (throwing LoggedErrorException in tests)
    assertThrows(LoggedErrorException::class.java) {
      util.load(GenerationalClassUtil.ExtensionFilter.BR, ProcessExpressions.Intermediate::class.java)
    }
  }
}
