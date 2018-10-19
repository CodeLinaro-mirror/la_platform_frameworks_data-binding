This project is used to have a library that is compiled
with AGP 3.1.4 and V1. We don't compile it as part of tests
since the goal is to have a project compiled with an old
version using V1.

Currently, it is used to reproduce b/117666264 as in the
MultiModuleTestApp. If you wish to modify this, don't
forget to update the aar in MultiModuleTestApp/app/libs.

to update:
```
./gradlew assembleDebug && cp library/build/outputs/aar/library-debug.aar \
../MultiModuleTestApp/testlibrary1/libs/lib_with_layout_v1.aar
 ```