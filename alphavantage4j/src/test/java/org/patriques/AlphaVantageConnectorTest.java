package org.patriques;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.patriques.input.Function;
import org.patriques.input.Symbol;
import org.patriques.output.AlphaVantageException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import static org.junit.jupiter.api.Assertions.*;

public class AlphaVantageConnectorTest {
    private static final MockURLStreamHandler URL_HANDLER = new MockURLStreamHandler();

    @BeforeAll
    public static void initUrlHandler() {
        URL.setURLStreamHandlerFactory(protocol -> "https".equals(protocol) ? URL_HANDLER : null);
    }

    @BeforeEach
    public void resetHandler() {
        URL_HANDLER.reset();
    }

    @Test
    public void testGetRequestBuildsUrlAndReturnsResponse() {
        URL_HANDLER.setResponse("{\"result\":\"ok\"}");
        AlphaVantageConnector connector = new AlphaVantageConnector("demo", 5000);
        String response = connector.getRequest(Function.TIME_SERIES_DAILY, new Symbol("IBM"));
        assertEquals("{\"result\":\"ok\"}", response);
        assertEquals("https://www.alphavantage.co/query?&function=TIME_SERIES_DAILY&symbol=IBM&apikey=demo",
                URL_HANDLER.getLastUrl().toString());
    }

    @Test
    public void testGetRequestThrowsAlphaVantageExceptionOnIoError() {
        URL_HANDLER.setThrowIOException(true);
        AlphaVantageConnector connector = new AlphaVantageConnector("demo", 5000);
        assertThrows(
                AlphaVantageException.class,
                () -> connector.getRequest(Function.TIME_SERIES_DAILY));
    }

    private static class MockURLStreamHandler extends URLStreamHandler {
        private URL lastUrl;
        private String response;
        private boolean throwIOException;

        void setResponse(String response) {
            this.response = response;
            this.throwIOException = false;
        }

        void setThrowIOException(boolean throwIOException) {
            this.throwIOException = throwIOException;
        }

        void reset() {
            this.lastUrl = null;
            this.response = null;
            this.throwIOException = false;
        }

        URL getLastUrl() {
            return lastUrl;
        }

        @Override
        protected URLConnection openConnection(URL u) {
            this.lastUrl = u;
            return new MockURLConnection(u, response, throwIOException);
        }
    }

    private static class MockURLConnection extends URLConnection {
        private final String response;
        private final boolean throwIOException;

        protected MockURLConnection(URL url, String response, boolean throwIOException) {
            super(url);
            this.response = response;
            this.throwIOException = throwIOException;
        }

        @Override
        public void connect() {
            // no-op
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (throwIOException) {
                throw new IOException("forced exception");
            }
            return new ByteArrayInputStream(response.getBytes());
        }
    }
}
