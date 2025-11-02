output "lambda_arns" {
  description = "Map of Lambda function ARNs"
  value = {
    for key, func in aws_lambda_function.functions : key => func.arn
  }
}
