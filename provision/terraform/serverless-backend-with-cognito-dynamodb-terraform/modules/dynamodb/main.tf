resource "aws_dynamodb_table" "table" {
  name         = var.table_name
  billing_mode = "PAY_PER_REQUEST" # On-demand capacity mode

  hash_key = "task_id" # partition key

  attribute {
    name = "task_id"
    type = "S" # String type
  }

  tags = {
    Name = var.table_name
  }
}
