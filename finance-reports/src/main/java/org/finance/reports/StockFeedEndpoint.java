package org.finance.reports;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;

@RestController
@Path("/stock")
public class StockFeedEndpoint {

	@GET
	@Path("history")
	public JsonArray getHistory(@QueryParam("ticker") String ticker, @QueryParam("years") int years)
			throws IOException {
		return getHistoryData(ticker, years);
	}


	private JsonArray getHistoryData(String ticker, int years) throws IOException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://host:8080/context/rest/method");
		return target.request(MediaType.APPLICATION_JSON).get(JsonArray.class);	}

}
