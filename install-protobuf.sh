#!/bin/sh
set -ex
wget https://protobuf.googlecode.com/files/protobuf-${PROTOBUF-VERSION}.tar.gz
tar -xzvf protobuf-${PROTOBUF-VERSION}.tar.gz
cd protobuf-${PROTOBUF-VERSION} && ./configure --prefix=/usr && make && sudo make install