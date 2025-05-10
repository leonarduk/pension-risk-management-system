from analysis.instrument.analyse_instrument import analyze_all_tickers
from integrations.portfolioperformance.api.positions import extract_holdings_from_transactions
from integrations.portfolioperformance.api.static.ftse_all_share_dict import ftse_all_share

if __name__ == '__main__':
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"

    all_ftse_tickers = [t + ".L" for t in ftse_all_share.keys()]

    df = extract_holdings_from_transactions(xml_file, by_account=False, cutoff_date=None)

    # unique, drop any NaNs, and convert to a plain Python list
    my_tickers = df['ticker'].dropna().unique().tolist()

    # normalise both lists the same way first (strip, upper-case, etc.) if needed
    all_set = {t.strip().upper() for t in all_ftse_tickers if t}  # remove blanks/None
    mine_set = {t.strip().upper() for t in my_tickers if t}

    missing_tickers = list(all_set - mine_set)  # tickers you don’t yet own

    print(len(all_ftse_tickers), "FTSE tickers")
    print(len(missing_tickers), "tickers not yet in portfolio")
    print(sorted(missing_tickers))

    analyze_all_tickers(
        xml_path="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        recent_days=5,
        group_signals=True,
        output_dir="output",
        tickers=missing_tickers,
        use_stockfeed=True
    )



