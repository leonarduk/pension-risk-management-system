# User Guide

This guide explains how to install and run the Pension Risk Management System, including its REST APIs, web front end, and Android application.

## Features

### Spring Boot REST API

The `timeseries-spring-boot-server` module exposes endpoints for price data and risk metrics:

| Method & Path | Description |
|---------------|-------------|
| `GET /stock/price/{ticker}` | Latest closing price for a ticker. |
| `GET /stock/ticker/{ticker}` | HTML table of historical prices with optional query parameters such as `years`, `interpolate`, `cleanDate`, and `category`. |
| `POST /stock/ticker` | JSON map of historical prices for one or more tickers. |
| `POST /series/map?source={src}&target={tgt}` | Align one price series to another using index rebasing. |
| `POST /risk/historic-var` | Calculate historical Value at Risk (VaR) from a list of returns. |
| `POST /risk/maxdrawdown` | Return maximum drawdown for a list of prices. |

Example requests:

```bash
curl http://localhost:8091/stock/price/MNP
```
Response:
```json
{"close": 123.45}
```

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"returns": [-0.02, 0.01, 0.03]}' \
  "http://localhost:8091/risk/historic-var?confidenceLevel=0.95"
```
Response:
```json
{"var": -0.02}
```

### React Web UI

The `ui/` module provides a React + Vite front‑end for exploring prices and analytics by calling the REST endpoints above.

### Android Application

The Android client fetches available tickers and their latest prices from the Spring Boot service and displays them in a list. The project is built with Gradle and can be imported into Android Studio.

## Installation

1. **Prerequisites**
   - Java 17+
   - Maven 3.6+
   - Node.js 18+ and npm
   - (Optional) Android Studio for the mobile app

2. **Clone the repository**
   ```bash
   git clone https://github.com/leonarduk/pension-risk-management-system.git
   cd pension-risk-management-system
   ```

3. **Build Java modules**
   ```bash
   mvn -q verify
   ```

4. **Install UI dependencies**
   ```bash
   cd ui
   npm install
   cd ..
   ```

## Running the Spring Boot Server

From the project root:

```bash
mvn -pl timeseries-spring-boot-server spring-boot:run
```

The server listens on port `8091` by default.

### CORS Configuration

Cross‑origin requests are controlled with the `cors.allowed-origins` property. Add a comma‑separated list of trusted origins in `timeseries-spring-boot-server/src/main/resources/application.properties` or set it via the `CORS_ALLOWED_ORIGINS` environment variable:

```bash
export CORS_ALLOWED_ORIGINS="http://localhost:5173"
```

## Launching the Front‑end

From the `ui/` directory:

```bash
npm run dev
```

The development server runs on Vite's default port (`5173`). Ensure the Spring Boot server is running and that `cors.allowed-origins` includes the UI origin.

## Building the Android App

In the `android-app/` directory:

```bash
./gradlew assembleDebug
```

Install the generated APK on a device or emulator. The app queries the Spring Boot server for tickers and prices, so the server must be running and reachable.

---

For more details, refer to module-specific READMEs and source code.

