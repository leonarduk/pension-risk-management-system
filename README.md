# pension-risk-management-system

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/31749b533d2e4621a9c2c878f21f2ae4)](https://www.codacy.com/app/github_65/pension-risk-management-system?utm_source=github.com&utm_medium=referral&utm_content=leonarduk/pension-risk-management-system&utm_campaign=badger) [![codecov](https://codecov.io/gh/leonarduk/pension-risk-management-system/branch/master/graph/badge.svg)](https://codecov.io/gh/leonarduk/pension-risk-management-system) [![Build Status](https://travis-ci.org/leonarduk/pension-risk-management-system.svg?branch=master)](https://travis-ci.org/leonarduk/pension-risk-management-system)



A tool that reads timeseries from Yahoo and [Alphavantage](https://www.alphavantage.co/documentation) and does analysis.

# timeseries-source-and-analysis
This module has tools to acquire the timeseries information, interpolate where there are missing data points, and saves to CSV or now, InfluxDb.  By saving to CSV to cache the data you can use the text editor of your choice to clean up the data.

Uses:

* [patriques82/alphavantage4j](https://github.com/patriques82/alphavantage4j)
* [sstrickx/yahoofinance-api](https://github.com/sstrickx/yahoofinance-api)
* [ta4j/ta4j](https://github.com/ta4j/ta4j)

# timeseries-spring-boot-server

Simple Spring boot app to allow you to query the data.  Currently I use this to provide a web service for other applications such as [buchen/portfolio](https://github.com/buchen/portfolio) as that app suffers from data quality issues when using Yahoo or Alphavantage

If you select interpolate=true, it will flat-line dates before and after the available date, and will linearly interpolate for points missing within the series.  clean=true will try to detect and fix errors were the price flips between GBX and GBP.

e.g. 
http://localhost:8091/stock/ticker/MNP?years=2&interpolate=true&clean=true

You can use the docker image https://hub.docker.com/r/leonarduk/finance-html-docker
