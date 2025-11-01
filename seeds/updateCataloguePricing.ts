/**
 * Update Catalogue Pricing Script
 * 
 * This script updates the catalogue with current pricing and labor time data
 * using web search functionality.
 */

import { CataloguePricingEnhancer } from '../services/CataloguePricingEnhancer';
import { CatalogueDataService } from '../services/CatalogueDataService';
import { PricingWebSearchService } from '../services/PricingWebSearchService';

async function updateCataloguePricing(): Promise<void> {
  console.log('='.repeat(60));
  console.log('CATALOGUE PRICING UPDATE - Starting...');
  console.log('='.repeat(60));
  console.log('');

  try {
    // Initialize services
    const catalogueService = new CatalogueDataService();
    const pricingSearchService = new PricingWebSearchService();
    const pricingEnhancer = new CataloguePricingEnhancer(
      catalogueService,
      pricingSearchService
    );

    // Update all catalogue pricing
    const result = await pricingEnhancer.updateAllCataloguePricing();

    // Generate and display report
    const report = pricingEnhancer.generatePricingReport(result);
    console.log('');
    console.log(report);

    // Display cache statistics
    const cacheStats = pricingSearchService.getCacheStats();
    console.log('');
    console.log('CACHE STATISTICS:');
    console.log(`  Cached Searches: ${cacheStats.size}`);
    console.log('');

    console.log('✅ Catalogue pricing update completed successfully!');
  } catch (error) {
    console.error('');
    console.error('❌ Error updating catalogue pricing:');
    console.error(error);
    process.exit(1);
  }
}

async function updateSpecificTrade(tradeName: string): Promise<void> {
  console.log(`Updating pricing for trade: ${tradeName}`);

  try {
    const catalogueService = new CatalogueDataService();
    const pricingEnhancer = new CataloguePricingEnhancer(catalogueService);

    // Find the trade by name
    const categories = await catalogueService.getCategories();
    let tradeFound = false;

    for (const category of categories) {
      const trades = await catalogueService.getTradesByCategory(category.id);
      const trade = trades.find(
        t => t.name.toLowerCase() === tradeName.toLowerCase()
      );

      if (trade) {
        console.log(`Found trade: ${trade.name} in category: ${category.name}`);
        const result = await pricingEnhancer.updateTradePricing(trade.id, trade.name);
        const report = pricingEnhancer.generatePricingReport(result);
        console.log('');
        console.log(report);
        tradeFound = true;
        break;
      }
    }

    if (!tradeFound) {
      console.error(`Trade '${tradeName}' not found in catalogue`);
      process.exit(1);
    }

    console.log('✅ Trade pricing update completed successfully!');
  } catch (error) {
    console.error('❌ Error updating trade pricing:', error);
    process.exit(1);
  }
}

async function updateSpecificAssembly(assemblyName: string, trade: string): Promise<void> {
  console.log(`Updating pricing for assembly: ${assemblyName}`);

  try {
    const catalogueService = new CatalogueDataService();
    const pricingEnhancer = new CataloguePricingEnhancer(catalogueService);

    // Search for assembly (simplified search)
    const categories = await catalogueService.getCategories();
    let assemblyFound = false;

    for (const category of categories) {
      const trades = await catalogueService.getTradesByCategory(category.id);
      
      for (const tradeItem of trades) {
        const scopes = await catalogueService.getScopesByTrade(tradeItem.id);
        
        for (const scope of scopes) {
          const assemblies = await catalogueService.getAssembliesByScope(scope.id);
          const assembly = assemblies.find(
            a => a.name.toLowerCase() === assemblyName.toLowerCase()
          );

          if (assembly) {
            console.log(`Found assembly: ${assembly.name}`);
            const result = await pricingEnhancer.updateAssemblyPricing(
              assembly.id,
              trade || tradeItem.name
            );
            
            console.log('');
            console.log('UPDATE RESULT:');
            console.log(`  Assembly: ${assembly.name}`);
            console.log(`  Old Cost: $${result.oldValue.toFixed(2)}`);
            console.log(`  New Cost: $${result.newValue.toFixed(2)}`);
            console.log(`  Change: ${((result.newValue - result.oldValue) / result.oldValue * 100).toFixed(2)}%`);
            console.log(`  Status: ${result.updated ? '✅ Updated' : '❌ Failed'}`);
            
            if (result.error) {
              console.log(`  Error: ${result.error}`);
            }
            
            assemblyFound = true;
            break;
          }
        }
        
        if (assemblyFound) break;
      }
      
      if (assemblyFound) break;
    }

    if (!assemblyFound) {
      console.error(`Assembly '${assemblyName}' not found in catalogue`);
      process.exit(1);
    }

    console.log('');
    console.log('✅ Assembly pricing update completed successfully!');
  } catch (error) {
    console.error('❌ Error updating assembly pricing:', error);
    process.exit(1);
  }
}

// Parse command line arguments
const args = process.argv.slice(2);
const command = args[0];

if (command === 'all' || !command) {
  // Update entire catalogue
  updateCataloguePricing()
    .then(() => process.exit(0))
    .catch((error) => {
      console.error(error);
      process.exit(1);
    });
} else if (command === 'trade') {
  // Update specific trade
  const tradeName = args[1];
  if (!tradeName) {
    console.error('Usage: npm run update:pricing trade <trade-name>');
    process.exit(1);
  }
  updateSpecificTrade(tradeName)
    .then(() => process.exit(0))
    .catch((error) => {
      console.error(error);
      process.exit(1);
    });
} else if (command === 'assembly') {
  // Update specific assembly
  const assemblyName = args[1];
  const trade = args[2];
  if (!assemblyName) {
    console.error('Usage: npm run update:pricing assembly <assembly-name> [trade]');
    process.exit(1);
  }
  updateSpecificAssembly(assemblyName, trade)
    .then(() => process.exit(0))
    .catch((error) => {
      console.error(error);
      process.exit(1);
    });
} else {
  console.error('Unknown command:', command);
  console.error('Usage:');
  console.error('  npm run update:pricing [all]              - Update entire catalogue');
  console.error('  npm run update:pricing trade <name>       - Update specific trade');
  console.error('  npm run update:pricing assembly <name> [trade] - Update specific assembly');
  process.exit(1);
}
