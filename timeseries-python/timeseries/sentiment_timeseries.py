# sentiment_timeseries.py
# Minimal script: Show sentiment scores from Reddit + Yahoo Finance headlines for UK and US stocks

import os
import time

import requests
from bs4 import BeautifulSoup
from dotenv import load_dotenv
from praw import Reddit
from textblob import TextBlob

# ---------- CONFIGURATION ---------- #

load_dotenv()

REDDIT_CLIENT_ID = os.getenv('REDDIT_CLIENT_ID')
REDDIT_CLIENT_SECRET = os.getenv('REDDIT_CLIENT_SECRET')
REDDIT_USER_AGENT = os.getenv('REDDIT_USER_AGENT')

UK_TICKERS = ['HSBA.L', 'BP.L', 'BARC.L']
US_TICKERS = ['AAPL', 'TSLA', 'AMZN']

# ---------- SENTIMENT FUNCTION ---------- #

def get_sentiment(text):
    analysis = TextBlob(text)
    print(f"Text: {text}\n→ Sentiment Score: {analysis.sentiment.polarity:.4f}\n")
    return analysis.sentiment.polarity

# ---------- YAHOO HEADLINE SCRAPER ---------- #

def fetch_yahoo_headlines(ticker):
    url = f"https://finance.yahoo.com/quote/{ticker}?p={ticker}"
    try:
        page = requests.get(url, headers={"User-Agent": "Mozilla/5.0"})
        soup = BeautifulSoup(page.content, 'html.parser')
        return [item.get_text(strip=True) for item in soup.find_all('h3') if item.get_text(strip=True)]
    except Exception as e:
        print(f"Failed to fetch Yahoo headlines for {ticker}: {e}")
        return []

# ---------- REDDIT SCRAPER ---------- #

def fetch_reddit_posts(reddit, ticker, limit=20):
    try:
        posts = reddit.subreddit('stocks+investing+wallstreetbets').search(ticker, limit=limit, sort='new')
        return [post.title + " " + (post.selftext or "") for post in posts]
    except Exception as e:
        print(f"Failed to fetch Reddit posts for {ticker}: {e}")
        return []

# ---------- MAIN ---------- #

def main():
    reddit = Reddit(client_id=REDDIT_CLIENT_ID,
                    client_secret=REDDIT_CLIENT_SECRET,
                    user_agent=REDDIT_USER_AGENT)

    for ticker in UK_TICKERS + US_TICKERS:
        print(f"--- Analyzing {ticker} ---\n")

        headlines = fetch_yahoo_headlines(ticker)
        for h in headlines:
            get_sentiment(h)

        posts = fetch_reddit_posts(reddit, ticker)
        for p in posts:
            get_sentiment(p)

        time.sleep(2)

if __name__ == "__main__":
    main()
