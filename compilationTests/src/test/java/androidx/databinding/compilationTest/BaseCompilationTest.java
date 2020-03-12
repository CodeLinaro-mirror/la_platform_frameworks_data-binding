/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.databinding.compilationTest;

import android.databinding.tool.processing.ScopedErrorReport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import android.databinding.tool.store.Location;
import android.databinding.tool.util.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.android.annotations.NonNull;


public class BaseCompilationTest {

    private static final String PRINT_ENCODED_ERRORS_PROPERTY
            = "android.injected.invoked.from.ide";
    private static final String ENABLE_V2_PROPERTY
            = "android.databinding.enableV2";
    private static final String USE_ANDROID_X_PROPERTY =
            "android.useAndroidX";
    @Rule
    public TestName name = new TestName();
    static Pattern VARIABLES = Pattern.compile("!@\\{([A-Za-z0-9_-]*)}");

    public static final String KEY_MANIFEST_PACKAGE = "PACKAGE";
    public static final String KEY_DEPENDENCIES = "DEPENDENCIES";
    public static final String KEY_SETTINGS_INCLUDES = "SETTINGS_INCLUDES";
    public static final String DEFAULT_APP_PACKAGE = "com.android.databinding.compilationTest.test";
    public static final String KEY_CLASS_NAME = "CLASSNAME";
    public static final String KEY_CLASS_TYPE = "CLASSTYPE";
    public static final String KEY_IMPORT_TYPE = "IMPORTTYPE";
    public static final String KEY_INCLUDE_ID = "INCLUDEID";
    public static final String KEY_VIEW_ID = "VIEWID";

    @Rule
    public TemporaryBuildFolder tmpBuildFolder =
            new TemporaryBuildFolder(new File("./build", "build-test"), false);

    File testFolder;

    protected void copyResourceTo(String name, String path) throws IOException {
        copyResourceTo(name, new File(testFolder, path));
    }

    protected void writeFile(String path, String contents) throws IOException {
        File targetFile = new File(testFolder, path);
        FileUtils.forceMkdir(targetFile.getParentFile());
        try(FileOutputStream fos = new FileOutputStream(targetFile)) {
            IOUtils.write(contents, fos);
        }
    }

    protected void copyResourceTo(String name, String path, Map<String, String> replacements)
            throws IOException {
        copyResourceTo(name, new File(testFolder, path), replacements);
    }

    protected void copyResourceDirectory(String name, String targetPath)
            throws URISyntaxException, IOException {
        URL dir = getClass().getResource(name);
        assertNotNull(dir);
        assertEquals("file", dir.getProtocol());
        File folder = new File(dir.toURI());
        assertTrue(folder.isDirectory());
        File target = new File(testFolder, targetPath);
        int len = folder.getAbsolutePath().length() + 1;
        for (File item : FileUtils.listFiles(folder, null, true)) {
            if (item.getAbsolutePath().equals(folder.getAbsolutePath())) {
                continue;
            }
            String resourcePath = item.getAbsolutePath().substring(len);

            copyResourceTo(name + "/" + resourcePath, new File(target, resourcePath));
        }
    }

    @Before
    public void clear() throws IOException {
        testFolder = tmpBuildFolder.getFolder();
    }

    /**
     * Extracts the text in the given location from the the at the given application path.
     *
     * @param pathInApp The path, relative to the root of the application under test
     * @param location  The location to extract
     * @return The string that is contained in the given location
     * @throws IOException If file is invalid.
     */
    protected String extract(String pathInApp, Location location) throws IOException {
        File file = new File(testFolder, pathInApp);
        assertTrue(file.exists());
        StringBuilder result = new StringBuilder();
        List<String> lines = FileUtils.readLines(file);
        for (int i = location.startLine; i <= location.endLine; i++) {
            if (i > location.startLine) {
                result.append("\n");
            }
            String line = lines.get(i);
            int start = 0;
            if (i == location.startLine) {
                start = location.startOffset;
            }
            int end = line.length() - 1; // inclusive
            if (i == location.endLine) {
                end = location.endOffset;
            }
            result.append(line.substring(start, end + 1));
        }
        return result.toString();
    }

