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

  # Los valores reales (bucket, key, region, dynamodb_table) se pasan desde
  # GitHub Actions con `-backend-config=...` en el paso `terraform init`,
  # porque el nombre del bucket depende del Account ID de AWS y no puede
  # quedar hardcodeado aquí.
  backend "s3" {}
}

provider "aws" {
  region = var.aws_region
}

provider "azuread" {
  tenant_id = var.azure_tenant_id
}
