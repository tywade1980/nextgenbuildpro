/**
 * Demo script showing complete seeding and verification process
 * 
 * This script demonstrates:
 * 1. How to run the seeding process
 * 2. How to verify the results
 * 3. How to handle errors
 */

import { seedCatalogue } from './seedCatalogue';
import { verifyCatalogue } from './verifyCatalogue';

async function runDemo(): Promise<void> {
  console.log('🚀 Starting Construction Catalogue Demo');
  console.log('=====================================');
  
  try {
    // Step 1: Run the seeding
    console.log('Step 1: Seeding the catalogue...');
    await seedCatalogue();
    console.log('✅ Seeding completed');
    
    // Step 2: Wait a moment for data to propagate
    console.log('\n⏳ Waiting for data to propagate...');
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // Step 3: Verify the results
    console.log('\nStep 2: Verifying the seeded data...');
    await verifyCatalogue();
    console.log('✅ Verification completed');
    
    console.log('\n🎉 Demo completed successfully!');
    console.log('\n📊 Summary:');
    console.log('- 10 construction categories created');
    console.log('- 3 sample trades created');
    console.log('- 3 sample scopes created');
    console.log('- 3 complete assemblies with tasks and materials');
    
  } catch (error) {
    console.error('\n❌ Demo failed:', error);
    throw error;
  }
}

// Run the demo
if (require.main === module) {
  runDemo()
    .then(() => {
      console.log('\nDemo completed successfully');
      process.exit(0);
    })
    .catch((error) => {
      console.error('\nDemo failed:', error);
      process.exit(1);
    });
}

export { runDemo };