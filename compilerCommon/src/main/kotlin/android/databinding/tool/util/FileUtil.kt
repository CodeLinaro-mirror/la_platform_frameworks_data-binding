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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File

object FileUtil {

    /**
     * Returns files in a directory, sorted by path.
     *
     * @see org.apache.commons.io.FileUtils.listFiles(java.io.File, org.apache.commons.io.filefilter.IOFileFilter, org.apache.commons.io.filefilter.IOFileFilter)
     */
    @JvmStatic
    fun listAndSortFiles(
        directory: File,
        fileFilter: IOFileFilter = TrueFileFilter.TRUE,
        dirFilter: IOFileFilter = TrueFileFilter.TRUE
    ): List<File> {
        return FileUtils.listFiles(directory, fileFilter, dirFilter).sortedBy { it.path }
    }

    /**
     * Returns files in a directory, sorted by path.
     *
     * @see org.apache.commons.io.FileUtils.listFiles(java.io.File, java.lang.String[], boolean)
     */
    @JvmStatic
    fun listAndSortFiles(
        directory: File, extensions: Array<String>, recursive: Boolean
    ): List<File> {
        return FileUtils.listFiles(directory, extensions, recursive).sortedBy { it.path }
    }
}