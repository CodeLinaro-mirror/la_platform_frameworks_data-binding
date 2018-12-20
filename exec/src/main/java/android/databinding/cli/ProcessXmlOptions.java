/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.databinding.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;

/**
 * Command line arguments that can be passed into this executable
 */
public class ProcessXmlOptions {
    @Parameter(names = "-package", required = true, description =
            "The package name of the application."
                    + " This should be the same package that R file uses.")
    private String appId;
    @Parameter(names = "-resInput", required = true, converter = FileConverter.class,
            description =
                    "The folder which contains merged resources. It is the folder that contains the"
                            + " layout folder, drawable folder etc etc.")
    private File resInput;
    @Parameter(names = "-resOutput", required = true, converter = FileConverter.class,
            description = "The output folder which will contain processes resources. This should "
                    + "be the input for aapt.")
    private File resOutput;
    @Parameter(names = "-layoutInfoOutput", required = true, converter = FileConverter.class,
            description =
                    "The folder into which data binding should export the xml files that keep the"
                            + " data binding related information for the layout files.")
    private File layoutInfoOutput;

    @Parameter(names = "-zipLayoutInfo", required = false, arity = 1,
            description =
                    "Whether the generated layout-info files should be zipped into 1 or not. If "
                            + "set "
                            + "to true (default), DataBinding will generate 1 layouts.zip file in"
                            + " the given"
                            + " layout-info out folder.")
    private boolean zipLayoutInfo = false;

    /**
     * True if Data Binding should generate code that uses androidX.
     */
    @Parameter(names = "-useAndroidX",
            required = false,
            arity = 1,
            description = "Specifies whether data binding should use androidX packages or not")
    private boolean useAndroidX = false;

    public String getAppId() {
        return appId;
    }

    public File getResInput() {
        return resInput;
    }

    public File getResOutput() {
        return resOutput;
    }

    public File getLayoutInfoOutput() {
        return layoutInfoOutput;
    }

    public boolean shouldZipLayoutInfo() {
        return zipLayoutInfo;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setResInput(File resInput) {
        this.resInput = resInput;
    }

    public void setResOutput(File resOutput) {
        this.resOutput = resOutput;
    }

    public void setLayoutInfoOutput(File layoutInfoOutput) {
        this.layoutInfoOutput = layoutInfoOutput;
    }

    public boolean getUseAndroidX() {
        return useAndroidX;
    }

    public void setUseAndroidX(boolean useAndroidX) {
        this.useAndroidX = useAndroidX;
    }

    public void setZipLayoutInfo(boolean zipLayoutInfo) {
        this.zipLayoutInfo = zipLayoutInfo;
    }

    @Override
    public String toString() {
        return "ProcessXmlOptions{" +
                "appId='" + appId + '\'' +
                ", resInput=" + resInput +
                ", resOutput=" + resOutput +
                ", layoutInfoOutput=" + layoutInfoOutput +
                ", zipLayoutInfo=" + zipLayoutInfo +
                ", useAndroidX=" + useAndroidX +
                '}';
    }
}
