package com.leonarduk.finance.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        sb.append("<td bgcolor='white'>")
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

    public static StringBuilder createHtmlText(final StringBuilder sbHead, final StringBuilder sbBody) {
        final StringBuilder buf = new StringBuilder("<html><head>")
                .append(sbHead == null ? new StringBuilder() : sbHead).append("</head><body>");
        buf.append(sbBody == null ? new StringBuilder() : sbBody).append("</body></html>\n");
        return buf;
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

}
