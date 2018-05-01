#!/usr/bin/env nix-shell
#!nix-shell -i bash --pure -p maven openjdk

set -eu
set -o pipefail

echo "Building backend using maven"
mvn clean package
