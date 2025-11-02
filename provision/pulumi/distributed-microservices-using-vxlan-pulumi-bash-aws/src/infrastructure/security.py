import pulumi_aws as aws
import pulumi
from typing import List


def create_security_groups(vpc: aws.ec2.Vpc) -> List[aws.ec2.SecurityGroup]:
    """Create security groups for each component"""
    config = pulumi.Config()

    ssh_port = config.require_int("ssh_port")
    http_port = config.require_int("http_port")
    vxlan_port = config.require_int("vxlan_port")
    ssh_src_ips = config.require("ssh_src_ips")
    http_src_ips = config.require("http_src_ips")
    vxlan_src_ips = config.require("vxlan_src_ips")

    # This security group will be used for all the EC2 instances
    security_group = aws.ec2.SecurityGroup(
        "public-security-group",
        vpc_id=vpc.id,
        description="Enable SSH and VxLAN traffic",
        ingress=[
            # SSH access from anywhere
            aws.ec2.SecurityGroupIngressArgs(
                protocol="tcp",
                from_port=ssh_port,
                to_port=ssh_port,
                cidr_blocks=[ssh_src_ips],  # User you public IP
            ),
            # Allows traffic for Gateway
            aws.ec2.SecurityGroupIngressArgs(
                protocol="tcp",
                from_port=http_port,
                to_port=http_port,
                cidr_blocks=[http_src_ips],  # User you public IP
            ),
            # Allows VXLAN traffic accross subnets (/22)
            aws.ec2.SecurityGroupIngressArgs(
                protocol="udp",
                from_port=vxlan_port,
                to_port=vxlan_port,
                cidr_blocks=[vxlan_src_ips],
            ),
        ],
        egress=[
            aws.ec2.SecurityGroupEgressArgs(
                protocol="-1",
                from_port=0,
                to_port=0,
                cidr_blocks=["0.0.0.0/0"],
            )
        ],
        tags={"Name": "public-security-group"},
    )

    return [security_group]
