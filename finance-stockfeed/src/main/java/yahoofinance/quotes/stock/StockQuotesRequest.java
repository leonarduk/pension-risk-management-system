package yahoofinance.quotes.stock;

import java.util.ArrayList;
import java.util.List;

import com.leonarduk.finance.stockfeed.Instrument;

import yahoofinance.quotes.QuotesProperty;
import yahoofinance.quotes.QuotesRequest;

/**
 *
 * @author Stijn Strickx
 */
public class StockQuotesRequest extends QuotesRequest<StockQuotesData> {

	/**
	 * Yahoo Finance is responding with formatted numbers in some cases. Because of this, those
	 * number may contain commas. This will screw up the CSV file.
	 *
	 * It's not possible to choose a different delimiter for the CSV or to disable the number
	 * formatting
	 *
	 * To work around this, we surround the vulnerable values by the stock symbol. This forces us to
	 * do manual parsing of the CSV lines instead of using the easy String.split
	 *
	 */
	public static final List<QuotesProperty> DEFAULT_PROPERTIES = new ArrayList<>();

	static {

		// Always keep the name and symbol in first and second place
		// respectively!
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Name);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Currency);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.StockExchange);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Ask);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.AskRealtime);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.AskSize);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Bid);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.BidRealtime);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.BidSize);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);

		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.LastTradePriceOnly);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.LastTradeSize);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.LastTradeDate);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.LastTradeTime);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Open);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.PreviousClose);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.DaysLow);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.DaysHigh);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Volume);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.AverageDailyVolume);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.YearHigh);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.YearLow);

		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.FiftydayMovingAverage);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.TwoHundreddayMovingAverage);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.SharesOutstanding);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.SharesOwned);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.MarketCapitalization);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.SharesFloat);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);

		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.DividendPayDate);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.ExDividendDate);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.TrailingAnnualDividendYield);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.TrailingAnnualDividendYieldInPercent);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.DilutedEPS);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.EPSEstimateCurrentYear);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.EPSEstimateNextQuarter);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.EPSEstimateNextYear);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.PERatio);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.PEGRatio);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.PriceBook);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.PriceSales);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.BookValuePerShare);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Revenue);
		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.EBITDA);
		StockQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.OneyrTargetPrice);

		StockQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.ShortRatio);
	}

	public StockQuotesRequest(final Instrument instrument) {
		super(instrument, StockQuotesRequest.DEFAULT_PROPERTIES);
	}

	@Override
	protected StockQuotesData parseCSVLine(final String line) {
		final List<String> parsedLine = new ArrayList<>();

		// first get company name, symbol, currency and exchange
		// because we need the symbol and currency or exchange might be the same
		// as the symbol!
		// pretty ugly code due to the bad format of the csv
		int pos1 = 0;
		int pos2 = 0;
		int skip = 2;

		if (line.startsWith("\"")) {
			pos1 = 1; // skip first \"
			pos2 = line.indexOf('\"', 1);
		}
		else {
			pos2 = line.indexOf(",\""); // last comma before the first symbol
			                            // (hopefully)
			skip = 1;
		}

		final String name = line.substring(pos1, pos2);
		pos1 = pos2 + skip; // skip \",
		pos2 = line.indexOf('\"', pos1 + 1);
		skip = 2;
		final String fullSymbol = line.substring(pos1, pos2 + 1);
		final String symbol = fullSymbol.substring(1, fullSymbol.length() - 1);

		pos1 = pos2 + skip;
		if (line.charAt(pos1) == '\"') {
			pos1 += 1;
			pos2 = line.indexOf('\"', pos1);
			skip = 2;
		}
		else {
			pos2 = line.indexOf(',', pos1);
			skip = 1;
		}
		final String currency = line.substring(pos1, pos2);

		pos1 = pos2 + skip;
		if (line.charAt(pos1) == '\"') {
			pos1 += 1;
			pos2 = line.indexOf('\"', pos1);
			skip = 2;
		}
		else {
			pos2 = line.indexOf(',', pos1);
			skip = 1;
		}
		final String exchange = line.substring(pos1, pos2);

		parsedLine.add(name);
		parsedLine.add(symbol);
		parsedLine.add(currency);
		parsedLine.add(exchange);

		pos1 = pos2 + skip; // skip \",
		for (; pos1 < line.length(); pos1++) {
			if (line.startsWith(fullSymbol, pos1)) {
				parsedLine.add(symbol);
				pos1 = pos1 + fullSymbol.length() + 1; // immediately skip the ,
				                                       // as well
				pos2 = line.indexOf(fullSymbol, pos1) - 1; // don't include last
				                                           // ,
				parsedLine.add(line.substring(pos1, pos2));
				parsedLine.add(symbol);
				pos1 = pos2 + fullSymbol.length() + 1;
			}
			else if (line.charAt(pos1) == '\"') {
				pos1 += 1;
				pos2 = line.indexOf('\"', pos1);
				parsedLine.add(line.substring(pos1, pos2));
				pos1 = pos2 + 1;
			}
			else if (line.charAt(pos1) != ',') {
				pos2 = line.indexOf(',', pos1);
				if (pos2 <= pos1) {
					pos2 = line.length();
				}
				parsedLine.add(line.substring(pos1, pos2));
				pos1 = pos2;
			}
		}
		return new StockQuotesData(
		        parsedLine.toArray(new String[this.properties.size()]));
	}

}
