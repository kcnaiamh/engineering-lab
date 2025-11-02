.PHONY: provision-infra configure-infra build cleanup

provision-infra:
	pulumi up -y
	pulumi config set create_key false
# 10 second delay
	sleep 10

configure-infra: provision-infra
# Copy vxlan setup script
	scp -o StrictHostKeyChecking=no ./scripts/setup-vxlan-host-1.sh host1:~
	scp -o StrictHostKeyChecking=no ./scripts/setup-vxlan-host-2.sh host2:~
	scp -o StrictHostKeyChecking=no ./scripts/setup-vxlan-host-3.sh host3:~

# Copy script to update /etc/hosts file
	scp ./scripts/update-hostfile.sh host1:~
	scp ./scripts/update-hostfile.sh host2:~
	scp ./scripts/update-hostfile.sh host3:~

# Execute update-hostfile.sh script
	ssh host1 "sudo bash update-hostfile.sh"
	ssh host2 "sudo bash update-hostfile.sh"
	ssh host3 "sudo bash update-hostfile.sh"

# Execute vxlan setup script
	ssh host1 "sed -i 's/\r//' setup-vxlan-host-1.sh && bash ~/setup-vxlan-host-1.sh"&
	ssh host2 "sed -i 's/\r//' setup-vxlan-host-2.sh && bash ~/setup-vxlan-host-2.sh"&
	ssh host3 "sed -i 's/\r//' setup-vxlan-host-3.sh && bash ~/setup-vxlan-host-3.sh"&

# Compress directories
	tar -czvf host_1.tar.gz ./host_1
	tar -czvf host_2.tar.gz ./host_2
	tar -czvf host_3.tar.gz ./host_3

# Copy compressed file
	scp host_1.tar.gz host1:~
	scp host_2.tar.gz host2:~
	scp host_3.tar.gz host3:~

# Extract compressed file
	ssh host1 "tar -xzvf host_1.tar.gz"
	ssh host2 "tar -xzvf host_2.tar.gz"
	ssh host3 "tar -xzvf host_3.tar.gz"

# 300 second delay
	sleep 300

build: configure-infra
# Run all databases on host3 machine
	ssh host3 "cd ~/host_3/ && docker compose up -d"
# Run microservices
	ssh host1 "cd ~/host_1/ && docker compose up -d"&
	ssh host2 "cd ~/host_2/ && docker compose up -d"&

cleanup:
	pulumi destroy -y
	pulumi config set create_key true
	bash ./scripts/cleanup.sh

	@echo "[+] cleanup done!"
