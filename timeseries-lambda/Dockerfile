FROM openjdk:18-jdk

# Placeholder environment variables for AWS credentials and SQS configuration
ENV AWS_ACCESS_KEY_ID="" \
    AWS_SECRET_ACCESS_KEY="" \
    AWS_REGION="" \
    SQS_QUEUE_URL=""

# https://medium.com/@MustehssunI/deploying-java-application-on-aws-lambda-using-docker-a95f6fc159da

COPY target/dependency/* /function/
COPY target/timeseries-lambda-1.0-SNAPSHOT.jar /function

# https://github.com/aws/aws-lambda-java-libs/issues/261
ENTRYPOINT [ "java", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-cp", "/function/*", "com.amazonaws.services.lambda.runtime.api.client.AWSLambda" ]
#CMD ["com.leonarduk.aws.apigateway.ApiGatewayHandler::handleRequest"]
CMD ["com.leonarduk.aws.sqs.SqsHandler::handleRequest"]

