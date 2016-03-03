#!/bin/bash
./gradlew clean cE eclipse build install gitChangelogTask || exit 1
cd violation-comments-to-github-gradle-plugin-example
./gradlew violationCommentsToGitHub -DGITHUB_PULLREQUESTID=false -DGITHUB_OAUTH2TOKEN=$GITHUB_OAUTH2TOKEN -i --stacktrace
./gradlew violationCommentsToGitHub -DGITHUB_PULLREQUESTID=$TRAVIS_PULL_REQUEST -DGITHUB_OAUTH2TOKEN=$GITHUB_OAUTH2TOKEN -i --stacktrace
