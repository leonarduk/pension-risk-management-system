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
                latest_feed="YAHOO"
            )
            .with_updated_at()
            .build()
        )


# ✅ Optional CLI usage or test driver
if __name__ == "__main__":
    from integrations.investing_com.BROKEN.instrument import InvestingInstrumentExtractor
    from instrument_details import upsert_instrument_from_json
    import json

    url = "https://www.investing.com/equities/assura-group"
    xml_file = "sample.xml"
    output_file = "sample_updated.xml"

    extractor = InvestingInstrumentExtractor(url)
    metadata = extractor.extract_metadata()

    print("✅ Extracted metadata:")
    print(json.dumps(metadata, indent=2))

    factory = InstrumentFactory(metadata)
    instrument = factory.build()

    print("✅ Built instrument:")
    print(json.dumps(instrument, indent=2))

    upsert_instrument_from_json(
        xml_file=xml_file,
        json_data=instrument,
        output_file=output_file
    )
    print(f"✅ Instrument written to {output_file}")
