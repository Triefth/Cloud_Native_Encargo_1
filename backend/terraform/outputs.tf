output "ec2_public_ip" {
  description = "IP Publica fija (Elastic IP) de la instancia EC2"
  value       = aws_eip.backend_eip.public_ip
}

output "ec2_ssh_private_key" {
  description = "Llave privada SSH generada por Terraform, usada por el pipeline de CI/CD para desplegar"
  value       = tls_private_key.deploy_key.private_key_openssh
  sensitive   = true
}

output "api_gateway_invoke_url" {
  description = "URL base del API Gateway (para configurar en Angular)"
  value       = aws_api_gateway_stage.api_stage.invoke_url
}