## Setting up
run the init.sh script in {src}/tools/data-binding. This will make the extensions and integration
tests runnable directly.

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

### Runtime Artifacts (extensions)
This project is compiled using android gradle plugin or bazel
to compile from gradle (under {src}/tools)

`./gradlew :dB:buildDataBindingRuntimeArtifacts`

to compile from bazel run

`bazel build //tools/data-binding:runtimeLibraries`

You can also compile them from command line if you've run init.sh
you must first compile the android gradle plugin:

```
cd {src}/tools && ./gradlew :pL
cd {src}/tools/data-binding && ./gradlew build
```

Due to the androidX change, we still keep the libraries in the old namespace.
They are under extensions-support so any change in extensions should be ported to
extensions-support.

## Running in the IDE
### Compile Time Code
The main project still has proper gradle configuration so you can open it in Intellij

### Runtime Libraries (a.k.a extensions)
First, compile the local gradle plugin and also make sure you've run init.gradle

`cd {src}/tools && ./gradlew :pL`

The run the init script:

`cd {src}/tools/data-binding && ./init.sh`

Now you can open extensions in Android Studio.

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

### generating online docs for runtime libs

`cd extensions && gradlew  :generateDocs -Pandroid.injected.invoked.from.ide=true --info -Ponline=true -PincludeDoclava`

// remove online parameter to get offline docs
// we pass invoked from ide to enable default setup
