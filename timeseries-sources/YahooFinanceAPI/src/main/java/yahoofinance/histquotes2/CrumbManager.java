package yahoofinance.histquotes2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yahoofinance.YahooFinance;
import yahoofinance.util.RedirectableRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Stijn on 23/05/2017.
 */
public class CrumbManager {

    private static final Logger log = LoggerFactory.getLogger(CrumbManager.class);

    private static String crumb = "";
    private static String cookie = "";

    private static void setCookie() throws IOException {
        if (YahooFinance.HISTQUOTES2_COOKIE != null && !YahooFinance.HISTQUOTES2_COOKIE.isEmpty()) {
            cookie = YahooFinance.HISTQUOTES2_COOKIE;
            log.debug("Set cookie from system property: {}", cookie);
            return;
        }

        URL request =  java.net.URI.create(YahooFinance.HISTQUOTES2_SCRAPE_URL).toURL();
        RedirectableRequest redirectableRequest = new RedirectableRequest(request, 5);
        redirectableRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);


        URLConnection connection = redirectableRequest.openConnection();

        for (String headerKey : connection.getHeaderFields().keySet()) {
            if ("Set-Cookie".equalsIgnoreCase(headerKey)) {
                for (String cookieField : connection.getHeaderFields().get(headerKey)) {
                    for (String cookieValue : cookieField.split(";")) {
                        if (cookieValue.matches("B=.*")) {
                            cookie = cookieValue;
                            log.debug("Set cookie from http request: {}", cookie);
                            return;
                        }
                    }
                }
            }
        }

        //  If cookie is not set, we should consent to activate cookie
        InputStreamReader is = new InputStreamReader(connection.getInputStream());
        BufferedReader br = new BufferedReader(is);
        String line;
        Pattern patternPostForm = Pattern.compile("(.*)(action=\"/consent\")(.*)");
        Pattern patternInput = Pattern.compile("(.*)(<input type=\"hidden\" name=\")(.*?)(\" value=\")(.*?)(\">)");
        Matcher matcher;
        Map<String, String> datas = new HashMap<>();
        boolean postFind = false;
        // Read source to get params data for post request
        while ((line = br.readLine()) != null) {
            matcher = patternPostForm.matcher(line);
            if (matcher.find()) {
                postFind = true;
            }

            if (postFind) {
                matcher = patternInput.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(3);
                    String value = matcher.group(5);
                    datas.put(name, value);
                }
            }

        }
        // If params are not empty, send the post request
        if (!datas.isEmpty()) {

            datas.put("namespace", YahooFinance.HISTQUOTES2_COOKIE_NAMESPACE);
            datas.put("agree", YahooFinance.HISTQUOTES2_COOKIE_AGREE);
            datas.put("originalDoneUrl", YahooFinance.HISTQUOTES2_SCRAPE_URL);
            datas.put("doneUrl", YahooFinance.HISTQUOTES2_COOKIE_OATH_DONEURL + datas.get("sessionId") + "&inline=" + datas.get("inline") + "&lang=" + datas.get("locale"));

            URL requestOath = java.net.URI.create(YahooFinance.HISTQUOTES2_COOKIE_OATH_URL).toURL();
            HttpURLConnection connectionOath;
            connectionOath = (HttpURLConnection) requestOath.openConnection();
            connectionOath.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
            connectionOath.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);
            connectionOath.setRequestMethod("POST");
            connectionOath.setDoOutput(true);
            connectionOath.setRequestProperty("Referer", connection.getURL().toString());
            connectionOath.setRequestProperty("Host", YahooFinance.HISTQUOTES2_COOKIE_OATH_HOST);
            connectionOath.setRequestProperty("Origin", YahooFinance.HISTQUOTES2_COOKIE_OATH_ORIGIN);
            connectionOath.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            StringBuilder params = new StringBuilder();

            for (String key : datas.keySet()) {
                if (params.isEmpty()) {
                    params.append(key);
                    params.append("=");
                    params.append(URLEncoder.encode(datas.get(key), StandardCharsets.UTF_8));
                } else {
                    params.append("&");
                    params.append(key);
                    params.append("=");
                    params.append(URLEncoder.encode(datas.get(key), StandardCharsets.UTF_8));

                }
            }


            log.debug("Params = {}", params);
            connectionOath.setRequestProperty("Content-Length", Integer.toString(params.toString().length()));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connectionOath.getOutputStream());
            outputStreamWriter.write(params.toString());
            outputStreamWriter.flush();
            connectionOath.setInstanceFollowRedirects(true);
            connectionOath.getResponseCode();
        }

        // Then Set the cookie with the cookieJar
        CookieStore cookieJar = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();
        for (HttpCookie hcookie : cookies) {
            if (hcookie.toString().matches("B=.*")) {
                cookie = hcookie.toString();
                log.debug("Set cookie from http request: {}", cookie);
                return;
            }
        }

        log.warn("Failed to set cookie from http request. Historical quote requests will most likely fail.");
    }

    private static void setCrumb() throws IOException {
        if (YahooFinance.HISTQUOTES2_CRUMB != null && !YahooFinance.HISTQUOTES2_CRUMB.isEmpty()) {
            crumb = YahooFinance.HISTQUOTES2_CRUMB;
            log.debug("Set crumb from system property: {}", crumb);
            return;
        }

        String crumbResult = getCrumbResult();

        if (crumbResult != null && !crumbResult.isEmpty()) {
            crumb = crumbResult.trim();
            log.debug("Set crumb from http request: {}", crumb);
        } else {
            log.warn("Failed to set crumb from http request. Historical quote requests will most likely fail.");
        }

    }

    private static String getCrumbResult() throws IOException {
        URL crumbRequest = java.net.URI.create(YahooFinance.HISTQUOTES2_CRUMB_URL).toURL();
        RedirectableRequest redirectableCrumbRequest = new RedirectableRequest(crumbRequest, 5);
        redirectableCrumbRequest.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
        redirectableCrumbRequest.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);

        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Cookie", cookie);

        URLConnection crumbConnection = redirectableCrumbRequest.openConnection(requestProperties);
        InputStreamReader is = new InputStreamReader(crumbConnection.getInputStream());
        BufferedReader br = new BufferedReader(is);
        return br.readLine();
    }

    public static void refresh() throws IOException {
        setCookie();
        setCrumb();
    }

    public static synchronized String getCrumb() throws IOException {
        if (crumb == null || crumb.isEmpty()) {
            refresh();
        }
        return crumb;
    }

    public static String getCookie() throws IOException {
        if (cookie == null || cookie.isEmpty()) {
            refresh();
        }
        return cookie;
    }

}
