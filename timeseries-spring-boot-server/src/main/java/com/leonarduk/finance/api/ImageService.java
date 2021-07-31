package com.leonarduk.finance.api;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("images")
public class ImageService {

	@GET
	@Path("/jpg/{file}")
	@Produces("image/jpeg")
	public Response getFile(@PathParam("file") final String fileName) throws Exception {
		String fullFilename = fileName + ".jpg";
		final File jpgFile = new File("target", fullFilename);
		if (jpgFile.canRead() && jpgFile.isFile()) {
			ResponseBuilder response = Response.ok(jpgFile);
			response.header("Content-Disposition", "attachment; filename=" + jpgFile.getName());
			return response.build();

		}
		throw new IOException("Cannot find " + fileName);

	}

}
