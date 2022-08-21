package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class DependencyFactory {

    private DependencyFactory() {
    }

    public static StockFeed stockFeed() {
        return new IntelligentStockFeed(new S3DataStore("timeseries-leonarduk",
                "timeseries", Region.EU_WEST_1.toString()));
    }
}
