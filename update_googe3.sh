#!/bin/bash
# this script uploads binaries to google3.
# if you are not running this in a workstation, you need to mount your workspace into a local folder
#    see: sshfs
# sample invocation ./update_googe3.sh 3.2.0-dev /tmp/my_g3_workspace v3_2_0
VERSION=$1 # 3.1.0-dev
GOOGLE3=$2 # e.g. /tmp/my_g3_workspace
REMOTE_VERSION=$3 # e.g. v3_1_0
if [ ! -e 'integration-tests' ] || [ ! -e 'extensions' ]; then
  echo "must run this under {src}/tools/data-binding"
  exit 1
fi

echo "copying version $VERSION to $GOOGLE3 under version $REMOTE_VERSION"
TMP_JAR_DIR='tmp_jar'
TMP_LIB_DIR='tmp_lib'
TMP_ADAPTERS_DIR='tmp_adapters'
TMP_X_LIB_DIR='tmp_x_lib'
TMP_X_ADAPTERS_DIR='tmp_x_adapters'

CUR_DIR=$PWD;
REPO_DIR="$CUR_DIR/../../out/repo"

# compile
echo "compiling base"
cd $CUR_DIR/../base
gw :pL :makeOfflineRepo --stacktrace

cd $CUR_DIR
create_target_dir() {
  echo "creating target dir $1"
  rm -rf $1
  mkdir $1
}

copy_jar() {
  TARGET=$1
  ARTIFACT_NAME=$2
  NEW_NAME=$3
  cp "$REPO_DIR/com/android/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION.jar" "$TARGET/$NEW_NAME.jar"
  cp "$REPO_DIR/com/android/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION-sources.jar" "$TARGET/$NEW_NAME-src.jar"
}

copy_aar() {
  TARGET=$1
  ARTIFACT_NAME=$2
  NEW_NAME=$3
  cp "$REPO_DIR/com/android/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION.aar" "$TARGET/$NEW_NAME.aar"
  cp "$REPO_DIR/com/android/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION-sources.jar" "$TARGET/$NEW_NAME-src.jar"
}

copy_x_jar() {
  TARGET=$1
  ARTIFACT_NAME=$2
  NEW_NAME=$3
  cp "$REPO_DIR/androidx/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION.jar" "$TARGET/$NEW_NAME.jar"
  cp "$REPO_DIR/androidx/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION-sources.jar" "$TARGET/$NEW_NAME-src.jar"
}

copy_x_aar() {
  TARGET=$1
  ARTIFACT_NAME=$2
  NEW_NAME=$3
  cp "$REPO_DIR/androidx/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION.aar" "$TARGET/$NEW_NAME.aar"
  cp "$REPO_DIR/androidx/databinding/$ARTIFACT_NAME/$VERSION/$ARTIFACT_NAME-$VERSION-sources.jar" "$TARGET/$NEW_NAME-src.jar"
}

remove_restrict_to() {
  sed -i '/RestrictTo/d' $1
}

# copy jars
create_target_dir $TMP_JAR_DIR
copy_x_jar $TMP_JAR_DIR "databinding-compiler" "compiler"
copy_x_jar $TMP_JAR_DIR "databinding-compiler-common" "compiler_common"
copy_x_jar $TMP_JAR_DIR "databinding-common" "x_common"
copy_jar $TMP_JAR_DIR "baseLibrary" "base"

./gradlew :dB:exec:jar :dB:exec:sourceJar
cp "exec/build/libs/exec-$VERSION.jar" "$TMP_JAR_DIR/exec.jar"
cp "exec/build/libs/exec-$VERSION-sources.jar" "$TMP_JAR_DIR/exec-src.jar"

mkdir -p "$GOOGLE3/third_party/java/android_databinding/$REMOTE_VERSION/."
cp $TMP_JAR_DIR/* "$GOOGLE3/third_party/java/android_databinding/$REMOTE_VERSION/."
# copy library
create_target_dir $TMP_LIB_DIR
copy_aar $TMP_LIB_DIR "library" "library"
cd $TMP_LIB_DIR
unzip library.aar
mv classes.jar "databinding-library.jar"
LIB_REMOTE_LOC=$GOOGLE3/third_party/java/android/android_sdk_linux/extras/android/compatibility/databinding/library/$REMOTE_VERSION/.
mkdir -p $LIB_REMOTE_LOC
cp -r databinding-library.jar AndroidManifest.xml res proguard.txt $LIB_REMOTE_LOC/.
cd $CUR_DIR

# copy X library
create_target_dir $TMP_X_LIB_DIR
copy_x_aar $TMP_X_LIB_DIR "databinding-runtime" "runtime"
cd $TMP_X_LIB_DIR
unzip runtime.aar
mv classes.jar "databinding-runtime.jar"
X_LIB_REMOTE_LOC=$GOOGLE3/third_party/java/androidx/databinding/runtime/$REMOTE_VERSION/.
mkdir -p $X_LIB_REMOTE_LOC
cp -r databinding-runtime.jar AndroidManifest.xml res proguard.txt $X_LIB_REMOTE_LOC
cd $CUR_DIR


# copy baseAdapters source. RestrictTo annotations becomes a problem in Google3 so just strip them.
create_target_dir $TMP_ADAPTERS_DIR
cp -r extensions-support/baseAdapters/src $TMP_ADAPTERS_DIR;

for file in `find $TMP_ADAPTERS_DIR -name "*.java"`
do
    remove_restrict_to "$file"
done
ADAPTERS_REMOTE_LOC=$GOOGLE3/third_party/java/android/android_sdk_linux/extras/android/compatibility/databinding/adapters/$REMOTE_VERSION/.
mkdir -p $ADAPTERS_REMOTE_LOC
cp -r $TMP_ADAPTERS_DIR/src $ADAPTERS_REMOTE_LOC


# copy baseAdapters source. RestrictTo annotations becomes a problem in Google3 so just strip them.
create_target_dir $TMP_X_ADAPTERS_DIR
cp -r extensions/baseAdapters/src $TMP_X_ADAPTERS_DIR;

for file in `find $TMP_X_ADAPTERS_DIR -name "*.java"`
do
    remove_restrict_to "$file"
done
X_ADAPTERS_REMOTE_LOC=$GOOGLE3/third_party/java/androidx/databinding/adapters/$REMOTE_VERSION/.
mkdir -p $X_ADAPTERS_REMOTE_LOC
cp -r $TMP_X_ADAPTERS_DIR/src $X_ADAPTERS_REMOTE_LOC
