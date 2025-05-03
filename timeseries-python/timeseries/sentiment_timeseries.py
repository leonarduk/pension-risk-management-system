import os
import time
import csv
from datetime import datetime
from pathlib import Path

import pandas as pd
from dotenv import load_dotenv
from transformers import pipeline
from praw import Reddit
import requests
from bs4 import BeautifulSoup

# ---------- CONFIGURATION ---------- #

load_dotenv()

REDDIT_CLIENT_ID = os.getenv('REDDIT_CLIENT_ID')
REDDIT_CLIENT_SECRET = os.getenv('REDDIT_CLIENT_SECRET')
REDDIT_USER_AGENT = os.getenv('REDDIT_USER_AGENT')

MODEL_NAME = "cardiffnlp/twitter-roberta-base-sentiment"
OUTPUT_CSV = "daily_sentiment.csv"
EXTRACTS_CSV = "sentiment_extracts.csv"

# ---------- LOAD PIPELINE ---------- #

sentiment_analyzer = pipeline("sentiment-analysis", model=MODEL_NAME)
print("Device set to use", sentiment_analyzer.device.type)

# ---------- SENTIMENT LOGIC ---------- #

def label_to_score(label, score):
    mapping = {
        "LABEL_0": -1.0,
        "LABEL_1": 0.0,
        "LABEL_2": 1.0
    }
    return mapping[label] * score

def score_to_label(score):
    if score >= 0.2:
        return "Very Positive"
    elif score >= 0.05:
        return "Positive"
    elif score >= 0.01:
        return "Slightly Positive"
    elif score <= -0.01 and score > -0.05:
        return "Slightly Negative"
    elif score <= -0.05 and score > -0.2:
        return "Negative"
    elif score <= -0.2:
        return "Very Negative"
    else:
        return "Neutral"

def analyze_sentiment(texts, source, ticker, today, extract_writer):
    if not texts:
        return 0.0
    total_score = 0.0
    batch_size = 16

    filtered = [t for t in texts if ticker.lower() in t.lower()]
    clean_texts = [text[:1024] for text in filtered]

    for i in range(0, len(clean_texts), batch_size):
        batch = clean_texts[i:i + batch_size]
        try:
            results = sentiment_analyzer(batch, truncation=True)
            for text, result in zip(batch, results):
                score = label_to_score(result["label"], result["score"])
                total_score += score
                if abs(score) > 0.0:
                    label = score_to_label(score)
                    print(f"  TEXT ({label}, {score:.4f}): {text[:160]}...")
                    extract_writer.writerow([today, ticker, round(score, 4), label, text])
        except Exception as e:
            print(f"Batch failed: {e}")
    return total_score / len(clean_texts) if clean_texts else 0.0

# ---------- YAHOO HEADLINES ---------- #

def fetch_yahoo_headlines(ticker):
    url = f"https://finance.yahoo.com/quote/{ticker}?p={ticker}"
    try:
        page = requests.get(url, headers={"User-Agent": "Mozilla/5.0"})
        soup = BeautifulSoup(page.content, 'html.parser')
        headlines = [item.get_text(strip=True) for item in soup.find_all('h3') if item.get_text(strip=True)]
        return headlines
    except Exception as e:
        print(f"Failed to fetch Yahoo headlines for {ticker}: {e}")
        return []

# ---------- REDDIT POSTS ---------- #

def fetch_reddit_posts(reddit, ticker, limit=20):
    try:
        posts = reddit.subreddit('stocks+investing+wallstreetbets').search(ticker, limit=limit, sort='new')
        return [post.title + " " + (post.selftext or "") for post in posts]
    except Exception as e:
        print(f"Failed to fetch Reddit posts for {ticker}: {e}")
        return []

# ---------- MAIN ---------- #

def main(tickers):
    today = datetime.today().date()
    reddit = Reddit(client_id=REDDIT_CLIENT_ID,
                    client_secret=REDDIT_CLIENT_SECRET,
                    user_agent=REDDIT_USER_AGENT)

    output_path = Path(OUTPUT_CSV)
    is_new_file = not output_path.exists() or output_path.stat().st_size == 0

    extract_path = Path(EXTRACTS_CSV)
    extract_new = not extract_path.exists() or extract_path.stat().st_size == 0

    with open(output_path, mode='a', newline='', encoding='utf-8') as outfile,          open(extract_path, mode='a', newline='', encoding='utf-8') as extractfile:

        writer = csv.writer(outfile)
        extract_writer = csv.writer(extractfile)

        if is_new_file:
            writer.writerow(['date', 'ticker', 'sentiment_score', 'sentiment_label'])

        if extract_new:
            extract_writer.writerow(['date', 'ticker', 'sentiment_score', 'sentiment_label', 'text'])

        for ticker in tickers:
            print(f"\n--- Analyzing {ticker} ---")

            yahoo_headlines = fetch_yahoo_headlines(ticker)
            yahoo_score = analyze_sentiment(yahoo_headlines, 'yahoo', ticker, today, extract_writer)

            reddit_posts = fetch_reddit_posts(reddit, ticker)
            reddit_score = analyze_sentiment(reddit_posts, 'reddit', ticker, today, extract_writer)

            combined_score = (yahoo_score + reddit_score) / 2 if reddit_posts else yahoo_score
            label = score_to_label(combined_score)

            writer.writerow([today, ticker, round(combined_score, 4), label])
            print(f"{today},{ticker},{combined_score:.4f},{label}")
            time.sleep(1)

# ---------- OPTIONAL CSV TO TICKER MAP ---------- #

def get_name_map_from_csv(positions_csv, name_field="Name", ticker_field="Symbol"):
    positions = pd.read_csv(positions_csv)
    return dict(zip(positions[name_field], positions[ticker_field]))


# ---------- ENTRY POINT ---------- #

if __name__ == "__main__":
    name_map = get_name_map_from_csv("steve_positions.csv")
    tickers = set(name_map.values())  # de-duplicate
    main(tickers)
