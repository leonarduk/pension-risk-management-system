import json
import os
import xml.etree.ElementTree as ET
import pandas as pd
from datetime import datetime
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

UUID_ALIASES = {
    "07ca79a6-4b1e-4906-8ee6-215d3299ac67": "Morningstar Category",
    "1ffc0583-55ce-4883-984c-15351794e905": "Morningstar Rating",
    "approval": "Needs Approval",
    "ter": "Total Expense Ratio (TER)",
    "5b3513c3-0b30-49f7-a645-8a391de3f6e1": "Region (MSCI)",
    "c8d30b61-8a29-4aa4-bbb3-e3b7fcf94fa1": "Developed Markets",
    "d6476d71-d6e5-4a93-aaa2-5b410ae354b6": "Europe",
    "a1770f46-42b3-47ff-b58d-13db3a1b48b2": "United Kingdom",
    "7783b59f-4f65-41a0-91c9-7c6376e5cfa3": "Asset Class",
    "23df78a0-05f7-4ce2-936f-b648b6f626aa": "Equity",
    "f101db2f-8813-4f07-b82c-fb2e98a3cb55": "Bond",
    "income_4_6": "Income Bearing (4–6 Medium)",
    "income_7_10": "Income Bearing (7–10 High)",
    "c4d9e8e6-9f52-412b-ae65-f64904fd45ee": "Sustainability Category",
    "ByCountry": "By Country Category",
    "UK Equity Income": "UK Equity Income Category"
}

def extract_lse_data_with_browser(url):
    options = Options()
    options.add_argument('--headless')
    options.add_argument('--disable-gpu')
    options.add_argument('--window-size=1920,1080')

    driver = webdriver.Chrome(options=options)
    driver.get(url)
    wait = WebDriverWait(driver, 15)

    try:
        name_elem = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "h1")))
        name = name_elem.text.strip()

        isin = ""
        try:
            isin_elem = wait.until(EC.presence_of_element_located(
                (By.XPATH, "//*[contains(text(), 'ISIN')]/following-sibling::div")
            ))
            isin = isin_elem.text.strip()
        except Exception as e:
            print("ISIN error:", e)

        expense_ratio = ""
        try:
            ratio_elem = wait.until(EC.presence_of_element_located(
                (By.XPATH, "//*[contains(text(), 'Total Expense Ratio')]/following-sibling::div")
            ))
            expense_ratio = ratio_elem.text.strip()
        except Exception as e:
            print("Expense ratio error:", e)

        currency = "GBX"
        try:
            currency_elem = wait.until(EC.presence_of_element_located(
                (By.CSS_SELECTOR, "#ticker strong")
            ))
            currency_text = currency_elem.text.strip()
            if currency_text.startswith("(") and currency_text.endswith(")"):
                currency = currency_text[1:-1].upper()
        except Exception as e:
            print("Currency error:", e)

        ticker = url.split("/")[4].upper() + ".L"

        return {
            "name": name,
            "ticker": ticker,
            "isin": isin,
            "expense_ratio": expense_ratio,
            "currency": currency
        }

    finally:
        driver.quit()

if __name__ == "__main__":
    url = "https://www.londonstockexchange.com/stock/PHGP/wisdomtree/company-page"
    instrument = extract_lse_data_with_browser(url)
    print(instrument)
