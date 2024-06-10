module YahooFinanceAPI {
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    exports yahoofinance;
    exports yahoofinance.histquotes;
    exports yahoofinance.quotes.stock;
    exports yahoofinance.quotes.csv;
    exports yahoofinance.quotes.fx;
}