# 🛎️ Investing.com Alert Sync (Unofficial API Wrapper)

This Python script logs into your [Investing.com](https://www.investing.com) account and extracts your personal price alerts from the [Alert Center](https://www.investing.com/members-admin/alert-center), storing them in a local JSON file.

---

## ✅ Features
- Authenticates using your email/password
- Navigates to your alert center
- Extracts:
  - Instrument name / ticker symbol
  - Condition (e.g., "price below")
  - Trigger price
  - Whether the alert is active
- Saves all alerts to a local `investing_alerts.json` file

---

## 🚀 Installation

### 1. Install Python (3.8+ recommended)
Make sure `python` and `pip` are available in your terminal.

### 2. Set up a virtual environment (optional but recommended)
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

### 3. Install dependencies
```bash
pip install playwright
python -m playwright install  # This downloads Chromium, Firefox, WebKit
```

---

## 🔐 Set Environment Variables
Create a `.env` file **or** set variables in your terminal:

### Option A: In your terminal
```bash
export INVESTING_EMAIL="you@example.com"
export INVESTING_PASSWORD="yourpassword"
```
On Windows Command Prompt:
```cmd
set INVESTING_EMAIL=you@example.com
set INVESTING_PASSWORD=yourpassword
```

### Option B: In `.env` file (if you use `python-dotenv`)
```dotenv
INVESTING_EMAIL=you@example.com
INVESTING_PASSWORD=yourpassword
```

---

## 🧪 Run the Script
```bash
python investing_alert_sync.py
```

This will:
- Launch Chromium (non-headless)
- Log in to your account
- Scrape your alert list
- Save to `investing_alerts.json`

---

## 📦 Output Format
A sample `investing_alerts.json` file:
```json
[
  {
    "symbol": "IUKD.L",
    "condition": "below",
    "trigger_price": 30.5,
    "active": true
  },
  ...
]
```

---

## 🧱 Future Ideas
- Create/update alerts programmatically (2-way sync)
- Compare local strategy alerts to Investing.com ones
- Auto-notify when local rules are triggered

---

## ⚠️ Disclaimer
This tool uses browser automation (Playwright) and relies on the current structure of Investing.com. It may break if the site layout changes or rate-limiting is introduced.

Use responsibly and do not overload their servers.

