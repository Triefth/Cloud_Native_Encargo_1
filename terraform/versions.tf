terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "~> 3.0"
    }
  }

  # Si usas S3 para backend de estado en CI/CD, descomenta la siguiente linea
  # backend "s3" {}
}

provider "aws" {
  region = var.aws_region
}

provider "azuread" {
  tenant_id = var.azure_tenant_id
}
