from analysis.instrument.analyse_instrument import analyze_all_tickers
from integrations.portfolioperformance.api.positions import extract_holdings_from_transactions

if __name__ == '__main__':
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"

    df = extract_holdings_from_transactions(xml_file, by_account=False, cutoff_date=None)

    # unique, drop any NaNs, and convert to a plain Python list
    my_tickers = df['ticker'].dropna().unique().tolist()

    analyze_all_tickers(
        xml_path="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        recent_days=5,
        group_signals=True,
        output_dir="instrument/output",
        tickers=my_tickers,
        use_stockfeed=False
    )



