#!/usr/bin/env bash

# Listen for Docker network creation events
docker events --filter 'event=create' --filter 'event=network' | while read event
do
  # Extract the network name from the event
  BRIDGE_ID=$(echo $event | grep -oP '(?<=network create )\S+')

  # If BRIDGE_ID is empty, continue to the next iteration
  if [ -z "$BRIDGE_ID" ]; then
    continue
  fi

  SHORT_BRIDGE_ID=${BRIDGE_ID:0:12}
  BRIDGE_NAME=$(echo -e "br-${SHORT_BRIDGE_ID}")

  # Update the .env file with the new network ID
  sed -i "s/bridge_name_placeholder/${BRIDGE_NAME}/g" .env

  # Update the Suricata YAML file with the new bridge ID
  sed -i "s/bridge_name_placeholder/${BRIDGE_NAME}/g" ./suricata/suricata.yaml

  exit 0
done
