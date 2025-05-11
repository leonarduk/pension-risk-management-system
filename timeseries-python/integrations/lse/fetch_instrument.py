import json

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from integrations.portfolioperformance.api.instrument_builder import InstrumentBuilder
from integrations.portfolioperformance.api.instrument_details import upsert_instrument_from_json


def extract_lse_data_with_browser(url):
    options = Options()
    options.add_argument('--headless')
    options.add_argument('--disable-gpu')
    options.add_argument('--window-size=1920,1080')

    driver = webdriver.Chrome(options=options)
    driver.get(url)
    wait = WebDriverWait(driver, 15)

    try:
        # Extract name with fallback
        try:
            name_elem = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "h1.instrument-header_title__Vv8WY")))
        except:
            name_elem = wait.until(EC.presence_of_element_located((By.TAG_NAME, "h1")))
        name = name_elem.text.strip()

        # Extract type (e.g. ETF, Equity)
        instrument_type = ""
        try:
            type_elem = wait.until(EC.presence_of_element_located(
                (By.CSS_SELECTOR, "#ticker > div > section > div.ticker-main > div:nth-child(1) > span")
            ))
            instrument_type = type_elem.text.strip()
        except Exception as e:
            print("Type error:", e)

        # Extract ISIN
        isin = ""
        try:
            isin_elem = wait.until(EC.presence_of_element_located(
                (By.XPATH, "//*[contains(text(), 'ISIN')]/following-sibling::div")
            ))
            isin = isin_elem.text.strip()
        except Exception as e:
            print("ISIN error:", e)

        expense_ratio = ""
        if instrument_type == "ETF":
            # Extract expense ratio
            try:
                ratio_elem = wait.until(EC.presence_of_element_located(
                    (By.XPATH, "//*[contains(text(), 'Total Expense Ratio')]/following-sibling::div")
                ))
                expense_ratio = ratio_elem.text.strip()
            except Exception as e:
                print("Expense ratio error:", e)

        eps = ""
        if instrument_type == "Equity":
            # Extract EPS (only for equities)
            try:
                eps_elem = wait.until(EC.presence_of_element_located(
                    (By.XPATH, "//*[contains(text(), 'Earnings Per Share')]/following-sibling::div")
                ))
                eps = eps_elem.text.strip()
            except Exception as e:
                print("EPS error:", e)

        # Extract currency
        currency = ""
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
            "eps": eps,
            "currency": currency,
            "type": instrument_type
        }

    finally:
        driver.quit()

def create_instrument(url: str, xml_file=None, output_file=None):
    instrument_dict = extract_lse_data_with_browser(url)
    instrument = (
        InstrumentBuilder()
        .with_name(instrument_dict["name"])
        .with_ticker(instrument_dict["ticker"])
        .with_currency(instrument_dict["currency"])
        .with_isin(instrument_dict["isin"])
        .with_type(instrument_dict["type"])
        # .with_expense_ratio(instrument_dict["expense_ratio"])
        # .with_eps(instrument_dict["eps"])
        .with_updated_at()
        .build()
    )

    print("✅ Built instrument from Yahoo:")
    print(json.dumps(instrument, indent=2))

    upsert_instrument_from_json(
        xml_file=xml_file,
        json_data=instrument,
        output_file=output_file
    )
    print(f"✅ Instrument written to: {output_file}")


if __name__ == "__main__":
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    output_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"

    output_file = xml_file

    url = "https://www.londonstockexchange.com/stock/TFIF/twentyfour-income-fund-limited/company-page"

    create_instrument(url=url, xml_file=xml_file, output_file=output_file)
