package com.leonarduk.aws.sqs;


import com.amazonaws.lambda.thirdparty.com.google.gson.Gson;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.common.collect.ImmutableMap;
import com.leonarduk.aws.QueryRunner;

public class SqsHandler implements RequestHandler<SQSEvent, Void> {
    private final QueryRunner queryRunner;

    public SqsHandler() {
        this.queryRunner = new QueryRunner();
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSMessage msg : event.getRecords()) {
            String messageBody = msg.getBody();
            Gson gson = new Gson();
            try {
                QueryRequest request = gson.fromJson(messageBody, QueryRequest.class);
                ImmutableMap<String, String> parameters = ImmutableMap.of(
                        QueryRunner.TICKER, request.ticker(),
                        QueryRunner.YEARS, String.valueOf(request.years()),
                        QueryRunner.CLEAN_DATA, String.valueOf(request.cleanData()),
                        QueryRunner.INTERPOLATE, String.valueOf(request.interpolate()));
                this.queryRunner.getResults(parameters);
            } catch (Exception e) {
                System.err.println(String.format("Error parsing message \n %s. \n %s", messageBody));
            }
        }
        return null;
    }
}