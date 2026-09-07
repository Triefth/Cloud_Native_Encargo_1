resource "aws_api_gateway_rest_api" "telemedicina_api" {
  name        = "Telemedicina-API"
  description = "API Gateway para redirigir peticiones al BFF de Telemedicina"
}

resource "aws_api_gateway_resource" "proxy_resource" {
  rest_api_id = aws_api_gateway_rest_api.telemedicina_api.id
  parent_id   = aws_api_gateway_rest_api.telemedicina_api.root_resource_id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "proxy_method" {
  rest_api_id   = aws_api_gateway_rest_api.telemedicina_api.id
  resource_id   = aws_api_gateway_resource.proxy_resource.id
  http_method   = "ANY"
  authorization = "NONE" # La validación JWT (MSAL) se hace en el backend/BFF
}

# Integración con EC2
resource "aws_api_gateway_integration" "ec2_integration" {
  rest_api_id             = aws_api_gateway_rest_api.telemedicina_api.id
  resource_id             = aws_api_gateway_resource.proxy_resource.id
  http_method             = aws_api_gateway_method.proxy_method.http_method
  integration_http_method = "ANY"
  type                    = "HTTP_PROXY"
  uri                     = "http://${aws_eip.backend_eip.public_ip}:8080/{proxy}"
}

resource "aws_api_gateway_deployment" "api_deployment" {
  depends_on  = [aws_api_gateway_integration.ec2_integration]
  rest_api_id = aws_api_gateway_rest_api.telemedicina_api.id
}

resource "aws_api_gateway_stage" "api_stage" {
  deployment_id = aws_api_gateway_deployment.api_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.telemedicina_api.id
  stage_name    = "prod"
}