    protected static void copyResourceTo(String name, File targetFile) throws IOException {
        File directory = targetFile.getParentFile();
        FileUtils.forceMkdir(directory);
        InputStream contents = BaseCompilationTest.class.getResourceAsStream(name);
        FileOutputStream fos = new FileOutputStream(targetFile);
        IOUtils.copy(contents, fos);
        IOUtils.closeQuietly(fos);
        IOUtils.closeQuietly(contents);
    }

    protected static Map<String, String> toMap(String... keysAndValues) {
        assertEquals(0, keysAndValues.length % 2);
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    protected void copyResourceTo(String name, File targetFile, Map<String, String> replacements)
            throws IOException {
        if (replacements.isEmpty()) {
            copyResourceTo(name, targetFile);
        }
        InputStream inputStream = getClass().getResourceAsStream(name);
        final String contents = IOUtils.toString(inputStream);
        IOUtils.closeQuietly(inputStream);

        StringBuilder out = new StringBuilder(contents.length());
        final Matcher matcher = VARIABLES.matcher(contents);
        int location = 0;
        while (matcher.find()) {
            int start = matcher.start();
            if (start > location) {
                out.append(contents, location, start);
            }
            final String key = matcher.group(1);
            final String replacement = replacements.get(key);
            if (replacement != null) {
                out.append(replacement);
            }
            location = matcher.end();
        }
        if (location < contents.length()) {
            out.append(contents, location, contents.length());
        }

        FileUtils.writeStringToFile(targetFile, out.toString());
    }

    protected void prepareProject() throws IOException, URISyntaxException {
        prepareApp(null);
    }

    private Map<String, String> addDefaults(Map<String, String> map) {
        if (map == null) {
            map = new HashMap<String, String>();
        }
        if (!map.containsKey(KEY_MANIFEST_PACKAGE)) {
            map.put(KEY_MANIFEST_PACKAGE, DEFAULT_APP_PACKAGE);
        }
        if (!map.containsKey(KEY_SETTINGS_INCLUDES)) {
            map.put(KEY_SETTINGS_INCLUDES, "include ':app'");
        }
        return map;
    }

    protected void prepareApp(Map<String, String> replacements) throws IOException,
            URISyntaxException {
        replacements = addDefaults(replacements);
        // how to get build folder, pass from gradle somehow ?
        FileUtils.forceMkdir(testFolder);
        copyResourceTo("/AndroidManifest.xml",
                new File(testFolder, "app/src/main/AndroidManifest.xml"), replacements);
        copyResourceTo("/project_build.gradle", new File(testFolder, "build.gradle"), replacements);
        copyResourceTo("/app_build.gradle", new File(testFolder, "app/build.gradle"), replacements);
        copyResourceTo("/settings.gradle", new File(testFolder, "settings.gradle"), replacements);
        copyGradle(testFolder);
    }

    private void copyCommonBuildScript(File checkoutRoot) throws IOException {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("CHECKOUT_ROOT", checkoutRoot.getAbsolutePath());
        copyResourceTo("/commonBuildScript.gradle",
                new File(testFolder.getParentFile(), "commonBuildScript.gradle"),
                replacements);
    }

    private void createLocalProperties(File checkoutRoot) throws IOException {
        File container = new File(checkoutRoot, "prebuilts/studio/sdk");
        File sdkFile;
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            sdkFile = new File(container, "darwin");
        } else {
            sdkFile = new File(container, "linux");
        }
        Properties properties = new Properties();
        properties.setProperty("sdk.dir", sdkFile.getAbsolutePath());
        try(FileOutputStream fos = FileUtils.openOutputStream(new File(testFolder, "local"
                + ".properties"))) {
            properties.store(fos, "");
        }
    }

    protected void prepareModule(String moduleName, String packageName,
            Map<String, String> replacements) throws IOException, URISyntaxException {
        replacements = addDefaults(replacements);
        replacements.put(KEY_MANIFEST_PACKAGE, packageName);
        File moduleFolder = new File(testFolder, moduleName);
        if (moduleFolder.exists()) {
            FileUtils.forceDelete(moduleFolder);
        }
        FileUtils.forceMkdir(moduleFolder);
        copyResourceTo("/AndroidManifest.xml",
                new File(moduleFolder, "src/main/AndroidManifest.xml"), replacements);
        copyResourceTo("/module_build.gradle", new File(moduleFolder, "build.gradle"),
                replacements);
    }

