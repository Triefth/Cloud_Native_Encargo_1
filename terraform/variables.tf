variable "aws_region" {
  description = "Región de AWS para el despliegue"
  default     = "us-east-1"
}

variable "git_repo_url" {
  description = "URL del repositorio GitHub para clonar y desplegar automáticamente"
  type        = string
  default     = "https://github.com/Triefth/Cloud_Native_Encargo_1.git"
}

variable "git_branch" {
  description = "Rama de Git a desplegar"
  type        = string
  default     = "test"
}

variable "backend_instance_type" {
  description = "Tipo de instancia EC2 para el Backend (Microservicios + BFF)"
  default     = "t2.medium"
}

variable "frontend_instance_type" {
  description = "Tipo de instancia EC2 para el Frontend (SPA React/Vite/Nginx)"
  default     = "t2.micro"
}

variable "instance_type" {
  description = "Tipo de instancia EC2 por defecto"
  default     = "t2.medium"
}

variable "manage_azuread_in_terraform" {
  description = "Si es true, Terraform intentará crear las aplicaciones en Microsoft Entra ID con el provider azuread. Si es false (por defecto), usa tus credenciales existentes de Azure Portal."
  type        = bool
  default     = false
}

# Credenciales de Microsoft Entra ID (CIAM / External ID)
variable "azure_client_id" {
  description = "Application (Client) ID del Frontend SPA registrado en Microsoft Entra ID"
  type        = string
  default     = "071271ca-f2e6-4328-90d3-1da0cd9edb3d"
}

variable "entra_client_id" {
  description = "Alias para azure_client_id"
  type        = string
  default     = ""
}

variable "azure_tenant_id" {
  description = "Directory (Tenant) ID de Microsoft Entra ID"
  type        = string
  default     = "3ad481d8-3136-47be-99b9-98077738d480"
}

variable "entra_tenant_id" {
  description = "Alias para azure_tenant_id"
  type        = string
  default     = ""
}

variable "azure_authority" {
  description = "Autoridad OIDC de Entra CIAM para el Frontend (login authority)"
  type        = string
  default     = "https://kaiserox.ciamlogin.com/3ad481d8-3136-47be-99b9-98077738d480/v2.0"
}

variable "azure_issuer" {
  description = "Issuer OIDC exacto emitido en los tokens JWT para validación backend"
  type        = string
  default     = "https://3ad481d8-3136-47be-99b9-98077738d480.ciamlogin.com/3ad481d8-3136-47be-99b9-98077738d480/v2.0"
}

variable "backend_api_identifier_uri" {
  description = "Application ID URI de la API protegida Backend (Audience)"
  type        = string
  default     = "api://telemedicina-rural-api"
}

variable "entra_audience" {
  description = "Alias para backend_api_identifier_uri"
  type        = string
  default     = ""
}

variable "frontend_redirect_uri" {
  description = "URI SPA registrada en Microsoft Entra (por defecto usa la Elastic IP de la EC2)"
  type        = string
  default     = ""
}

variable "backend_read_scope_id" {
  description = "UUID estable del scope read expuesto por la API"
  type        = string
  default     = "11111111-1111-4111-8111-111111111111"
}

variable "backend_write_scope_id" {
  description = "UUID estable del scope write expuesto por la API"
  type        = string
  default     = "22222222-2222-4222-8222-222222222222"
}
