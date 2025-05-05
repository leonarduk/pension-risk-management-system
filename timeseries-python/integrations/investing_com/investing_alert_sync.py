import os
import asyncio
from dotenv import load_dotenv
from playwright.async_api import async_playwright

load_dotenv()

EMAIL = os.getenv("INVESTING_EMAIL")
PASSWORD = os.getenv("INVESTING_PASSWORD")

ALERT_CENTER_URL = "https://www.investing.com/members-admin/alert-center"
LOGIN_URL = "https://www.investing.com/members-admin/login"

async def scrape_alerts():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context()
        page = await context.new_page()

        # Go to login page
        await page.goto(LOGIN_URL)

        # Accept GDPR popup if it shows up
        try:
            await page.wait_for_selector("button:has-text('I Accept')", timeout=5000)
            await page.click("button:has-text('I Accept')")
            print("✅ Accepted privacy consent.")
        except:
            print("ℹ️ No consent popup detected.")

        # Dismiss UK Edition popup if present
        try:
            await page.click("text=No thanks", timeout=3000)
            print("✅ Dismissed UK Edition popup.")
        except:
            print("ℹ️ No UK Edition popup detected.")

        # Print current URL for debugging
        print("🔍 Current URL:", page.url)

        # Fill and submit login form using more reliable selectors
        try:
            login_form = page.locator("form#loginForm")
            await login_form.wait_for(timeout=10000)
            await login_form.locator("input[name='user']").fill(EMAIL)
            await login_form.locator("input[name='password']").fill(PASSWORD)
            await login_form.locator("input[type='submit']").click()
            print("🔐 Submitted login form.")
        except Exception as e:
            print("❌ Login process failed:", e)
            await page.screenshot(path="login_failed_fields.png", full_page=True)
            return

        # Screenshot after login attempt
        await page.screenshot(path="login_debug.png", full_page=True)

        # Wait for successful redirect
        try:
            await page.wait_for_url("**/members-admin/**", timeout=15000)
            print("✅ Logged in successfully.")
        except:
            print("❌ Login may have failed or took too long.")
            return

        # Navigate to alert center
        await page.goto(ALERT_CENTER_URL)
        await page.wait_for_selector("#alertCenterTbl")

        # Extract alert info (simple version: get table rows)
        rows = await page.query_selector_all("#alertCenterTbl tbody tr")
        print(f"\n📊 Found {len(rows)} alerts:\n")

        for i, row in enumerate(rows):
            cols = await row.query_selector_all("td")
            texts = [await col.inner_text() for col in cols]
            print(f"{i+1}.", " | ".join(texts))

        await browser.close()

if __name__ == "__main__":
    asyncio.run(scrape_alerts())

## this not working yet.  Can't get the login to work.