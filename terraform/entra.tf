resource "azuread_application" "backend" {
  display_name     = "telemedicina-backend-api"
  sign_in_audience = "AzureADMyOrg"
  identifier_uris  = [var.backend_api_identifier_uri]

  api {
    requested_access_token_version = 2

    oauth2_permission_scope {
      admin_consent_description  = "Permite leer datos de Telemedicina"
      admin_consent_display_name = "Leer datos de Telemedicina"
      enabled                    = true
      id                         = var.backend_read_scope_id
      type                       = "User"
      value                      = "read"
    }

    oauth2_permission_scope {
      admin_consent_description  = "Permite modificar datos de Telemedicina"
      admin_consent_display_name = "Modificar datos de Telemedicina"
      enabled                    = true
      id                         = var.backend_write_scope_id
      type                       = "User"
      value                      = "write"
    }
  }
}

resource "azuread_application" "frontend" {
  display_name     = "telemedicina-frontend-spa"
  sign_in_audience = "AzureADMyOrg"

  single_page_application {
    redirect_uris = [endswith(var.frontend_redirect_uri, "/") ? var.frontend_redirect_uri : "${var.frontend_redirect_uri}/"]
  }

  required_resource_access {
    resource_app_id = azuread_application.backend.client_id

    resource_access {
      id   = var.backend_read_scope_id
      type = "Scope"
    }

    resource_access {
      id   = var.backend_write_scope_id
      type = "Scope"
    }
  }
}