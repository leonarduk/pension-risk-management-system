# Strata Integration Design

## Overview
Strata is an open-source analytics and market risk library developed by OpenGamma. It provides comprehensive components for financial analytics, including reference data, market data abstractions, pricing engines and a unified API for calculating risk measures across portfolios.

## Benefits for Risk Analytics
- **Standardized financial model** – Strata-basics defines money, currency, schedules and other domain concepts that help ensure consistent modelling.
- **Market data management** – Strata-market offers curve and surface abstractions, interpolation and sensitivity objects that can underpin robust market data handling.
- **Pricing and risk** – Strata-pricer supplies pricers for many asset classes, enabling calculation of values and sensitivities.
- **Measure calculations** – Strata-measure exposes a high-level API to compute measures such as present value or par rate over scenarios or portfolios.

These capabilities can enhance the pension risk management system by supplying proven analytics components and reducing the need to build valuation logic from scratch.

## Proposed Integration Points
1. **Market Data Feeds**
   - Map existing time-series sources to Strata-market data structures (curves, surfaces, identifiers).
   - Use Strata's interpolation utilities for building yield curves from stored rates.
2. **Pricing Services**
   - Incorporate Strata-pricer in the Spring Boot server to price fixed-income instruments and obtain risk measures (PV, DV01, etc.).
   - Expose pricing results via REST endpoints for the UI and downstream analytics.
3. **Scenario Analysis**
   - Employ Strata-measure's `CalculationRunner` to evaluate portfolios under multiple scenarios generated from time-series shocks.
   - Store scenario results for reporting and visualization.
4. **Reporting**
   - Utilize Strata's reporting utilities to produce standardized risk reports that can be rendered or exported by the UI module.

## Follow-up Tasks
- [ ] Prototype mapping of the existing time-series database to Strata's market data objects.
- [ ] Evaluate integration of Strata-pricer into the Spring Boot pricing service.
- [ ] Design a scenario generation module compatible with Strata's `CalculationRunner`.
- [ ] Assess performance and scalability of Strata when applied to pension fund portfolios.
- [ ] Investigate licensing and support considerations for long-term adoption.

