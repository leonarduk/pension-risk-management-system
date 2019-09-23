package com.leonarduk.finance.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;

@Path("images")
public class ImageService {

	public void convert(File svgFile, File jpegFile) throws Exception {
		// create a JPEG transcoder
		JPEGTranscoder t = new JPEGTranscoder();
		// set the transcoding hints
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
		// create the transcoder input
		String svgURI = svgFile.toURL().toString();
		TranscoderInput input = new TranscoderInput(svgURI);
		// create the transcoder output
		OutputStream ostream = new FileOutputStream(jpegFile);
		TranscoderOutput output = new TranscoderOutput(ostream);
		// save the image
		t.transcode(input, output);
		// flush and close the stream then exit
		ostream.flush();
		ostream.close();
	}

	@GET
	@Path("/jpg/{file}")
	@Produces("image/jpeg")
	public Response getFile(@PathParam("file") final String fileName) throws Exception {
		String fullFilename = fileName + ".svg";
		final File svgFile = new File("target", fullFilename);
		if (svgFile.canRead() && svgFile.isFile()) {
			final File jpgFile = new File("target", fileName + ".jpg");

			convert(svgFile, jpgFile);

			ResponseBuilder response = Response.ok((Object) jpgFile);
			response.header("Content-Disposition", "attachment; filename=" + jpgFile.getName());
			return response.build();

		}
		throw new IOException("Cannot find " + fileName);

	}

}