package org.patriques;

import org.patriques.input.ApiParameter;
import org.patriques.input.ApiParameterBuilder;
import org.patriques.output.AlphaVantageException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Connection to Alpha Vantage API.
 *
 * @see ApiConnector
 */
public class AlphaVantageConnector implements ApiConnector {
    private static final String BASE_URL = "https://www.alphavantage.co/query?";
    private final String apiKey;
    private final int timeOut;

    /**
     * Creates an AlphaVantageConnector.
     *
     * @param apiKey  the secret key to access the api.
     * @param timeOut the timeout for when reading the connection should give up.
     */
    public AlphaVantageConnector(String apiKey, int timeOut) {
        this.apiKey = apiKey;
        this.timeOut = timeOut;
    }

    /**
     * The main method for connecting to the api. Given the api parameters it will
     * create the url query and read the response.
     *
     * @param apiParameters the api parameters used in the query
     * @return the response from AlphaVantage
     * @throws AlphaVantageException if the request or parsing fails
     */
    @Override
    public String getRequest(ApiParameter... apiParameters) {
        if (apiParameters == null) {
            throw new IllegalArgumentException("Api parameters cannot be null");
        }

        String params = getParameters(apiParameters);
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            // Create the request URL
            URL request = new URL(BASE_URL + params);
            // Opens the connection to the url
            URLConnection connection = request.openConnection();
            // Sets the timeout for the connection
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);

            // Reads the response from the connection
            inputStream = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStream);

            StringBuilder responseBuilder = new StringBuilder();

            // Reads the response line by line
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
            }
            // Returns the response
            return responseBuilder.toString();
        } catch (IOException e) {
            // If the request fails then throw an exception
            throw new AlphaVantageException("failure sending request", e);
        } finally {
            // Ensure resources are closed
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // Log or handle this secondary exception, if necessary
            }
        }
    }

    /**
     * Builds up the url query from the api parameters used to append to the base url.
     *
     * @param apiParameters the api parameters used in the query
     * @return the query string to use in the url
     */
    private String getParameters(ApiParameter... apiParameters) {
        ApiParameterBuilder urlBuilder = new ApiParameterBuilder();
        for (ApiParameter parameter : apiParameters) {
            urlBuilder.append(parameter);
        }
        urlBuilder.append("apikey", apiKey);
        return urlBuilder.getUrl();
    }
}
