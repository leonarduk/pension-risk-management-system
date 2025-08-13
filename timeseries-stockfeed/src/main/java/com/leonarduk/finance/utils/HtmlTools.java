package com.leonarduk.finance.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class HtmlTools {
    public static final Logger logger = LoggerFactory.getLogger(HtmlTools.class.getName());
    private static final String BUNDLE_NAME = "messages";

    private static ResourceBundle bundle() {
        return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
    }

    public static void addField(final Object value, final StringBuilder sb, final ValueFormatter formatterRaw) {
        final ValueFormatter formatter = formatterRaw == null ? (Object::toString) : formatterRaw;
        if (sb == null) {
            throw new IllegalArgumentException(bundle().getString("htmltools.null_stringbuilder"));
        }
        if (null == value) {
            HtmlTools.logger.warn(bundle().getString("htmltools.null_value"));
        }
        sb.append("<td bgcolor='")
                .append(HtmlTools.getColour(value == null ? "" : value)).append("'>")
                .append(formatter.format(value == null ? "" : value)).append("</td>");
    }

    public static void addHeader(final String nameRaw, final StringBuilder sb) {
        if (sb == null) {
            throw new IllegalArgumentException(bundle().getString("htmltools.null_stringbuilder"));
        }
        String name = nameRaw;
        if (null == name) {
            name = "";
            HtmlTools.logger.warn(bundle().getString("htmltools.null_field"));
        }
        sb.append("<th>").append(name).append("</th>");
    }

    public static StringBuilder createHtmlText(final StringBuilder sbHead, final StringBuilder sbBody) {
        final StringBuilder buf = new StringBuilder("<html><head>")
                .append(sbHead == null ? new StringBuilder() : sbHead).append("</head><body>");
        buf.append(sbBody == null ? new StringBuilder() : sbBody).append("</body></html>\n");
        return buf;
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
