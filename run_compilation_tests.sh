#!/bin/bash

readonly script_dir="$(dirname $0)"
readonly dist_dir="$1"

export JAVA_HOME="$(realpath "${script_dir}"/../../prebuilts/studio/jdk/linux)"

(cd "${script_dir}"/.. && ./gradlew :publishLocal) || exit $?
(cd "${script_dir}" && ./gradlew :dataBinding:compilationTests:testClasses) || exit $?
(cd "${script_dir}" && ./gradlew :dataBinding:compilationTests:test)

if [[ -d "${dist_dir}" ]]; then
  # on AB/ATP, put JUnit XML in place for junit-xml-forwarding
  mkdir "${dist_dir}"/host-test-reports
  zip -j "${dist_dir}"/host-test-reports/compilationTests.zip "${script_dir}"/compilationTests/build/test-results/test/*.xml
fi
