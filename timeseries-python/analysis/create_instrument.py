from integrations.lse.fetch_instrument import extract_lse_data_with_browser, create_instrument

if __name__ == '__main__':
    url = "https://www.londonstockexchange.com/stock/TFIF/twentyfour-income-fund-limited/company-page"

    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    output_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"


    instrument = extract_lse_data_with_browser(url)
    print(instrument)

    create_instrument(url=url, xml_file=xml_file, output_file=output_file)
