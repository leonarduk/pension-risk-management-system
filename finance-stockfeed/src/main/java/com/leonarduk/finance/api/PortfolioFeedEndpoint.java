package com.leonarduk.finance.api;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.leonarduk.finance.AnalyseSnapshot;

@Named
@Path("/portfolio")
public class PortfolioFeedEndpoint {

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("extended")
	public String getExtendedAnalysis() throws IOException, URISyntaxException {
		return AnalyseSnapshot.createPortfolioReport(true, true).toString();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("analysis")
	public String getHistory() throws IOException, URISyntaxException {
		return AnalyseSnapshot.createPortfolioReport(false, true).toString();
	}

}
