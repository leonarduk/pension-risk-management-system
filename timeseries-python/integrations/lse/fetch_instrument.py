from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

def extract_lse_data_with_browser(url):
    options = Options()
    options.add_argument('--headless')
    options.add_argument('--disable-gpu')
    options.add_argument('--window-size=1920,1080')

    driver = webdriver.Chrome(options=options)
    driver.get(url)

    try:
        # Wait for the instrument name to appear in any <h1>
        name_elem = WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "h1"))
        )
        name = name_elem.text.strip()

        isin = ""
        rows = driver.find_elements(By.CSS_SELECTOR, '.table__row')
        for row in rows:
            cells = row.find_elements(By.CLASS_NAME, 'table__cell')
            if len(cells) >= 2 and 'ISIN' in cells[0].text:
                isin = cells[1].text.strip()
                break

        ticker = url.split("/")[4].upper() + ".L"

        return {
            "name": name,
            "ticker": ticker,
            "isin": isin,
            "currency": "GBX"
        }

    finally:
        driver.quit()

# 🔧 Example usage:
if __name__ == "__main__":
    url = "https://www.londonstockexchange.com/stock/PHGP/wisdomtree/company-page"
    instrument = extract_lse_data_with_browser(url)
    print(instrument)
