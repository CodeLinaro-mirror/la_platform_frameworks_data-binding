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
package androidx.databinding.compilationTest

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

private val pattern: Pattern = Pattern.compile("!@\\{([A-Za-z0-9_-]*)}")

/**
 * Copies the resource to the target file.
 */
fun copyResourceToFile(name: String, targetFile: File) {
    val directory = targetFile.parentFile
    FileUtils.forceMkdir(directory)
    BaseCompilationTest::class.java.getResourceAsStream(name).use { input ->
        FileUtils.copyInputStreamToFile(input, targetFile)
    }
}

/**
 * Looks for special sentinel [pattern] in the resource and perform replacements. If replacement for
 * a key is not provided, then the sentinel will be replaced with blank string.
 *
 * Then copies the replaced resource to target.
 */
fun copyResourceWithReplacement(
    name: String,
    targetFile: File,
    replacements: Map<String, String> = emptyMap()
) {
    BaseCompilationTest::class.java.getResourceAsStream(name).use { inputStream ->
        val contents = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        val out = StringBuilder(contents.length)
        val matcher = pattern.matcher(contents)
        var location = 0
        while (matcher.find()) {
            val start = matcher.start()
            if (start > location) {
                out.append(contents, location, start)
            }
            val key = matcher.group(1)
            val replacement = replacements[key]
            if (replacement != null) {
                out.append(replacement)
            }
            location = matcher.end()
        }
        if (location < contents.length) {
            out.append(contents, location, contents.length)
        }
        targetFile.parentFile.mkdirs()
        targetFile.writeText(out.toString(), StandardCharsets.UTF_8)
    }
}
