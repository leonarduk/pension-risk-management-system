import os
import xml.etree.ElementTree as ET
import pandas as pd

def find_matching_security(root, search_name):
    securities = root.findall(".//securities/security")
    for sec in securities:
        name = sec.findtext("name", "").strip()
        if search_name.lower() in name.lower():
            return sec
    return None

def extract_attributes(security):
    result = {}
    for entry in security.findall(".//attributes/map/entry"):
        key_elem = entry.find("string")
        value_elem = entry.find("*[2]")  # second child (value)
        if key_elem is not None and value_elem is not None:
            key = key_elem.text.strip()
            value = value_elem.text.strip() if value_elem.text else ""
            result[f"custom:{key}"] = value
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
                    if name in taxonomies:
                        taxonomies[name].append(class_name)
                    else:
                        taxonomies[name] = [class_name]
    return taxonomies

def extract_instrument_info(xml_file, instrument_name):
    if not os.path.exists(xml_file):
        raise FileNotFoundError(f"File not found: {xml_file}")

    tree = ET.parse(xml_file)
    root = tree.getroot()

    security = find_matching_security(root, instrument_name)
    if security is None:
        print(f"❌ No instrument found matching '{instrument_name}'")
        return

    base_info = {
        "uuid": security.findtext("uuid", ""),
        "name": security.findtext("name", "").strip(),
        "currencyCode": security.findtext("currencyCode", ""),
        "isin": security.findtext("isin", ""),
        "tickerSymbol": security.findtext("tickerSymbol", ""),
        "wkn": security.findtext("wkn", ""),
        "feed": security.findtext("feed", ""),
        "feedURL": security.findtext("feedURL", ""),
        "prices": security.findtext("prices", ""),
        "latestFeed": security.findtext("latestFeed", ""),
        "latest": security.findtext("latest", ""),
        "events": security.findtext("events", ""),
        "isRetired": security.findtext("isRetired", ""),
        "updatedAt": security.findtext("updatedAt", ""),
        "id": security.attrib.get("id", "")
    }

    # Merge attributes
    attributes = extract_attributes(security)
    base_info.update(attributes)

    # Add tags and categories as blank for now
    base_info["tags"] = ""
    base_info["categories"] = ""

    # Add taxonomy info
    taxonomy_data = extract_taxonomies(root, base_info["id"])
    for taxo_name, values in taxonomy_data.items():
        base_info[f"taxonomy:{taxo_name}"] = ", ".join(values)

    df = pd.DataFrame(base_info.items(), columns=["Field", "Value"])
    print("✅ Instrument Information:")
    print(df.to_string(index=False))

def main():
    extract_instrument_info(
        xml_file="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        instrument_name="iShares UK Dividend ETF GBP Dist UCITS"
    )

if __name__ == "__main__":
    main()
