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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
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

    public S3DataStore(final String bucketName, final String folderName, final String region) {
        this.bucketName = bucketName;
        this.folderName = folderName;
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setRegion(region);
        this.s3 = builder.build();
    }

    @Override
    public void storeSeries(StockV1 stock) throws IOException {
        String filepath = this.getQueryName(stock.getInstrument());
        S3DataStore.log.info("Save stock to {}", filepath);

        StringBuilder sb = this.seriesToCsv(stock.getHistory(), stock.getInstrument());
        com.leonarduk.finance.utils.FileUtils.writeFile(filepath, sb);

        this.s3.putObject(
                this.bucketName,
                this.getS3Filepath(stock.getInstrument()),
                new File(filepath)
        );

        Files.deleteIfExists(Paths.get(filepath));
    }

    @Override
    public boolean contains(final StockV1 stock) {
        return this.s3.doesObjectExist(this.bucketName, this.getS3Filepath(stock.getInstrument()));
    }

    @Override
    protected BufferedReader openReader() throws IOException {
        Path path = new File(getQueryName(getInstrument())).toPath();
        S3DataStore.log.info("Read file from " + path.toAbsolutePath());

        if (!Files.exists(path)) {
            throw new IOException(path.toAbsolutePath() + " not found");
        }

        BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        br.readLine();
        return br;
    }

    @Override
    protected String getQueryName(Instrument instrument) {
        return Paths.get(System.getProperty("java.io.tmpdir"),
                instrument.getExchange().name() + "_" + instrument.code() + ".csv")
                .toString();
    }

    private String getS3Filepath(Instrument instrument) {
        return folderName + "/" + instrument.code() + ".csv";
    }

    @Override
    public Source getSource() {
        return Source.MANUAL;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private StringBuilder seriesToCsv(List<Bar> series, Instrument instrument) {
        StringBuilder sb = new StringBuilder("date,open,high,low,close,volume,comment,ticker\n");
        for (Bar historicalQuote : series) {
            try {
                sb.append(historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().toString());
                StringUtils.addValue(sb, historicalQuote.getOpenPrice());
                StringUtils.addValue(sb, historicalQuote.getHighPrice());
                StringUtils.addValue(sb, historicalQuote.getLowPrice());
                StringUtils.addValue(sb, historicalQuote.getClosePrice());
                StringUtils.addValue(sb, historicalQuote.getVolume());
                final String comment = (historicalQuote instanceof Commentable) ?
                        ((Commentable) historicalQuote).getComment()
                        : "";
                sb.append(",").append(comment);
                sb.append(",").append(instrument.code());
                sb.append("\n");
            } catch (final Exception e) {
                S3DataStore.log.warn(String.format("Cannot add %s",
                        historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().toString()), e);
            }
        }
        return sb;
    }


}
