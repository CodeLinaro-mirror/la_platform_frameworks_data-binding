#!/bin/bash
# convenient script to run all available integration tests. make sure you have a running emulator
./init.sh
ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $ROOT_DIR;
MAVEN_REPO=$ROOT_DIR/test_maven_repo
rm -rf $MAVEN_REPO;
mkdir $MAVEN_REPO;

SETTINGS_FILES=`find integration-tests integration-tests-support -name "settings.gradle"`
echo "list of test apps:"
echo "$SETTINGS_FILES"
for f in $SETTINGS_FILES
do
  DIR_NAME=`dirname $f`
  echo "will upload archives for $DIR_NAME"
  ( cd $DIR_NAME && ./gradlew uploadArchives -Pandroid.injected.invoked.from.ide=true -Pmaven_repo=$MAVEN_REPO ) || { echo "$f failed" ; exit 1; }
done

for f in $SETTINGS_FILES
do
  DIR_NAME=`dirname $f`
  echo "will run tests for $DIR_NAME"
  ( cd $DIR_NAME && ./gradlew testDebug connectedCheck -Pandroid.injected.invoked.from.ide=true -Pmaven_repo=$MAVEN_REPO ) || { echo "$f failed" ; exit 1; }
done