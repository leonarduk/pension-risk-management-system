package com.leonarduk.finance.scraper;

import java.io.IOException;
import java.util.Optional;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simple utility for retrieving and parsing web pages. */
public class WebScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebScraper.class);

    /**
     * Fetch the content of the supplied URL and return the HTML as XML.
     *
     * @param url page to retrieve
     * @return HTML content as XML
     * @throws IOException if the request fails
     */
    public String getPageAsXml(final String url) throws IOException {
        try (WebClient client = new WebClient()) {
            client.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = client.getPage(url);
            return page.asXml();
        }
    }

    /**
     * Retrieve the first node matching the supplied XPath expression and return
     * its normalised text.
     *
     * @param url page to retrieve
     * @param xpath XPath expression to evaluate
     * @return optional normalised text for the first matching node
     * @throws IOException if the request fails
     */
    public Optional<String> getTextByXPath(final String url, final String xpath)
            throws IOException {
        try (WebClient client = new WebClient()) {
            client.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = client.getPage(url);
            DomNode node = page.getFirstByXPath(xpath);
            return node == null ? Optional.empty() : Optional.ofNullable(node.asNormalizedText());
        }
    }
}

