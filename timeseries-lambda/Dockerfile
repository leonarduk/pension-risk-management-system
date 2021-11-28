FROM adoptopenjdk/openjdk15:ubuntu-jre


COPY target/dependency/* /function/
COPY target/timeseries-lambda.jar /function

ENTRYPOINT [ "/opt/java/openjdk/bin/java", "-cp", "/function/*", "com.amazonaws.services.lambda.runtime.api.client.AWSLambda" ]
CMD ["de.rieckpil.blog.Java15Lambda::handleRequest"]