package com.leonarduk.finance.stockfeed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import com.leonarduk.finance.AnalyseSnapshot;
import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.portfolio.Valuation;
import com.leonarduk.finance.utils.NumberUtils;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

public class AnalyseSnapshotTest {
	@Test
	public final void testAnalyseStock() {
		final Optional<Stock> stock = Optional.empty();
		final Position position = new Position("test", Instrument.CASH, BigDecimal.valueOf(100),
		        stock, "Cash");
		final Valuation actual = AnalyseSnapshot.analyseStock(position,
		        LocalDate.now().minusYears(1), LocalDate.now());

		Assert.assertTrue(NumberUtils.areSame(BigDecimal.valueOf(100d), actual.getValuation()));
		Assert.assertTrue(NumberUtils.areSame(BigDecimal.ONE, actual.getPrice()));
	}

	@Test
	public void testCalculateReturn() {

		final List<Tick> ticks = new ArrayList<>();
		ticks.add(new Tick(LocalDate.parse("2017-04-07").toDateTimeAtCurrentTime(), 80.0, 90.0,
		        70.0, 85.0, 10.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-10").toDateTimeAtCurrentTime(), 85.0, 95.0,
		        75.0, 90.0, 10.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-11").toDateTimeAtCurrentTime(), 100.0, 110.0,
		        90.0, 105.0, 10.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-12").toDateTimeAtCurrentTime(), 100.0, 110.0,
		        90.0, 105.0, 10.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-13").toDateTimeAtCurrentTime(), 105.0, 115.0,
		        95.0, 110.0, 20.0));

		final TimeSeries series = new TimeSeries(ticks);
		Assert.assertEquals(5.88, AnalyseSnapshot.calculateReturn(series, 1).doubleValue(), 0);
		Assert.assertEquals(29.41, AnalyseSnapshot.calculateReturn(series, 4).doubleValue(), 0);
	}

	@Test
	public void testCreateValuation() throws Exception {
		final Position position = new Position("", Instrument.UNKNOWN, BigDecimal.valueOf(100),
		        Optional.empty(), null);
		final Tick lastTick = new Tick(LocalDate.parse("2017-04-13").toDateTimeAtCurrentTime(),
		        105.0, 115.0, 95.0, 110.0, 20.0);
		Assert.assertTrue(NumberUtils.areSame(BigDecimal.valueOf(11000),
		        AnalyseSnapshot.createValuation(position, lastTick).getValuation()));
	}
}
