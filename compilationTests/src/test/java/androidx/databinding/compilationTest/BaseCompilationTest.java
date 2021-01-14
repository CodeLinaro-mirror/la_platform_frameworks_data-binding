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
import android.databinding.tool.store.Location;
import android.databinding.tool.util.Preconditions;
import com.android.annotations.NonNull;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import kotlin.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static androidx.databinding.compilationTest.DataBindingCompilationTestUtilsKt.copyResourceToFile;
import static androidx.databinding.compilationTest.DataBindingCompilationTestUtilsKt.copyResourceWithReplacement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class BaseCompilationTest {

    private static final String PRINT_ENCODED_ERRORS_PROPERTY
            = "android.injected.invoked.from.ide";
    private static final String ENABLE_V2_PROPERTY
            = "android.databinding.enableV2";
    private static final String USE_ANDROID_X_PROPERTY =
            "android.useAndroidX";
    @Rule
    public TestName name = new TestName();

    public static final String KEY_MANIFEST_PACKAGE = "PACKAGE";
    public static final String KEY_DEPENDENCIES = "DEPENDENCIES";
    public static final String KEY_SETTINGS_INCLUDES = "SETTINGS_INCLUDES";
    public static final String DEFAULT_APP_PACKAGE = "com.android.databinding.compilationTest.test";
    public static final String KEY_CLASS_NAME = "CLASSNAME";
    public static final String KEY_CLASS_TYPE = "CLASSTYPE";
    public static final String KEY_IMPORT_TYPE = "IMPORTTYPE";
    public static final String KEY_INCLUDE_ID = "INCLUDEID";
    public static final String KEY_VIEW_ID = "VIEWID";

    /**
     * Maximum time allowed when reading the output stream, error stream, and waiting for
     * completion of the launched Gradle build process.
     */
    @NonNull private static final Duration GRADLE_BUILD_TIMEOUT = Duration.ofMinutes(5);

    @Rule
    public TemporaryBuildFolder tmpBuildFolder =
            new TemporaryBuildFolder(new File("./build", "build-test"), false);

    File testFolder;

    protected void copyResourceTo(String name, String path) throws IOException {
        copyResourceToFile(name, new File(testFolder, path));
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
        copyResourceWithReplacement(name, new File(testFolder, path), replacements);
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

    protected static Map<String, String> toMap(String... keysAndValues) {
        assertEquals(0, keysAndValues.length % 2);
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
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
        copyResourceWithReplacement("/AndroidManifest.xml",
                new File(testFolder, "app/src/main/AndroidManifest.xml"), replacements);
        copyResourceWithReplacement("/project_build.gradle",
                                    new File(testFolder, "build.gradle"), replacements);
        copyResourceWithReplacement("/app_build.gradle",
                                    new File(testFolder, "app/build.gradle"), replacements);
        copyResourceWithReplacement("/settings.gradle",
                                    new File(testFolder, "settings.gradle"), replacements);
        copyGradle(testFolder);
    }

    private void copyCommonBuildScript(File checkoutRoot) throws IOException {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("CHECKOUT_ROOT", checkoutRoot.getAbsolutePath());
        copyResourceWithReplacement("/commonBuildScript.gradle",
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
        copyResourceWithReplacement("/AndroidManifest.xml",
                new File(moduleFolder, "src/main/AndroidManifest.xml"), replacements);
        copyResourceWithReplacement("/module_build.gradle", new File(moduleFolder, "build.gradle"),
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
        // Temporarily limit the number of workers to reduce the chance that the Gradle build
        // process hangs (bug 148496072).
        // TODO(148496072): Remove this once the bug is fixed.
        args.add("--max-workers=4");
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
        Pair<Boolean, String> outputInfo =
            readInputStreamWithTimeout(process.getInputStream(), GRADLE_BUILD_TIMEOUT);
        Pair<Boolean, String> errorInfo =
            readInputStreamWithTimeout(process.getErrorStream(), GRADLE_BUILD_TIMEOUT);
        boolean executionInfo =
            process.waitFor(GRADLE_BUILD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        ensureProcessCompleted(process, outputInfo, errorInfo, executionInfo, GRADLE_BUILD_TIMEOUT);
        return new CompilationResult(
            process.exitValue(), outputInfo.getSecond(), errorInfo.getSecond());
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
     * Checks whether the given process has completed.
     *
     * If the process timed out, this method will
     *   - destroy the process (see bug 148598093)
     *   - throw a runtime exception containing the output and error logs from the process so far
     *     for debugging (see bug 148496072).
     *
     * @param process the process to check
     * @param outputInfo the result of reading the process's output (see readInputStreamWithTimeout)
     * @param errorInfo the result of reading the process's error (see readInputStreamWithTimeout)
     * @param executionInfo the result of calling Process.waitFor() on the process
     * @param timeout the timeout used when obtaining outputInfo, errorInfo, and executionInfo
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private void ensureProcessCompleted(
            @NonNull Process process,
            @NonNull Pair<Boolean, String> outputInfo,
            @NonNull Pair<Boolean, String> errorInfo,
            boolean executionInfo,
            @SuppressWarnings("SameParameterValue") @NonNull Duration timeout) {
        boolean processCompleted = outputInfo.getFirst() && errorInfo.getFirst() && executionInfo;
        if (!processCompleted) {
            process.destroyForcibly();
            StringBuilder message = new StringBuilder();
            if (!outputInfo.getFirst()) {
                message.append("Timeout (" + timeout.getSeconds() + "s) when reading output logs.\n");
            }
            if (!errorInfo.getFirst()) {
                message.append("Timeout (" + timeout.getSeconds() + "s) when reading error logs.\n");
            }
            if (!executionInfo) {
                message.append("Timeout (" + timeout.getSeconds() + "s) when waiting for process to complete.\n");
            }
            if (outputInfo.getSecond().isEmpty()) {
                message.append("Output logs: (empty)\n");
            } else {
                message.append("Output logs:\n" + outputInfo.getSecond() + "\n");
            }
            if (errorInfo.getSecond().isEmpty()) {
                message.append("Error logs: (empty)\n");
            } else {
                message.append("Error logs:\n" + errorInfo.getSecond() + "\n");
            }
            throw new RuntimeException(message.toString());
        }
    }

    /**
     * Reads the input stream and stores the result in the given StringBuilder. This is so that if
     * this method times out, the caller may still be able to get the result so far, see
     * readInputStreamWithTimeout method.
     */
    private static void readInputStream(
            @NonNull InputStream inputStream, @NonNull StringBuilder result) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Synchronize result as it may be read from another thread, see
                // readInputStreamWithTimeout method
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (result) {
                    result.append(line).append("\n");
                }
            }
        }
    }

    /**
     * Reads the input stream with a timeout.
     *
     * @return a pair of value. The first value is `true` if reading was successful (did not time
     *     out). The second value is what has been read so far (could be incomplete if it timed
     *     out).
     */
    @NonNull
    private static Pair<Boolean, String> readInputStreamWithTimeout(
            @NonNull InputStream inputStream,
            @SuppressWarnings("SameParameterValue") @NonNull Duration timeout) {
        StringBuilder result = new StringBuilder();
        Callable<Void> callable = () -> {
            readInputStream(inputStream, result);
            return null;
        };
        @SuppressWarnings("UnstableApiUsage")
        TimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());
        try {
            timeLimiter.callWithTimeout(callable, timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // The launched thread that does the actual reading of the input stream should have been
            // cancelled with InterruptedException by the TimeLimiter, so no need to clean it up
            // here.
            // Synchronize result as the launched thread may still be running for a bit longer and
            // writing to the result after this thread sent the cancellation signal (see
            // readInputStream method).
            synchronized (result) {
                return new Pair<>(false, result.toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        // Synchronize result, see above comment
        synchronized (result) {
            return new Pair<>(true, result.toString());
        }
    }
}
