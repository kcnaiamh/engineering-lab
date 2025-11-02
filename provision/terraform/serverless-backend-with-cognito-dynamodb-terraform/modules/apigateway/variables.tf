variable "api_name" {
  description = "Name of the API Gateway"
  type        = string
}

variable "lambda_functions_arns" {
  description = "Map of Lambda ARNs for integration"
  type        = map(string)
}

variable "cognito_user_pool_arn" {
  description = "ARN of the Cognito User Pool for authorizer"
  type        = string
}

variable "resource_server_identifier" {
  description = "Identifier for the Cognito Resource Server"
  type        = string
}
