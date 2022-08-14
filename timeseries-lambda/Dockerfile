FROM openjdk:18-jdk

# https://medium.com/@MustehssunI/deploying-java-application-on-aws-lambda-using-docker-a95f6fc159da

COPY target/dependency/* /function/
COPY target/timeseries-lambda-1.0-SNAPSHOT.jar /function

ENTRYPOINT [ "/opt/java/openjdk/bin/java", "-cp", "/function/*", "com.amazonaws.services.lambda.runtime.api.client.AWSLambda" ]
CMD ["com.leonarduk.aws.App::handleRequest"]

