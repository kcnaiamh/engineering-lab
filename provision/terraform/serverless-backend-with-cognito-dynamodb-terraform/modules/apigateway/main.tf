# Get current AWS region for constructing ARNs
data "aws_region" "current" {}

# Main REST API Gateway for the Todo application
resource "aws_api_gateway_rest_api" "todo_api" {
  name        = var.api_name
  description = "REST API for Todo Task management"
}

# Cognito authorizer to protect API endpoints with user pool authentication
resource "aws_api_gateway_authorizer" "cognito_authorizer" {
  name            = "cognito_authorizer"
  rest_api_id     = aws_api_gateway_rest_api.todo_api.id
  type            = "COGNITO_USER_POOLS"
  provider_arns   = [var.cognito_user_pool_arn]
  identity_source = "method.request.header.Authorization"
}

# Base /tasks resource for listing and creating tasks
resource "aws_api_gateway_resource" "tasks" {
  rest_api_id = aws_api_gateway_rest_api.todo_api.id
  parent_id   = aws_api_gateway_rest_api.todo_api.root_resource_id
  path_part   = "tasks"
}

# Individual task resource /tasks/{task_id} for get, update, delete operations
resource "aws_api_gateway_resource" "task_item" {
  rest_api_id = aws_api_gateway_rest_api.todo_api.id
  parent_id   = aws_api_gateway_resource.tasks.id
  path_part   = "{task_id}"
}

# HTTP methods (GET, POST, PATCH, DELETE) protected by Cognito with custom scope
resource "aws_api_gateway_method" "methods" {
  for_each             = local.api_methods
  rest_api_id          = aws_api_gateway_rest_api.todo_api.id
  resource_id          = each.value.resource_id
  http_method          = each.value.http_method
  authorization        = "COGNITO_USER_POOLS"
  authorizer_id        = aws_api_gateway_authorizer.cognito_authorizer.id
  authorization_scopes = ["${var.resource_server_identifier}/access"]
}

# Lambda integrations for each method - maps API requests to Lambda functions
resource "aws_api_gateway_integration" "integrations" {
  for_each                = local.api_methods
  rest_api_id             = aws_api_gateway_rest_api.todo_api.id
  resource_id             = each.value.resource_id
  http_method             = aws_api_gateway_method.methods[each.key].http_method
  integration_http_method = "POST" # Lambda is always invoked via POST
  type                    = "AWS"
  uri                     = "arn:aws:apigateway:${data.aws_region.current.region}:lambda:path/2015-03-31/functions/${each.value.lambda_arn}/invocations"

  # DELETE method needs request template to pass task_id from URL path to Lambda
  request_templates = each.key == "delete" ? {
    "application/json" = "{ \"task_id\": \"$input.params('task_id')\" }"
  } : {}
}

# Define successful response structure for each method
resource "aws_api_gateway_method_response" "method_response" {
  for_each    = local.api_methods
  rest_api_id = aws_api_gateway_rest_api.todo_api.id
  resource_id = each.value.resource_id
  http_method = aws_api_gateway_method.methods[each.key].http_method
  status_code = "200"
  response_models = {
    "application/json" = "Empty"
  }
}

# Map Lambda responses back to API Gateway responses
resource "aws_api_gateway_integration_response" "integration_response" {
  for_each           = local.api_methods
  rest_api_id        = aws_api_gateway_rest_api.todo_api.id
  resource_id        = each.value.resource_id
  http_method        = aws_api_gateway_method.methods[each.key].http_method
  status_code        = aws_api_gateway_method_response.method_response[each.key].status_code
  response_templates = {}
  depends_on = [
    aws_api_gateway_integration.integrations,
    aws_api_gateway_method.methods,
    aws_api_gateway_method_response.method_response
  ]
}

# Grant API Gateway permission to invoke Lambda functions
resource "aws_lambda_permission" "api_gateway_permissions" {
  for_each      = var.lambda_functions_arns
  statement_id  = "AllowAPIGatewayInvoke_${each.key}"
  action        = "lambda:InvokeFunction"
  function_name = each.value
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.todo_api.execution_arn}/*/*"
}

# Deployment of the API - triggers redeploy when configuration changes
resource "aws_api_gateway_deployment" "deployment" {
  rest_api_id = aws_api_gateway_rest_api.todo_api.id

  # SHA1 hash ensures redeployment when any API component changes
  triggers = {
    redeployment = sha1(jsonencode(concat(
      [
        aws_api_gateway_authorizer.cognito_authorizer.id,
        aws_api_gateway_resource.tasks.id,
        aws_api_gateway_resource.task_item.id
      ],
      [for i in aws_api_gateway_method.methods : i.id],
      [for i in aws_api_gateway_integration.integrations : i.id],
      [for i in aws_api_gateway_integration_response.integration_response : i.id],
      [for i in aws_api_gateway_method_response.method_response : i.id],
    )))
  }

  depends_on = [
    aws_api_gateway_method.methods,
    aws_api_gateway_integration.integrations,
    aws_api_gateway_method_response.method_response,
    aws_api_gateway_integration_response.integration_response
  ]

  lifecycle {
    create_before_destroy = true
  }
}

# Production stage that makes the API publicly accessible
resource "aws_api_gateway_stage" "prod" {
  rest_api_id   = aws_api_gateway_rest_api.todo_api.id
  deployment_id = aws_api_gateway_deployment.deployment.id
  stage_name    = "prod"
}
