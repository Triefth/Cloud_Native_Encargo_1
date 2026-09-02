output "ec2_public_ip" {
  description = "IP Publica de la instancia EC2"
  value       = aws_instance.backend_server.public_ip
}

output "api_gateway_invoke_url" {
  description = "URL base del API Gateway (para configurar en Angular)"
  value       = aws_api_gateway_stage.api_stage.invoke_url
}