#!/bin/bash

if [[ -n $1 && -n $2 ]];
then 
  git stash
  mvn release:update-versions -DautoVersionSubmodules=true 
  mvn clean deploy -P assembly
  git reset --hard
  find . -type f -exec sed -i 's/$1/$2/g' {} \;
else
  echo "Missing version arguments. Usage: $0 [old-version] [new-version]"
  exit 1
fi

exit 0


