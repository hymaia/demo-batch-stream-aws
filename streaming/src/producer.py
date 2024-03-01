import os

import boto3
import json
from datetime import datetime
import random
import time

# Initialiser le client Kinesis
kinesis_client = boto3.client("kinesis", region_name="eu-west-1")

# Nom de votre Kinesis Data Stream
STREAM_NAME = os.environ["DATA_STREAM"]


# Fonction pour générer une adresse IP fictive
def generate_ip():
    return f"{random.randint(1, 255)}.{random.randint(1, 255)}.{random.randint(1, 255)}.{random.randint(1, 255)}"


# Fonction pour générer une URL fictive
def generate_url():
    return f"https://example.com/{random.randint(1, 50)}"


# Fonction pour envoyer un événement au Kinesis Data Stream
def send_event_to_kinesis(ip, url, date):
    data = {"ip": ip, "url": url, "date": date}
    kinesis_client.put_record(
        StreamName=STREAM_NAME,
        Data=json.dumps(data),
        PartitionKey=ip,  # Utilisation de l'IP comme clé de partition pour une distribution uniforme des enregistrements
    )


def main():
    while True:  # Boucle infinie pour envoyer des événements continuellement
        for _ in range(5):  # Envoyer 5 événements
            ip = generate_ip()
            url = generate_url()
            date = datetime.now().isoformat()
            send_event_to_kinesis(ip, url, date)
            print(f"Event sent: IP={ip}, URL={url}, Date={date}")
        time.sleep(10)  # Attendre 10 secondes avant d'envoyer le prochain lot


if __name__ == "__main__":
    main()
