import os
from datetime import datetime
import requests
from bs4 import BeautifulSoup
import boto3


def fetchFields(url: str):
    html_text = requests.get(url).text
    soup = BeautifulSoup(html_text, 'html.parser')

    field_map = {}
    table = soup.find('table', attrs={'class': 'table--plain table-stats'})

    rows = table.find_all('tr')
    for row in rows:
        cols = row.find_all('td')
        field_map[cols[0].get_text().rstrip()] = cols[1].get_text()

    return field_map

def saveFieldsToDb(fields: dict):
    datestring = datetime.today().strftime('%Y-%m-%d')
    dynamodb = boto3.client('dynamodb')
    for field in fields:
        dynamodb.put_item(TableName='HomeStats',
                          Item={
                              'Field': {'S': field},
                              'Date': {'S': datestring},
                              'Value': {'S': fields[field]}
                          }
                          )


def fetchField(field: str, url: str):
    return fetchFields(url)[field]


def lambda_handler(event, context):
    SITE = os.environ['site']  # URL of the site to check, stored in the site environment variable
    print('Checking {} at {}...'.format(SITE, event['time']))
    try:
        field_map = fetchFields(SITE)
        print(field_map)
        saveFieldsToDb(field_map)
    except:
        print('Check failed!')
        raise
    else:
        print('Check passed!')
        return event['time']
    finally:
        print('Check complete at {}'.format(str(datetime.now())))
