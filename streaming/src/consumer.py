import base64
import os
import boto3
import json

# Initialisez les clients pour DynamoDB et Kinesis Firehose
dynamodb = boto3.resource("dynamodb", region_name="eu-west-1")
firehose = boto3.client("firehose", region_name="eu-west-1")

# Noms de la table DynamoDB et du stream Firehose
TABLE_NAME = os.environ["TABLE_NAME"]
FIREHOSE_STREAM_NAME = os.environ["FIREHOSE_NAME"]


def lambda_handler(event, context):
    firehose_records = []

    for record in event["Records"]:
        # Décodez les données Kinesis
        payload = base64.b64decode(record["kinesis"]["data"])
        data = json.loads(payload)

        # Récupérez les champs de l'événement
        ip = data["ip"]
        # url et date peuvent être utilisés si nécessaire

        # Mettez à jour le compteur pour l'IP dans DynamoDB
        table = dynamodb.Table(TABLE_NAME)
        response = table.update_item(
            Key={"ip": ip},
            UpdateExpression="ADD views_count :inc",
            ExpressionAttributeValues={":inc": 1},
            ReturnValues="UPDATED_NEW",
        )

        # Ajoutez l'enregistrement à la liste pour Firehose
        firehose_record = {"Data": json.dumps(data)}
        firehose_records.append(firehose_record)

    # Envoyez les enregistrements à Firehose en une seule opération batch
    if firehose_records:
        response = firehose.put_record_batch(
            DeliveryStreamName=FIREHOSE_STREAM_NAME, Records=firehose_records
        )

    return "Succès!"
