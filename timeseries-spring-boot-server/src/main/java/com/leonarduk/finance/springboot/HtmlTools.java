package com.leonarduk.finance.springboot;

import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.ValueFormatter;

import java.util.List;

public class HtmlTools {


	public static StringBuilder createHtmlText(final StringBuilder sbHead, final StringBuilder sbBody) {
		final StringBuilder buf = new StringBuilder("<html><head>")
				.append(sbHead == null ? new StringBuilder() : sbHead).append("</head><body>");
		buf.append(sbBody == null ? new StringBuilder() : sbBody).append("</body></html>\n");
		return buf;
	}
    public static void addHeader(final String nameRaw, final StringBuilder sb) {
        if (sb == null) {
            throw new IllegalArgumentException("Passed in null StringBuilder");
        }
        String name = nameRaw;
        if (null == name) {
            name = "";
//            log.warn("Null field name supplied - treat as empty string");
        }
        sb.append("<th>").append(name).append("</th>");
    }

    public static void printTable(final StringBuilder sb, final List<List<DataField>> records) {
        if (!records.isEmpty()) {
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
    public static void addField(final Object value, final StringBuilder sb, final ValueFormatter formatterRaw) {
        final ValueFormatter formatter = formatterRaw == null ? (Object::toString) : formatterRaw;
        if (sb == null) {
            throw new IllegalArgumentException("Passed in null StringBuilder");
        }
        sb.append("<td bgcolor='" + HtmlTools.getColour(value == null ? "" : value) + "'>")
            .append(formatter.format(value == null ? "" : value)).append("</td>");
    }
    public static String getColour(final Object value) {
        String colour = "white";
//        if (((value != null) && (value.equals(RecommendedTrade.BUY.name())
//            || ((value instanceof LocalDate)
//            && (Double.valueOf(Duration.between(LocalDate.now(), ((LocalDate) value)).toDays()))
//            .equals(Double.valueOf(0)))
//            || ((value instanceof BigDecimal) && (((BigDecimal) value).compareTo(BigDecimal.ZERO) > 0))))
//            || ((value instanceof Double) && ((Double) value).compareTo(Double.valueOf(0)) > 0)) {
//            colour = "green";
//        }
//
//        if (((value != null) && value.equals(RecommendedTrade.SELL.name())) || ((value instanceof LocalDate)
//            && (DateUtils.getDiffInWorkDays(LocalDate.now(), ((LocalDate) value))) > 1
//            || ((value instanceof BigDecimal) && (((BigDecimal) value).compareTo(BigDecimal.ZERO) < 0))
//            || ((value instanceof Double) && ((Double) value).compareTo(Double.valueOf(0)) < 0))) {
//            colour = "red";
//        }
        return colour;
    }
}
