#!/bin/bash -ex

source android/scripts/constants.sh
source android/scripts/get_build_number_from_tag.sh

function incrementSnapshotNumber(){
  mkdir -p "$(dirname $BUILD_NUMBER_FILE)" && touch "$BUILD_NUMBER_FILE"
  local res=$(getBuildNumberFromTag)
  echo "$((res + 1))" > "${BUILD_NUMBER_FILE}"
}
incrementSnapshotNumber
