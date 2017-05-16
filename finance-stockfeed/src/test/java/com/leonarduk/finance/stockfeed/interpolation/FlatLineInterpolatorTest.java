package com.leonarduk.finance.stockfeed.interpolation;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

public class FlatLineInterpolatorTest {
	private TimeSeriesInterpolator	interpolator;
	private TimeSeries				series;

	@Before
	public void setUp() throws Exception {
		this.interpolator = new FlatLineInterpolator();
		final List<Tick> ticks = Arrays.asList(new Tick[] { //
		        new Tick(LocalDate.parse("2017-04-14").toDateTimeAtStartOfDay(), 105, 115, 95, 110,
		                2000),
		        new Tick(LocalDate.parse("2017-04-07").toDateTimeAtStartOfDay(), 100, 112, 92, 102,
		                5000),
		        new Tick(LocalDate.parse("2017-04-03").toDateTimeAtStartOfDay(), 100, 110, 90, 105,
		                1000) });
		this.series = new TimeSeries(ticks);
	}

	@Test
	public void testInterpolate() {
		final TimeSeries actual = this.interpolator.interpolate(this.series);
		Assert.assertEquals(10, actual.getTickCount());
		Assert.assertEquals(LocalDate.parse("2017-04-03"),
		        actual.getTick(0).getEndTime().toLocalDate());
		Assert.assertEquals(LocalDate.parse("2017-04-04"),
		        actual.getTick(1).getEndTime().toLocalDate());
		Assert.assertEquals(LocalDate.parse("2017-04-05"),
		        actual.getTick(2).getEndTime().toLocalDate());
		Assert.assertEquals(LocalDate.parse("2017-04-07"),
		        actual.getTick(4).getEndTime().toLocalDate());
		Assert.assertEquals(LocalDate.parse("2017-04-14"),
		        actual.getTick(9).getEndTime().toLocalDate());

		Assert.assertEquals(actual.getTick(0).getClosePrice(), actual.getTick(1).getClosePrice());
		Assert.assertEquals(actual.getTick(4).getClosePrice(), actual.getTick(5).getClosePrice());

	}

}
