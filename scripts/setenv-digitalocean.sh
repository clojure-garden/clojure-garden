#!/bin/bash
pushd ./deployment/digitalocean || pushd ../deployment/digitalocean || exit

grep -v '^#' .env
export $(grep -v '^#' .env | xargs)

popd || exit
