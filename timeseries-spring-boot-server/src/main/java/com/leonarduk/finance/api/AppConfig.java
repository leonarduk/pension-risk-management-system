package com.leonarduk.finance.api;

import com.leonarduk.finance.stockfeed.DataStore;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Named;

@Configuration
public class AppConfig {
    @Named
    public static class JerseyConfig extends ResourceConfig {
        public JerseyConfig() {
            this.register(PortfolioFeedEndpoint.class);
            this.register(StockFeedEndpoint.class);
        }
    }

    @Bean
    public FileBasedDataStore fileBasedDataStore() {
        return new FileBasedDataStore("db");
    }


    @Bean
    public StockFeed stockFeed() {
        return new IntelligentStockFeed(fileBasedDataStore());
    }

    @Bean
    public StockFeed stockFeed(DataStore dataStore) {
        return new IntelligentStockFeed(dataStore);
    }

    @Bean
    public SnapshotAnalyser snapshotAnalyser(DataStore dataStore){
        return new SnapshotAnalyser(dataStore);
    }

}
