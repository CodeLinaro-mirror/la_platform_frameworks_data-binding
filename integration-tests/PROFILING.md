To profile the annotation processor in sample projects, you can use [honest profiler](https://github.com/jvm-profiling-tools/honest-profiler)
along with [FlameGraph](https://github.com/brendangregg/FlameGraph) tool which can generate interactive svgs.

After you install both of these, you can run a test app with profiler on for gradle:

e.g. for TestApp (assuming honest profiler is installed in /home/yboyar/app/honest-profiler):

```
gw clean assembleAndroidTest -Pandroid.injected.invoked.from.ide=true  --no-build-cache -Dorg.gradle.jvmargs="-agentpath:/home/yboyar/app/honest-profiler/liblagent.so=interval=7,logPath=/home/yboyar/app/honest-profiler/logs/test_app.hpl"
```
This will output a log into `logPath`.

Then, you should fold it:

```
cd /home/yboyar/app/honest-profiler;
java -cp honest-profiler.jar com.insightfullogic.honest_profiler.ports.console.FlameGraphDumperApplication logs/test_app.hpl logs/test_app.folded
```

Now it is ready for svg generation:

```
~/app/FlameGraph-master/flamegraph.pl logs/test_app.folded > graph.svg
```

This will export everything from gradle. You probably only want data binding related stuff so you can also do:

```
grep databinding logs/test_app.folded | ~/app/FlameGraph-master/flamegraph.pl > databinding.svg
```

Now you can open the svg in chrome.