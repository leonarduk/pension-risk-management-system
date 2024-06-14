package com.leonarduk.finance.springboot;

import com.leonarduk.finance.stockfeed.DataStore;
import com.leonarduk.finance.stockfeed.FxFeed;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.alphavantage.AlphavantageFeed;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    StockFeed stockFeed(DataStore dataStore){
        return  new IntelligentStockFeed(dataStore);
    }

    @Bean
    FxFeed fxFeed(){
        return new AlphavantageFeed();
    }

    @Value("${filestore.path}")
    @Bean
    DataStore dataStore(String fileLocation){
        return  new FileBasedDataStore(fileLocation);
    }

}
