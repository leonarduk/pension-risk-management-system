import json
import os
from datetime import datetime
import finnhub

def create_instrument_from_finnhub(result, xml_file: str, output_file: str):
    required = ["description", "symbol"]
    for field in required:
        if field not in result or not result[field]:
            raise ValueError(f"Missing field '{field}' in search result")

    instrument = {
        "uuid": str(os.urandom(16).hex()),
        "name": result["description"],
        "isin": "",  # Finnhub symbol lookup does not include ISIN
        "tickerSymbol": result["symbol"],
        "currencyCode": "GBX",  # default fallback; real implementation should resolve this
        "feed": "GENERIC_HTML_TABLE",
        "feedURL": f"http://localhost:8091/stock/ticker/{result['symbol'].replace(':', '')}",
        "latestFeed": "",
        "isRetired": False,
        "updatedAt": datetime.utcnow().isoformat() + "Z",
        "customAttributes": {},
        "taxonomies": {}
    }

    print("\u2705 Built instrument from Finnhub:")
    print(json.dumps(instrument, indent=2))

    from integrations.portfolioperformance.api.instrument_details import upsert_instrument_from_json
    upsert_instrument_from_json(
        xml_file=xml_file,
        json_data=instrument,
        output_file=output_file
    )
    print(f"\u2705 Instrument written to: {output_file}")

if __name__ == "__main__":
    FINNHUB_API_KEY = os.getenv("FINNHUB_API_KEY")
    if not FINNHUB_API_KEY:
        raise EnvironmentError("Please set the FINNHUB_API_KEY environment variable.")

    client = finnhub.Client(api_key=FINNHUB_API_KEY)

    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    output_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"

    search_term = "Experian"
    results = client.symbol_lookup(search_term).get("result", [])
    selected = next((r for r in results if r["symbol"] == "EXPN.L"), None)
    if not selected:
        raise ValueError(f"No matching symbol 'EXPN.L' found in search results for {search_term}")

    create_instrument_from_finnhub(
        result=selected,
        xml_file=xml_file,
        output_file=output_file
    )
