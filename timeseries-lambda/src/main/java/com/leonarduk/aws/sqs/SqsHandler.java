package com.leonarduk.aws.sqs;


import com.amazonaws.lambda.thirdparty.com.google.gson.Gson;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.leonarduk.aws.QueryRunner;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.Map;

@Slf4j
public class SqsHandler implements RequestHandler<SQSEvent, Void> {
    private final QueryRunner queryRunner;

    public SqsHandler() {
        this.queryRunner = new QueryRunner();
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        if (event == null || event.getRecords() == null || event.getRecords().isEmpty()){
            log.info("SQS Message has no records");
        }

        assert event != null;
        for (SQSMessage msg : event.getRecords()) {
            String messageBody = msg.getBody();
            try {
                this.queryRunner.getResults(getParameterMap(messageBody));
            } catch (Exception e) {
                System.err.printf("""
                        Error parsing message\s
                         %s.\s
                         %s%n""", messageBody, e.getMessage());
            }
        }
        return null;
    }

    public Map<String, String> getParameterMap(String messageBody) {
        Gson gson = new Gson();
        QueryRequest request = gson.fromJson(messageBody, QueryRequest.class);
        return ImmutableMap.of(
                QueryRunner.TICKER, request.getTicker(),
                QueryRunner.YEARS, String.valueOf(request.getYears()),
                QueryRunner.CLEAN_DATA, String.valueOf(request.isCleanData()),
                QueryRunner.INTERPOLATE, String.valueOf(request.isInterpolate()));
    }
}