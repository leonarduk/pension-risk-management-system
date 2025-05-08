import json
import os
import xml.etree.ElementTree as ET
import pandas as pd
from datetime import date, datetime

UUID_ALIASES = {
    # Morningstar
    "07ca79a6-4b1e-4906-8ee6-215d3299ac67": "Morningstar Category",
    "1ffc0583-55ce-4883-984c-15351794e905": "Morningstar Rating",

    # ESG / Approval / Internal Attributes
    "approval": "Needs Approval",
    "ter": "Total Expense Ratio (TER)",

    # Regions (MSCI-style)
    "5b3513c3-0b30-49f7-a645-8a391de3f6e1": "Region (MSCI)",
    "c8d30b61-8a29-4aa4-bbb3-e3b7fcf94fa1": "Developed Markets",
    "d6476d71-d6e5-4a93-aaa2-5b410ae354b6": "Europe",
    "a1770f46-42b3-47ff-b58d-13db3a1b48b2": "United Kingdom",

    # Type / Asset Class
    "7783b59f-4f65-41a0-91c9-7c6376e5cfa3": "Asset Class",
    "23df78a0-05f7-4ce2-936f-b648b6f626aa": "Equity",
    "f101db2f-8813-4f07-b82c-fb2e98a3cb55": "Bond",

    # Income Bearing
    "income_4_6": "Income Bearing (4–6 Medium)",
    "income_7_10": "Income Bearing (7–10 High)",

    # Sustainability / Custom ESG
    "c4d9e8e6-9f52-412b-ae65-f64904fd45ee": "Sustainability Category",

    # Categories (custom hierarchies)
    "ByCountry": "By Country Category",
    "UK Equity Income": "UK Equity Income Category"
}


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

    new_security = False
    if sec is None:
        new_id = str(int(max((int(s.attrib.get("id", "0")) for s in securities_root.findall("security")), default=0)) + 1)
        sec = ET.Element("security", id=new_id)
        new_security = True
        print(f"➕ Created new <security> with ID {new_id}")

        ET.SubElement(sec, "uuid").text = json_data.get("uuid", "")
        ET.SubElement(sec, "name").text = json_data.get("name", "")
        ET.SubElement(sec, "currencyCode").text = json_data.get("currencyCode", "")
        ET.SubElement(sec, "isin").text = json_data.get("isin", "")
        ET.SubElement(sec, "tickerSymbol").text = json_data.get("tickerSymbol", "")
        ET.SubElement(sec, "feed").text = "MANUAL"
        ET.SubElement(sec, "prices")
        ET.SubElement(sec, "latestFeed").text = ""

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
