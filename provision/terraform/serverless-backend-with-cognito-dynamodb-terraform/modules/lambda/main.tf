# IAM Role for Lambda functions
resource "aws_iam_role" "lambda_role" {
  name = "lambda_dynamodb_tasktable_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# IAM Policy for DynamoDB access
resource "aws_iam_policy" "dynamodb_policy" {
  name        = "lambda_dynamodb_tasktable_policy"
  description = "Policy for Lambda to access DynamoDB table"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action : [
          "dynamodb:PutItem",
          "dynamodb:DeleteItem",
          "dynamodb:Scan",
          "dynamodb:UpdateItem"
        ]
        Effect   = "Allow"
        Resource = var.dynamodb_table_arn
      }
    ]
  })
}

# Attach policy to role
resource "aws_iam_role_policy_attachment" "dynamodb_attachment" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.dynamodb_policy.arn
}

# Attach basic Lambda execution policy
resource "aws_iam_role_policy_attachment" "basic_execution" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Zip python files
data "archive_file" "zips" {
  for_each    = var.file_names
  type        = "zip"
  source_file = "${path.module}/files/${each.value}"
  output_path = "${path.module}/zips/${each.key}.zip"
}

# Create Lambda functions from map
resource "aws_lambda_function" "functions" {
  for_each = var.lambda_functions

  function_name = "${each.key}_lambda"
  handler       = each.value.handler
  runtime       = each.value.runtime
  filename      = each.value.filename
  role          = aws_iam_role.lambda_role.arn

  depends_on = [data.archive_file.zips]

  environment {
    variables = each.value.env_vars
  }

  tags = {
    Name = "${each.key}_lambda"
  }
}
