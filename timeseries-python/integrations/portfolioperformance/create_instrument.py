import json
import os
from datetime import datetime

from integrations.portfolioperformance.api.static.ftse_lookup import FTSE_LOOKUP


def lookup_ftse_instrument(query):
    q = query.strip().lower()
    for inst in FTSE_LOOKUP:
        if q in (inst["ticker"].lower(), inst["isin"].lower(), inst["name"].lower()):
            return inst
    return None

def create_instrument_from_ftse(data, xml_file: str, output_file: str):
    instrument = {
        "uuid": str(os.urandom(16).hex()),
        "name": data["name"],
        "isin": data["isin"],
        "tickerSymbol": data["ticker"],
        "currencyCode": data["currency"],
        "feed": "GENERIC_HTML_TABLE",
        "feedURL": f"http://localhost:8091/stock/ticker/{data['ticker'].replace(':', '')}",
        "latestFeed": "",
        "isRetired": False,
        "updatedAt": datetime.utcnow().isoformat() + "Z",
        "customAttributes": {},
        "taxonomies": {}
    }

    print("\u2705 Built instrument from FTSE static list:")
    print(json.dumps(instrument, indent=2))

    from integrations.portfolioperformance.api.instrument_details import upsert_instrument_from_json
    upsert_instrument_from_json(
        xml_file=xml_file,
        json_data=instrument,
        output_file=output_file
    )
    print(f"\u2705 Instrument written to: {output_file}")

if __name__ == "__main__":
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    output_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"

    query = "Experian"
    match = lookup_ftse_instrument(query)
    if not match:
        raise ValueError(f"No FTSE instrument found for query '{query}'")

    create_instrument_from_ftse(
        data=match,
        xml_file=xml_file,
        output_file=output_file
    )