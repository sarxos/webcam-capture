#!/bin/bash

git stash
mvn release:update-versions -DautoVersionSubmodules=true 
mvn clean deploy -P assembly
git reset --hard
git stash apply && git stash drop


