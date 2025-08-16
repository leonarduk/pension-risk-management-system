package org.patriques.output;

/**
 * Exception thrown when an error occurs while parsing or retrieving data from the
 * Alpha Vantage service.
 */
public class AlphaVantageException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AlphaVantageException(String message, Exception e) {
    super(message, e);
  }

  public AlphaVantageException(String message) {
    super(message);
  }
}
