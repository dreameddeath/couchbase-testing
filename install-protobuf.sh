#!/bin/sh
set -ex
if [ ! -d "$HOME/protobuf/lib" ]; then
    wget https://github.com/google/protobuf/releases/download/v${PROTOBUF_VERSION}/protobuf-${PROTOBUF_VERSION}.tar.gz
    tar -xzvf protobuf-${PROTOBUF_VERSION}.tar.gz
    cd protobuf-${PROTOBUF_VERSION} && ./configure --prefix=${HOME}/protobuf && make && make install
else
  echo 'Using cached directory.';
fi