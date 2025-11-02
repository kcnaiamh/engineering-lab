#!/usr/bin/env bash
set -euxo pipefail

# Update system and install basic tools
sudo apt-get update -y
sudo apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common \
    net-tools \
    iproute2 \
    bridge-utils \
    tcpdump \
    iptables \
    jq

# Install Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository -y "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

# Enable IP forwarding and bridge networking for Docker and VxLAN.
echo "net.ipv4.ip_forward = 1" | sudo tee -a /etc/sysctl.conf
echo "net.bridge.bridge-nf-call-iptables = 1" | sudo tee -a /etc/sysctl.conf
echo "net.bridge.bridge-nf-call-ip6tables = 1" | sudo tee -a /etc/sysctl.conf
sudo modprobe br_netfilter
echo "br_netfilter" | sudo tee /etc/modules-load.d/br_netfilter.conf
sudo sysctl -p

# Install VxLAN tools
sudo apt-get install -y iputils-ping vlan

# Add ubuntu user to docker group
sudo usermod -aG docker ubuntu

VM1_PRIVATE_IP="0.0.0.0"   # placeholder IP
VM2_PRIVATE_IP="0.0.0.0"   # placeholder IP
VM3_PRIVATE_IP="0.0.0.0"   # placeholder IP

# Create docker network
BRIDGE_NAME="br-vxlan"
sudo docker network create --subnet=10.10.0.0/16 --gateway=10.10.0.1 --driver=bridge -o "com.docker.network.bridge.name"="${BRIDGE_NAME}" ${BRIDGE_NAME}

# Creates and configures the VxLAN tunnel 'vxlan0' and attaches it to the Docker bridge.
NET_IF="enX0" # CHANGE IT
sudo ip link add vxlan0 type vxlan id 444 dstport 4789 dev ${NET_IF}
sudo ip link set vxlan0 up
sudo ip link set vxlan0 master ${BRIDGE_NAME}

# Append a forwarding database entry to the VxLAN interface for each peer EC2's private IP.
sudo bridge fdb append 00:00:00:00:00:00 dev vxlan0 dst ${VM2_PRIVATE_IP}
sudo bridge fdb append 00:00:00:00:00:00 dev vxlan0 dst ${VM3_PRIVATE_IP}
