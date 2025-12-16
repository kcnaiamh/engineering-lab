#!/bin/bash

sudo apt-get update
sudo apt-get upgrade -y

# install bcc build dependencies
sudo apt-get install -y zip bison build-essential cmake flex git libedit-dev \
  libllvm18 llvm-18-dev libclang-18-dev python3 zlib1g-dev libelf-dev libfl-dev python3-setuptools \
  liblzma-dev libdebuginfod-dev arping netperf iperf libpolly-18-dev

# install and compile bcc
cd /tmp # libbpf.so doesn't link inside /vagrant shared folder. So, building bcc in /tmp 
git clone https://github.com/iovisor/bcc.git
mkdir bcc/build; cd bcc/build
cmake ..
make
sudo make install
cmake -DPYTHON_CMD=python3 .. # build python3 binding
pushd src/python/
make
sudo make install
popd

# install the kernel headers
sudo apt-get install -y linux-headers-$(uname -r)
ls -la /lib/modules/$(uname -r)/build