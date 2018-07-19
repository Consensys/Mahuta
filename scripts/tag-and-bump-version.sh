#!/bin/bash

IPFS_STORE_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | sed 's/-SNAPSHOT//g')
./scripts/gittag.sh $IPFS_STORE_VERSION

mvn --batch-mode release:update-versions -DdevelopmentVersion=$IPFS_STORE_VERSION

NEW_DEV_VERSION=$(scripts/get-next-dev-ver.sh)
echo $NEW_DEV_VERSION

git checkout testtagging

mvn --batch-mode release:update-versions -DdevelopmentVersion=$NEW_DEV_VERSION-SNAPSHOT

git add .
git commit -m "CircleCI build $CIRCLE_BUILD_NUM updating version after tag"
git push origin testtagging
