import pulumi
import pulumi_aws as aws
import pulumi_tls as tls
import os
import secrets
import string
from typing import List


def read_file(file_path: str) -> str:
    """Read and return the contents of a file"""
    with open(f"./{file_path}", "r") as fd:
        return fd.read()


def gen_password(length: int, use_special: bool = False) -> str:
    """Generate a secure random password."""
    pools = string.ascii_letters + string.digits
    if use_special:
        pools += string.punctuation

    # Ensure at least one of each basic type
    required = [
        secrets.choice(string.ascii_uppercase),
        secrets.choice(string.ascii_lowercase),
        secrets.choice(string.digits),
    ]

    if use_special:
        required.append(secrets.choice(string.punctuation))

    remaining = [secrets.choice(pools) for _ in range(length - len(required))]
    password_chars = required + remaining

    secrets.SystemRandom().shuffle(password_chars)
    return "".join(password_chars)


def create_ssh_key(key_name: str) -> aws.ec2.KeyPair:
    """Create an SSH key pair"""

    # Generate a private key (only if Pulumi actually needs it)
    tls_key = tls.PrivateKey(
        f"{key_name}-tls",
        algorithm="RSA",
        rsa_bits=4096,
    )

    # Declare the AWS KeyPair as a managed Pulumi resource
    aws_key = aws.ec2.KeyPair(
        key_name, key_name=key_name, public_key=tls_key.public_key_openssh
    )

    # Save private key locally
    private_key_path = os.path.join(
        os.path.expanduser("~"), ".ssh", f"{key_name}.id_rsa"
    )

    def write_private_key(private_key_pem: str) -> None:
        if pulumi.runtime.is_dry_run():
            return

        os.makedirs(os.path.dirname(private_key_path), exist_ok=True)

        with open(private_key_path, "w") as private_key_file:
            private_key_file.write(private_key_pem)
        os.chmod(private_key_path, 0o600)

    tls_key.private_key_pem.apply(write_private_key)
    return aws_key


def create_config_file(instances: List[aws.ec2.Instance], ssh_key_name: str) -> None:
    """Generate SSH config file dynamically for all instances."""

    def write_config(ips: List[str]):
        if pulumi.runtime.is_dry_run():
            return

        config_lines = []

        for i, ip in enumerate(ips, start=1):
            config_lines.append(f"""
Host host{i}
    HostName {ip}
    User ubuntu
    IdentityFile ~/.ssh/{ssh_key_name}.id_rsa
""".strip("\n")
            )

        config_content = "\n\n".join(config_lines)

        config_path = os.path.join(os.path.expanduser("~"), ".ssh", "config")

        with open(config_path, "w") as config_file:
            config_file.write(config_content)

    pulumi.Output.all(*[inst.public_ip for inst in instances]).apply(write_config)
