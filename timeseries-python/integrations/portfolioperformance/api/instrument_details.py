import json
import os
import xml.etree.ElementTree as ET
import pandas as pd

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

    # Print nicely in table form
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

    # Find by ISIN
    sec = None
    for s in securities_root.findall("security"):
        if s.findtext("isin", "").strip() == json_data["isin"]:
            sec = s
            break

    # Create if missing
    if sec is None:
        new_id = str(int(max((int(s.attrib.get("id", "0")) for s in securities_root.findall("security")), default=0)) + 1)
        sec = ET.SubElement(securities_root, "security", id=new_id)
        print(f"➕ Created new <security> with ID {new_id}")
    else:
        print(f"✏️ Updating existing <security> with ID {sec.attrib.get('id')}")

    def update_field(tag, value):
        if value is None:
            return
        el = sec.find(tag)
        if el is None:
            el = ET.SubElement(sec, tag)
        if el.text != str(value):
            el.text = str(value)

    # Only update fields explicitly passed in JSON
    for field in ["uuid", "name", "currencyCode", "isin", "tickerSymbol", "wkn", "feed", "feedURL", "latestFeed", "updatedAt"]:
        update_field(field, json_data.get(field))

    # Special case: isRetired
    if "isRetired" in json_data:
        update_field("isRetired", "true" if json_data["isRetired"] else "false")

    # Update custom attributes carefully
    if "customAttributes" in json_data:
        attr_root = sec.find("attributes")
        if attr_root is None:
            attr_root = ET.SubElement(sec, "attributes")
        attr_map = attr_root.find("map")
        if attr_map is None:
            attr_map = ET.SubElement(attr_root, "map")

        existing_keys = {entry.find("string").text: entry for entry in attr_map.findall("entry") if entry.find("string") is not None}

        for key_label, value in json_data["customAttributes"].items():
            if not key_label.startswith("custom:"):
                continue
            key_uuid = next((k for k, v in UUID_ALIASES.items() if f"custom:{v}" == key_label), key_label)

            if key_uuid in existing_keys:
                # Update value if different
                value_elem = existing_keys[key_uuid].find("*[2]")
                if value_elem is not None and value_elem.text != value:
                    value_elem.text = value
            else:
                entry = ET.SubElement(attr_map, "entry")
                ET.SubElement(entry, "string").text = key_uuid
                value_type = "boolean" if value.lower() in ("true", "false") else "string"
                ET.SubElement(entry, value_type).text = value

    # Write the file
    output_path = output_file or xml_file
    tree.write(output_path, encoding="utf-8", xml_declaration=True)
    print(f"✅ XML written to: {output_path}")


def main():
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"

    # # Show in table
    # extract_instrument(xml_file=xml_file, identifier="Experian plc", format="table")

    # Export as JSON
    instrument = extract_instrument(xml_file=xml_file, identifier="GB00B19NLV48", format="json")
    print("\n✅ JSON Format:")
    print(json.dumps(instrument, indent=2))

    instrument["tickerSymbol"] = "EXPN.L"

    upsert_instrument_from_json(
        xml_file=xml_file,
        json_data=instrument,
        output_file=xml_file
    )

    # Export as JSON
    data = extract_instrument(xml_file, "GB00B19NLV48", format="json")
    print("\n✅ JSON Format:")
    print(json.dumps(data, indent=2))



if __name__ == "__main__":
    main()
