import xml.etree.ElementTree as ET
from collections import defaultdict
from datetime import datetime

import pandas as pd


def extract_holdings_from_transactions(xml_file, by_account=False, cutoff_date=None):
    tree = ET.parse(xml_file)
    root = tree.getroot()

    # Handle cutoff date parsing
    if isinstance(cutoff_date, str):
        cutoff_date = datetime.strptime(cutoff_date, "%Y-%m-%d")

    # Build lookup: securityId -> {name, ticker, isin}
    securities = {}
    for sec in root.findall(".//securities/security"):
        sid = sec.attrib.get("id")
        securities[sid] = {
            "name": sec.findtext("name", default=""),
            "isin": sec.findtext("isin", default=""),
            "ticker": sec.findtext("tickerSymbol", default="")
        }

    records = []

    if by_account:
        for account in root.findall(".//account"):
            account_name = account.findtext("name", default="Unnamed Account")
            holdings = defaultdict(float)

            for tx in account.findall(".//account-transaction"):
                date_str = tx.attrib.get("date")
                if cutoff_date and date_str:
                    try:
                        tx_date = datetime.strptime(date_str, "%Y-%m-%d")
                        if tx_date > cutoff_date:
                            continue
                    except ValueError:
                        pass

                security_elem = tx.find("security")
                shares_elem = tx.find("shares")
                if security_elem is not None and shares_elem is not None:
                    sec_id = security_elem.attrib.get("reference")
                    try:
                        shares = float(shares_elem.text.strip())
                        holdings[sec_id] += shares
                    except (ValueError, AttributeError):
                        continue

            for sec_id, total_shares in holdings.items():
                if abs(total_shares) > 1e-6:
                    meta = securities.get(sec_id, {})
                    records.append({
                        "account": account_name,
                        "securityId": sec_id,
                        "name": meta.get("name"),
                        "ticker": meta.get("ticker"),
                        "isin": meta.get("isin"),
                        "quantity": total_shares
                    })

    else:
        holdings = defaultdict(float)
        for tx in root.findall(".//account-transaction"):
            date_str = tx.attrib.get("date")
            if cutoff_date and date_str:
                try:
                    tx_date = datetime.strptime(date_str, "%Y-%m-%d")
                    if tx_date > cutoff_date:
                        continue
                except ValueError:
                    pass

            security_elem = tx.find("security")
            shares_elem = tx.find("shares")
            if security_elem is not None and shares_elem is not None:
                sec_id = security_elem.attrib.get("reference")
                try:
                    shares = float(shares_elem.text.strip())
                    holdings[sec_id] += shares
                except (ValueError, AttributeError):
                    continue

        for sec_id, total_shares in holdings.items():
            if abs(total_shares) > 1e-6:
                meta = securities.get(sec_id, {})
                records.append({
                    "securityId": sec_id,
                    "name": meta.get("name"),
                    "ticker": meta.get("ticker"),
                    "isin": meta.get("isin"),
                    "quantity": total_shares
                })

    return pd.DataFrame(records)

# ✅ Main function
def main(xml_file: str, by_account: bool = True, cutoff_date=None):
    pd.set_option("display.max_rows", None)
    pd.set_option("display.max_columns", None)
    pd.set_option("display.width", 0)
    pd.set_option("display.max_colwidth", None)

    df = extract_holdings_from_transactions(xml_file, by_account=by_account, cutoff_date=cutoff_date)

    if not df.empty:
        if cutoff_date:
            print(f"✅ Reconstructed Holdings as of {cutoff_date}:")
        else:
            print("✅ Reconstructed Holdings (latest):")
        print(df)
    else:
        print("❌ No holdings reconstructed from transactions.")

# 🎯 Run it for dev/test
if __name__ == "__main__":
    from datetime import timedelta
    two_months_ago = datetime.today() - timedelta(days=60)

    main(
        xml_file="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        by_account=True,
        cutoff_date=two_months_ago  # or e.g. "2024-03-01"
    )
