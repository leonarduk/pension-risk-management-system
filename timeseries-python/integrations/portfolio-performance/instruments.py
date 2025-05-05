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

def build_tag_lookup(root):
    return {t.attrib["id"]: t.attrib.get("name", "") for t in root.findall(".//tags/tag")}

def build_category_lookup(root):
    categories = {}
    def recurse(cat, path=""):
        name = cat.attrib.get("name", "")
        cat_id = cat.attrib.get("id")
        full_path = f"{path} > {name}" if path else name
        categories[cat_id] = full_path
        for child in cat.findall("./child"):
            recurse(child, full_path)
    for c in root.findall(".//categories/category"):
        recurse(c)
    return categories

def build_taxonomy_lookup(root):
    taxonomy_values = {}
    for taxonomy in root.findall(".//taxonomies/taxonomy"):
        taxonomy_name = taxonomy.attrib.get("name")
        for node in taxonomy.findall(".//node"):
            node_id = node.attrib.get("id")
            node_path = node.attrib.get("name", "")
            parent = node.find("parent")
            if parent is not None and parent.attrib.get("id"):
                parent_id = parent.attrib["id"]
                parent_path = taxonomy_values.get(parent_id, "")
                if parent_path:
                    node_path = f"{parent_path} > {node_path}"
            taxonomy_values[node_id] = node_path
    return taxonomy_values

def extract_security_info(xml_file: str, search_value: str):
    if not os.path.exists(xml_file):
        raise FileNotFoundError(f"File not found: {xml_file}")

    tree = ET.parse(xml_file)
    root = tree.getroot()

    tag_lookup = build_tag_lookup(root)
    category_lookup = build_category_lookup(root)
    taxonomy_lookup = build_taxonomy_lookup(root)

    for sec in root.findall(".//securities/security"):
        name = sec.findtext("name", default="").lower()
        isin = sec.findtext("isin", default="").lower()
        ticker = sec.findtext("tickerSymbol", default="").lower()

        if search_value.lower() in (name, isin, ticker):
            # 1. Base child elements and attributes
            details = {child.tag: child.text.strip() if child.text else "" for child in sec if child.tag != "attributes"}
            details.update(sec.attrib)

            # 2. Custom attributes (map-style)
            for entry in sec.findall(".//attributes/map/entry"):
                elems = list(entry)
                if len(elems) >= 2 and elems[0].tag == "string":
                    key = elems[0].text.strip() if elems[0].text else ""
                    value_elem = elems[1]
                    value = value_elem.text.strip() if value_elem.text else ""
                    # details[f"custom:{key}"] = value
                    key_alias = UUID_ALIASES.get(key, key)
                    details[f"custom:{key_alias}"] = value

            # 3. Tags
            tag_ids = [tag.attrib.get("id") for tag in sec.findall(".//tags/tag")]
            tag_names = [tag_lookup.get(tid, f"[unknown id {tid}]") for tid in tag_ids]
            details["tags"] = ", ".join(tag_names)

            # 4. Categories
            cat_ids = [cat.attrib.get("id") for cat in sec.findall(".//categories/category")]
            cat_names = [category_lookup.get(cid, f"[unknown id {cid}]") for cid in cat_ids]
            details["categories"] = ", ".join(cat_names)

            # 5. Taxonomies
            for assignment in sec.findall(".//taxonomyAssignments/assignment"):
                tax_name = assignment.attrib.get("taxonomy")
                node_id = assignment.attrib.get("node")
                label = taxonomy_lookup.get(node_id, f"[unknown node {node_id}]")
                details[f"taxonomy:{tax_name}"] = label

            return details

    print(f"❌ No instrument found matching '{search_value}'")
    return {}

# ✅ Main for testing or CLI use
def main(xml_file: str, search_value: str):
    data = extract_security_info(xml_file, search_value)
    if data:
        df = pd.DataFrame(data.items(), columns=["Field", "Value"])
        pd.set_option("display.max_rows", None)
        pd.set_option("display.max_columns", None)
        pd.set_option("display.width", 0)
        pd.set_option("display.max_colwidth", None)

        print("✅ Instrument Information:")
        print(df.to_string(index=False))
    else:
        print("❌ No data extracted.")

if __name__ == "__main__":
    main(
        xml_file="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        search_value="IE00B0M63060"  # or ISIN/ticker
    )
