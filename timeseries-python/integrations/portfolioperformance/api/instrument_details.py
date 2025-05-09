import os
import xml.etree.ElementTree as ET
from datetime import datetime

import pandas as pd

from integrations.portfolioperformance.api.instrument_filter import build_security_index, build_taxonomy_reverse_lookup
from integrations.portfolioperformance.api.static.ftse_all_share_dict import ftse_all_share
from integrations.portfolioperformance.api.static.uuid_aliases import UUID_ALIASES


def find_matching_security(root, identifier):
    identifier = identifier.strip().lower()
    for sec in root.findall(".//securities/security"):
        name = sec.findtext("name", "").strip().lower()
        isin = sec.findtext("isin", "").strip().lower()
        ticker = sec.findtext("tickerSymbol", "").strip().lower()
        if identifier in (name, isin, ticker):
            return sec
    return None


def extract_attributes(security):
    result = {}
    for entry in security.findall(".//attributes/map/entry"):
        key_elem = entry.find("string")
        value_elem = entry.find("*[2]")
        if key_elem is not None and value_elem is not None:
            key = key_elem.text.strip()
            value = value_elem.text.strip() if value_elem.text else ""
            key_alias = UUID_ALIASES.get(key, key)
            result[f"custom:{key_alias}"] = value
    return result


def extract_taxonomies(root, security_id):
    taxonomies = {}
    for taxonomy in root.findall(".//taxonomies/taxonomy"):
        name = taxonomy.findtext("name", "Unknown")
        for classification in taxonomy.findall(".//classification"):
            for assignment in classification.findall("assignments/assignment"):
                inv = assignment.find("investmentVehicle")
                if inv is not None and inv.attrib.get("class") == "security" and inv.attrib.get("reference") == str(security_id):
                    class_name = classification.findtext("name")
                    taxonomies.setdefault(name, []).append(class_name)
    return taxonomies


def extract_instrument(xml_file, identifier, format="table"):
    if not os.path.exists(xml_file):
        raise FileNotFoundError(f"File not found: {xml_file}")

    tree = ET.parse(xml_file)
    root = tree.getroot()
    sec = find_matching_security(root, identifier)

    if sec is None:
        msg = f"❌ Instrument not found matching '{identifier}'"
        if format == "json":
            raise ValueError(msg)
        else:
            print(msg)
            return

    instrument = {
        "id": sec.attrib.get("id"),
        "uuid": sec.findtext("uuid"),
        "name": sec.findtext("name", "").strip(),
        "currencyCode": sec.findtext("currencyCode"),
        "isin": sec.findtext("isin"),
        "tickerSymbol": sec.findtext("tickerSymbol"),
        "wkn": sec.findtext("wkn"),
        "feed": sec.findtext("feed"),
        "feedURL": sec.findtext("feedURL"),
        "latestFeed": sec.findtext("latestFeed"),
        "isRetired": sec.findtext("isRetired") == "true",
        "updatedAt": sec.findtext("updatedAt"),
        "customAttributes": extract_attributes(sec),
        "taxonomies": extract_taxonomies(root, sec.attrib.get("id"))
    }

    if format == "json":
        return instrument

    flat_data = {**instrument}
    flat_data.pop("customAttributes", None)
    flat_data.pop("taxonomies", None)
    flat_data.update(instrument["customAttributes"])
    for taxo, values in instrument["taxonomies"].items():
        flat_data[f"taxonomy:{taxo}"] = ", ".join(values)

    df = pd.DataFrame(flat_data.items(), columns=["Field", "Value"])
    print("✅ Instrument Information:")
    print(df.to_string(index=False))


def upsert_instrument_from_json(xml_file, json_data, output_file=None):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    securities_root = root.find(".//securities")
    if securities_root is None:
        raise ValueError("No <securities> section found.")

    sec = None
    for s in securities_root.findall("security"):
        if s.findtext("isin", "").strip() == json_data["isin"]:
            sec = s
            break

    if sec is None:
        new_id = str(int(max((int(s.attrib.get("id", "0")) for s in securities_root.findall("security")), default=0)) + 1)
        sec = ET.Element("security", id=new_id)
        print(f"➕ Created new <security> with ID {new_id}")

        ET.SubElement(sec, "uuid").text = json_data.get("uuid", "")
        ET.SubElement(sec, "name").text = json_data.get("name", "")
        ET.SubElement(sec, "currencyCode").text = json_data.get("currencyCode", "")
        ET.SubElement(sec, "isin").text = json_data.get("isin", "")
        ET.SubElement(sec, "tickerSymbol").text = json_data.get("tickerSymbol", "")

        ET.SubElement(sec, "feed").text = json_data.get("feed", "MANUAL")

        feed_url = json_data.get("feedURL", "")
        if feed_url:
            ET.SubElement(sec, "feedURL").text = feed_url

        ET.SubElement(sec, "prices")

        latest_feed = json_data.get("latestFeed", "")
        if latest_feed:
            ET.SubElement(sec, "latestFeed").text = latest_feed

        latest = ET.SubElement(sec, "latest", {"v": "0"})
        ET.SubElement(latest, "high").text = "0"
        ET.SubElement(latest, "low").text = "0"
        ET.SubElement(latest, "volume").text = "0"

        attr_root = ET.SubElement(sec, "attributes")
        ET.SubElement(attr_root, "map")

        ET.SubElement(sec, "events")
        ET.SubElement(sec, "isRetired").text = "false"
        ET.SubElement(sec, "updatedAt").text = json_data.get("updatedAt", datetime.utcnow().isoformat() + "Z")

        securities_root.append(sec)
    else:
        print(f"✏️ Updating existing <security> with ID {sec.attrib.get('id')}")

    output_path = output_file or xml_file
    tree.write(output_path, encoding="utf-8", xml_declaration=True)
    print(f"✅ XML written to: {output_path}")

