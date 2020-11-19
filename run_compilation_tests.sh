#!/bin/bash

readonly script_dir="$(dirname $0)"

export JAVA_HOME="$(realpath "${script_dir}"/../../prebuilts/studio/jdk/jdk11/linux)"

(cd "${script_dir}"/.. && ./gradlew :publishLocal) || exit $?
(cd "${script_dir}" && ./gradlew :dataBinding:compilationTests:testClasses) || exit $?
(cd "${script_dir}" && ./gradlew :dataBinding:compilationTests:test)

if [[ -d "${DIST_DIR}" ]]; then
  # on AB/ATP, put JUnit XML in place for junit-xml-forwarding
  mkdir "${DIST_DIR}"/host-test-reports
  zip -j "${DIST_DIR}"/host-test-reports/compilationTests.zip "${script_dir}"/compilationTests/build/test-results/test/*.xml
fi
