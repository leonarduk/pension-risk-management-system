//package com.leonarduk.finance.stockfeed;
//
//import java.io.BufferedInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.charset.Charset;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.jfree.chart.axis.Tick;
//import org.joda.time.DateTime;
//import org.slf4j.LoggerFactory;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.ta4j.core.TimeSeries;
//
//import au.com.bytecode.opencsv.CSVReader;
//
//public class QuandlTicksLoader {
//
//	public static class QuandlDataFeed {
//		private final DateTime					endDate;
//		private final String					quandlCode;
//		private final DateTime					startDate;
//
//		private static final DateTimeFormatter	DATE_FORMATTER		= DateTimeFormat
//		        .forPattern(QuandlDataFeed.ISO_8601_DATE_FMT);
//		private static final String				ISO_8601_DATE_FMT	= "yyyy-MM-dd";
//
//		// changes these to a Builder if more params
//		public QuandlDataFeed(final String quandlCode) {
//			this(quandlCode, DateTime.now().minusYears(1), DateTime.now());
//		}
//
//		public QuandlDataFeed(final String quandlCode,
//		        final DateTime startdate) {
//			this(quandlCode, startdate, DateTime.now());
//		}
//
//		public QuandlDataFeed(final String quandlCode, final DateTime startDate,
//		        final DateTime endDate) {
//			this.quandlCode = quandlCode;
//			this.startDate = startDate;
//			this.endDate = endDate;
//		}
//
//		private URL buildURL() throws IOException {
//			final String urlString = String.format(
//			        "https://www.quandl.com/api/v3/datasets/%s.csv?start_date=%s&end_date=%s&order=asc",
//			        this.quandlCode,
//			        QuandlDataFeed.DATE_FORMATTER.print(this.startDate),
//			        QuandlDataFeed.DATE_FORMATTER.print(this.endDate));
//			System.out.println(
//			        String.format("Quandl Dataset URL for %s", urlString));
//
//			try {
//				return new URL(urlString);
//			}
//			catch (final MalformedURLException e) {
//				throw new IOException("Invalid URL");
//			}
//		}
//
//		private TimeSeries get(final URL url) throws IOException {
//			HttpURLConnection connection = null;
//			try {
//				connection = (HttpURLConnection) url.openConnection();
//				connection.setRequestMethod("GET");
//				connection.setUseCaches(true);
//				return this.toTimeSeries(
//				        new BufferedInputStream(connection.getInputStream()));
//			}
//			catch (final FileNotFoundException fnfe) {
//				throw new IOException(
//				        String.format("%s is not a valid Quandl URL", url));
//			}
//			catch (final Exception e) {
//				e.printStackTrace();
//				throw new IOException(e);
//			}
//			finally {
//				if (connection != null) {
//					connection.disconnect();
//				}
//			}
//		}
//
//		public TimeSeries load() throws IOException {
//			return this.get(this.buildURL());
//		}
//
//		private TimeSeries toTimeSeries(final InputStream is) {
//			final List<Tick> ticks = new ArrayList<>();
//
//			final CSVReader csvReader = new CSVReader(
//			        new InputStreamReader(is, Charset.forName("UTF-8")), ',',
//			        '"', 1);
//			try {
//				String[] line;
//				while ((line = csvReader.readNext()) != null) {
//					final DateTime date = new DateTime(
//					        QuandlDataFeed.DATE_FORMATTER
//					                .parseDateTime(line[0]));
//					final double open = Double.parseDouble(line[1]);
//					final double high = Double.parseDouble(line[2]);
//					final double low = Double.parseDouble(line[3]);
//					final double close = Double.parseDouble(line[4]);
//					final double volume = Double.parseDouble(line[5]);
//
//					ticks.add(new Tick(date, open, high, low, close, volume));
//				}
//			}
//			catch (final IOException ioe) {
//				LoggerFactory.getLogger(QuandlTicksLoader.class.getName()).error( "Unable to load ticks from CSV", ioe);
//			}
//			catch (final NumberFormatException nfe) {
//				LoggerFactory.getLogger(QuandlTicksLoader.class.getName())
//				        .error( "Error while parsing value", nfe);
//			}
//
//			return new TimeSeries(this.quandlCode + "_ticks", ticks);
//		}
//	}
//
//	public static void main(final String args[]) throws IOException {
//		final QuandlDataFeed quandl = new QuandlDataFeed("YAHOO/PHGP.L",
//		        DateTime.now().minusYears(20));
//		final TimeSeries series = quandl.load();
//
//		System.out.println("Series: " + series.getName() + " ("
//		        + series.getSeriesPeriodDescription() + ")");
//		System.out.println("Number of ticks: " + series.getTickCount());
//		System.out.println("Last tick: \n" + "\tVolume: "
//		        + series.getLastTick().getVolume() + "\n" + "\tOpen price: "
//		        + series.getLastTick().getOpenPrice() + "\n" + "\tClose price: "
//		        + series.getLastTick().getClosePrice());
//	}
//}
