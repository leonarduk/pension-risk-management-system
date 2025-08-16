package com.leonarduk.aws.sqs;


import com.amazonaws.lambda.thirdparty.com.google.gson.Gson;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.leonarduk.aws.QueryRunner;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class SqsHandler implements RequestHandler<SQSEvent, Void> {
    private final QueryRunner queryRunner;

    public SqsHandler() {
        queryRunner = new QueryRunner();
    }

    @Override
    public Void handleRequest(final SQSEvent event, final Context context) {
        if (null == event || null == event.getRecords() || event.getRecords().isEmpty()){
            SqsHandler.log.info("SQS Message has no records");
        }

        assert null != event;
        for (final SQSEvent.SQSMessage msg : event.getRecords()) {
            final String messageBody = msg.getBody();
            try {
                queryRunner.getResults(this.getParameterMap(messageBody));
            } catch (final Exception e) {
                System.err.printf("""
                        Error parsing message\s
                         %s.\s
                         %s%n""", messageBody, e.getMessage());
            }
        }
        return null;
    }

    public Map<String, String> getParameterMap(final String messageBody) {
        final Gson gson = new Gson();
        final QueryRequest request = gson.fromJson(messageBody, QueryRequest.class);
        return Map.of(
                QueryRunner.TICKER, request.getTicker(),
                QueryRunner.YEARS, String.valueOf(request.getYears()),
                QueryRunner.CLEAN_DATA, String.valueOf(request.isCleanData()),
                QueryRunner.INTERPOLATE, String.valueOf(request.isInterpolate()));
    }
}