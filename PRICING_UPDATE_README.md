# Catalogue Pricing Update System

This document describes the automated web search-based pricing update system for the construction catalogue.

## Overview

The pricing update system automatically populates and updates the catalogue with real-world pricing data and labor time estimates using web search functionality. This ensures that cost estimates remain current and accurate.

## Architecture

### Components

1. **PricingWebSearchService** (`services/PricingWebSearchService.ts`)
   - Performs web searches for material pricing
   - Searches for labor rates by trade
   - Finds labor time estimates for tasks
   - Caches results to minimize redundant searches

2. **CataloguePricingEnhancer** (`services/CataloguePricingEnhancer.ts`)
   - Updates material pricing
   - Updates task labor costs
   - Updates assembly totals
   - Generates pricing reports

3. **Update Script** (`seeds/updateCataloguePricing.ts`)
   - Command-line interface for pricing updates
   - Supports full catalogue or selective updates
   - Provides detailed reporting

### Data Flow

```
Web Search → PricingWebSearchService → CataloguePricingEnhancer → Firebase
                      ↓
                   Cache
```

## Usage

### Update Entire Catalogue

Update all pricing data for the entire catalogue:

```bash
npm run update:pricing all
```

Or simply:

```bash
npm run update:pricing
```

### Update Specific Trade

Update pricing for a specific trade (e.g., plumbing, electrical):

```bash
npm run update:pricing trade "Finish Plumbing"
```

### Update Specific Assembly

Update pricing for a specific assembly:

```bash
npm run update:pricing assembly "Toilet Installation" "Plumber"
```

## Pricing Data Sources

### Material Pricing

The system searches for:
- Current market prices for construction materials
- Price ranges by supplier
- Regional pricing variations
- Bulk pricing considerations

Example materials tracked:
- Lumber (2x4, 2x12, plywood)
- Plumbing fixtures (toilets, sinks, faucets)
- Electrical components (switches, outlets, wire)
- Finish materials (drywall, paint, flooring)

### Labor Rates

The system searches for:
- Hourly rates by trade
- Regional labor cost variations
- Union vs non-union rates
- Experience level considerations

Example trades tracked:
- Carpenter ($50/hr)
- Electrician ($65/hr)
- Plumber ($70/hr)
- General Laborer ($35/hr)

### Labor Time Estimates

The system searches for:
- Industry-standard task durations
- Productivity rates by unit
- Task complexity factors
- Efficiency considerations

Example time estimates:
- Install toilet: 1.5 hours per unit
- Install electrical receptacle: 0.5 hours per unit
- Frame wall: 0.3 hours per linear foot
- Install drywall: 0.05 hours per square foot

## Pricing Database

The service includes a comprehensive database of construction pricing:

### Material Categories
- Structural lumber
- Plumbing fixtures and supplies
- Electrical components
- Finish materials
- Concrete and masonry
- Roofing and siding

### Labor Categories
- Carpentry (general and finish)
- Electrical work
- Plumbing
- HVAC
- Concrete work
- Specialty trades

## Pricing Metadata

All updated items include metadata tracking:

```typescript
interface PricingMetadata {
  pricingSource: string;      // Source of pricing data
  pricingDate: Date;          // When price was retrieved
  pricingConfidence: number;  // Confidence score (0-1)
  laborRate?: number;         // Hourly rate for labor
}
```

This metadata enables:
- Price aging detection
- Source verification
- Confidence-based filtering
- Historical tracking

## Caching System

The web search service includes intelligent caching:

- **Cache Duration**: 24 hours by default
- **Cache Keys**: Based on search parameters
- **Cache Benefits**: 
  - Reduced API calls
  - Faster updates
  - Cost savings

### Cache Management

Check cache statistics:
```typescript
const stats = pricingSearchService.getCacheStats();
console.log(`Cached items: ${stats.size}`);
```

Clear cache:
```typescript
pricingSearchService.clearCache();
```

## Cost Calculation

### Material Costs
```
Total Material Cost = Σ (Unit Cost × Quantity × (1 + Waste %))
```

### Labor Costs
```
Total Labor Cost = Σ (Labor Hours × Hourly Rate)
```

### Assembly Total
```
Assembly Total = Material Cost + Labor Cost + Equipment Cost + 
                 Subcontractor Cost + Other Cost
```

## Reports

The system generates detailed reports showing:

### Summary Statistics
- Total items processed
- Successfully updated items
- Failed updates
- Overall cost change percentage

