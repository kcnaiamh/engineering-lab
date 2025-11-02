# Create Cognito User Pool
resource "aws_cognito_user_pool" "pool" {
  name = var.user_pool_name
}

# Create Resource Server for scopes (required for client credentials)
resource "aws_cognito_resource_server" "resource_server" {
  identifier   = var.resource_server_identifier # Unique identifier for this resource server
  name         = "${var.user_pool_name}-resource-server"
  user_pool_id = aws_cognito_user_pool.pool.id

  # Define a custom scope that clients can request
  scope {
    scope_name        = "access"
    scope_description = "All access"
  }
}

# Create User Pool Client for machine-to-machine (client credentials)
resource "aws_cognito_user_pool_client" "client" {
  name                                 = var.app_client_name
  user_pool_id                         = aws_cognito_user_pool.pool.id
  generate_secret                      = true                                         # Required for client credentials
  allowed_oauth_flows                  = ["client_credentials"]                       # Enables OAuth 2.0 client credentials grant type
  allowed_oauth_scopes                 = ["${var.resource_server_identifier}/access"] # Scopes this client can request
  allowed_oauth_flows_user_pool_client = true                                         # Enables OAuth flows for this client
  depends_on                           = [aws_cognito_resource_server.resource_server]
}

# Create a custom domain for the Cognito hosted UI
# This provides a user-facing domain for OAuth endpoints like /oauth2/token
resource "aws_cognito_user_pool_domain" "main" {
  domain       = "todo-task-121" # Must be globally unique across all Cognito user pools
  user_pool_id = aws_cognito_user_pool.pool.id
}
