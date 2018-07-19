#!/bin/bash

#get highest tags across all branches, not just the current branch
VERSION=`git describe --tags $(git rev-list --tags --max-count=1)`

VNUM1="$(cut -d'.' -f1 <<<"$VERSION")"
VNUM2="$(cut -d'.' -f2 <<<"$VERSION")"
VNUM3="$(cut -d'.' -f3 <<<"$VERSION")"
VNUM3=$((VNUM3+1))

#create new version
NEW_DEV_VERSION="$VNUM1.$VNUM2.$VNUM3"
echo $NEW_DEV_VERSION