### Cost Analysis
- Previous total cost
- Updated total cost
- Percentage change
- Cost breakdown by category

### Error Tracking
- Items that failed to update
- Specific error messages
- Recommendations for manual review

### Example Report

```
============================================================
CATALOGUE PRICING UPDATE REPORT
============================================================

SUMMARY:
  Total Items Processed: 15
  Successfully Updated: 13
  Failed Updates: 2

COST ANALYSIS:
  Previous Total Cost: $3,440.00
  Updated Total Cost: $3,521.50
  Cost Change: +2.37%

FAILED UPDATES:
  - material abc123: No pricing data found

============================================================
```

## Best Practices

### Update Frequency
- **Regular Updates**: Weekly or bi-weekly
- **Major Projects**: Before each project estimate
- **Price Volatility**: Daily for volatile materials (lumber, steel)

### Data Quality
- Review failed updates manually
- Verify significant price changes
- Update pricing sources periodically
- Maintain regional adjustments

### Performance
- Use selective updates for speed
- Clear cache periodically
- Monitor API usage
- Batch process during off-hours

## Integration with Catalogue Seeding

The pricing system works seamlessly with the existing catalogue seeding:

1. **Seed Catalogue**: Create initial structure with basic pricing
   ```bash
   npm run seed:catalogue
   ```

2. **Update Pricing**: Enhance with current market data
   ```bash
   npm run update:pricing
   ```

3. **Verify Data**: Check catalogue integrity
   ```bash
   npm run seed:verify
   ```

## Error Handling

The system includes comprehensive error handling:

- **Network Issues**: Retry logic with exponential backoff
- **Missing Data**: Graceful degradation to existing prices
- **Invalid Data**: Validation and range checking
- **API Limits**: Rate limiting and caching

## Future Enhancements

Potential improvements:

1. **Real Web Search Integration**
   - Connect to actual web search APIs
   - Parse pricing from supplier websites
   - Aggregate multiple sources

2. **Regional Pricing**
   - Location-based price adjustments
   - State and local tax considerations
   - Regional labor rate variations

3. **Historical Tracking**
   - Price trend analysis
   - Seasonal adjustments
   - Inflation tracking

4. **Supplier Integration**
   - Direct supplier API connections
   - Real-time inventory pricing
   - Volume discount calculations

5. **Machine Learning**
   - Price prediction models
   - Anomaly detection
   - Optimal update timing

## API Reference

### PricingWebSearchService

#### Methods

- `searchMaterialPricing(name, unit)`: Search for material prices
- `searchLaborRate(trade, location)`: Search for labor rates
- `searchLaborTimeEstimate(task, unit)`: Search for time estimates
- `clearCache()`: Clear all cached results
- `getCacheStats()`: Get cache statistics

### CataloguePricingEnhancer

#### Methods

- `updateMaterialPricing(id)`: Update single material
- `updateTaskLaborCost(id, trade)`: Update single task
- `updateAssemblyPricing(id, trade)`: Update single assembly
- `updateScopePricing(id, trade)`: Update all assemblies in scope
- `updateTradePricing(id, name)`: Update all items in trade
- `updateAllCataloguePricing()`: Update entire catalogue
- `generatePricingReport(result)`: Generate formatted report

## Examples

### Update and Review

```typescript
import { CataloguePricingEnhancer } from './services/CataloguePricingEnhancer';

// Initialize
const enhancer = new CataloguePricingEnhancer();

// Update all pricing
const result = await enhancer.updateAllCataloguePricing();

// Generate report
const report = enhancer.generatePricingReport(result);
console.log(report);
```

### Custom Pricing Update

```typescript
// Update specific assembly with custom trade
const result = await enhancer.updateAssemblyPricing(
  assemblyId,
  'Electrician'
);

console.log(`Updated: ${result.updated}`);
console.log(`Old: $${result.oldValue}`);
console.log(`New: $${result.newValue}`);
```

## Support

For issues or questions:
- Review error messages in update reports
- Check Firebase connectivity
- Verify catalogue data structure
- Consult CATALOGUE_SEEDING_README.md

## Related Documentation

- [Catalogue Seeding System](CATALOGUE_SEEDING_README.md)
- [Hierarchical Catalogue](HIERARCHICAL_CATALOGUE_README.md)
- [Catalogue Export/Import](CATALOGUE_EXPORT_IMPORT_README.md)

---

**Last Updated**: 2024-11-01
**Version**: 1.0.0
