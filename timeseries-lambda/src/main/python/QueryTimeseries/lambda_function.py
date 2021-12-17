import boto3
import os
import logging


logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):
    # logger.info('## ENVIRONMENT VARIABLES\r' +os.environ)))
    logger.info('## EVENT\r' + str(event))
    logger.info('## CONTEXT\r' + str(context))

    BUCKET_NAME = 'timeseries-leonarduk'
    KEY = 'timeseries/PHGP.csv'
    s3 = boto3.client('s3','eu-west-1')
    response = s3.select_object_content(
        Bucket = BUCKET_NAME,
        Key = KEY,
        ExpressionType = 'SQL',
        Expression = "Select * from s3object s limit 10",
        InputSerialization = {'CSV': {"FileHeaderInfo": "NONE"}},
        OutputSerialization = {'JSON': {}},
    )

    for event in response['Payload']:
        if 'Records' in event:
            records = event['Records']['Payload'].decode('utf-8')
            print(F"Row {records}")

    let response = {
        statusCode: 200,
        headers: {
            "x-custom-header" : "my custom header value"
        },
        body: JSON.stringify(responseBody)
    }
    console.log("response: " + JSON.stringify(response))
    return response;
    return F"{ statusCode: 200,
    headers: {
    },
    body: {json.dumps(records)},
    isBase64Encoded:  false
    }"
    return records