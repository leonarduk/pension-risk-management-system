import os
from configparser import ConfigParser
from pathlib import Path


def load_api_key() -> str:
    """Retrieve Finnhub API key from environment or local config file."""
    api_key = os.getenv("FINNHUB_API_KEY")
    if api_key:
        return api_key

    config_path = Path(__file__).with_name("finnhub.cfg")
    if config_path.exists():
        config = ConfigParser()
        config.read(config_path)
        api_key = config.get("finnhub", "api_key", fallback=None)
        if api_key:
            return api_key

    raise EnvironmentError(
        "Finnhub API key not found. Set FINNHUB_API_KEY or create finnhub.cfg"
    )
