import finnhub

from .config import load_api_key

# Setup client
finnhub_client = finnhub.Client(api_key=load_api_key())

# Search for instrument by name
res = finnhub_client.symbol_lookup("Aviva")
print(res)

# # Get quote
# quote = finnhub_client.quote('EXPN.L')
# print(quote)

# # Get company profile (includes ISIN)
# profile = finnhub_client.company_profile2(symbol='EXPN.L')
# print(profile.get('isin'))
