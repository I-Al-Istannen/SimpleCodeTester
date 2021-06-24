#!/usr/bin/env bash
set -e

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <username>"
fi

docker kill -s SIGKILL "codetester-$1"
