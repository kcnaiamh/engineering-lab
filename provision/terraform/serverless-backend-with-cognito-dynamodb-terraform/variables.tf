variable "region" {
  description = "AWS region"
  type        = string
}

variable "table_name" {
  description = "DynamoDB table name"
  type        = string
}

variable "user_pool_name" {
  description = "Cognito User Pool name"
  type        = string
}

variable "resource_server_identifier" {
  description = "Cognito Resource Server identifier"
  type        = string
}

variable "app_client_name" {
  description = "Cognito App Client name"
  type        = string
}

variable "api_name" {
  description = "API Gateway name"
  type        = string
}

variable "lambda_functions" {
  description = "Map of Lambda functions configurations"
  type = map(object({
    handler  = string
    runtime  = string
    filename = string
    env_vars = map(string)
  }))
}

variable "file_names" {
  description = "Lambda file names"
  type        = map(string)
}
