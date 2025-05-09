# integrations/portfolioperformance/api/instrument_builder.py
from datetime import datetime, UTC
from uuid import uuid4

class InstrumentBuilder:
    def __init__(self):
        self.data = {
            "uuid": str(uuid4()),
            "isRetired": False,
            "customAttributes": {},
            "taxonomies": {},
            "updatedAt": datetime.now(UTC).isoformat()
        }

    # NEW -----------------------------------------------------------
    def with_id(self, value: int | str):
        """Hard-set the <security id> that PortfolioPerformance uses."""
        self.data["id"] = str(value)
        return self
    # ---------------------------------------------------------------

    def with_name(self, name):               self.data["name"]          = name;           return self
    def with_isin(self, isin):               self.data["isin"]          = isin;           return self
    def with_ticker(self, ticker):           self.data["tickerSymbol"]  = ticker;         return self
    def with_currency(self, currency):       self.data["currencyCode"]  = currency;       return self

    def with_feed(self, feed, feed_url=None, latest_feed=None):
        self.data["feed"]      = feed or "GENERIC_HTML_TABLE"
        self.data["feedURL"]   = feed_url or ""
        self.data["latestFeed"]= latest_feed or ""
        return self

    def with_custom_attribute(self, label, value):
        self.data["customAttributes"][label] = str(value)
        return self

    def with_updated_at(self, iso_time=None):
        self.data["updatedAt"] = iso_time or datetime.now(UTC).isoformat()
        return self

    def build(self):
        for key in ("name", "currencyCode", "tickerSymbol"):
            if not self.data.get(key):
                raise ValueError(f"Missing required field: {key}")
        # defaults already set above
        return self.data
