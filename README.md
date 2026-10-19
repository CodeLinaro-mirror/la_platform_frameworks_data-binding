## Setting up
run the init.sh script in {src}/tools/data-binding. This will make the integration tests runnable
directly.

`cd {src}/tools/data-binding && ./init.sh`

To match the sdk used by android gradle plugin, this script sets the local properties to use the
checked in sdk. This might conflict with your local sdk or Android Studio setup. Unfortunately,
Android Studio does not support multiple SDKs so you need to live with that. (and your local sdk
might just work fine if tools did not upgrade to an unreleased
version. YMMV)

## Building Artifacts
### Compile Time Artifacts
They are compiled as part of the Android Gradle Plugin when they are shipped.
Under {src}/tools, you can run tasks for

`./gradlew :dB:<TASK_NAME>`

It also works independently, so you can just run

`cd {src}/data-binding && ./gradlew :dB:comp:test`

It also compiles with BAZEL :). It will take ages when you run it for the first time, go for a
coffee. Then it will be faster and more reliable than gradle.

`bazel build //tools/data-binding/...`

### Runtime Artifacts
The runtime libraries (e.g. androidx.databinding:databinding-runtime) are not built from source.
AGP adds the version in DATA_BINDING_RUNTIME_VERSION (in runtime_version.bzl), the latest stable
version on maven.google.com, to projects that use data binding or view binding. Bazel takes them
from the prebuilts in {src}/prebuilts/tools/common/m2/repository, and

`bazel build //tools/data-binding:runtimeLibraries`

packages them as a Maven repository for tests.

To update them, add the new version to the prebuilts, then update DATA_BINDING_RUNTIME_VERSION in
runtime_version.bzl and its copy in the integration test framework (TestVersions.kt in
{src}/tools/base/build-system/integration-test/framework).

Their sources are kept in extensions/ (androidx.databinding) and extensions-support/ (the old
com.android.databinding namespace) for reference only. Nothing builds them anymore.

## Running in the IDE
### Compile Time Code
The main project still has proper gradle configuration so you can open it in Intellij

## Running Android Gradle Plugin Data Binding Tests
Some of data binding tests are only in AGP. To run them:

`./gradlew :base:build-system:integration-test:databinding:te`
or filter them like:
`./gradlew :base:build-system:integration-test:databinding:te --tests DataBindingWithDaggerTest`

You can run data binding unit tests via:
`bazel test //tools/data-binding/...`


### Running Integration Tests
These are run by gradle build.

`./gradlew :base:build-system:integration-test:application:cIT -D:base:build-system:integration-test:application:connectedIntegrationTest.single=DataBinding\*`
We also compile them in bazel builds:

`bazel test //tools/base/build-system/integration-test/databinding:tests --test_output=errors
--action_env="GTEST_COLOR=1"`

You can pass `--test_filter=<test class name>\*` to filter certain tests

If you did run `./init.sh`, you can open integration tests in Android Studio.

Due to the AndroidX change, we still keep old integration tests that use the android.databinding
package.
They live in integration-tests-support folder and are an exact copy of what we had at the time of
the migration.

### Running Compilation Tests
The entire compilationTests module can be developed and run from intellij (tools/adt/idea).

To run the tests in bazel:

`bazel test --config=remote -- //tools/data-binding/compilationTests:studio.compilationTests_tests`

### Making Build File Changes
There are multiple ways data binding is compiled so build file changes are a bit tricky.

If you add a new dependency, you should update:
   {src}/tools/data-binding/BUILD.bazel
   compiler/db-compiler.iml and compilerCommon/db-compilerCommon.iml

Manually editing them and then running bazel to test is the most reliable approach. If you break it,
presubmit will catch. Bazel uses these iml files while compiling. You may need to modify the .idea
folder inside {src}/tools if your dependency does not already exist for some other project.

After changing the iml files, you should run `bazel run //tools/base/bazel:iml_to_build` to
re-generate the related bazel files. (if you forget, presubmit will probably catch it)

If you add a new integration test app, update
{src}/tools/base/build-system/integration-test/src/test/java/com/android/build/gradle/integration/databinding/DataBindingIntegrationTestAppsTest.java to include it.


## Misc

### working on compiler
If you are working on compiler but testing via integration tests, run:
`./gradlew :publishAndroidGradleLocal //(in tools/base)`
then run your integration test.

### debugging the compiler

The easiest way to debug the compiler is to run it from the command line and
attach remotely.

First, in IntelliJ, prepare a [Remote configuration](https://www.jetbrains.com/help/idea/run-debug-configuration-remote-debug.html)
if you don't already have one. You can use the default options.

Next, kill some Daemons to make sure that new ones we start up will definitely
be using our new settings.

`jps | egrep "(Gradle|KotlinCompile)Daemon"| awk '{print $1}'| xargs kill -9`

Next, build and run your app via the command-line. Note we add an `invoked.from.ide`
property even though we aren't using an IDE. This prevents the build scripts from
trying to load some files that may not exist without additional configuration.

If you are running an app that only has Java in it, the following should work,
e.g. in *tools/data-binding/integration-tests/AppWithDataBindingInTests*:

```
./gradlew clean assembleDebug -Pandroid.injected.invoked.from.ide=true \
--no-daemon -Dorg.gradle.debug=true
```

If the app you're compiling has Kotlin in it, you may need to use the following
arguments instead, e.g. in
*tools/data-binding/integration-tests/KotlinTestApp*:

```
./gradlew clean assembleDebug -Pandroid.injected.invoked.from.ide=true \
-Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=y"
```

Finally, at some point, while the app is building, it will look like Gradle got
stuck. Unfortunately, there's no text to tell you when the debugger is waiting, but
this is when you should hit the debug button for your Remote configuration. If it
connects, then everything worked! At this point, you should start hitting 
breakpoints in databinding code.  

### all gradle tests at once
```
./gradlew :base:build-system:integration-test:databinding:test :base:build-system:integration-test:application:cIT -D:base:build-system:integration-test:application:connectedIntegrationTest.single=DataBinding\*
```
