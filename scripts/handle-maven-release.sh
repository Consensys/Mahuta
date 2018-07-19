#!/bin/bash

RELEASE_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | sed 's/-SNAPSHOT//g')

echo $RELEASE_VERSION

VNUM1="$(cut -d'.' -f1 <<<"$RELEASE_VERSION")"
VNUM2="$(cut -d'.' -f2 <<<"$RELEASE_VERSION")"
VNUM3="$(cut -d'.' -f3 <<<"$RELEASE_VERSION")"
VNUM3=$((VNUM3+1))


#create new version
NEW_DEV_VERSION="$VNUM1.$VNUM2.$VNUM3"

echo $NEW_DEV_VERSION

#mvn -B release:prepare -DreleaseVersion=$RELEASE_VERSION -DdevelopmentVersion=$NEW_DEV_VERSION-SNAPSHOT

#mvn --batch-mode release:update-versions -DdevelopmentVersion=$IPFS_STORE_VERSION

#NEW_DEV_VERSION=$(scripts/get-next-dev-ver.sh)
#echo $NEW_DEV_VERSION

#git checkout testtagging

#mvn --batch-mode release:update-versions -DdevelopmentVersion=$NEW_DEV_VERSION-SNAPSHOT

#git add .
#git commit -m "CircleCI build $CIRCLE_BUILD_NUM updating version after tag"
#git push origin testtagging
