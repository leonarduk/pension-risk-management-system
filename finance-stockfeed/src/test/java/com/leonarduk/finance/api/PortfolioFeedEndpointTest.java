package com.leonarduk.finance.api;

import java.io.IOException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;
import com.leonarduk.finance.SnapshotAnalyser;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;

public class PortfolioFeedEndpointTest {

	private PortfolioFeedEndpoint	endpoint;
	private IntelligentStockFeed	intelligentStockFeed;

	@Before
	public void setUp() throws Exception {
		this.intelligentStockFeed = Mockito.mock(IntelligentStockFeed.class);
		final SnapshotAnalyser snapshotAnalyser = new SnapshotAnalyser(this.intelligentStockFeed);
		this.endpoint = new PortfolioFeedEndpoint(snapshotAnalyser);
	}

	// @Test
	// public final void testGetExtendedAnalysis() {
	// Assert.fail("Not yet implemented");
	// }
	//
	// @Test
	// public final void testGetHistory() {
	// Assert.fail("Not yet implemented");
	// }

	@Test
	public final void testGetPortfolios() throws IOException {
		final Set<String> expected = Sets.newHashSet(new String[] { "Steve ISA", "Permanent",
		        "Steve SIPP", "Lucy ISA", "Global Market", "Risk Parity", "Ivy", "All Seaons",
		        "Marc Faber", "Swensen", "Family", "El-Erian", "Rob Arnott" });
		Assert.assertEquals(expected, this.endpoint.getPortfolios());
	}

	@Test
	public final void testGetPositions() throws IOException {
		Assert.assertEquals(75, this.endpoint.getPositions().getHoldings().size());
	}

	// @Test
	// public final void testGetValuations() throws IOException {
	// ValuationReport expected = new ValuationReport(valuations, portfolioValuation, fromDate,
	// toDate);
	// Assert.assertEquals(expected, this.endpoint.getValuations());
	// }

}
