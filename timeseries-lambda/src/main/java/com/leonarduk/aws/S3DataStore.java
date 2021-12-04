package com.leonarduk.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.leonarduk.finance.stockfeed.DataStore;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.file.AbstractCsvStockFeed;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class S3DataStore  extends AbstractCsvStockFeed implements DataStore {

    private final String bucketName;
    private final String folderName;
    private final String region;

    private AmazonS3 s3;

    public S3DataStore(String bucketName, String folderName, String region){
        this.bucketName = bucketName;
        this.folderName = folderName;
        this.region = region;
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setRegion(this.region);
        s3 = builder.build();
    }

    public static final Logger log	= LoggerFactory
            .getLogger(S3DataStore.class.getName());

    @Override
    public void storeSeries(final StockV1 stock) throws IOException {
        final File file = this.getStock(stock);
        log.info("Save stock to " + file.getAbsolutePath());

        final StringBuilder sb = TimeseriesUtils.seriesToCsv(stock.getHistory());
        com.leonarduk.finance.utils.FileUtils.writeFile(file.getAbsolutePath(), sb);

        s3.putObject(
                bucketName,
                getS3Filepath(stock.getInstrument()),
                file
        );
    }

    public File getStock(final Instrument instrument) throws IOException {
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

    private String getS3Filepath(final Instrument instrument){
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


}
