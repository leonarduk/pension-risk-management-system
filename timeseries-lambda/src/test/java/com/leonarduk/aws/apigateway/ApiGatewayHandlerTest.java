package com.leonarduk.aws.apigateway;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.leonarduk.aws.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

class ApiGatewayHandlerTest {

    private ApiGatewayHandler buildHandler(AmazonS3 s3, QueryRunner runner, String bucket) throws Exception {
        ApiGatewayHandler handler = new ApiGatewayHandler(s3, bucket);
        if (runner != null) {
            Field field = ApiGatewayHandler.class.getDeclaredField("queryRunner");
            field.setAccessible(true);
            field.set(handler, runner);
        }
        return handler;
    }

    @Test
    void uploadsResultToS3() throws Exception {
        AmazonS3 s3 = Mockito.mock(AmazonS3.class);
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        Mockito.when(runner.getResults(Mockito.any())).thenReturn("HTML");

        ApiGatewayHandler handler = buildHandler(s3, runner, "bucket");

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of(QueryRunner.TICKER, "TEST"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        Assertions.assertEquals(200, response.getStatusCode());
        Mockito.verify(s3).putObject(Mockito.eq("bucket"), Mockito.eq("results/TEST.html"), Mockito.anyString());
    }

    @Test
    void s3FailureReturnsBadGateway() throws Exception {
        AmazonS3 s3 = Mockito.mock(AmazonS3.class);
        Mockito.doThrow(new AmazonServiceException("fail"))
                .when(s3).putObject(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        Mockito.when(runner.getResults(Mockito.any())).thenReturn("HTML");

        ApiGatewayHandler handler = buildHandler(s3, runner, "bucket");

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of(QueryRunner.TICKER, "TEST"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        Assertions.assertEquals(502, response.getStatusCode());
    }

    @Test
    void queryRunnerFailureReturnsServerError() throws Exception {
        AmazonS3 s3 = Mockito.mock(AmazonS3.class);
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        Mockito.when(runner.getResults(Mockito.any())).thenThrow(new IOException("io"));

        ApiGatewayHandler handler = buildHandler(s3, runner, "bucket");

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of(QueryRunner.TICKER, "TEST"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        Assertions.assertEquals(500, response.getStatusCode());
        Mockito.verify(s3, Mockito.never())
                .putObject(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
