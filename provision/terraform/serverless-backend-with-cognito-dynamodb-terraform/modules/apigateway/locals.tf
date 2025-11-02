locals {
  api_methods = {
    for k, v in var.lambda_functions_arns : k => {
      lambda_arn  = v
      resource_id = k == "delete" ? aws_api_gateway_resource.task_item.id : aws_api_gateway_resource.tasks.id
      http_method = upper(k)
    }
  }
}
