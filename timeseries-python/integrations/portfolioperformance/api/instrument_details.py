import os
import xml.etree.ElementTree as ET
from datetime import datetime

import pandas as pd

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
