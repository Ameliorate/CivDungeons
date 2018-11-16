#!/usr/bin/env bash

gitroot="$(git rev-parse --show-toplevel)"
currentdir="$(pwd)"

if [ -d "$gitroot/testserver" ]; then
  echo Test server already exists.
  exit
fi

mkdir "$gitroot/testserver"
wget -O "$gitroot/testserver/BuildTools.jar" https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
git config --global --unset core.autocrlf
cd "$gitroot/testserver" # cd into testserver to work around buildtools placing files in the current dir rather than
                         # the dir buildtools is in.
java -jar "$gitroot/testserver/BuildTools.jar" -rev 1.12.2
cd "$currentdir"
