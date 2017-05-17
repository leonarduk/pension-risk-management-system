package yahoofinance.histquotes;

import java.io.IOException;
import java.util.Calendar;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.DateUtils;

public class HistQuotesRequestTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testCreateUrl() throws IOException {
		final Calendar from = DateUtils.dateToCalendar(LocalDate.parse("2016-05-17"));
		final Calendar to = DateUtils.dateToCalendar(LocalDate.parse("2017-05-17"));
		Assert.assertEquals(
		        "http://ichart.yahoo.com/table.csv?s=PHGP.L&a=4&b=17&c=2016&d=4&e=17&f=2017&g=d&ignore=.csv",
		        HistQuotesRequest.createUrl(Instrument.fromString("PHGP"), from, to,
		                Interval.DAILY));
	}

}
