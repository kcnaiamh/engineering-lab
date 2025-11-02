variable "lambda_functions" {
  description = "Map of Lambda function configurations"
  type = map(object({
    handler  = string
    runtime  = string
    filename = string
    env_vars = map(string)
  }))
}

variable "dynamodb_table_arn" {
  description = "ARN of the DynamoDB table for IAM policy"
  type        = string
}

variable "file_names" {
  description = "Lambda file names"
  type        = map(string)
}
