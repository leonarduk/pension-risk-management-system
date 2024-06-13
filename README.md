# pension-risk-management-system

[![Maven Build](https://github.com/leonarduk/pension-risk-management-system/actions/workflows/maven.yml/badge.svg)](https://github.com/leonarduk/pension-risk-management-system/actions/workflows/maven.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/54fd223faa12484f8c3255f50085456b)](https://app.codacy.com/gh/leonarduk/pension-risk-management-system/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) 
[![codecov](https://codecov.io/gh/leonarduk/pension-risk-management-system/branch/master/graph/badge.svg)](https://codecov.io/gh/leonarduk/pension-risk-management-system)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.leonarduk/pension-risk-management-system/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.leonarduk/pension-risk-management-system)
[![Known Vulnerabilities](https://snyk.io/test/github/leonarduk/pension-risk-management-system/badge.svg)](https://snyk.io/test/github/leonarduk/pension-risk-management-system)

A tool that reads timeseries from Yahoo and [Alphavantage](https://www.alphavantage.co/documentation) and does analysis.

# timeseries-sources
This module has tools to acquire the timeseries information, interpolate where there are missing data points, and saves to CSV or now, InfluxDb.  By saving to CSV to cache the data you can use the text editor of your choice to clean up the data.

Uses:

* [patriques82/alphavantage4j](https://github.com/patriques82/alphavantage4j) - I had to include this in my code base, as the provided maven repo was unstable.  This also made it easier to make a small bug fix.
* [sstrickx/yahoofinance-api](https://github.com/sstrickx/yahoofinance-api)  - copied to my code as this seems dead now, and I wanted to make a small change to fit the code into my code
* [ta4j/ta4j](https://github.com/ta4j/ta4j)
* [leonarduk/webscraper-core](https://github.com/leonarduk/webscraper-core)

# timeseries-spring-boot-server

Simple Spring boot app to allow you to query the data.  Currently I use this to provide a web service for other applications such as [buchen/portfolio](https://github.com/buchen/portfolio) as that app suffers from data quality issues when using Yahoo or Alphavantage

If you select interpolate=true, it will flat-line dates before and after the available date, and will linearly interpolate for points missing within the series.  clean=true will try to detect and fix errors were the price flips between GBX and GBP.

e.g.
http://localhost:8091/stock/ticker/MNP?years=2&interpolate=true&clean=true

