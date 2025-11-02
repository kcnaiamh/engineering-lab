import json
import boto3

dynamodb = boto3.resource("dynamodb", region_name="us-west-2")
table = dynamodb.Table("TaskTable") # change table name

def lambda_handler(event, context):
    try:
        body = event
        task_id = body.get("task_id")

        if not task_id:
            return {"statusCode": 400, "body": json.dumps({"error": "task_id is required"})}

        response = table.delete_item(
            Key = {"task_id": task_id},
            ConditionExpression = "attribute_exists(task_id)"
        )

        return {"statusCode": 200, "body": json.dumps({"message": f"Task {task_id} deleted"})}

    except Exception as e:
        return {"statusCode": 500, "body": json.dumps({"error": str(e)})}
