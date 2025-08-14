package com.leonarduk.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.leonarduk.finance.stockfeed.DataStore;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.file.AbstractCsvStockFeed;
import com.leonarduk.finance.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Persist {@link StockV1} data in an S3 bucket.
 * <p>
 * The {@link #contains(StockV1)} method checks for the presence of the
 * corresponding object in the configured S3 bucket and returns {@code true}
 * only when that object exists.
 * </p>
 */
@Slf4j
public class S3DataStore extends AbstractCsvStockFeed implements DataStore {

    private final String bucketName;
    private final String folderName;
    private final AmazonS3 s3;

    public S3DataStore(String bucketName, String folderName, String region) {
        this.bucketName = bucketName;
        this.folderName = folderName;
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setRegion(region);
        s3 = builder.build();
    }

    @Override
    public void storeSeries(final StockV1 stock) throws IOException {
        final String filepath = getQueryName(stock.getInstrument());
        log.info("Save stock to {}", filepath);

        final StringBuilder sb = seriesToCsv(stock.getHistory(), stock.getInstrument());
        com.leonarduk.finance.utils.FileUtils.writeFile(filepath, sb);

        s3.putObject(
                bucketName,
                getS3Filepath(stock.getInstrument()),
                new File(filepath)
        );

        Files.deleteIfExists(Paths.get(filepath));
    }

    @Override
    public boolean contains(StockV1 stock) {
        return s3.doesObjectExist(bucketName, getS3Filepath(stock.getInstrument()));
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
        return Paths.get(System.getProperty("java.io.tmpdir"),
                instrument.getExchange().name() + "_" + instrument.code() + ".csv")
                .toString();
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
        final StringBuilder sb = new StringBuilder("date,open,high,low,close,volume,comment,ticker\n");
        for (final Bar historicalQuote : series) {
            try {
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
            } catch (Exception e) {
                log.warn(String.format("Cannot add %s",
                        historicalQuote.getEndTime().toLocalDate().toString()), e);
            }
        }
        return sb;
    }


}
