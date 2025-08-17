package com.leonarduk.aws.apigateway;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.leonarduk.aws.QueryRunner;

import java.io.IOException;
import java.util.Map;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class ApiGatewayHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final QueryRunner queryRunner;
    private final AmazonS3 s3Client;
    private final String resultBucket;

    public ApiGatewayHandler() {
        this(AmazonS3ClientBuilder.standard().build(), System.getenv("RESULT_BUCKET"));
    }

    ApiGatewayHandler(AmazonS3 s3Client, String resultBucket) {
        this.queryRunner = new QueryRunner();
        this.s3Client = s3Client;
        this.resultBucket = resultBucket;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            Map<String, String> params = input.getQueryStringParameters();
            String result = this.queryRunner.getResults(params);

            if (this.s3Client != null && this.resultBucket != null && !this.resultBucket.isBlank()) {
                String ticker = params != null ? params.get(QueryRunner.TICKER) : "result";
                String key = "results/" + (ticker != null ? ticker : "result") + ".json";
                this.s3Client.putObject(this.resultBucket, key, result);
            }

            responseEvent.setBody(result);
            responseEvent.setStatusCode(200);
            responseEvent.setHeaders(Map.of("Content-Type", "application/json"));
        } catch (SdkClientException e) {
            responseEvent.setBody("S3_ERROR: " + e.getMessage());
            responseEvent.setStatusCode(502);
        } catch (IOException e) {
            e.printStackTrace();
            responseEvent.setBody("FAILED: " + e.getMessage());
            responseEvent.setStatusCode(500);
        }
        return responseEvent;
    }

}
