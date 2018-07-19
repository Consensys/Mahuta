#!/bin/bash
GIT_COMMIT_DESC=$(git log --format=oneline -n 1 $CIRCLE_SHA1)
echo Git commit message: $GIT_COMMIT_DESC
echo Git PR Number: $CIRCLE_PR_NUMBER
if [[ "$GIT_COMMIT_DESC" != *"maven-release-plugin"* && "$CIRCLE_PR_NUMBER" == "" ]]; then
  RELEASE_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | sed 's/-SNAPSHOT//g')

  VNUM1="$(cut -d'.' -f1 <<<"$RELEASE_VERSION")"
  VNUM2="$(cut -d'.' -f2 <<<"$RELEASE_VERSION")"
  VNUM3="$(cut -d'.' -f3 <<<"$RELEASE_VERSION")"
  VNUM3=$((VNUM3+1))


  #create new version
  NEW_DEV_VERSION="$VNUM1.$VNUM2.$VNUM3"

  git config --global user.email "info@kauri.io"
  git config --global user.name "circleci"

  echo Running release:prepare with releaseVersion:$RELEASE_VERSION and  DdevelopmentVersion:$NEW_DEV_VERSION-SNAPSHOT

  mvn -B release:prepare -DreleaseVersion=$RELEASE_VERSION -DdevelopmentVersion=$NEW_DEV_VERSION-SNAPSHOT

  mvn -s ./.circleci/.circleci.settings.xml release:perform -Darguments="-Dmaven.javadoc.skip=true" -DskipTests
fi
