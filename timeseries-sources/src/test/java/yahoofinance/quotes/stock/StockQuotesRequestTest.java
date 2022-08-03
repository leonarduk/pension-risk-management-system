//package yahoofinance.quotes.stock;
//
//import java.io.IOException;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.leonarduk.finance.stockfeed.Instrument;
//
//public class StockQuotesRequestTest {
//
//	@Before
//	public void setUp() throws Exception {
//	}
//
//	@Test
//	public final void testParseCSVLineString() throws IOException {
//		final String line = "\"ETFS METAL SECURITIES LD ETFS P\",\"PHGP.L\",\"GBp\",\"LSE\",N/A,N/A,\"PHGP.L\",N/A,\"PHGP.L\",9000.00,N/A,\"PHGP.L\",N/A,\"PHGP.L\",9317.00,\"PHGP.L\",53,\"PHGP.L\",\"5/22/2017\",\"4:29pm\",9292.00,9247.00,9277.40,9321.00,16256,38234,9321.00,79.77,95.45,94.29,\"PHGP.L\",N/A,\"PHGP.L\",\"PHGP.L\",N/A,\"PHGP.L\",N/A,\"PHGP.L\",N/A,\"PHGP.L\",N/A,N/A,N/A,N/A,N/A,N/A,0.00,N/A,N/A,0.00,N/A,N/A,0.00,N/A,N/A,N/A,N/A";
//		final StockQuotesRequest request = new StockQuotesRequest(
//		        Instrument.fromString("PHGP"));
//		final StockQuotesData data = request.parseCSVLine(line);
//		Assert.assertNotNull(data.getQuote());
//	}
//
//}
