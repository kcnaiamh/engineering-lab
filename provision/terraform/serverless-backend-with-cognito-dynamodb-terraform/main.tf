module "dynamodb" {
  source = "./modules/dynamodb"

  table_name = var.table_name
}

module "cognito" {
  source = "./modules/cognito"

  user_pool_name             = var.user_pool_name
  resource_server_identifier = var.resource_server_identifier
  app_client_name            = var.app_client_name
}

module "lambda" {
  source = "./modules/lambda"

  lambda_functions   = var.lambda_functions
  dynamodb_table_arn = module.dynamodb.table_arn
  file_names         = var.file_names
}

module "apigateway" {
  source = "./modules/apigateway"

  api_name                   = var.api_name
  lambda_functions_arns      = module.lambda.lambda_arns
  cognito_user_pool_arn      = module.cognito.user_pool_arn
  resource_server_identifier = var.resource_server_identifier
}
