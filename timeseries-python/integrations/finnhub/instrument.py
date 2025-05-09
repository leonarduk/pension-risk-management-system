import finnhub

# Setup client
finnhub_client = finnhub.Client(api_key="btjtt2v48v6vivbnrcf0")

# Search for instrument by name
res = finnhub_client.symbol_lookup('Aviva')
print(res)

# # Get quote
# quote = finnhub_client.quote('EXPN.L')
# print(quote)

# # Get company profile (includes ISIN)
# profile = finnhub_client.company_profile2(symbol='EXPN.L')
# print(profile.get('isin'))
