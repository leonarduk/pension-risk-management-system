package com.leonarduk.aws.sqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {

    private  String ticker;
    private  int years;
    private  boolean cleanData;
    private  boolean interpolate;
}