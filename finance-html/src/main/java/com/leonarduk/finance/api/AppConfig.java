package com.leonarduk.finance.api;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import com.leonarduk.finance.stockfeed.*;
import com.leonarduk.finance.stockfeed.feed.alphavantage.AlphavantageFeed;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;

@Configuration
public class AppConfig {
    @Inject
    private DataSourceProperties dataSourceProperties;

    @Named
    public static class JerseyConfig extends ResourceConfig {
        public JerseyConfig() {
            this.register(CORSResponseFilter.class);
            this.register(PortfolioFeedEndpoint.class);
            this.register(StockFeedEndpoint.class);
            this.register(ImageService.class);
        }
    }

    @Bean
    public DataStore dataStore() {
        //TODO add details for non local DB
        String bucket = "portfolio";
        String org = "leonarduk";
        String token = "fX6n4UJqXg7Aq2OY7MerSxPB-624Sqwua4LVyRadKHlT91q3Wf-RopTm7YHZroT0actf46RrfXs9lR4i08sA2w==";
        String serverUrl = "http://localhost:8086";

        return new InfluxDBDataStore(bucket, org, token, serverUrl);
    }

    @Bean
    public FxFeed fxFeed() {
        return new AlphavantageFeed();
    }

    @Bean
    public SnapshotAnalyser snapshotAnalyser(DataStore dataStore){
        return new SnapshotAnalyser(dataStore);
    }

    @Bean
    public StockFeed stockFeed(DataStore dataStore) {
        return new IntelligentStockFeed(dataStore);
    }


    @Bean
    public DataSource dataSource() {
        final DataSource dataSource = DataSourceBuilder.create(this.dataSourceProperties.getClassLoader())
            .url(this.dataSourceProperties.getUrl()).username(this.dataSourceProperties.getUsername())
            .password(this.dataSourceProperties.getPassword()).build();
        return new DataSourceSpy(dataSource);
    }
}
