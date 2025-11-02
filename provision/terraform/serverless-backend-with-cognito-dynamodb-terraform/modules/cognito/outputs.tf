output "user_pool_arn" {
  description = "Cognito User Pool ARN"
  value       = aws_cognito_user_pool.pool.arn
}

output "app_client_id" {
  description = "Cognito App Client ID"
  value       = aws_cognito_user_pool_client.client.id
}

output "app_client_secret" {
  description = "Cognito App Client Secret"
  value       = aws_cognito_user_pool_client.client.client_secret
  sensitive   = true
}
