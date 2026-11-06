locals {
  azure_tenant_id = var.entra_tenant_id != "" ? var.entra_tenant_id : (
    var.azure_tenant_id != "" ? var.azure_tenant_id : "3ad481d8-3136-47be-99b9-98077738d480"
  )

  azure_client_id = var.entra_client_id != "" ? var.entra_client_id : (
    var.azure_client_id != "" ? var.azure_client_id : "071271ca-f2e6-4328-90d3-1da0cd9edb3d"
  )

  azure_authority = var.azure_authority != "" ? var.azure_authority : (
    "https://kaiserox.ciamlogin.com/${local.azure_tenant_id}/v2.0"
  )

  azure_audience = var.entra_audience != "" ? var.entra_audience : (
    var.backend_api_identifier_uri != "" ? var.backend_api_identifier_uri : "api://telemedicina-rural-api"
  )

  azure_issuer = var.azure_issuer != "" ? var.azure_issuer : (
    "https://${local.azure_tenant_id}.ciamlogin.com/${local.azure_tenant_id}/v2.0"
  )
}
