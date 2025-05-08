import json
from datetime import datetime
from uuid import uuid4

from integrations.portfolioperformance.api.instrument_details import upsert_instrument_from_json


class InstrumentBuilder:
    def __init__(self):
        self.data = {
            "uuid": str(uuid4()),
            "isRetired": False,
            "customAttributes": {},
            "taxonomies": {},
            "updatedAt": datetime.utcnow().isoformat() + "Z"
        }

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
        self.data["updatedAt"] = iso_time or datetime.utcnow().isoformat() + "Z"
        return self

    def build(self):
        # Ensure required fields are present
        required = ["name", "isin", "currencyCode", "tickerSymbol"]
        missing = [k for k in required if not self.data.get(k)]
        if missing:
            raise ValueError(f"Missing required fields: {', '.join(missing)}")

        # Fill in structurally expected values
        self.data.setdefault("feed", "GENERIC_HTML_TABLE")
        self.data.setdefault("feedURL", "")
        self.data.setdefault("latestFeed", "")
        self.data.setdefault("isRetired", False)
        self.data.setdefault("updatedAt", datetime.utcnow().isoformat() + "Z")

        return self.data


def main():
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    output_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"

    builder = (
        InstrumentBuilder()
        .with_name("My New ETF")
        .with_isin("GB00TEST00001")
        .with_ticker("TEST.L")
        .with_currency("GBP")
        .with_feed("GENERIC_HTML_TABLE", feed_url="http://localhost:8091/stock/ticker/TEST", latest_feed="YAHOO")
        .with_custom_attribute("custom:Total Expense Ratio (TER)", "0.18")
        .with_custom_attribute("custom:Needs Approval", "true")
        .with_updated_at()
    )

    new_instrument = builder.build()

    print("✅ Built instrument:")
    print(json.dumps(new_instrument, indent=2))

    upsert_instrument_from_json(
        xml_file=xml_file,
        json_data=new_instrument,
        output_file=output_file
    )


if __name__ == "__main__":
    main()
