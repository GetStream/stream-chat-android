#!/bin/sh

# Check if docker is installed.
command -v docker >/dev/null 2>&1 || { echo >&2 "I require 'docker' but it's not installed. Aborting."; exit 1; }

# Configure vale config and docusarus files into docker container and run `vale` to check documentation.
docker run --rm -v $(pwd)/.vale.ini:/docs/.vale.ini --rm -v $(pwd)/.styles:/docs/.styles/ --rm -v $(pwd)/docusaurus:/docs/docusaurus -w /docs -i -t  jdkato/vale docusaurus
