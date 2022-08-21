package com.leonarduk.aws.sqs;

public record QueryRequest(String ticker, int years, boolean cleanData, boolean interpolate) {

}
