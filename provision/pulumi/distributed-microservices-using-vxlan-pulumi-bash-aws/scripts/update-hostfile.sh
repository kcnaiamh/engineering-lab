#!/usr/bin/env bash

# Define the host entries to be added
HOSTS_ENTRIES=$(cat <<EOF
10.10.0.9   postgres-user
10.10.0.10  mongodb-product
10.10.0.11  mongodb-order
10.10.0.12  postgres-inventory
10.10.0.7   order-service
10.10.0.8   user-service
10.10.0.5   product-service
10.10.0.6   inventory-service
10.10.0.100 nginx-gateway-master
10.10.0.101 nginx-gateway-backup
EOF
)

# Define the file path for /etc/hosts
HOSTS_FILE="/etc/hosts"

# Use sudo for elevated permissions
if [ "$(id -u)" != "0" ]; then
    echo "This script must be run as root. Please use 'sudo'."
    exit 1
fi

echo "Updating $HOSTS_FILE..."

# Get a list of hostnames from the new entries
HOSTNAMES=$(echo "$HOSTS_ENTRIES" | awk '{print $2}')

# Remove any existing entries for these hostnames to prevent duplicates
for HOSTNAME in $HOSTNAMES; do
    sed -i "/[[:space:]]\+$HOSTNAME\s*$/d" "$HOSTS_FILE"
done

# Append the new entries to the hosts file
echo "$HOSTS_ENTRIES" >> "$HOSTS_FILE"

echo "Successfully updated $HOSTS_FILE."

exit 0