    protected CompilationResult runGradle(String... params)
            throws IOException, InterruptedException {
        File pathToExecutable = new File(testFolder, "gradlew");
        List<String> args = new ArrayList<String>();
        args.add(pathToExecutable.getAbsolutePath());
        args.add("-P" + PRINT_ENCODED_ERRORS_PROPERTY + "=true");;
        args.add("-P" + USE_ANDROID_X_PROPERTY + "=true");
        args.add("--no-daemon");
        if ("true".equals(System.getProperties().getProperty("useReleaseVersion", "false"))) {
            args.add("-PuseReleaseVersion=true");
        }
        if ("true".equals(System.getProperties().getProperty("addRemoteRepos", "false"))) {
            args.add("-PaddRemoteRepos=true");
        }
        Collections.addAll(args, params);
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.environment().putAll(System.getenv());
        String javaHome = System.getProperty("java.home");
        if (StringUtils.isNotBlank(javaHome)) {
            builder.environment().put("JAVA_HOME", javaHome);
        }
        builder.environment().put("ANDROID_SDK_HOME", new File(testFolder, "/android_sdk_home").getAbsolutePath());
        // to debug uncomment the next line
        // builder.environment().put("GRADLE_OPTS", "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006");
        builder.directory(testFolder);
        Process process = builder.start();
        String output = collect(process.getInputStream());
        String error = collect(process.getErrorStream());
        int result = process.waitFor();
        return new CompilationResult(result, output, error);
    }

    private void copyGradle(File outFolder) throws IOException {
        File toolsDir = findToolsDir();
        copyCommonBuildScript(toolsDir.getParentFile());
        createLocalProperties(toolsDir.getParentFile());
        File gradleDir = new File(toolsDir, "external/gradle");
        File propsFile = new File(toolsDir, "gradle/wrapper/gradle-wrapper.properties");
        Properties properties = new Properties();

        try (FileInputStream propStream = FileUtils.openInputStream(propsFile)) {
            properties.load(propStream);
        }
        String distributionUrl = properties.getProperty("distributionUrl");
        String[] sections = distributionUrl.split("/");
        String version = sections[sections.length - 1];
        File distroFile = new File(gradleDir, version);
        Preconditions.check(distroFile.exists(), "cannot find gradle distro");
        properties.setProperty("distributionUrl", distroFile.getCanonicalFile().toURI().toString());
        FileUtils.copyDirectory(new File(toolsDir, "gradle"), new File(outFolder, "gradle"),
                new IOFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !file.getName().endsWith(".git");
                    }

                    @Override
                    public boolean accept(File file, String s) {
                        return !s.endsWith(".git");
                    }
                });
        try (FileOutputStream propsOutStream = FileUtils.openOutputStream(
                new File(outFolder, "gradle/wrapper/gradle-wrapper"
                        + ".properties"))) {
            properties.store(propsOutStream, "");
        }
        File gradlew = new File(outFolder, "gradlew");
        FileUtils.copyFile(new File(toolsDir, "gradlew"), gradlew);
        gradlew.setExecutable(true);
    }

    @NonNull
    private File findToolsDir() throws IOException {
        File toolsFolder = new File(".").getCanonicalFile();
        while (toolsFolder.exists()) {
            File dataBinding = new File(toolsFolder, "data-binding");
            File base = new File(toolsFolder, "base");
            if (dataBinding.exists() && dataBinding.isDirectory() &&
                    base.exists() && base.isDirectory()) {
                break;
            } else {
                if (toolsFolder.getParentFile() == null ||
                        toolsFolder.getParentFile().equals(toolsFolder)) {
                    throw new IllegalStateException("Cannot find tools folder");
                }
                toolsFolder = toolsFolder.getParentFile();
            }
        }
        return toolsFolder;
    }

    /**
     * Finds the error file referenced in the given error report.
     * Handles possibly relative paths.
     *
     * Throws an assertion exception if the error file reported cannot be found.
     */
    @NonNull
    File requireErrorFile(ScopedErrorReport report) {
        String path = report.getFilePath();
        assertNotNull(path);
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        // might be relative, try in test project folder
        file = new File(testFolder, path);
        assertTrue("required error file is missing in " + file.getAbsolutePath(), file.exists());
        return file;
    }

    /**
     * Use this instead of IO utils so that we can easily log the output when necessary
     */
    private static String collect(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
