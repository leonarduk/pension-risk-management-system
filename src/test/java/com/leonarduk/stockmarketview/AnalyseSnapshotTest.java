package com.leonarduk.stockmarketview;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.leonarduk.stockmarketview.portfolio.Position;
import com.leonarduk.stockmarketview.stockfeed.Instrument;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

public class AnalyseSnapshotTest {

	@Test
	public void testCalculateReturn() {

		List<Tick> ticks = new ArrayList<>();
		ticks.add(new Tick(LocalDate.parse("2017-04-13").toDateTimeAtCurrentTime(), 105.0, 115.0, 95.0, 110.0, 20.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-12").toDateTimeAtCurrentTime(), 100.0, 110.0, 90.0, 105.0, 10.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-11").toDateTimeAtCurrentTime(), 100.0, 110.0, 90.0, 105.0, 10.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-10").toDateTimeAtCurrentTime(), 100.0, 110.0, 90.0, 105.0, 10.0));
		ticks.add(new Tick(LocalDate.parse("2017-04-07").toDateTimeAtCurrentTime(), 80.0, 90.0, 70.0, 85.0, 10.0));

		TimeSeries series = new TimeSeries(ticks);
		assertEquals(4.76, AnalyseSnapshot.calculateReturn(series, 1));
		assertEquals(23.53, AnalyseSnapshot.calculateReturn(series, 5));
	}

	@Test
	public void testCreateValuation() throws Exception {
		Position position = new Position("", Instrument.FR0010446930, Decimal.HUNDRED, Optional.empty(), null);
		Tick lastTick = new Tick(LocalDate.parse("2017-04-13").toDateTimeAtCurrentTime(), 105.0, 115.0, 95.0, 110.0,
				20.0);
		assertEquals(Decimal.valueOf(11000), AnalyseSnapshot.createValuation(position, lastTick).getValuation());
	}
}
