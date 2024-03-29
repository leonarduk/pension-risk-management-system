package com.leonarduk.aws.apigateway;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.leonarduk.aws.QueryRunner;

import java.io.IOException;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class ApiGatewayHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final QueryRunner queryRunner;

    public ApiGatewayHandler() {
        this.queryRunner = new QueryRunner();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        // TODO: invoking the api call using s3Client.
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            responseEvent.setBody(this.queryRunner.getResults(input.getQueryStringParameters()));
            responseEvent.setStatusCode(200);
        } catch (IOException e) {
            e.printStackTrace();
            responseEvent.setBody("FAILED: " + e.getMessage());
            responseEvent.setStatusCode(500);
        }
        return responseEvent;
    }

}
