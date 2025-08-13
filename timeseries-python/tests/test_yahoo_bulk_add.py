import xml.etree.ElementTree as ET

from integrations.yahoo import instrument


class DummyTicker:
    def __init__(self, ticker):
        self.info = {
            "longName": f"{ticker} Corp",
            "currency": "GBp",
            "symbol": ticker,
        }


def test_bulk_add_from_yahoo_dedupes(tmp_path, monkeypatch):
    xml_in = tmp_path / "portfolio.xml"
    xml_out = tmp_path / "out.xml"
    xml_in.write_text("<?xml version='1.0'?><portfolio><securities/></portfolio>")

    monkeypatch.setattr(instrument.yf, "Ticker", DummyTicker)

    tickers = {"vod", "VOD", "VOD.L"}
    instrument.bulk_add_from_yahoo(str(xml_in), tickers, str(xml_out))

    tree = ET.parse(xml_in)
    securities = tree.getroot().findall(".//securities/security")
    assert len(securities) == 1
    sec = securities[0]
    assert sec.findtext("tickerSymbol") == "VOD.L"
    assert sec.findtext("currencyCode") == "GBX"


def test_bulk_add_from_yahoo_adds_multiple(tmp_path, monkeypatch):
    xml_in = tmp_path / "portfolio.xml"
    xml_out = tmp_path / "out.xml"
    xml_in.write_text("<?xml version='1.0'?><portfolio><securities/></portfolio>")

    monkeypatch.setattr(instrument.yf, "Ticker", DummyTicker)

    tickers = {"ABC", "DEF"}
    instrument.bulk_add_from_yahoo(str(xml_in), tickers, str(xml_out))

    tree = ET.parse(xml_in)
    securities = tree.getroot().findall(".//securities/security")
    assert {s.findtext("tickerSymbol") for s in securities} == {"ABC.L", "DEF.L"}
    assert [s.attrib["id"] for s in securities] == ["1", "2"]
