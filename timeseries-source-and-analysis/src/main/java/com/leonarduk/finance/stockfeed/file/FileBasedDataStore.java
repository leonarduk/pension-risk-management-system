package com.leonarduk.finance.stockfeed.file;

import com.leonarduk.finance.stockfeed.DataStore;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.FileUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.ta4j.core.Bar;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class FileBasedDataStore extends AbstractCsvStockFeed implements DataStore {

    public static final Logger log	= LoggerFactory
            .getLogger(FileBasedDataStore.class.getName());

    private final String storeLocation;

    public FileBasedDataStore(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    @Override
    public void storeSeries(final StockV1 stock) throws IOException {

        final File file = this.getStock(stock);
        FileBasedDataStore.log.info("Save stock to " + file.getAbsolutePath());
        final List<Bar> series = stock.getHistory();

        /**
         * Building header
         */
        final StringBuilder sb = TimeseriesUtils.seriesToCsv(series);
        FileUtils.writeFile(file.getAbsolutePath(), sb);

    }

    public File getStock(final Instrument instrument) {
        return new File(this.storeLocation, this.getQueryName(instrument));
    }

    @Override
    public boolean contains(StockV1 stock) throws IOException {
        return this.getStock(stock).exists();
    }

    protected File getStock(final StockV1 stock) throws IOException {
        final File folder = new File(this.storeLocation);
        if (!folder.exists() && !folder.mkdir()) {
            throw new IOException("Failed to create " + this.storeLocation);
        }

        return this.getStock(stock.getInstrument());
    }

    @Override
    protected BufferedReader openReader() throws IOException {
        final File file = new File(this.storeLocation,
                this.getQueryName(this.getInstrument()));
        FileBasedDataStore.log.info("Read file from " + file.getAbsolutePath());

        if (!file.exists()) {
            throw new IOException(file.getAbsolutePath() + " not found");
        }

        final FileReader in = new FileReader(file);
        final BufferedReader br = new BufferedReader(in);

        // Skip first line that contains column names
        br.readLine();
        return br;
    }

    @Override
    protected String getQueryName(final Instrument instrument) {
        return instrument.getExchange().name() + "_" + instrument.code()
                + ".csv";
    }

    @Override
    public Source getSource() {
        return Source.MANUAL;
    }

    @Override
    public boolean isAvailable() {
        final File store = new File(this.storeLocation);
        return (store.exists() & store.isDirectory()) && store.canWrite()
                && store.canRead();
    }


}
