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
  default     = "develop"
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

variable "azure_tenant_id" {
  description = "Tenant ID de Microsoft Entra. La creacion del tenant queda fuera de Terraform."
  type        = string
}

variable "azure_issuer" {
  description = "Issuer OIDC exacto usado por el User Flow de Entra External ID"
  type        = string
}

variable "frontend_redirect_uri" {
  description = "URI SPA registrada en Microsoft Entra (debe terminar en / si no tiene path)"
  type        = string
  default     = "http://localhost:5173/"
}

variable "backend_api_identifier_uri" {
  description = "Application ID URI de la API protegida"
  type        = string
  default     = "api://telemedicina-rural-api"
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