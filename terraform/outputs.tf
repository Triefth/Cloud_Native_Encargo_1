output "backend_public_ip" {
  description = "IP Publica fija (Elastic IP) de la instancia EC2 de Backend"
  value       = aws_eip.backend_eip.public_ip
}

output "frontend_public_ip" {
  description = "IP Publica fija (Elastic IP) de la instancia EC2 de Frontend"
  value       = aws_eip.frontend_eip.public_ip
}

output "frontend_url" {
  description = "URL de acceso al portal web de Frontend"
  value       = "http://${aws_eip.frontend_eip.public_ip}"
}

output "ec2_public_ip" {
  description = "IP Publica fija (Elastic IP) de la instancia EC2 Backend (legacy alias)"
  value       = aws_eip.backend_eip.public_ip
}

output "ec2_ssh_private_key" {
  description = "Llave privada SSH generada por Terraform, usada por el pipeline de CI/CD para desplegar"
  value       = tls_private_key.deploy_key.private_key_openssh
  sensitive   = true
}

output "api_gateway_invoke_url" {
  description = "URL base del API Gateway (para configurar en Angular)"
  value       = aws_apigatewayv2_stage.api_stage.invoke_url
}

output "azure_backend_client_id" {
  description = "Client ID de la API backend registrada en Microsoft Entra"
  value       = azuread_application.backend.client_id
}

output "azure_frontend_client_id" {
  description = "Client ID de la SPA registrada en Microsoft Entra"
  value       = azuread_application.frontend.client_id
}

output "azure_backend_scope_read" {
  description = "Scope read completo para MSAL"
  value       = "${var.backend_api_identifier_uri}/read"
}