# http://localhost:8091/stock/download/ticker/REL?years=20&interpolate=false&clean=true
# date,open,high,low,close,volume
# 2005-01-04,481.75,494.50,481.75,494.25,11529668.00,Alphavantage
# 2005-01-05,490.00,493.75,490.00,492.25,9736498.00,Alphavantage

import csv
import requests

CSV_URL = 'http://localhost:8091/stock/download/ticker/REL?years=1&interpolate=false&clean=true'

with requests.Session() as s:
    download = s.get(CSV_URL)

    decoded_content = download.content.decode('utf-8')

    cr = csv.reader(decoded_content.splitlines(), delimiter=',')
    my_list = list(cr)
    for row in my_list:
        print(row)

import pandas as pd

df = pd.read_csv(CSV_URL)

print(df.head())