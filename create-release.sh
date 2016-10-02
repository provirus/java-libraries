#!/bin/bash

set -e

# Check params
if [ $# -ne 1 ]
	then
		echo Usage: $0 version;
    echo E.g: $0 0.1.0
		echo Version is MAJOR.MINOR.BUGFIX
		exit 1;
fi

export VERSION=$1
RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo ----==[ Update copyrights ]==----
cd $RUN_PATH/scripts
./javaheaderchanger.sh > /dev/null

echo ----==[ Compile and deploy to jcenter ]==----
cd $RUN_PATH
./gradlew clean bintrayUpload

echo ----==[ Create git tag ]==----
git tag -a -m $VERSION $VERSION

echo ----==[ Operation completed successfully ]==----

git push --tags

echo
echo You can see published items on https://bintray.com/foilen/maven
