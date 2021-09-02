# fit a line to the economic data
from numpy import sin
from numpy import sqrt
from numpy import arange
from pandas import read_csv
from scipy.optimize import curve_fit
from matplotlib import pyplot


# define the true objective function
def straightlinefit(x, a, b):
    return a * x + b

# define the true objective function
def sinecurve(x, a, b, c, d):
    return a * sin(b - x) + c * x ** 2 + d

# define the true objective function
def curvedlinefit(x, a, b, c):
	return a * x + b * x**2 + c

# load the dataset
def loadData():
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/longley.csv'
    dataframe = read_csv(url, header=None)
    return dataframe.values

def plotdata(fittingmethod):
    # choose the input and output variables
    data = loadData()
    x, y = data[:, 4], data[:, -1]
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

# plotdata(sinecurve)
# plotdata(straightlinefit)
plotdata(curvedlinefit)