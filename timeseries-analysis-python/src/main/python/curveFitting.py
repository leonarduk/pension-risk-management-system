# fit a line to the economic data
from numpy import sin
from numpy import sqrt
from numpy import arange
from pandas import read_csv
import pandas as pd

from scipy.optimize import curve_fit
from matplotlib import pyplot

def straightlinefit(x, a, b):
    return a * x + b

def sinecurve(x, a, b, c, d):
    return a * sin(b - x) + c * x ** 2 + d

def curvedlinefit(x, a, b, c):
	return a * x + b * x**2 + c

# load the dataset
def loadData():
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/longley.csv'
    # 1     2           3       4       5           6       7
    # 83,   234.289,    235.6,  159,    107.608,    1947,   60.323
    dataframe = read_csv(url, header=None)
    return dataframe.values

def getDataFrame(ticker, years=10 ):
    CSV_URL = 'http://localhost:8091/stock/download/ticker/' + ticker + '?years=' + str(years) + '&interpolate=true&clean=true'

    # df = pd.read_csv(CSV_URL, usecols=['date', 'close'], index_col='date')
    df = read_csv(CSV_URL, header=0 )

    # df['date'].str.replace("-", "").astype(int)
    df["date"] = pd.to_datetime(df["date"]).dt.strftime("%Y%m%d").astype(int)
    # df['date'].dt.strftime("%Y-%m-%d").astype(int)
    df.insert(0, 'ID', range(1, 1 + len(df)))
    print(CSV_URL + " " + str(df.size))

    return df

def plotdata(fittingmethod):
    # choose the input and output variables
    # data = loadData()

    #   1           2       3       4       5       6       7
    # date,         open,   high, low,      close, volume, comment
    # 2020-09-02,   15.15, 15.15, 15.14,    15.15, 85.00, Alphavantage
    data = getDataFrame("HMBR.L",1 )
    y = data["close"]
    x = data["ID"]
    # curve fit
    popt, _ = curve_fit(fittingmethod, x, y)
    # summarize the parameter values
    if len(popt) == 4:
        a, b, c, d = popt
    if len(popt) == 3:
        a, b, c = popt
    if len(popt) == 2:
        a, b = popt
    print('y = %.5f * x + %.5f' % (a, b))
    # plot input vs output
    pyplot.scatter(x, y)
    # define a sequence of inputs between the smallest and largest known inputs
    x_line = arange(min(x), max(x), 1)
    # calculate the output for the range
    y_line = fittingmethod(x_line, *popt)
    # create a line plot for the mapping function
    pyplot.plot(x_line, y_line, '--', color='red')
    pyplot.show()

plotdata(sinecurve)
# plotdata(straightlinefit)
# plotdata(curvedlinefit)