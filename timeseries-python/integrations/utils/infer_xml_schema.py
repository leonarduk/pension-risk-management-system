import xml.etree.ElementTree as ET
import os
from xml.dom import minidom

def walk_tree_one_sample(element, path="", seen=None):
    if seen is None:
        seen = set()

    tag_path = f"{path}/{element.tag}"
    if tag_path in seen:
        return None
    seen.add(tag_path)

    # Create a copy of the element with original attributes
    new_elem = ET.Element(element.tag, attrib=element.attrib)

    # Keep actual text if available
    if element.text and element.text.strip():
        new_elem.text = element.text.strip()

    # Recursively add only the first encountered child of each tag path
    for child in element:
        child_elem = walk_tree_one_sample(child, tag_path, seen)
        if child_elem is not None:
            new_elem.append(child_elem)

    return new_elem

def create_sample_xml(xml_file, output_file="sample.xml"):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    sample_root = walk_tree_one_sample(root)

    # Convert ElementTree to string and pretty print
    rough_string = ET.tostring(sample_root, encoding='utf-8')
    pretty_xml = minidom.parseString(rough_string).toprettyxml(indent="  ")

    with open(output_file, "w", encoding="utf-8") as f:
        f.write(pretty_xml)

    print(f"Sample XML written to {output_file}")

def main(xml_file: str, output_file: str = "sample.xml"):
    if not os.path.exists(xml_file):
        print(f"Error: File not found - {xml_file}")
        return

    create_sample_xml(xml_file, output_file)

if __name__ == "__main__":
    main("C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml")
