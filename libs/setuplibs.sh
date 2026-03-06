#!/usr/bin/env bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd $SCRIPT_DIR

if [ -d ./mbedtls ]; then
	echo "found mbedtls"
	mkdir -p ./mbedtls/build
	cp ./scripts/build-mbedtls.sh ./mbedtls/build/build.sh
else
	echo "mbedtls library not installed, skipping setup"
fi

cd $SCRIPT_DIR

if [ -d ./spdlog ]; then
	echo "found spdlog"
	mkdir -p ./spdlog/build
	cp ./scripts/build-spdlog.sh ./spdlog/build/build.sh
else
	echo "spdlog library not installed, skipping setup"
fi
