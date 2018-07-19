#!/bin/sh

# This script will be executed after commit if placed in .git/hooks/post-commit

# Semantic Versioning 2.0.0 guideline
#
# Given a version number MAJOR.MINOR.PATCH, increment the:
# MAJOR version when you make incompatible API changes,
# MINOR version when you add functionality in a backwards-compatible manner, and
# PATCH version when you make backwards-compatible bug fixes.

echo "Starting the taging process based on commit messages since last tag and +semver"

#get highest tags across all branches, not just the current branch
VERSION=`git describe --tags $(git rev-list --tags --max-count=1)`

if [[ "${VERSION}" == "" ]]; then
  #no tag has been created for this repo
  if [[ "$1" =~ [0-9]\.[0-9]\.[0-9] ]]; then
    VERSION=$1
    echo "Tagged with ${VERSION} (Ignoring fatal:cannot describe - this means commit is untagged) "
    git tag "${VERSION}"
    git push origin ${VERSION}
    exit 0
  else
    echo "No tag exists for this repo, please supply first tag version"
    exit 1
  fi
fi

# split into array
VERSION_BITS=(${VERSION//./ })

echo "Latest version tag: $VERSION"

VNUM1=${VERSION_BITS[0]}
VNUM2=${VERSION_BITS[1]}
VNUM3=${VERSION_BITS[2]}

# Taken from gitversion
# major-version-bump-message: '\+semver:\s?(breaking|major)'
# minor-version-bump-message: '\+semver:\s?(feature|minor)'
# patch-version-bump-message: '\+semver:\s?(fix|patch)'
# get commits since last tag and extract the count for "semver: (major|minor|patch)"
COUNT_OF_COMMIT_MSG_HAVE_SEMVER_MAJOR=`git log --pretty=%B ${VERSION}..HEAD | egrep -c 'breaking|major'`
COUNT_OF_COMMIT_MSG_HAVE_SEMVER_MINOR=`git log --pretty=%B ${VERSION}..HEAD | egrep -c 'feature|minor'`
COUNT_OF_COMMIT_MSG_HAVE_SEMVER_PATCH=`git log --pretty=%B ${VERSION}..HEAD | egrep -c 'fix|patch'`

if [ $COUNT_OF_COMMIT_MSG_HAVE_SEMVER_MAJOR -gt 0 ]; then
    VNUM1=$((VNUM1+1))
    VNUM2=0
    VNUM3=0
elif [ $COUNT_OF_COMMIT_MSG_HAVE_SEMVER_MINOR -gt 0 ]; then
    VNUM2=$((VNUM2+1))
    VNUM3=0
elif [ $COUNT_OF_COMMIT_MSG_HAVE_SEMVER_PATCH -gt 0 ]; then
    VNUM3=$((VNUM3+1))
fi

# count all commits for a branch
GIT_COMMIT_COUNT=`git rev-list --count HEAD`
echo "Commit count: $GIT_COMMIT_COUNT"
export BUILD_NUMBER=$GIT_COMMIT_COUNT

#always tag if there is no semver bump then always bump fix/patch version
if [ $COUNT_OF_COMMIT_MSG_HAVE_SEMVER_MAJOR -gt 0 ] ||  [ $COUNT_OF_COMMIT_MSG_HAVE_SEMVER_MINOR -gt 0 ] || [ $COUNT_OF_COMMIT_MSG_HAVE_SEMVER_PATCH -gt 0 ]; then
    NEW_TAG="$VNUM1.$VNUM2.$VNUM3"
else
    VNUM3=$((VNUM3+1))
    NEW_TAG="$VNUM1.$VNUM2.$VNUM3"
fi

echo "Updating $VERSION to $NEW_TAG"

echo "Tagged with $NEW_TAG"
git tag "$NEW_TAG"
git push origin ${NEW_TAG}
