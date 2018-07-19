#!/bin/bash

IPFS_STORE_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | sed 's/-SNAPSHOT//g')

echo $IPFS_STORE_VERSION

NEW_DEV_VERSION=$(scripts/get-next-dev-ver.sh)
echo $NEW_DEV_VERSION
