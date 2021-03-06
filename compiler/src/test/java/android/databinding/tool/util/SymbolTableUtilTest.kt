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

package android.databinding.tool.util

import com.google.common.collect.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.IllegalStateException
import java.nio.file.Files

class SymbolTableUtilTest {

    @Test
    fun testParsingEmptyLocalFile() {
        val testFile = Files.createTempFile("local", "R.txt")
        Files.write(testFile, """""".toByteArray())

        var found = false
        try {
            parseLocalRTxt(testFile.toFile())
        } catch (exception: IllegalStateException) {
            found = true
            assertTrue(exception.message!!.contains("Incorrect package-aware R.txt format."))
        }
        assertTrue(found)
    }

    @Test
    fun testParsingCorrectLocalFile() {
        val testFile = Files.createTempFile("local", "R.txt")
        Files.write(
                testFile,
                """
                // This is a comment expected in package-aware R.txt
                local
                string first
                int second
                styleable parent child1 child2
                """.trimIndent().toByteArray())

        val result = parseLocalRTxt(testFile.toFile())

        assertEquals(result.rPackage, "")
        assertEquals(result.resources.keySet().size, 3)
        assertEquals(result.resources.values().size, 5)

        assertTrue(result.contains("string", "first"))
        assertTrue(result.contains("int", "second"))
        assertTrue(result.contains("styleable", "parent"))
        assertTrue(result.contains("styleable", "parent_child1"))
        assertTrue(result.contains("styleable", "parent_child2"))
    }

    @Test
    fun testParsingCorrectDependencyRTxt() {
        val testFile = Files.createTempFile("dependency", "R.txt")
        Files.write(
                testFile,
                """
                com.test.lib
                string first
                """.trimIndent().toByteArray())

        val result = parsePackageAwareRTxt(testFile.toFile())

        assertEquals(result.rPackage, "com.test.lib")
        assertEquals(result.resources.keySet().size, 1)
        assertEquals(result.resources.values().size, 1)
        assertTrue(result.contains("string", "first"))
    }

    @Test
    fun testParsingMultipleFiles() {
        val localFile = Files.createTempFile("local", "R.txt")
        Files.write(
                localFile,
                """
                // This is a comment expected in package-aware R.txt
                local
                string first
                """.trimIndent().toByteArray())

        val dependencyFile = Files.createTempFile("dependency", "R.txt")
        Files.write(
                dependencyFile,
                """
                com.test.lib
                string second
                """.trimIndent().toByteArray())

        val result = parseRTxtFiles(localFile.toFile(), ImmutableList.of(dependencyFile.toFile()))

        assertEquals(result.symbolTables!!.size, 2)
        assertEquals(result.symbolTables!![0].rPackage, "")
        assertEquals(result.symbolTables!![1].rPackage, "com.test.lib")

        assertEquals(result.getRPackagePrefix(null, "string", "first"), "")
        assertEquals(result.getRPackagePrefix(null, "string", "second"), "com.test.lib.")
        assertEquals(result.getRPackagePrefix("android", "string", "not_found"), "android.")
    }

    @Test
    fun testParsingNoFiles() {
        val result = parseRTxtFiles(null, null)
        assertEquals(result, EMPTY_RESOURCES)

        assertEquals(result.getRPackagePrefix(null, "string", "hello"), "")
    }

    @Test
    fun testIncorrectResources() {
        val testFile = Files.createTempFile("local", "R.txt")
        Files.write(testFile, "default string first".toByteArray())

        var found = false
        try {
            readResources(testFile.toFile().readLines().iterator())
        } catch (exception: IllegalStateException) {
            found = true
            assertEquals(exception.message, "Illegal line in R.txt: 'default string first'")
        }
        assertTrue(found)
    }

    @Test
    fun testEmptyResources() {
        val testFile = Files.createTempFile("local", "R.txt")
        Files.write(testFile, "".toByteArray())

        val result = readResources(testFile.toFile().readLines().iterator())
        assertTrue(result.isEmpty)
    }
}
