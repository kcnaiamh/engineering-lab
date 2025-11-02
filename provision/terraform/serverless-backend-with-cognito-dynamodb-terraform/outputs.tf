output "cognito_app_client_id" {
  description = "Cognito App Client ID"
  value       = module.cognito.app_client_id
}

output "cognito_app_client_secret" {
  description = "Cognito App Client Secret"
  value       = module.cognito.app_client_secret
  sensitive   = true
}

output "api_endpoint" {
  description = "API Gateway endpoint URL"
  value       = module.apigateway.api_endpoint
}
