# integrations/portfolioperformance/api/instrument_builder.py
from datetime import datetime, UTC
from uuid import uuid4


class InstrumentBuilder:
    """Fluent helper for creating/updating <security> dictionaries that
    can later be serialised to PortfolioPerformance XML.

    All mutator helpers return *self* so calls can be chained:

    >>> instrument = (
    ...     InstrumentBuilder()
    ...     .with_name("AstraZeneca plc")
    ...     .with_isin("GB0009895292")
    ...     .with_ticker("AZN.L")
    ...     .with_currency("GBX")
    ...     .with_type("Equity")               # 👈 NEW
    ...     .build()
    ... )
    """

    def __init__(self):
        self.data = {
            "uuid": str(uuid4()),
            "isRetired": False,
            "customAttributes": {},
            "taxonomies": {},
            "updatedAt": datetime.now(UTC).isoformat(),
        }

    # ------------------------------------------------------------------
    # Hard‑set the <security id> that PortfolioPerformance uses
    # ------------------------------------------------------------------
    def with_id(self, value: int | str):
        """Manually override the auto‑assigned <security id>."""
        self.data["id"] = str(value)
        return self

    # ------------------------------------------------------------------
    # NEW ➜ allow callers to specify the *type* of security
    # ------------------------------------------------------------------
    def with_type(self, security_type: str):
        """Set the `<type>` element for the security (e.g. 'Stock', 'ETF')."""
        self.data["type"] = security_type
        return self

    # ------------------------------------------------------------------
    # Simple setters that write straight through to the backing dict
    # ------------------------------------------------------------------
    def with_name(self, name):
        self.data["name"] = name
        return self

    def with_isin(self, isin):
        self.data["isin"] = isin
        return self

    def with_ticker(self, ticker):
        self.data["tickerSymbol"] = ticker
        return self

    def with_currency(self, currency):
        self.data["currencyCode"] = currency
        return self

    def with_feed(self, feed, feed_url=None, latest_feed=None):
        self.data["feed"] = feed or "GENERIC_HTML_TABLE"
        self.data["feedURL"] = feed_url or ""
        self.data["latestFeed"] = latest_feed or ""
        return self

    def with_custom_attribute(self, label, value):
        self.data["customAttributes"][label] = str(value)
        return self

    def with_updated_at(self, iso_time=None):
        self.data["updatedAt"] = iso_time or datetime.now(UTC).isoformat()
        return self

    # ------------------------------------------------------------------
    # Finalise: sanity‑check mandatory fields then return raw dict
    # ------------------------------------------------------------------
    def build(self):
        for key in ("name", "currencyCode", "tickerSymbol"):
            if not self.data.get(key):
                raise ValueError(f"Missing required field: {key}")
        return self.data
