package com.leonarduk.finance.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HtmlToolsTest {

    @Test
    public void testAddFieldEscapesHtml() {
        StringBuilder sb = new StringBuilder();
        String malicious = "<script>alert(1)</script>";
        HtmlTools.addField(malicious, sb, null);
        Assertions.assertEquals("<td bgcolor='white'>&lt;script&gt;alert(1)&lt;/script&gt;</td>", sb.toString());
    }

    @Test
    public void testAddHeaderEscapesHtml() {
        StringBuilder sb = new StringBuilder();
        String malicious = "<script>alert(1)</script>";
        HtmlTools.addHeader(malicious, sb);
        Assertions.assertEquals("<th>&lt;script&gt;alert(1)&lt;/script&gt;</th>", sb.toString());
    }
}
