# fit a line to the economic data
from numpy import sin
from numpy import arange
from pandas import read_csv
from numpy import exp, pi, sqrt
from datacollection.TimeSeriesAPI import getTimeSeriesWithIncremementingKey as getDataFrame

from scipy.optimize import curve_fit
from matplotlib import pyplot

def straightlinefit(x, a, b):
    return a * x + b

def sinecurve(x, a, b, c, d):
    return a * sin(b - x) + c * x ** 2 + d

def curvedlinefit(x, a, b, c):
	return a * x + b * x**2 + c

def gaussian(x, amp, cen, wid):
    """1-d gaussian: gaussian(x, amp, cen, wid)"""
    return (amp / (sqrt(2*pi) * wid)) * exp(-(x-cen)**2 / (2*wid**2))

# load the dataset
def loadData():
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/longley.csv'
    # 1     2           3       4       5           6       7
    # 83,   234.289,    235.6,  159,    107.608,    1947,   60.323
    dataframe = read_csv(url, header=None)
    return dataframe.values


def plotdata(fittingmethod, symbol, years):
    #   1           2       3       4       5       6       7
    # date,         open,   high, low,      close, volume, comment
    # 2020-09-02,   15.15, 15.15, 15.14,    15.15, 85.00, Alphavantage
    data = getDataFrame(symbol, years )
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

symbol = "XGLS.L"
years = 1
# curvedlinefit, sinecurve, straightlinefit, gaussian
method = straightlinefit

plotdata(method, symbol, years)
