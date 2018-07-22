#!/bin/bash
GIT_COMMIT_DESC=$(git log --format=oneline -n 1 $CIRCLE_SHA1)
echo Git commit message: $GIT_COMMIT_DESC
echo Git PR Number: $CIRCLE_PR_NUMBER
if [[ "$GIT_COMMIT_DESC" != *"maven-release-plugin"* && "$CIRCLE_PR_NUMBER" == "" ]]; then
  #get highest tags across all branches, not just the current branch
  git checkout development

  mvn --batch-mode release:update-versions

  git config --global user.email "info@kauri.io"
  git config --global user.name "circleci"

  git add .
  git commit -m "CircleCI build $CIRCLE_BUILD_NUM updating development version after release"
  git push origin development
fi
