from bs4 import BeautifulSoup
from requests_html import HTMLSession


class InvestingInstrumentExtractor:
    def __init__(self, url):
        self.url = url
        self.soup = None

    def fetch(self):
        session = HTMLSession()
        headers = {
            "User-Agent": (
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/122.0.0.0 Safari/537.36"
            ),
            "Accept-Language": "en-US,en;q=0.9",
            "Referer": "https://www.google.com"
        }
        response = session.get(self.url, headers=headers)

        try:
            # Use existing browser binary path here
            response.html.render(
                timeout=20,
                sleep=1,
                keep_page=True,
                scrolldown=0,
                reload=False,
                executablePath="C:/Program Files/Google/Chrome/Application/chrome.exe"  # or Edge path
            )
        except Exception as e:
            raise Exception(f"❌ JavaScript rendering failed: {e}")

        if response.status_code != 200:
            raise Exception(f"❌ Failed to fetch {self.url} — Status {response.status_code}")

        self.soup = BeautifulSoup(response.html.html, "lxml")

    def extract_metadata(self):
        if self.soup is None:
            self.fetch()

        name = self.soup.find("h1").text.strip()

        # Details box — look for ISIN, Currency, Symbol
        details_box = self.soup.find("div", class_="instrumentHead__details")
        text = details_box.get_text(separator="|") if details_box else ""

        isin = currency = ticker = None
        for item in text.split("|"):
            if "ISIN" in item.upper():
                isin = item.split(":")[-1].strip()
            elif "Currency" in item:
                currency = item.split(":")[-1].strip()
            elif "Symbol" in item:
                ticker = item.split(":")[-1].strip()

        return {
            "name": name,
            "isin": isin,
            "currency": currency,
            "ticker": ticker or self.url.rstrip("/").split("/")[-1].upper(),
            "source_url": self.url
        }
