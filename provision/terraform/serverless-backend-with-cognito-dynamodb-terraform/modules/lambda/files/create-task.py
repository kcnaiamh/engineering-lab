import json
import boto3
import uuid
from datetime import datetime, timezone, timedelta

dynamodb = boto3.resource("dynamodb", region_name="us-west-2")
table = dynamodb.Table("TaskTable") # change table name

def lambda_handler(event, context):
    try:
        body = event

        title = body.get("title")
        description = body.get("description")
        task_status = body.get("task_status")
        priority = body.get("priority")

        if not title:
            return {"statusCode": 400, "body": json.dumps({"error": "Task name is required"})}

        task_id = str(uuid.uuid4())
        item = {
            "task_id": task_id,
            "title": title,
            "description": description,
            "task_status": task_status,
            "priority": priority,
            "created_at": datetime.now(timezone(timedelta(hours=6))).isoformat(),
        }

        table.put_item(Item=item)

        return {"statusCode": 201, "body": json.dumps(item)}

    except Exception as e:
        return {"statusCode": 500, "body": json.dumps({"error": str(e)})}
