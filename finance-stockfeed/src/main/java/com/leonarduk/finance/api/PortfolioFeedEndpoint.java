package com.leonarduk.finance.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.leonarduk.finance.AnalyseSnapshot;
import com.leonarduk.finance.portfolio.Portfolio;

import jersey.repackaged.com.google.common.collect.Sets;

@Named
@Path("/portfolio")
public class PortfolioFeedEndpoint {
	private final static Logger logger = Logger.getLogger(PortfolioFeedEndpoint.class.getName());

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("extended")
	public String getExtendedAnalysis(@QueryParam("fromDate") final String fromDate,
	        @QueryParam("toDate") final String toDate,
	        @QueryParam("interpolate") final boolean interpolate)
	        throws IOException, URISyntaxException {
		return AnalyseSnapshot.createPortfolioReport(fromDate, toDate, interpolate, true, true)
		        .toString();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("analysis")
	public String getHistory(@QueryParam("fromDate") final String fromDate,
	        @QueryParam("toDate") final String toDate,
	        @QueryParam("interpolate") final boolean interpolate)
	        throws IOException, URISyntaxException {
		return AnalyseSnapshot.createPortfolioReport(fromDate, toDate, interpolate, false, true)
		        .toString();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/api/display/")
	public Portfolio getPositions() throws IOException {
		PortfolioFeedEndpoint.logger.info("JSON query of positions");
		return new Portfolio(Sets.newHashSet(AnalyseSnapshot.getPositions()));
	}

}
