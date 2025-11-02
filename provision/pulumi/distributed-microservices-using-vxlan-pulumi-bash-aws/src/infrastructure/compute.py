import pulumi
import pulumi_aws as aws
from typing import List, Dict


def create_instances(
    network: Dict[aws.ec2.Vpc, aws.ec2.Subnet],
    security_groups: List[aws.ec2.SecurityGroup],
) -> List[aws.ec2.Instance]:
    """Create EC2 instances for each component"""
    config = pulumi.Config()

    instance_count = config.require_int("instance_count")
    ssh_key_name = config.require("ssh_key_name")

    disable_fw_ud = "ufw disable && systemctl stop ufw"

    instances = []
    for i in range(instance_count):
        instance = aws.ec2.Instance(
            resource_name=f"instance-{i}",
            instance_type="t2.micro",
            ami="ami-01811d4912b4ccb26",
            subnet_id=network["public_subnets"][i].id,
            key_name=ssh_key_name,
            vpc_security_group_ids=[security_groups[0].id],
            associate_public_ip_address=True,
            user_data=disable_fw_ud,
            user_data_replace_on_change=True,
            tags={"Name": f"instance-{i}"},
        )
        instances.append(instance)

    return instances
