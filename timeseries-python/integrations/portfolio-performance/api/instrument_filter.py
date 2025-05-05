import os
import xml.etree.ElementTree as ET
import pandas as pd

UUID_ALIASES = {
    # Morningstar
    "07ca79a6-4b1e-4906-8ee6-215d3299ac67": "Morningstar Category",
    "1ffc0583-55ce-4883-984c-15351794e905": "Morningstar Rating",
    # ESG / Internal
    "approval": "Needs Approval",
    "ter": "Total Expense Ratio (TER)",
    # Regions
    "5b3513c3-0b30-49f7-a645-8a391de3f6e1": "Region (MSCI)",
    "a1770f46-42b3-47ff-b58d-13db3a1b48b2": "United Kingdom",
    # Type / Asset Class
    "7783b59f-4f65-41a0-91c9-7c6376e5cfa3": "Asset Class",
    "23df78a0-05f7-4ce2-936f-b648b6f626aa": "Equity",
    "f101db2f-8813-4f07-b82c-fb2e98a3cb55": "Bond",
    # Income
    "income_4_6": "Income Bearing (4–6 Medium)",
    "income_7_10": "Income Bearing (7–10 High)"
}

def build_security_index(root):
    securities = {}
    for sec in root.findall(".//securities/security"):
        sid = sec.attrib.get("id")
        record = {
            "name": sec.findtext("name", ""),
            "isin": sec.findtext("isin", ""),
            "tickerSymbol": sec.findtext("tickerSymbol", ""),
            "id": sid
        }

        # Add readable custom attributes
        for entry in sec.findall(".//attributes/map/entry"):
            elems = list(entry)
            if len(elems) >= 2:
                key = elems[0].text.strip()
                val = elems[1].text.strip() if elems[1].text else ""
                key_alias = UUID_ALIASES.get(key, key)
                record[f"custom:{key_alias}"] = val
        securities[sid] = record
    return securities

def build_taxonomy_reverse_lookup(root):
    reverse = {}
    for taxonomy in root.findall(".//taxonomies/taxonomy"):
        tax_name = taxonomy.attrib.get("name")
        for classification in taxonomy.findall(".//classification"):
            class_name = classification.findtext("name", "")
            for assignment in classification.findall(".//assignment"):
                inv = assignment.find("investmentVehicle")
                if inv is not None and inv.attrib.get("class") == "security":
                    sec_id = inv.attrib.get("reference")
                    reverse.setdefault(tax_name, {}).setdefault(class_name, []).append(sec_id)
    return reverse

def filter_by_attribute(attr_key, attr_value, xml_file, op="eq"):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    securities = build_security_index(root)

    matches = []
    for sec in securities.values():
        value = sec.get(f"custom:{attr_key}")
        if value is None:
            continue
        try:
            if op == "lt" and float(value) < float(attr_value):
                matches.append(sec)
            elif op == "eq" and str(value).lower() == str(attr_value).lower():
                matches.append(sec)
        except:
            continue
    return matches

def filter_by_taxonomy(taxonomy_name, class_name, xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    taxonomy_lookup = build_taxonomy_reverse_lookup(root)
    securities = build_security_index(root)

    matching_ids = taxonomy_lookup.get(taxonomy_name, {}).get(class_name, [])
    return [securities[sid] for sid in matching_ids if sid in securities]

def display_results(results, title="Filtered Instruments"):
    if results:
        df = pd.DataFrame(results)[["name", "tickerSymbol", "isin"]]
        pd.set_option("display.max_rows", None)
        pd.set_option("display.width", 0)
        print(f"\n✅ {title} ({len(df)} matches):")
        print(df.to_string(index=False))
    else:
        print("❌ No matching instruments found.")

def main():
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"

    # Example 1: TER < 0.2
    results = filter_by_attribute("Total Expense Ratio (TER)", 0.2, xml_file, op="lt")
    display_results(results, "Instruments with TER < 0.2")

    # Example 2: Morningstar Category = UK Equity Income
    results = filter_by_attribute("Morningstar Category", "UK Equity Income", xml_file, op="eq")
    display_results(results, "Instruments with Morningstar Category = UK Equity Income")

    # Example 3: From taxonomy: Category = UK Equity Income
    results = filter_by_taxonomy("Category", "UK Equity Income", xml_file)
    display_results(results, "Taxonomy: Category = UK Equity Income")

if __name__ == "__main__":
    main()
