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
    """Insert *or* update a <security> in the PortfolioPerformance XML.

    In addition to the existing fields, this version also handles ``type`` –
    written as a simple <type> element on the security node.
    """

    tree = ET.parse(xml_file)
    root = tree.getroot()
    securities_root = root.find(".//securities")
    if securities_root is None:
        raise ValueError("No <securities> section found.")

    # ------------------------------------------------------------------
    # Normalise ticker for look‑up (upper case & ensure ".L" suffix)
    # ------------------------------------------------------------------
    def _norm(t):
        t = (t or "").strip().upper()
        return t if t.endswith(".L") else f"{t}.L"

    incoming_ticker = _norm(json_data.get("tickerSymbol", ""))

    # Try to locate an existing <security> with the same ticker
    sec = None
    for s in securities_root.findall("security"):
        if _norm(s.findtext("tickerSymbol")) == incoming_ticker:
            sec = s
            break

    if sec is None:
        # ➕ create new
        next_id = max((int(s.attrib.get("id", 0)) for s in securities_root.findall("security")), default=0) + 1
        sec = ET.SubElement(securities_root, "security", id=str(next_id))
        print(f"➕  new <security id='{next_id}'> ({incoming_ticker})")
    else:
        print(f"✏️  updating existing <security id='{sec.attrib.get('id')}'> ({incoming_ticker})")

    # ------------------------------------------------------------------
    # Populate / overwrite the standard fields – now incl. <type>
    # ------------------------------------------------------------------
    field_map = {
        "uuid":          json_data.get("uuid", ""),
        "name":          json_data.get("name", ""),
        "currencyCode":  json_data.get("currencyCode", ""),
        "isin":          json_data.get("isin", ""),
        "tickerSymbol":  json_data.get("tickerSymbol", ""),
        "type":          json_data.get("type", ""),          #  🆕
        "feed":          json_data.get("feed", "MANUAL"),
        "feedURL":       json_data.get("feedURL", ""),
        "latestFeed":    json_data.get("latestFeed", ""),
        "isRetired":     "true" if json_data.get("isRetired") else "false",
        "updatedAt":     json_data.get("updatedAt", datetime.utcnow().isoformat() + "Z"),
    }

    for tag, value in field_map.items():
        elem = sec.find(tag)
        if elem is None:
            elem = ET.SubElement(sec, tag)
        elem.text = value

    # Ensure mandatory children exist
    for tag in ("prices", "events"):
        if sec.find(tag) is None:
            ET.SubElement(sec, tag)
    if sec.find("latest") is None:
        latest = ET.SubElement(sec, "latest", {"v": "0"})
        ET.SubElement(latest, "high").text = "0"
        ET.SubElement(latest, "low").text = "0"
        ET.SubElement(latest, "volume").text = "0"

    # Write back
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
#  Helper:  FTSE tickers missing from your PP file (normalised to *.L)
# ------------------------------------------------------------------
def ftse_tickers_missing_from_file(xml_file, ftse_map_module="ftse_all_share_dict"):
    """
    Return FTSE-All-Share tickers that are not present in your
    PortfolioPerformance XML, normalising everything to end with '.L'.
    """
    # 1)  Your PP tickers  →  ensure *.L suffix
    file_tickers = {
        (t if t.upper().endswith(".L") else f"{t}.L").upper()
        for t in get_all_tickers(xml_file)
    }

    # 2)  FTSE-All-Share tickers  →  ensure *.L suffix
    ftse_tickers = {
        (t if t.upper().endswith(".L") else f"{t}.L").upper()
        for t in ftse_all_share.keys()
    }

    # 3)  Difference
    return ftse_tickers - file_tickers


# ------------------------------------------------------------------
# Optional: get missing (ticker, name) pairs
# ------------------------------------------------------------------
def missing_ftse_with_names(xml_file, ftse_map_module="ftse_all_share_dict"):
    from importlib import import_module
    ftse_mod = import_module(ftse_map_module)
    missing = ftse_tickers_missing_from_file(xml_file, ftse_map_module)
    # remove ".L" before lookup in the map
    return {t.rstrip(".L"): ftse_mod.ftse_all_share.get(t.rstrip(".L"), "??") for t in missing}

def upsert_security_element(securities_root, json_data, next_id):
    """
    Append or update a <security>. Return (element, next_id).
    `next_id` is the next free integer id as *str*.
    """
    def norm(t):                                    # `.L` & upper
        t = (t or "").strip().upper()
        return t if t.endswith(".L") else f"{t}.L"

    # ─ Try match by ticker ─────────────────────────────
    incoming_ticker = norm(json_data.get("tickerSymbol", ""))
    for sec in securities_root.findall("security"):
        if norm(sec.findtext("tickerSymbol")) == incoming_ticker:
            return sec, next_id                    # update existing

    # ─ Create new  ─────────────────────────────────────
    sec = ET.SubElement(securities_root, "security", id=str(next_id))
    next_id += 1                                   # bump for caller
    print(f"➕  new <security id='{sec.attrib['id']}'>  ({incoming_ticker})")
    #   ...  populate tags exactly as before  ...
    return sec, next_id

from yfinance import Ticker, shared
from curl_cffi.requests.exceptions import HTTPError

def bulk_add_missing_ftse(xml_path, tickers_to_add, out_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    securities_root = root.find(".//securities")

    # compute first free id once
    next_id = max(int(s.attrib.get("id", "0")) for s in securities_root.findall("security")) + 1

    for tkr in sorted(tickers_to_add):
        try:
            info = Ticker(tkr).info                 # can raise 404
        except HTTPError as e:
            print(f"⚠️  {tkr}: {e}.  Skipped.")
            continue

        json_data = {
            "uuid":        shared.generate_uuid(),  # helper in yfinance
            "name":        info.get("longName") or info.get("shortName", tkr),
            "currencyCode": info.get("currency", "GBP"),
            "tickerSymbol": tkr,
            "feed":        "GENERIC_HTML_TABLE",
            "updatedAt":   datetime.utcnow().isoformat() + "Z",
            "isin":        info.get("isin", ""),
        }

        _, next_id = upsert_security_element(securities_root, json_data, next_id)

    tree.write(out_path, encoding="utf-8", xml_declaration=True)
    print(f"✅  wrote updated XML with {next_id-1} securities ➜  {out_path}")



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


