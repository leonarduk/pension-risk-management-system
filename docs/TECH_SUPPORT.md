# Technical Support Guide

## Prerequisites

- Java 11 or later
- Maven 3.6 or later
- Python 3 with [pre-commit](https://pre-commit.com/) installed
- Node.js and npm for building the UI module
- Docker for running the Lambda module

## Environment Variables

- `ALPHAVANTAGE_API_KEYS` – comma-separated API keys for AlphaVantage data access
- `CORS_ALLOWED_ORIGINS` – optional list of origins allowed by the Spring Boot server
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`, `AWS_REGION`, `SQS_QUEUE_URL` – required by the `timeseries-lambda` module

## Build and Test

```bash
mvn verify
pre-commit run --all-files
```

Run the first command from the project root to compile all modules and execute tests. The second command runs formatting and linting hooks for the Python code.

## Diagnostics and Troubleshooting

- **Maven errors** – run `mvn -e verify` for full stack traces or `mvn clean` to remove stale artifacts
- **Pre-commit failures** – reinstall hooks with `pre-commit clean` and rerun
- **Spring Boot server issues** – start with `mvn -pl timeseries-spring-boot-server spring-boot:run` and review the console logs (enable debug output with `--debug`)
- **Lambda module errors** – ensure all AWS credentials are set and check `docker logs <container>` for authentication or SQS connection problems
- **AlphaVantage feed problems** – verify `ALPHAVANTAGE_API_KEYS` is set and not rate-limited
