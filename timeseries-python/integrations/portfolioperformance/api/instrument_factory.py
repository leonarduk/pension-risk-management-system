from integrations.portfolioperformance.api.instrument_builder import InstrumentBuilder


class InstrumentFactory:
    def __init__(self, metadata: dict):
        self.metadata = metadata

    def build(self):
        required = ["name", "isin", "currency", "ticker"]
        missing = [key for key in required if not self.metadata.get(key)]
        if missing:
            raise ValueError(f"Missing required metadata: {', '.join(missing)}")

        return (
            InstrumentBuilder()
            .with_name(self.metadata["name"])
            .with_isin(self.metadata["isin"])
            .with_ticker(self.metadata["ticker"])
            .with_currency(self.metadata["currency"])
            .with_feed(
                "GENERIC_HTML_TABLE",
                feed_url=self.metadata.get("source_url"),
                latest_feed="YAHOO",
            )
            .with_updated_at()
            .build()
        )
