package com.leonarduk.finance.stockfeed.file;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FileBasedDataStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void isAvailableTrueWhenDirectoryExists() {
        FileBasedDataStore store = new FileBasedDataStore(tempDir.toString());
        assertTrue(store.isAvailable());
    }

    @Test
    void isAvailableFalseWhenPathIsFile() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.createFile(file);
        FileBasedDataStore store = new FileBasedDataStore(file.toString());
        assertFalse(store.isAvailable());
    }

    @Test
    void storeSeriesCreatesFileAndContains() throws Exception {
        FileBasedDataStore store = new FileBasedDataStore(tempDir.toString());
        Instrument instrument = Instrument.CASH;

        StockV1 stock = Mockito.mock(StockV1.class);
        Mockito.when(stock.getInstrument()).thenReturn(instrument);
        Mockito.when(stock.getHistory()).thenReturn(Collections.singletonList(Mockito.mock(Bar.class)));

        store.storeSeries(stock);
        assertTrue(store.contains(stock));
    }

    @Test
    void openReaderThrowsWhenFileMissing() throws Exception {
        TestableDataStore store = new TestableDataStore(tempDir.toString());
        store.setInstrument(Instrument.CASH);
        assertThrows(IOException.class, store::openReaderPublic);
    }

    private static class TestableDataStore extends FileBasedDataStore {
        TestableDataStore(String location) {
            super(location);
        }
        public BufferedReader openReaderPublic() throws IOException {
            return super.openReader();
        }
    }
}

