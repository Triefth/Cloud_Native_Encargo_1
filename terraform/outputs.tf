output "backend_public_ip" {
  description = "IP Publica fija (Elastic IP) de la instancia EC2 de Backend"
  value       = aws_eip.backend_eip.public_ip
}

output "frontend_public_ip" {
  description = "IP Publica fija (Elastic IP) de la instancia EC2 de Frontend"
  value       = aws_eip.frontend_eip.public_ip
}

output "frontend_url" {
  description = "URL HTTP de acceso al portal web de Frontend"
  value       = "http://${aws_eip.frontend_eip.public_ip}"
}

output "frontend_https_url" {
  description = "URL HTTPS segura requerida para autenticacion con Microsoft Entra ID"
  value       = "https://${aws_eip.frontend_eip.public_ip}"
}

output "azure_redirect_uri" {
  description = "URI EXACTA a registrar en Azure Portal (App Registrations -> Authentication -> Single-page application)"
  value       = "https://${aws_eip.frontend_eip.public_ip}/"
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
  description = "URL base del API Gateway de AWS"
  value       = aws_apigatewayv2_stage.api_stage.invoke_url
}

output "azure_frontend_client_id" {
  description = "Client ID de la SPA configurada en Microsoft Entra"
  value       = local.azure_client_id
}

output "azure_audience" {
  description = "Audience de la API backend en Microsoft Entra"
  value       = local.azure_audience
}

output "azure_backend_scope_read" {
  description = "Scope read completo para MSAL"
  value       = "${local.azure_audience}/read"
}

output "kafka_ui_url" {
  description = "URL para acceder a Kafka UI en el puerto 8090"
  value       = "http://${aws_eip.backend_eip.public_ip}:8090"
}

output "kafka_service_url" {
  description = "URL para acceder al microservicio Kafka / Pipeline en el puerto 8888"
  value       = "http://${aws_eip.backend_eip.public_ip}:8888"
}
