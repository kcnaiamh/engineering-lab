variable "user_pool_name" {
  description = "Name of the Cognito User Pool"
  type        = string
}

variable "resource_server_identifier" {
  description = "Identifier for the Cognito Resource Server"
  type        = string
}

variable "app_client_name" {
  description = "Name of the Cognito App Client"
  type        = string
}
