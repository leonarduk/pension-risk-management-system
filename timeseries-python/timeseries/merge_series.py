import pandas as pd
import matplotlib.pyplot as plt

# Load data
etf = pd.read_csv("C:/Users/User/workspaces/github/pension-risk-management-system/db/L_GLDW.csv", index_col=0, parse_dates=True)
index = pd.read_csv("C:/Users/User/workspaces/github/pension-risk-management-system/db/L_^GOLD - Copy.csv", index_col=0, parse_dates=True)

# Rename for clarity
etf.columns = ['ETF']
index.columns = ['Index']

# Drop missing values
etf.dropna(inplace=True)
index.dropna(inplace=True)

# Find overlap period
common_index = etf.join(index, how='inner')

# Calculate scaling factor — linear regression could also be used
scaling_factor = (common_index['ETF'] / common_index['Index']).mean()
print(f"Scaling factor: {scaling_factor:.4f}")

# Scale the entire index series
scaled_index = index['Index'] * scaling_factor
scaled_index.name = 'Scaled Index'

# Use scaled index for dates before ETF starts
extension = scaled_index[scaled_index.index < etf.index.min()]

# Combine extended series with original ETF series
extended_etf = pd.concat([extension, etf['ETF']])

# Plot to verify
plt.figure(figsize=(12, 6))
plt.plot(extended_etf, label="Extended ETF", linewidth=2)
plt.plot(etf, label="Original ETF", linestyle="--")
plt.plot(scaled_index, label="Scaled Index", alpha=0.5)
plt.legend()
plt.title("ETF Extended Backwards Using Scaled Index")
plt.grid(True)
plt.tight_layout()
plt.savefig("output/extended_etf_plot.png")
plt.close()

# Save the extended series
extended_etf.to_csv("output/extended_etf.csv")
print("✅ Extended ETF saved to output/extended_etf.csv and plotted.")
