/**
 * Demo script for the pricing update system
 * 
 * This demonstrates how to use the PricingWebSearchService and
 * CataloguePricingEnhancer to update catalogue pricing.
 */

import { PricingWebSearchService } from '../services/PricingWebSearchService';

async function demonstratePricingSystem(): Promise<void> {
  console.log('='.repeat(70));
  console.log('CATALOGUE PRICING UPDATE SYSTEM DEMONSTRATION');
  console.log('='.repeat(70));
  console.log('');

  const pricingService = new PricingWebSearchService();

  // Demonstrate material pricing search
  console.log('1. MATERIAL PRICING SEARCH');
  console.log('-'.repeat(70));
  
  const materials = [
    { name: '2x4 lumber', unit: 'each' },
    { name: 'concrete', unit: 'cubic yard' },
    { name: 'toilet', unit: 'each' },
    { name: 'electrical receptacle', unit: 'each' },
  ];

  for (const material of materials) {
    const result = await pricingService.searchMaterialPricing(
      material.name,
      material.unit
    );
    
    if (result) {
      console.log(`\n📦 ${material.name}`);
      console.log(`   Average Price: $${result.averagePrice.toFixed(2)} per ${result.unit}`);
      console.log(`   Price Range: $${result.priceRange.min.toFixed(2)} - $${result.priceRange.max.toFixed(2)}`);
      console.log(`   Source: ${result.sources.join(', ')}`);
      console.log(`   Last Updated: ${result.lastUpdated.toLocaleDateString()}`);
    }
  }

  console.log('');
  console.log('='.repeat(70));
  console.log('');

  // Demonstrate labor rate search
  console.log('2. LABOR RATE SEARCH');
  console.log('-'.repeat(70));
  
  const trades = [
    'carpenter',
    'electrician',
    'plumber',
    'general laborer',
  ];

  for (const trade of trades) {
    const result = await pricingService.searchLaborRate(trade);
    
    if (result) {
      console.log(`\n👷 ${result.trade.charAt(0).toUpperCase() + result.trade.slice(1)}`);
      console.log(`   Hourly Rate: $${result.hourlyRate.toFixed(2)}/hour`);
      console.log(`   Rate Range: $${result.rateRange.min.toFixed(2)} - $${result.rateRange.max.toFixed(2)}`);
      console.log(`   Location: ${result.location || 'USA'}`);
      console.log(`   Source: ${result.sources.join(', ')}`);
    }
  }

  console.log('');
  console.log('='.repeat(70));
  console.log('');

  // Demonstrate labor time estimates
  console.log('3. LABOR TIME ESTIMATES');
  console.log('-'.repeat(70));
  
  const tasks = [
    { description: 'install toilet', unit: 'each' },
    { description: 'install light switch', unit: 'each' },
    { description: 'frame wall', unit: 'linear foot' },
    { description: 'build staircase', unit: 'flight' },
  ];

  for (const task of tasks) {
    const result = await pricingService.searchLaborTimeEstimate(
      task.description,
      task.unit
    );
    
    if (result) {
      console.log(`\n⏱️  ${task.description.charAt(0).toUpperCase() + task.description.slice(1)}`);
      console.log(`   Labor Time: ${result} hours per ${task.unit}`);
      
      // Calculate cost with carpenter rate
      const carpenterRate = 50; // $50/hour
      const cost = result * carpenterRate;
      console.log(`   Estimated Labor Cost: $${cost.toFixed(2)} (at $${carpenterRate}/hour)`);
    }
  }

  console.log('');
  console.log('='.repeat(70));
  console.log('');

  // Demonstrate cache functionality
  console.log('4. CACHE STATISTICS');
  console.log('-'.repeat(70));
  
  const stats = pricingService.getCacheStats();
  console.log(`\n📊 Cache Performance:`);
  console.log(`   Cached Searches: ${stats.size}`);
  console.log(`   Cache Keys:`);
  stats.keys.slice(0, 5).forEach(key => {
    console.log(`   - ${key}`);
  });
  if (stats.keys.length > 5) {
    console.log(`   ... and ${stats.keys.length - 5} more`);
  }

  console.log('');
  console.log('='.repeat(70));
  console.log('');

  // Demonstrate cost calculation example
  console.log('5. COST CALCULATION EXAMPLE');
  console.log('-'.repeat(70));
  console.log('\n📋 Example: Toilet Installation');
  console.log('');

  // Get pricing data
  const toiletMaterial = await pricingService.searchMaterialPricing('toilet', 'each');
  const waxRingMaterial = await pricingService.searchMaterialPricing('wax ring', 'each');
  const plumberRate = await pricingService.searchLaborRate('plumber');
  const installTime = await pricingService.searchLaborTimeEstimate('install toilet', 'each');

  if (toiletMaterial && waxRingMaterial && plumberRate && installTime) {
    const materialCost = toiletMaterial.averagePrice + waxRingMaterial.averagePrice;
    const laborCost = installTime * plumberRate.hourlyRate;
    const totalCost = materialCost + laborCost;

    console.log('   Materials:');
    console.log(`   - Toilet: $${toiletMaterial.averagePrice.toFixed(2)}`);
    console.log(`   - Wax Ring: $${waxRingMaterial.averagePrice.toFixed(2)}`);
    console.log(`   Material Subtotal: $${materialCost.toFixed(2)}`);
    console.log('');
    console.log('   Labor:');
    console.log(`   - Time Required: ${installTime} hours`);
    console.log(`   - Plumber Rate: $${plumberRate.hourlyRate.toFixed(2)}/hour`);
    console.log(`   Labor Subtotal: $${laborCost.toFixed(2)}`);
    console.log('');
    console.log(`   TOTAL COST: $${totalCost.toFixed(2)}`);
    console.log('');
    console.log(`   With 15% markup: $${(totalCost * 1.15).toFixed(2)}`);
  }

  console.log('');
  console.log('='.repeat(70));
  console.log('');

  // Usage instructions
  console.log('6. HOW TO USE THIS SYSTEM');
  console.log('-'.repeat(70));
  console.log('');
  console.log('To update your entire catalogue with current pricing:');
  console.log('   npm run update:pricing');
  console.log('');
  console.log('To update a specific trade:');
  console.log('   npm run update:pricing trade "Finish Plumbing"');
  console.log('');
  console.log('To update a specific assembly:');
  console.log('   npm run update:pricing assembly "Toilet Installation" "Plumber"');
  console.log('');
  console.log('For more information, see PRICING_UPDATE_README.md');
  console.log('');
  console.log('='.repeat(70));
  console.log('');
  console.log('✅ Demonstration complete!');
  console.log('');
}

// Run the demonstration
if (require.main === module) {
  demonstratePricingSystem()
    .then(() => {
      console.log('Demo finished successfully.');
      process.exit(0);
    })
    .catch((error) => {
      console.error('Demo failed:', error);
      process.exit(1);
    });
}

export { demonstratePricingSystem };
