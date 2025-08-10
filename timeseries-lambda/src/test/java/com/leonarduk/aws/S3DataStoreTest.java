package com.leonarduk.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

class S3DataStoreTest {

    private S3DataStore createStore(boolean objectExists, Instrument instrument) throws Exception {
        S3DataStore store = new S3DataStore("bucket", "folder", "eu-west-1");
        AmazonS3 mockS3 = (AmazonS3) Proxy.newProxyInstance(
                AmazonS3.class.getClassLoader(),
                new Class[]{AmazonS3.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("doesObjectExist")) {
                        String bucket = (String) args[0];
                        String key = (String) args[1];
                        boolean matches = bucket.equals("bucket") && key.equals("folder/" + instrument.code() + ".csv");
                        return objectExists && matches;
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    return null;
                });
        Field field = S3DataStore.class.getDeclaredField("s3");
        field.setAccessible(true);
        field.set(store, mockS3);
        return store;
    }

    @Test
    void containsReturnsTrueWhenObjectExists() throws Exception {
        Instrument instrument = Instrument.fromString("TEST", "L", "EQUITY", "GBP");
        StockV1 stock = new StockV1(instrument);
        S3DataStore store = createStore(true, instrument);
        Assertions.assertTrue(store.contains(stock));
    }

    @Test
    void containsReturnsFalseWhenObjectMissing() throws Exception {
        Instrument instrument = Instrument.fromString("TEST", "L", "EQUITY", "GBP");
        StockV1 stock = new StockV1(instrument);
        S3DataStore store = createStore(false, instrument);
        Assertions.assertFalse(store.contains(stock));
    }
}
