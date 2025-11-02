import json
import boto3

dynamodb = boto3.resource("dynamodb", region_name="us-west-2")
table = dynamodb.Table("TaskTable") # change table name

def lambda_handler(event, context):
    try:
        response = table.scan()
        tasks = response.get("Items", [])

        return {"statusCode": 200, "body": json.dumps(tasks)}

    except Exception as e:
        return {"statusCode": 500, "body": json.dumps({"error": str(e)})}
