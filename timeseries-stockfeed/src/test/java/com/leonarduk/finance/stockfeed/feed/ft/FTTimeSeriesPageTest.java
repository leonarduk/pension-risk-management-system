package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.ta4j.core.Bar;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FTTimeSeriesPageTest {

    private Instrument createInstrument() {
        return Instrument.fromString("TEST","L","EQUITY",Instrument.GBP);
    }

    @Test
    public void testParsesHtmlTable() throws Exception {
        String html = "<html><body><div class='mod-tearsheet-historical-prices__results'>" +
                "<table class='mod-ui-table__table'><tbody>" +
                "<tr><td><span></span><span>Fri, Aug 20, 2021</span></td>" +
                "<td>10</td><td>12</td><td>9</td><td>11</td>" +
                "<td><span></span><span>1000</span></td></tr>" +
                "</tbody></table></div></body></html>";
        Path temp = Files.createTempFile("ft",".html");
        Files.writeString(temp, html);
        WebDriver driver = new HtmlUnitDriver(true);
        Instrument instrument = createInstrument();
        FTTimeSeriesPage page = new FTTimeSeriesPage(driver, temp.toUri().toString());
        Optional<List<Bar>> barsOpt = page.getTimeseries(instrument,
                LocalDate.of(2021,8,20), LocalDate.of(2021,8,20));
        driver.quit();
        Assert.assertTrue(barsOpt.isPresent());
        List<Bar> bars = barsOpt.get();
        Assert.assertEquals(1, bars.size());
        Assert.assertEquals(11.0, bars.get(0).getClosePrice().doubleValue(), 0.001);
    }

    @Test
    public void testCsvFallbackWhenTableMissing() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/data", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();
                String response;
                if (query != null && query.contains("format=csv")) {
                    response = "Date,Open,High,Low,Close,Volume\n" +
                            "2021-08-20,10,12,9,11,1000\n";
                    exchange.getResponseHeaders().add("Content-Type","text/csv");
                } else {
                    response = "<html><body>No table</body></html>";
                    exchange.getResponseHeaders().add("Content-Type","text/html");
                }
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });
        server.start();
        int port = server.getAddress().getPort();

        WebDriver driver = new HtmlUnitDriver(true);
        Instrument instrument = createInstrument();
        String baseUrl = "http://localhost:" + port + "/data";
        FTTimeSeriesPage page = new FTTimeSeriesPage(driver, baseUrl);
        Optional<List<Bar>> barsOpt = page.getTimeseries(instrument,
                LocalDate.of(2021,8,20), LocalDate.of(2021,8,20));
        driver.quit();
        server.stop(0);

        Assert.assertTrue("Fallback should return data", barsOpt.isPresent());
        Assert.assertEquals(1, barsOpt.get().size());
    }
}

