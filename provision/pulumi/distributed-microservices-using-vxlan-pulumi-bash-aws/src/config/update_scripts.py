import pulumi
from typing import List


def update_scripts(file_path: str, instances: List):
    """
    Update IP addresses in a file using Pulumi's apply() method.
    Replaces placeholders like VM1_PRIVATE_IP="0.0.0.0" with actual private IPs.
    """

    def update_file_content(ips: List[str]) -> None:
        # Read file once
        with open(file_path, "r") as fd:
            content = fd.read()

        # Dynamically replace placeholders for each EC2
        for idx, ip in enumerate(ips, start=1):
            placeholder = f'VM{idx}_PRIVATE_IP="0.0.0.0"'
            replacement = f'VM{idx}_PRIVATE_IP="{ip}"'
            content = content.replace(placeholder, replacement)

        # Write updated content back
        with open(file_path, "w") as fd:
            fd.write(content)

    # Collect all private IPs into one Output
    all_private_ips = pulumi.Output.all(
        *(instance.private_ip for instance in instances)
    )

    # Apply transformation
    all_private_ips.apply(update_file_content)
