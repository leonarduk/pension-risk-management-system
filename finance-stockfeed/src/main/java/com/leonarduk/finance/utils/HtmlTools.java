package com.leonarduk.finance.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.leonarduk.finance.chart.ChartDisplay;
import com.leonarduk.finance.chart.PieChartFactory;
import com.leonarduk.finance.portfolio.RecommendedTrade;
import com.leonarduk.finance.portfolio.Valuation;

import eu.verdelhan.ta4j.Decimal;

public class HtmlTools {
	public static final Logger logger = Logger
	        .getLogger(HtmlTools.class.getName());

	public static void addField(final Object value, final StringBuilder sb,
	        final ValueFormatter formatterRaw) {
		final ValueFormatter formatter = formatterRaw == null
		        ? (Object::toString) : formatterRaw;
		if (sb == null) {
			throw new IllegalArgumentException("Passed in null StringBuilder");
		}
		if (null == value) {
			HtmlTools.logger
			        .warning("Null value supplied - treat as empty string");
		}
		sb.append("<td bgcolor='"
		        + HtmlTools.getColour(value == null ? "" : value) + "'>")
		        .append(formatter.format(value == null ? "" : value))
		        .append("</td>");
	}

	public static void addHeader(final String nameRaw, final StringBuilder sb) {
		if (sb == null) {
			throw new IllegalArgumentException("Passed in null StringBuilder");
		}
		String name = nameRaw;
		if (null == name) {
			name = "";
			HtmlTools.logger.warning(
			        "Null field name supplied - treat as empty string");
		}
		sb.append("<th>").append(name).append("</th>");
	}

	public static void addPieChartAndTable(
	        final Map<String, Double> assetTypeMap, final StringBuilder sbBody,
	        final List<Valuation> valuations, final String title,
	        final String key, final String value) throws IOException {
		final PieChartFactory pieChartFactory = new PieChartFactory(title);
		pieChartFactory.addAll(assetTypeMap);
		assetTypeMap.put("Total",
		        NumberUtils
		                .roundDecimal(
		                        Decimal.valueOf(pieChartFactory.getTotal()))
		                .toDouble());
		sbBody.append(ChartDisplay.getTable(assetTypeMap, key, value));
		final String filename = title.replace(" ", "_");
		sbBody.append(ChartDisplay.saveImageAsSvgAndReturnHtmlLink(filename,
		        400, 400, pieChartFactory.buildChart()));
	}

	public static StringBuilder createHtmlText(final StringBuilder sbHead,
	        final StringBuilder sbBody) {
		final StringBuilder buf = new StringBuilder("<html><head>")
		        .append(sbHead == null ? new StringBuilder() : sbHead)
		        .append("</head><body>");
		buf.append(sbBody == null ? new StringBuilder() : sbBody)
		        .append("</body></html>\n");
		return buf;
	}

	public static String getColour(final Object value) {
		String colour = "white";
		if (((value != null)
		        && (value
		                .equals(RecommendedTrade.BUY
		                        .name())
		                || ((value instanceof LocalDate)
		                        && (Decimal
		                                .valueOf(Days
		                                        .daysBetween(LocalDate.now(),
		                                                ((LocalDate) value))
		                                        .getDays()))
		                                                .equals(Decimal.ZERO))
		                || ((value instanceof BigDecimal)
		                        && (((BigDecimal) value)
		                                .compareTo(BigDecimal.ZERO) > 0))))
		        || ((value instanceof Decimal)
		                && ((Decimal) value).isGreaterThan(Decimal.ZERO))) {
			colour = "green";
		}

		if (((value != null) && value.equals(RecommendedTrade.SELL.name()))
		        || ((value instanceof LocalDate) && (Decimal.valueOf(
		                Days.daysBetween(LocalDate.now(), ((LocalDate) value))
		                        .getDays())).isGreaterThan(Decimal.ONE))
		        || ((value instanceof BigDecimal) && (((BigDecimal) value)
		                .compareTo(BigDecimal.ZERO) < 0))
		        || ((value instanceof Decimal)
		                && ((Decimal) value).isLessThan(Decimal.ZERO))) {
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
			try {
				key = URLEncoder.encode(key, "UTF-8");
				value = URLEncoder.encode(value, "UTF-8");
			}
			catch (final UnsupportedEncodingException ex) {
				HtmlTools.logger.log(Level.SEVERE, ex.getMessage(), ex);
				// Still try to continue with unencoded values
			}
			sb.append(String.format("%s=%s", key, value));
		}
		return sb.toString();
	}

	public static void printTable(final StringBuilder sb,
	        final List<List<DataField>> records) {
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

}
