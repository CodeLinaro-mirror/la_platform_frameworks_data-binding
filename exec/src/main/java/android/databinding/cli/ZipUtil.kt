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

import android.databinding.tool.util.L
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipUtil {
  fun unzip(file: File, outFolder: File) {
    if (!outFolder.exists() && !outFolder.mkdirs()) {
      throw RuntimeException("unable to create out folder ${outFolder.absolutePath}")
    }
    val zipFile = ZipFile(file)
    zipFile.use {
      zipFile.entries().iterator().forEach { entry ->
        if (entry.isDirectory) {
          File(outFolder, entry.name).mkdirs()
        } else {
          zipFile.getInputStream(entry).use { File(outFolder, entry.name).writeBytes(zipFile.getInputStream(entry).readBytes(1000)) }
        }
      }
    }
  }

  fun zip(folder: File, outFile: File) {
    val inputAbsPath = folder.absolutePath.length
    val fos = FileOutputStream(outFile)
    fos.use {
      val zos = ZipOutputStream(fos)
      zos.use {
        FileUtil.listAndSortFiles(folder).forEach { file ->
          val entry = ZipEntry(file.absolutePath.substring(inputAbsPath + 1))
          try {
            zos.putNextEntry(entry)
            zos.write(file.readBytes())
            zos.closeEntry()
          } catch (t: Throwable) {
            L.e(t, "cannot write zip file. Filed on %s", file)
          }
        }
      }
    }
  }
}
