#!/bin/bash -ex

source android/scripts/constants.sh

function getBuildNumberFromTag(){
  local gitDescribeCommand="git tag -l --sort=-creatordate *.*-androidsnapshot-*"
  local buildNumberSedCommand="s:^.*-androidsnapshot-\(.*\)$:\1:"
  local buildNumber="$(${gitDescribeCommand} | head -n 1 | sed ${buildNumberSedCommand})"

  echo "${buildNumber}"
}

getBuildNumberFromTag