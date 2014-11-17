#!/bin/sh
set -ex
wget https://protobuf.googlecode.com/files/protobuf-${PROTOBUF_VERSION}.tar.gz
tar -xzvf protobuf-${PROTOBUF_VERSION}.tar.gz
cd protobuf-${PROTOBUF_VERSION} && ./configure --prefix=/usr && make && sudo make install