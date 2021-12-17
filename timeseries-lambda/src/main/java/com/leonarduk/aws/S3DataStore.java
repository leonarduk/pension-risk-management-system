package com.leonarduk.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.leonarduk.finance.stockfeed.DataStore;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.file.AbstractCsvStockFeed;
import com.leonarduk.finance.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class S3DataStore extends AbstractCsvStockFeed implements DataStore {

    public static final Logger log = LoggerFactory
            .getLogger(S3DataStore.class.getName());
    private final String bucketName;
    private final String folderName;
    private final String region;
    private final AmazonS3 s3;

    public S3DataStore(String bucketName, String folderName, String region) {
        this.bucketName = bucketName;
        this.folderName = folderName;
        this.region = region;
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setRegion(this.region);
        s3 = builder.build();
    }

    @Override
    public void storeSeries(final StockV1 stock) throws IOException {
        final String filepath = getQueryName(stock.getInstrument());
        log.info("Save stock to " + filepath);

        final StringBuilder sb = seriesToCsv(stock.getHistory(), stock.getInstrument());
        com.leonarduk.finance.utils.FileUtils.writeFile(filepath, sb);

        s3.putObject(
                bucketName,
                getS3Filepath(stock.getInstrument()),
                new File(filepath)
        );
    }

    public File getStock(final Instrument instrument) throws IOException {
        log.info(String.format("getStock: %s", instrument.code()));

        S3Object s3object = s3.getObject(bucketName, getS3Filepath(instrument));
        S3ObjectInputStream inputStream = s3object.getObjectContent();

        File destination = new File(this.getQueryName(this.getInstrument()));
        FileUtils.copyInputStreamToFile(inputStream, destination);
        return destination;
    }

    @Override
    public boolean contains(StockV1 stock) {
        return s3.doesBucketExistV2(getS3Filepath(stock.getInstrument()));
    }

    protected File getStock(final StockV1 stock) throws IOException {
        return this.getStock(stock.getInstrument());
    }

    @Override
    protected BufferedReader openReader() throws IOException {
        final File file = new File(this.getQueryName(this.getInstrument()));
        log.info("Read file from " + file.getAbsolutePath());

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
        return "/tmp/" + instrument.getExchange().name() + "_" + instrument.code()
                + ".csv";
    }

    private String getS3Filepath(final Instrument instrument) {
        return this.folderName + "/" + instrument.code() + ".csv";
    }

    @Override
    public Source getSource() {
        return Source.MANUAL;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private StringBuilder seriesToCsv(final List<Bar> series, final Instrument instrument) {
        final StringBuilder sb = new StringBuilder(" date,open,high,low,close,volume,comment,ticker\n");
        for (final Bar historicalQuote : series) {
            sb.append(historicalQuote.getEndTime().toLocalDate().toString());
            StringUtils.addValue(sb, historicalQuote.getOpenPrice());
            StringUtils.addValue(sb, historicalQuote.getMaxPrice());
            StringUtils.addValue(sb, historicalQuote.getMinPrice());
            StringUtils.addValue(sb, historicalQuote.getClosePrice());
            StringUtils.addValue(sb, historicalQuote.getVolume());
            String comment = (historicalQuote instanceof Commentable) ?
                    ((Commentable) historicalQuote).getComment()
                    : "";
            sb.append(",").append(comment);
            sb.append(",").append(instrument.code());
            sb.append("\n");
        }
        return sb;
    }

}
