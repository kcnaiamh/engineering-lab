import json
import boto3
from botocore.exceptions import ClientError

dynamodb = boto3.resource("dynamodb", region_name="us-west-2")
table = dynamodb.Table("TaskTable") # change table name

def lambda_handler(event, context):
    try:
        body = event

        task_id = body.get("task_id")

        if not task_id:
            return {
                "statusCode": 400,
                "body": json.dumps({"error": "task_id is required"})
            }

        # Map of allowed fields to update
        allowed_fields = {
            "title": body.get("title"),
            "description": body.get("description"),
            "task_status": body.get("task_status"),
            "priority": body.get("priority")
        }

        update_expression = []
        expression_values = {}

        # Build update expression for each provided field
        for field, value in allowed_fields.items():
            if value is not None:
                update_expression.append(f"{field} = :{field}")
                expression_values[f":{field}"] = value

        if not update_expression:
            return {
                "statusCode": 400,
                "body": json.dumps({"error": "No fields to update"})
            }

        # Perform the update only if the item exists
        try:
            response = table.update_item(
                Key={"task_id": task_id},
                UpdateExpression="SET " + ", ".join(update_expression),
                ExpressionAttributeValues=expression_values,
                ConditionExpression="attribute_exists(task_id)",  # <-- Only update if task exists
                ReturnValues="ALL_NEW"
            )

            # Convert Decimal types to standard types for JSON serialization
            updated_item = json.loads(json.dumps(response["Attributes"], default=str))

            return {
                "statusCode": 200,
                "body": json.dumps(updated_item)
            }

        except ClientError as e:
            if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
                return {
                    "statusCode": 404,
                    "body": json.dumps({"error": f"Task with id {task_id} does not exist"})
                }
            else:
                raise

    except json.JSONDecodeError:
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Invalid JSON in request body"})
        }

    except Exception as e:
        print(f"Error updating task: {str(e)}")  # CloudWatch logging
        return {
            "statusCode": 500,
            "body": json.dumps({"error": "Internal server error"})
        }
