package yahoofinance.quotes;

/**
 *
 * @author Stijn Strickx
 */
public enum QuotesProperty {

	AfterHoursChangeRealtime("c8"), // After Hours Change (Realtime)
	AnnualizedGain("g3"), // Annualized Gain
	Ask("a"), // Ask
	AskRealtime("b2"), // Ask (Realtime)
	AskSize("a5"), // Ask Size
	AverageDailyVolume("a2"), // Average Daily Volume
	Bid("b"), // Bid
	BidRealtime("b3"), // Bid (Realtime)
	BidSize("b6"), // Bid Size
	BookValuePerShare("b4"), // Book Value Per Share
	Change("c1"), // Change
	Change_ChangeInPercent("c"), // ChangeÂ Change In Percent
	ChangeFromFiftydayMovingAverage("m7"), // Change From Fiftyday Moving Average
	ChangeFromTwoHundreddayMovingAverage("m5"), // Change From Two Hundredday Moving Average
	ChangeFromYearHigh("k4"), // Change From Year High
	ChangeFromYearLow("j5"), // Change From Year Low
	ChangeInPercent("p2"), // Change In Percent
	ChangeInPercentFromYearHigh("k5"), // Change In Percent (Realtime)
	ChangeInPercentRealtime("k2"), // Change (Realtime)
	ChangeRealtime("c6"), // Commission
	Commission("c3"), // Currency
	Currency("c4"), // Days High
	DaysHigh("h"), // Days Low
	DaysLow("g"), // Days Range
	DaysRange("m"), // Days Range (Realtime)
	DaysRangeRealtime("m2"), // Days Value Change
	DaysValueChange("w1"), // Days Value Change (Realtime)
	DaysValueChangeRealtime("w4"), // Dividend Pay Date
	DilutedEPS("e"), // Trailing Annual Dividend Yield
	DividendPayDate("r1"), // Trailing Annual Dividend Yield In Percent
	EBITDA("j4"), // Diluted E P S
	EPSEstimateCurrentYear("e7"), // E B I T D A
	EPSEstimateNextQuarter("e9"), // E P S Estimate Current Year
	EPSEstimateNextYear("e8"), // E P S Estimate Next Quarter
	ExDividendDate("q"), // E P S Estimate Next Year
	FiftydayMovingAverage("m3"), // Ex Dividend Date
	HighLimit("l2"), // Fiftyday Moving Average
	HoldingsGain("g4"), // Shares Float
	HoldingsGainPercent("g1"), // High Limit
	HoldingsGainPercentRealtime("g5"), // Holdings Gain
	HoldingsGainRealtime("g6"), // Holdings Gain Percent
	HoldingsValue("v1"), // Holdings Gain Percent (Realtime)
	HoldingsValueRealtime("v7"), // Holdings Gain (Realtime)
	LastTradeDate("d1"), // Holdings Value
	LastTradePriceOnly("l1"), // Holdings Value (Realtime)
	LastTradeRealtimeWithTime("k1"), // Last Trade Date
	LastTradeSize("k3"), // Last Trade Price Only
	LastTradeTime("t1"), // Last Trade (Realtime) With Time
	LastTradeWithTime("l"), // Last Trade Size
	LowLimit("l3"), // Last Trade Time
	MarketCapitalization("j1"), // Last Trade With Time
	MarketCapRealtime("j3"), // Low Limit
	MoreInfo("i"), // Market Capitalization
	Name("n"), // Market Cap (Realtime)
	Notes("n4"), // More Info
	OneyrTargetPrice("t8"), // Name
	Open("o"), // Notes
	OrderBookRealtime("i5"), // Oneyr Target Price
	PEGRatio("r5"), // Open
	PERatio("r"), // Order Book (Realtime)
	PERatioRealtime("r2"), // P E G Ratio
	PercentChangeFromFiftydayMovingAverage("m8"), // P E Ratio
	PercentChangeFromTwoHundreddayMovingAverage("m6"), // P E Ratio (Realtime)
	PercentChangeFromYearLow("j6"), // Percent Change From Fiftyday Moving Average
	PreviousClose("p"), // Percent Change From Two Hundredday Moving Average
	PriceBook("p6"), // Change In Percent From Year High
	PriceEPSEstimateCurrentYear("r6"), // Percent Change From Year Low
	PriceEPSEstimateNextYear("r7"), // Previous Close
	PricePaid("p1"), // Price Book
	PriceSales("p5"), // Price E P S Estimate Current Year
	Revenue("s6"), // Price E P S Estimate Next Year
	SharesFloat("f6"), // Price Paid
	SharesOutstanding("j2"), // Price Sales
	SharesOwned("s1"), // Revenue
	ShortRatio("s7"), // Shares Owned
	StockExchange("x"), // Shares Outstanding
	Symbol("s"), // Short Ratio
	TickerTrend("t7"), // Stock Exchange
	TradeDate("d2"), // Symbol
	TradeLinks("t6"), // Ticker Trend
	TradeLinksAdditional("f"), // Trade Date
	TrailingAnnualDividendYield("d"), // Trade Links
	TrailingAnnualDividendYieldInPercent("y"), // Trade Links Additional
	TwoHundreddayMovingAverage("m4"), // Two Hundredday Moving Average
	Volume("v"), // Volume
	YearHigh("k"), // Year High
	YearLow("j"), // Year Low
	YearRange("w"); // Year Range

	private final String tag;

	QuotesProperty(final String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return this.tag;
	}
}
