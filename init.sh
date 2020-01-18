#!/bin/bash
current_dir=$PWD;
if [ ! -e 'integration-tests' ] || [ ! -e 'extensions' ]; then
  echo "must run this under {src}/tools/data-binding"
  exit 1
fi
OLD_GRADLE="https\://services.gradle.org/distributions/gradle-5.6.4-bin.zip"
tools_dir="$current_dir/..";
copy_gradle() {
  project_name=${PWD##*/}
  echo "PROJECT NAME:${project_name}"
  extra_dots=$1
    echo "copying $PWD"
  rm -rf gradle
  rm -rf gradlew
  cp -R "$tools_dir/gradle" .
  if  [[ $project_name == LibCompileWith* ]] ;
  then
    echo "copy old gradle"
    sed -i "/distributionUrl=/c\distributionUrl=$OLD_GRADLE" gradle/wrapper/gradle-wrapper.properties
  else
    echo "copy latest gradle"
    sed -i -e "s#distributionUrl\=#distributionUrl=$extra_dots/#g" gradle/wrapper/gradle-wrapper.properties
  fi
  cp "$tools_dir/gradlew" .
}
OIFS="$IFS"
IFS=$'\n'

cd extensions;
copy_gradle "../.."
cd ../extensions-support;
copy_gradle "../.."
echo "start in $current_dir"
cd $current_dir;
for line in `find integration-tests integration-tests-support -name 'settings.gradle'`
do
  echo "will prepare build for ${line}";
  cd $current_dir;
  dir_name=`(dirname ${line})`
  cd "$dir_name"
  copy_gradle "../../.."
done

linux_sdk="$current_dir/../../prebuilts/studio/sdk/linux"
mac_sdk="$current_dir/../../prebuilts/studio/sdk/darwin"
unamestr=`uname | tr '[:upper:]' '[:lower:]'`

create_local_properties() {
  echo "creating local properties in $PWD"
  if [ "${unamestr}" = "darwin"  ]; then
    echo "sdk.dir=$mac_sdk" > "local.properties"
  else
    echo "sdk.dir=$linux_sdk" > "local.properties"
  fi
}

cd $current_dir;
for line in `find integration-tests integration-tests-support -name 'settings.gradle'`
do
  cd $current_dir;
  dir_name=`(dirname ${line})`
  cd "$dir_name"
  create_local_properties
done

IFS="$OIFS"
