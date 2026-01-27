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
import java.nio.charset.Charset
import java.util.zip.ZipFile
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Suppress("MemberVisibilityCanPrivate")
@RunWith(JUnit4::class)
class GenerateBaseClassesTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun testBaseClassGeneration() {
    val out = tempFolder.root
    out.mkdirs()
    val outClassInfo = File(out, "classInfo.zip")
    val outSourceFile = File(out, "classes.zip")
    val options =
      GenerateBaseClassesOptions().apply {
        layoutInfoFolder = prepareInfoZip("base", BASE_PKG)
        dependencyClassInfoFolders = emptyList()
        packageName = BASE_PKG
        classInfoOut = outClassInfo
        sourceFileOut = outSourceFile
        zipSourceOutput = true
      }
    AndroidDataBinding.generateBaseClasses(options)
    assertThat(outClassInfo.exists(), `is`(true))
    assertThat(outSourceFile.exists(), `is`(true))
    assertThat(ZipFile(outClassInfo).hasFile("$BASE_PKG-binding_classes.json"), `is`(true))
    assertThat(ZipFile(outSourceFile).hasFile(BASE_CLASS), `is`(true))

    // now compile a dependency
    testDependant(outClassInfo)
  }

  private fun testDependant(classInfo: File) {
    val out = File(tempFolder.root, "step2")
    out.mkdirs()
    val outClassInfo = File(out, "classInfo.zip")
    val outSourceFile = File(out, "classes.zip")
    val options =
      GenerateBaseClassesOptions().apply {
        layoutInfoFolder = prepareInfoZip("dependant", DEPENDANT_PKG)
        dependencyClassInfoFolders = listOf(classInfo)
        packageName = DEPENDANT_PKG
        classInfoOut = outClassInfo
        sourceFileOut = outSourceFile
        zipSourceOutput = true
      }
    AndroidDataBinding.generateBaseClasses(options)
    assertThat(outClassInfo.exists(), `is`(true))
    assertThat(outSourceFile.exists(), `is`(true))
    val outClassInfoZip = ZipFile(outClassInfo)
    assertThat(outClassInfoZip.hasFile("$DEPENDANT_PKG-binding_classes.json"), `is`(true))
    assertThat(outClassInfoZip.hasFile("$BASE_PKG-binding_classes.json"), `is`(false))

    val sourceJar = ZipFile(outSourceFile)
    assertThat(sourceJar.hasFile(DEPENDANT_CLASS), `is`(true))
    assertThat(sourceJar.hasFile(BASE_CLASS), `is`(false))
    val code = sourceJar.entryInputStream(DEPENDANT_CLASS).reader(Charset.defaultCharset()).readText()
    assertThat(code, containsString("ActivityBinding included"))
  }

  private fun prepareInfoZip(inputCase: String, pkg: String): File {
    val out = File(tempFolder.root, inputCase)
    val resOut = File(out, "resOut")
    val infoOut = File(out, "infoOut")
    val options =
      ProcessXmlOptions().apply {
        appId = pkg
        resInput = File("src/test-data/$inputCase")
        resOutput = resOut
        layoutInfoOutput = infoOut
        setZipLayoutInfo(true)
      }
    AndroidDataBinding.doRun(options)
    val layoutInfoZip = File(infoOut, "layout-info.zip")
    assertThat("Test sanity", layoutInfoZip.exists(), `is`(true))
    return layoutInfoZip
  }

  companion object {
    val DEPENDANT_CLASS = "foo/baz/databinding/DependantBinding.java"
    val BASE_CLASS = "foo/bar/databinding/ActivityBinding.java"
    val DEPENDANT_PKG = "foo.baz"
    val BASE_PKG = "foo.bar"
  }
}
