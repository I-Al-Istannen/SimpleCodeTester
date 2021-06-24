#!/usr/bin/env bash
set -e

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <username>"
fi

# --rm
#   Delete the container on exit
# -i
#   Keep stdin open as we transfer data through it
# --init
#   Add dockers init process to reap zombies and relay signals
# --cap-drop
#   Drop every capability
# --network
#   Deny network access
# --pids-limit
#   Limit generated PIDs to 2000. This limits the container to 2000 processes in total!
#   This should be enough to render fork bombs useless
# --memory
#   Limit the memory consumption
# --read-only
#   The user does not need to create any file in the image
# --name
#   Give the container a name with a unique prefix so we can prevent the script from
#   affecting other containers.
# name
#   Name of image
docker run                                                                              \
    --rm                                                                                \
    -i                                                                                  \
    --init                                                                              \
    --cap-drop=ALL                                                                      \
    --network=none                                                                      \
    --pids-limit=2000                                                                   \
    --memory=200M                                                                       \
    --read-only                                                                         \
    --name "codetester-$1"                                                              \
    codetester-sandbox
