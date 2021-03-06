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
@file:JvmName("SymbolTableUtil")
package android.databinding.tool.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMultimap
import java.io.File

private const val STYLEABLE: String = "styleable"
val EMPTY_RESOURCES: Resources = Resources(null)

data class Resources(val symbolTables: ImmutableList<SymbolTable>?) {

    /**
     * Returns the prefix to be used in R class references in Java/Kotlin. For example: "android."
     * for "android.R.color.white", "com.my.lib." for "com.my.lib.R.string.text" and an empty string
     * for default R class reference e.g. "R.attr.my_attr".
     */
    fun getRPackagePrefix(packageName: String?, type: String, name: String): String {
        when {
            "android" == packageName -> {
                return "android."
            }
            symbolTables != null -> {
                for (table in symbolTables) {
                    if (table.contains(type, name)) {
                        // For local module references, return empty prefix as the R class will use
                        // the same local package.
                        if (table.rPackage.isEmpty()) return ""
                        // For any non-local references, use the full package plus a "." for the
                        // prefix.
                        return "${table.rPackage}."
                    }
                }
                throw RuntimeException(
                        "Unexpected error: Resource not found: $type $name. Please file a bug at " +
                        "https://b.corp.google.com/issues/new?component=192708&template=840533")
            }
            else -> {
                // If we don't have a list of resources, it means the local R class contains all
                // resources, both local and from dependencies, so just use the local R class.
                return  ""
            }
        }
    }
}


data class SymbolTable constructor(
        val rPackage: String,
        val resources: ImmutableMultimap<String, String>) {

    fun contains(type: String, name: String) : Boolean {
        return resources[type].orEmpty().contains(name)
    }
}

fun parseRTxtFiles(localRFile: File?, dependenciesRFiles: List<File>?) : Resources {
    // If not using non-transitive R, return empty Resources (only local R class should be used)
    if (localRFile == null || dependenciesRFiles == null) return EMPTY_RESOURCES

    val symbolTables = ImmutableList.builder<SymbolTable>()
    // local resources at the front of the list
    symbolTables.add(parseLocalRTxt(localRFile))
    // then add the rest of the dependencies, in order
    dependenciesRFiles.forEach { symbolTables.add(parsePackageAwareRTxt(it)) }

    return Resources(symbolTables.build())
}

fun parseLocalRTxt(file: File): SymbolTable {
    file.useLines {
        val iterator: Iterator<String> = it.iterator()
        // First line is a comment
        if (!iterator.hasNext())
            error("Incorrect package-aware R.txt format. " +
                    "Failed to parse file: ${file.absolutePath}")
        iterator.next()
        // Second line is local package we can ignore
        if (!iterator.hasNext())
            error("Resource list needs to contain the local package. " +
                    "Failed to parse file: ${file.absolutePath}")
        val localPackage = iterator.next()
        if (localPackage != "local")
            error("Illegal local package '$localPackage' in file ${file.absolutePath}")
        // Finally we can start parsing the resources
        val resources = try {
            readResources(iterator)
        } catch (e: java.lang.IllegalStateException) {
            throw IllegalStateException("Failed to parse file: ${file.absolutePath}", e)
        }
        // Local table should use a blank package to use default R class
        return SymbolTable("", resources)
    }
}

fun parsePackageAwareRTxt(file: File) : SymbolTable {
    // R.txt is verified before being written, no need to re-verify package or resource names.
    file.useLines {
        val iterator: Iterator<String> = it.iterator()
        // First line contains the package
        if (!iterator.hasNext())
            error("Resource list needs to contain the local package. " +
                    "Failed to parse file: ${file.absolutePath}")
        val pckg: String =  iterator.next()
        val resources = try {
            readResources(iterator)
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Failed to parse file: ${file.absolutePath}", e)
        }
        return SymbolTable(pckg, resources)
    }
}

fun readResources(lines: Iterator<String>) : ImmutableMultimap<String, String> {
    val resources = ImmutableMultimap.builder<String, String>()
    while (lines.hasNext()) {
        val line = lines.next()
        val chunks = line.split(" ")
        if (chunks.size < 2 || (chunks[0] != STYLEABLE && chunks.size != 2))
            error("Illegal line in R.txt: '$line'")
        resources.put(chunks[0], sanitizeName(chunks[1]))
        if (chunks[0] == STYLEABLE) {
            val parent = sanitizeName(chunks[1])
            for (i in 2 until chunks.size) {
                // Styleable children exist in the R class as R.styleable.parent_child.
                resources.put(STYLEABLE, "${parent}_${sanitizeName(chunks[i])}")
            }
        }
    }
    return resources.build()
}

private fun sanitizeName(name: String): String {
    return name.replace('.', '_').replace(':', '_')
}
