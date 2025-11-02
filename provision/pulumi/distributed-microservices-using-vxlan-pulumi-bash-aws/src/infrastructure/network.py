import pulumi_aws as aws
from typing import Dict
import pulumi


def create_network_infrastructure() -> Dict[aws.ec2.Vpc, aws.ec2.Subnet]:
    """Create VPC, subnets, internet gateway, and route tables."""
    config = pulumi.Config()

    instance_count = config.require_int("instance_count")
    vpc_cidr = config.require("vpc_cidr")
    public_subnet_cidrs = config.require_object("public_subnet_cidrs")

    az_names = aws.get_availability_zones(state="available")

    # Create VPC
    vpc = aws.ec2.Vpc(
        "poc-vpc",
        cidr_block=vpc_cidr,
        enable_dns_support=True,
        enable_dns_hostnames=True,
        tags={"Name": "poc-vpc"},
    )

    # Create internet gateway
    igw = aws.ec2.InternetGateway("poc-igw", vpc_id=vpc.id, tags={"Name": "poc-igw"})

    # Create public subnet
    public_subnets = []
    for i in range(instance_count):
        subnet = aws.ec2.Subnet(
            f"poc-public-subnet-{i}",
            vpc_id=vpc.id,
            cidr_block=public_subnet_cidrs[i],
            map_public_ip_on_launch=True,
            availability_zone=az_names.names[i],
            tags={"Name": f"poc-public-subnet-{i}"},
        )
        public_subnets.append(subnet)

    # Create route tables
    public_rt = aws.ec2.RouteTable(
        "poc-public-rt",
        vpc_id=vpc.id,
        routes=[aws.ec2.RouteTableRouteArgs(cidr_block="0.0.0.0/0", gateway_id=igw.id)],
        tags={"Name": "poc-public-rt"},
    )

    # Associate Subnets with Route Table
    for i in range(instance_count):
        aws.ec2.RouteTableAssociation(
            f"public-rt-association-{i}",
            subnet_id=public_subnets[i].id,
            route_table_id=public_rt.id,
        )

    return {
        "vpc": vpc,
        "public_subnets": public_subnets,
    }
