package com.leonarduk.finance.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.leonarduk.finance.AnalyseSnapshot;
import com.leonarduk.finance.portfolio.Position;

@Named
@Path("/portfolio")
public class PortfolioFeedEndpoint {

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
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/api/display/")
	public List<Position> getHistory() throws IOException {
		return AnalyseSnapshot.getPositions();
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

}
