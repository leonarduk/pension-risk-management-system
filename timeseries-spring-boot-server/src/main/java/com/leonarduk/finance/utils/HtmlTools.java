package com.leonarduk.finance.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leonarduk.finance.chart.ChartDisplay;
import com.leonarduk.finance.chart.PieChartFactory;
import com.leonarduk.finance.portfolio.RecommendedTrade;
import com.leonarduk.finance.portfolio.Valuation;

public class HtmlTools {
	public static final Logger logger = LoggerFactory.getLogger(HtmlTools.class.getName());

	public static void addField(final Object value, final StringBuilder sb, final ValueFormatter formatterRaw) {
		final ValueFormatter formatter = formatterRaw == null ? (Object::toString) : formatterRaw;
		if (sb == null) {
			throw new IllegalArgumentException("Passed in null StringBuilder");
		}
		if (null == value) {
			HtmlTools.logger.warn("Null value supplied - treat as empty string");
		}
		sb.append("<td bgcolor='" + HtmlTools.getColour(value == null ? "" : value) + "'>")
				.append(formatter.format(value == null ? "" : value)).append("</td>");
	}

	public static void addHeader(final String nameRaw, final StringBuilder sb) {
		if (sb == null) {
			throw new IllegalArgumentException("Passed in null StringBuilder");
		}
		String name = nameRaw;
		if (null == name) {
			name = "";
			HtmlTools.logger.warn("Null field name supplied - treat as empty string");
		}
		sb.append("<th>").append(name).append("</th>");
	}

	public static void addPieChartAndTable(final Map<String, Double> assetTypeMap, final StringBuilder sbBody,
			final List<Valuation> valuations, final String title, final String key, final String value)
			throws Exception {
		final PieChartFactory pieChartFactory = new PieChartFactory(title);
		pieChartFactory.addAll(assetTypeMap);
		assetTypeMap.put("Total", pieChartFactory.getTotal().doubleValue());
		sbBody.append(ChartDisplay.getTable(assetTypeMap, key, value));
		final String filename = title.replace(" ", "_");
		sbBody.append(ChartDisplay.saveImageAsSvgAndReturnHtmlLink(filename, 400, 400, pieChartFactory.buildChart()));
	}

	public static StringBuilder createHtmlText(final StringBuilder sbHead, final StringBuilder sbBody) {
		final StringBuilder buf = new StringBuilder("<html><head>")
				.append(sbHead == null ? new StringBuilder() : sbHead).append("</head><body>");
		buf.append(sbBody == null ? new StringBuilder() : sbBody).append("</body></html>\n");
		return buf;
	}

	public static String getColour(final Object value) {
		String colour = "white";
		if (((value != null) && (value.equals(RecommendedTrade.BUY.name())
				|| ((value instanceof LocalDate)
						&& (Double.valueOf(Duration.between(LocalDate.now(), ((LocalDate) value)).toDays()))
								.equals(Double.valueOf(0)))
				|| ((value instanceof BigDecimal) && (((BigDecimal) value).compareTo(BigDecimal.ZERO) > 0))))
				|| ((value instanceof Double) && ((Double) value).compareTo(Double.valueOf(0)) > 0)) {
			colour = "green";
		}

		if (((value != null) && value.equals(RecommendedTrade.SELL.name())) || ((value instanceof LocalDate)
				&& (DateUtils.getDiffInWorkDays(LocalDate.now(), ((LocalDate) value))) > 1
				|| ((value instanceof BigDecimal) && (((BigDecimal) value).compareTo(BigDecimal.ZERO) < 0))
				|| ((value instanceof Double) && ((Double) value).compareTo(Double.valueOf(0)) < 0))) {
			colour = "red";
		}
		return colour;
	}

	public static String getURLParameters(final Map<String, String> params) {
		final StringBuilder sb = new StringBuilder();

		for (final Entry<String, String> entry : params.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			String key = entry.getKey();
			String value = entry.getValue();
            key = URLEncoder.encode(key, StandardCharsets.UTF_8);
            value = URLEncoder.encode(value, StandardCharsets.UTF_8);
            sb.append(String.format("%s=%s", key, value));
		}
		return sb.toString();
	}

	public static void printTable(final StringBuilder sb, final List<List<DataField>> records) {
		if (records.size() > 0) {
			sb.append("<table border=\"1\"><tr>");
			records.get(0).stream().forEach(f -> {
				if (f.isDisplay()) {
					HtmlTools.addHeader(f.getName(), sb);
				}
			});
			sb.append("</tr>");

			for (final List<DataField> list : records) {
				sb.append("<tr>");
				list.stream().forEach(f -> {
					if (f.isDisplay()) {
						HtmlTools.addField(f.getValue(), sb, f.getFormatter());
					}
				});
				sb.append("</tr>");

			}
			sb.append("</table>");
		}
	}

	public static void convertSvgToJPG(File svgFile, File jpegFile) throws Exception {
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

}
