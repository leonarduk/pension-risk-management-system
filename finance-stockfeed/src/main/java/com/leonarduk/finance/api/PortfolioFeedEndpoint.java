package com.leonarduk.finance.api;

import java.io.IOException;

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
	@Path("analysis")
	public String getHistory() throws IOException {
		return AnalyseSnapshot.createPortfolioReport().toString();
	}

}
