import pulumi
from infrastructure.network import create_network_infrastructure
from infrastructure.security import create_security_groups
from infrastructure.compute import create_instances
from utils.helpers import create_ssh_key, create_config_file
from config.update_scripts import update_scripts


def main() -> None:
    """Main function to orchestrate infrastructure deployment."""

    # Load configuration
    config = pulumi.Config()
    ssh_key_name = config.require("ssh_key_name")
    create_key = config.get_bool("create_key")

    # Create ssh key to access EC2s
    if create_key:
        create_ssh_key(ssh_key_name)

    network_stack = create_network_infrastructure()  # returns vpc, public_subnets[]
    security_stack = create_security_groups(network_stack["vpc"])

    # Compute stack
    instances = create_instances(
        network=network_stack,
        security_groups=security_stack,
    )

    create_config_file(instances, ssh_key_name)

    pulumi.export("ec2 public ips", [instance.public_ip for instance in instances])
    pulumi.export("ec2 private ips", [instance.private_ip for instance in instances])

    for i in range(1, 4):
        update_scripts(f"../scripts/setup-vxlan-host-{i}.sh", instances)


if __name__ == "__main__":
    main()
