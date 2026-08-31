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

package android.databinding.tool.store

import android.databinding.tool.store.ResourceBundle.LayoutFileBundle
import java.io.ByteArrayInputStream
import java.io.File
import java.io.ObjectOutputStream
import java.net.URL
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class V1CompatLayoutInfoLoaderTest {
  @Rule @JvmField val tmpFolder = TemporaryFolder()

  @Test
  fun testLoadValidV1CompatLayoutInfo() {
    val folder = tmpFolder.newFolder("v1-artifacts")
    val file = File(folder, "test-layoutinfo.bin")

    val xml =
      """
      <?xml version="1.0" encoding="utf-8"?>
            <Layout layout="activity_main" modulePackage="com.example.app" filePath="layout/activity_main.xml" directory="layout" isMerge="false">
              <Variables name="user" type="com.example.app.User"/>
              <Targets/>
            </Layout>
      """
        .trimIndent()

    val intermediate = V1CompatLayoutInfoLoader.IntermediateV2Compat()
    intermediate.addEntry("activity_main", xml)

    file.outputStream().use { ObjectOutputStream(it).use { oos -> oos.writeObject(intermediate) } }

    val log = V1CompatLayoutInfoLoader().load(folder)
    assertThat(log.mappings().containsKey("activity_main"), `is`(true))
    assertThat(log.mappings()["activity_main"]?.modulePackage, `is`("com.example.app"))
    assertThat(log.mappings()["activity_main"]?.variables?.get("user"), `is`("com.example.app.User"))
  }

  @Test
  fun testRejectUnauthorizedClassDeserialization() {
    val folder = tmpFolder.newFolder("v1-unauthorized")
    val file = File(folder, "malicious-layoutinfo.bin")

    // java.net.URL is Serializable but NOT on the allowlist
    file.outputStream().use { ObjectOutputStream(it).use { oos -> oos.writeObject(URL("http://example.com")) } }

    // Loading should throw an exception because the object filter rejects java.net.URL
    assertThrows(Exception::class.java) {
      V1CompatLayoutInfoLoader().load(folder)
    }
  }

  @Test
  fun testFromXMLRejectsDTDAndExternalEntities() {
    val maliciousXml =
      """
      <?xml version="1.0" encoding="utf-8"?>
            <!DOCTYPE Layout [
              <!ENTITY xxe SYSTEM "http://127.0.0.1:9999/evil">
            ]>
            <Layout layout="&xxe;" modulePackage="com.example.app" filePath="layout/activity_main.xml" directory="layout" isMerge="false">
              <Targets/>
            </Layout>
      """
        .trimIndent()

    val isStream = ByteArrayInputStream(maliciousXml.toByteArray(Charsets.UTF_8))
    assertThrows(Exception::class.java) {
      LayoutFileBundle.fromXML(isStream)
    }
  }
}
