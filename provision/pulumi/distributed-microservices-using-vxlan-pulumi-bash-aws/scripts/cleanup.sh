rm *.tar.gz

sed -i 's|VM1_PRIVATE_IP=".*"|VM1_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-1.sh
sed -i 's|VM2_PRIVATE_IP=".*"|VM2_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-1.sh
sed -i 's|VM3_PRIVATE_IP=".*"|VM3_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-1.sh

sed -i 's|VM1_PRIVATE_IP=".*"|VM1_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-2.sh
sed -i 's|VM2_PRIVATE_IP=".*"|VM2_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-2.sh
sed -i 's|VM3_PRIVATE_IP=".*"|VM3_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-2.sh

sed -i 's|VM1_PRIVATE_IP=".*"|VM1_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-3.sh
sed -i 's|VM2_PRIVATE_IP=".*"|VM2_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-3.sh
sed -i 's|VM3_PRIVATE_IP=".*"|VM3_PRIVATE_IP="0.0.0.0"|g' ./scripts/setup-vxlan-host-3.sh
