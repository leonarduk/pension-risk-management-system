module YahooFinanceAPI {
    exports yahoofinance.histquotes;
    exports yahoofinance;
    exports yahoofinance.quotes.stock;
    exports yahoofinance.quotes.csv;
    exports yahoofinance.quotes.fx;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
}