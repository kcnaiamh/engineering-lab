terraform {
  required_providers {
    archive = {
      source  = "hashicorp/archive"
      version = "2.7.1"
    }

    aws = {
      source  = "hashicorp/aws"
      version = "6.14.1"
    }
  }
}

provider "archive" {
  # Configuration options
}

provider "aws" {
  region = var.region
}
