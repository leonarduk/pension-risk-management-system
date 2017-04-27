package com.leonarduk.finance.stockfeed.ft;

import java.io.IOException;
import java.util.Optional;

import org.htmlparser.util.ParserException;

import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.web.HtmlParser;
import com.leonarduk.web.HtmlParserImpl;

import yahoofinance.Stock;

public class FTFeed extends StockFeed {

	@Override
	public Optional<Stock> get(Exchange exchange, String ticker, int years) throws IOException {
		try {
			HtmlParser parser = new HtmlParserImpl("https://markets.ft.com/data/etfs/tearsheet/historical?s=" + ticker);
			/**
			 * 
			 * https://bitbucket.org/financewebsite/finance_website/src/23673965b326bb4516a36b034dc004f6dc401f14/tags/20111210_Old_Website/public_html/finance/php/DataFeeds.php?at=master&fileviewer=file-view-default
			 * 
			 * need to set dates and repeat for each 12 month period
			 * https://markets.ft.com/data/etfs/tearsheet/historical?s=M9SF:GER:EUR
			 * 
			 * function getDataFromLinkedPage($symbol, $startDate, $endDate){
			 * $target = $this->getBaseURL().
			 * "Tearsheets/PriceHistoryPopup?symbol=" . $symbol;
			 * $this->printSource($target);
			 * 
			 * $return_array = http_get($target, $this->getBaseURL());
			 * 
			 * # $tableContainerText = return_between($return_array['FILE'],
			 * "<tableContainer>", "</tableContainer>", EXCL); $bodyText =
			 * return_between($return_array['FILE'], "<tBody>", "</tBody>",
			 * EXCL);
			 * 
			 * ## remove all , $bodyText = str_replace(",", "", $bodyText);
			 * 
			 * ## remove all
			 * <tr>
			 * $bodyText = str_replace("
			 * <tr>
			 * ", "", $bodyText);
			 * 
			 * ## remove all
			 * <td class="text">$bodyText = str_replace('
			 * <td class="text">', "", $bodyText); $bodyText = str_replace('
			 * <td>', "", $bodyText);
			 * 
			 * ## convert</td>
			 * </tr>
			 * to new line $bodyText = str_replace('</td>
			 * </tr>
			 * ', "\n", $bodyText);
			 * 
			 * ## convert</td> to , $bodyText = str_replace('</td>', ",",
			 * $bodyText);
			 * 
			 * # Request the data $rows = explode("\n", $bodyText);
			 * 
			 * return $this->processResultsTable($rows, $startDate, $endDate,
			 * $scalingFactor); }
			 */

		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
