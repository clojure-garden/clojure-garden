#!/bin/bash
pushd ./deployment/local || pushd ../deployment/local || exit

grep -v '^#' .env
export $(grep -v '^#' .env | xargs)

popd || exit
