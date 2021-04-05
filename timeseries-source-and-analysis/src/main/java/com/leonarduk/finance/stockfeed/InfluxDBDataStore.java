package com.leonarduk.finance.stockfeed;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.Ready;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InfluxDBDataStore implements DataStore, AutoCloseable {
    private final InfluxDBClient client;
    private final String bucket;
    private final String org;
    private final String token;
    private final String serverUrl;

    public InfluxDBDataStore(final String bucket, final String org, final String token, final String serverUrl) {
        this.bucket = bucket;
        this.org = org;
        this.token = token;
        this.serverUrl = serverUrl;

        // You can generate a Token from the "Tokens Tab" in the UI
        this.client = InfluxDBClientFactory.create(this.serverUrl, token.toCharArray());
    }

    @Override
    public void storeSeries(StockV1 stock) throws IOException {
        try (WriteApi writeApi = this.client.getWriteApi()) {
            for (final Bar historicalQuote : stock.getHistory()) {
                writeApi.writeMeasurement(bucket, org, WritePrecision.NS, historicalQuote);
            }
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            Ready.StatusEnum status = this.client.ready().getStatus();
            return status.equals(Ready.StatusEnum.READY);
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public Optional<StockV1> get(Instrument instrument, int years) throws IOException {
        String query = String.format("from(bucket: \"%s\") |> range(start: -%dy)" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"HistoricalQuote\")" +
                "|> filter(fn: (r) => r[\"_field\"] == \"close\" or r[\"_field\"] == \"open\"" +
                " or r[\"_field\"] == \"low\" or r[\"_field\"] == \"high\"" +
                " or r[\"_field\"] == \"comment\" or r[\"_field\"] == \"adjClose\"" +
                " or r[\"_field\"] == \"symbol\" or r[\"_field\"] == \"volume\")" +
                "  |> filter(fn: (r) => r[\"symbol\"] == \"%s\")" +
                "  |> aggregateWindow(every: 1d, fn: last, createEmpty: false)" +
                "|> yield(name: \"last\")", this.bucket, years, instrument.getCode());

        return getResultFromQuery(instrument, this.org, query);

    }

    @Override
    public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate) throws IOException {
        // 2021-03-21T22:00:34.000Z,
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final String timeSuffix = "T00:00:00.000Z";
        String query = String.format("from(bucket: \"%s\") |> range(start: %s, stop: %s)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"HistoricalQuote\")" +
                        "|> filter(fn: (r) => r[\"_field\"] == \"close\" or r[\"_field\"] == \"open\"" +
                        " or r[\"_field\"] == \"low\" or r[\"_field\"] == \"high\"" +
                        " or r[\"_field\"] == \"comment\" or r[\"_field\"] == \"adjClose\"" +
                        " or r[\"_field\"] == \"symbol\" or r[\"_field\"] == \"volume\")" +
                        "  |> filter(fn: (r) => r[\"symbol\"] == \"%s\")" +
                        "  |> aggregateWindow(every: 1d, fn: last, createEmpty: false)" +
                        "|> yield(name: \"last\")", this.bucket, formatter.format(fromDate) + timeSuffix,
                formatter.format(toDate) + timeSuffix,
                instrument.getCode());
        return getResultFromQuery(instrument, this.org, query);
    }

    private Optional<StockV1> getResultFromQuery(Instrument instrument, String org, String query) {
        List<FluxTable> tables = this.client.getQueryApi().query(query, org);
        ConcurrentHashMap<Instant, Map> dateMap = new ConcurrentHashMap<>();
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                Instant cobInstant = fluxRecord.getTime();
                Map<String, Object> map = dateMap.getOrDefault(cobInstant, new HashMap<String, Object>());
                map.put((String) fluxRecord.getValueByKey("_field"),
                        fluxRecord.getValueByKey("_value"));
                map.put("date", cobInstant);
                dateMap.put(cobInstant, map);
//                System.out.println(dateMap.get(cobInstant));
            }
        }
        final List<Bar> quotes = new LinkedList<>();
        Iterator<Map> resultIter = dateMap.values().iterator();
        while (resultIter.hasNext()) {
            ExtendedHistoricalQuote asHistoricalQuote = new ExtendedHistoricalQuote(instrument, resultIter.next());
            if (asHistoricalQuote.getDate().getDayOfWeek() != DayOfWeek.SATURDAY
                    && asHistoricalQuote.getDate().getDayOfWeek() != DayOfWeek.SUNDAY) {
                quotes.add(asHistoricalQuote);
            }
        }

        Collections.sort(quotes, (o1, o2) -> {
            return o2.getEndTime().compareTo(o1.getEndTime());
        });

        return Optional.of(AbstractStockFeed.createStock(instrument, quotes));
    }

    @Override
    public boolean contains(StockV1 stock) throws IOException {
        return false;
    }

    @Override
    public void close() throws Exception {
        // Close it if your application is terminating or you are not using it anymore.
        client.close();
    }
}
