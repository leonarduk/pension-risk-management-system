# Finnhub Integration

Scripts in this directory interact with the [Finnhub](https://finnhub.io) API.
An API key is required and can be supplied in one of two ways:

1. **Environment variable**
   ```bash
   export FINNHUB_API_KEY=your_key_here
   ```
2. **Configuration file**
   Create a file named `finnhub.cfg` alongside these scripts containing:
   ```ini
   [finnhub]
   api_key=your_key_here
   ```

`instrument.py` and `instrument_create.py` will automatically load the key
using this logic. After configuration you can run the example search:

```bash
python instrument.py
```