# ------------------------------------------------------------------
#  NEW HELPERS
# ------------------------------------------------------------------

def get_all_tickers(xml_file, unique=True, skip_blank=True):
    """
    Return a list of ticker symbols found in the XML.

    Args
    ----
    xml_file : str | pathlib.Path
        Path to the PortfolioPerformance XML file.
    unique : bool, default True
        If True, duplicates are removed.
    skip_blank : bool, default True
        If True, empty / missing tickerSymbol fields are ignored.

    Returns
    -------
    list[str]
        List of ticker symbols (optionally deduplicated).
    """
    tree = ET.parse(xml_file)
    securities = build_security_index(tree.getroot())

    tickers = [
        sec.get("tickerSymbol", "").strip()
        for sec in securities.values()
        if not (skip_blank and not sec.get("tickerSymbol", "").strip())
    ]

    return sorted(set(tickers)) if unique else tickers


# ------------------------------------------------------------------
#  SAFER helper – skips or falls back if <security> has no id
# ------------------------------------------------------------------

def instruments_without_tickers(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    securities = build_security_index(root)

    # Build raw <security> lookup, but skip any without an id/uuid
    raw_sec_lookup = {}
    for s in root.findall(".//securities/security"):
        sid = s.attrib.get("id") or s.findtext("uuid")      # uuid fallback
        if not sid:
            continue                                        # give up on totally anonymous nodes
        raw_sec_lookup[sid] = s

    taxonomy_lookup = build_taxonomy_reverse_lookup(root)

    def _type_from_tax(sec_id):
        for tax_name, classes in taxonomy_lookup.items():
            if any(word in tax_name.lower() for word in ("type", "asset class")):
                for class_name, ids in classes.items():
                    if sec_id in ids:
                        return class_name
        return ""

    results = []
    for sec in securities.values():
        if sec.get("tickerSymbol", "").strip():
            continue   # we only want those *without* tickers

        sec_id = sec.get("id") or sec.get("uuid") or ""
        raw_elem = raw_sec_lookup.get(sec_id, ET.Element(""))

        sec_type = (
            raw_elem.attrib.get("type", "")
            or raw_elem.findtext("type", "")
            or _type_from_tax(sec_id)
            or "Unknown"
        ).strip()

        results.append(
            {
                "id": sec_id or "N/A",
                "name": sec["name"],
                "type": sec_type,
            }
        )
    return results

# ------------------------------------------------------------------
#  Prerequisites
# ------------------------------------------------------------------
# 1. Make sure ftse_all_share_dict.py (or .json) is on PYTHONPATH
# 2. Keep get_all_tickers(xml_file) from earlier

# ------------------------------------------------------------------
#  Helper
# ------------------------------------------------------------------
def ftse_tickers_missing_from_file(xml_file, ftse_map_module="ftse_all_share_dict"):
    """
    Return the set of FTSE‑All‑Share tickers that do not appear
    in PortfolioPerformance's get_all_tickers(xml_file) list.

    Parameters
    ----------
    xml_file : str | pathlib.Path
        Path to the PortfolioPerformance XML export.
    ftse_map_module : str
        Module name that contains `ftse_all_share` dict
        (default assumes `from ftse_all_share_dict import ftse_all_share`).

    Returns
    -------
    set[str]
        Tickers that are in FTSE All‑Share but missing in your XML file.
    """
    # ── 1) all tickers in your PP file ──────────────────────────────
    file_tickers = set(get_all_tickers(xml_file))

    # ── 2) all FTSE tickers (module import) ─────────────────────────
    ftse_tickers = set(ftse_all_share.keys())

    # ── 3) difference ───────────────────────────────────────────────
    return ftse_tickers - file_tickers


# ------------------------------------------------------------------
#  Example usage
# ------------------------------------------------------------------
if __name__ == "__main__":
    xml_file = r"C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"

    # --- new demo calls -------------------------------------------------
    all_tickers = get_all_tickers(xml_file)
    print(f"\n✅ Found {len(all_tickers)} unique tickers")
    print(all_tickers[:20], "...")          # preview

    missing = ftse_tickers_missing_from_file(xml_file)
    print(f"\n⛔  {len(missing)} FTSE‑All‑Share tickers are NOT in your XML:")
    print(sorted(list(missing))[:50], "…")  # preview first 50

    # no_ticker = instruments_without_tickers(xml_file)
    # print(f"\n⚠️  Instruments with NO ticker ({len(no_ticker)}):")
    # for item in no_ticker:
    #     print(
    #         f"  • {item['name']:<45} "
    #         f"(type={item['type'] or 'Unknown':<15}  id={item['id']})"
    #     )


