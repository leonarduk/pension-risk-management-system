package com.leonarduk.finance.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("download")
public class ImageService {

	@GET
	@Path("/svg/{file}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getSvg(@PathParam("file") final String fileName) throws IOException {
		String fullFilename = fileName + ".svg";
		final File svgFile = new File("target", fullFilename);
		if (svgFile.canRead() && svgFile.isFile()) {
			return Response
					.ok(Files.readString(Paths.get(svgFile.getAbsolutePath())), MediaType.APPLICATION_OCTET_STREAM)
					.header("Content-Disposition", "attachment; filename=\"" + fullFilename + "\"").build();
		}
		throw new IOException("Cannot find " + fileName);
	}

}