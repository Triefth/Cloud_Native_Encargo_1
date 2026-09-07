resource "aws_apigatewayv2_api" "telemedicina_api" {
  name          = "Telemedicina-API"
  protocol_type = "HTTP"

  cors_configuration {
    allow_headers = ["Authorization", "Content-Type", "X-Requested-With", "Accept"]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
    allow_origins = [var.frontend_redirect_uri]
    max_age       = 3600
  }
}

resource "aws_apigatewayv2_authorizer" "entra_jwt" {
  api_id           = aws_apigatewayv2_api.telemedicina_api.id
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]
  name             = "entra-id-jwt"

  jwt_configuration {
    audience = [azuread_application.backend.client_id]
    issuer   = var.azure_issuer
  }
}

resource "aws_apigatewayv2_integration" "ec2_integration" {
  api_id                 = aws_apigatewayv2_api.telemedicina_api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "http://${aws_eip.backend_eip.public_ip}:8080/{proxy}"
  payload_format_version = "1.0"
  request_parameters = {
    "overwrite:path" = "/$request.path.proxy"
  }
}

resource "aws_apigatewayv2_route" "proxy_route" {
  api_id             = aws_apigatewayv2_api.telemedicina_api.id
  route_key          = "ANY /{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.ec2_integration.id}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.entra_jwt.id
}

resource "aws_apigatewayv2_stage" "api_stage" {
  api_id      = aws_apigatewayv2_api.telemedicina_api.id
  name        = "$default"
  auto_deploy = true
}