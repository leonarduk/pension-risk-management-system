
package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * The module containing all dependencies required by the {@link App}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of S3Client
     */
    public static S3Client s3Client() {
        return S3Client.builder()
                       .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                       .region(Region.EU_WEST_1)
                       .httpClientBuilder(UrlConnectionHttpClient.builder())
                       .build();
    }

    public static StockFeed stockFeed() {
//        return new IntelligentStockFeed(new S3DataStore());
        return new IntelligentStockFeed(new FileBasedDataStore("db"));
    }
}
