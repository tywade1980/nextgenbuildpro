/**
 * Verification script to check seeded catalogue data
 * 
 * This script validates that the seeding completed successfully by
 * checking for the expected categories, trades, scopes, and assemblies
 */

import { CatalogueDataService } from '../services/CatalogueDataService';
import { 
  collection, 
  getDocs, 
  query, 
  where,
  orderBy 
} from 'firebase/firestore';
import { firestore } from '../firebase';

async function verifyCatalogue(): Promise<void> {
  console.log('🔍 Verifying catalogue seeding results...');
  
  const catalogueService = new CatalogueDataService();
  
  try {
    // Verify categories
    await verifyCategories();
    
    // Verify specific trades
    await verifyTrades();
    
    // Verify specific assemblies
    await verifyAssemblies();
    
    console.log('✅ Catalogue verification completed successfully');
  } catch (error) {
    console.error('❌ Verification failed:', error);
    throw error;
  }
}

async function verifyCategories(): Promise<void> {
  console.log('📂 Verifying categories...');
  
  const categoriesRef = collection(firestore, 'categories');
  const q = query(categoriesRef, where('isActive', '==', true), orderBy('sequence'));
  const snapshot = await getDocs(q);
  
  const categories = snapshot.docs.map(doc => doc.data());
  
  const expectedCategories = [
    'Pre-Construction',
    'Foundation',
    'Structural', 
    'Exterior Envelope',
    'Plumbing',
    'HVAC',
    'Electrical',
    'Interior Finishes',
    'Specialty Areas',
    'Outdoor Spaces'
  ];
  
  console.log(`Found ${categories.length} categories`);
  
  expectedCategories.forEach(expectedName => {
    const found = categories.find(cat => cat.name === expectedName);
    if (found) {
      console.log(`  ✅ ${expectedName}`);
    } else {
      console.log(`  ❌ Missing: ${expectedName}`);
    }
  });
}

async function verifyTrades(): Promise<void> {
  console.log('🔧 Verifying trades...');
  
  const tradesRef = collection(firestore, 'trades');
  const q = query(tradesRef, where('isActive', '==', true));
  const snapshot = await getDocs(q);
  
  const trades = snapshot.docs.map(doc => doc.data());
  
  const expectedTrades = [
    'Finish Carpentry',
    'Finish Plumbing',
    'Finish Electrical'
  ];
  
  console.log(`Found ${trades.length} trades`);
  
  expectedTrades.forEach(expectedName => {
    const found = trades.find(trade => trade.name === expectedName);
    if (found) {
      console.log(`  ✅ ${expectedName}`);
    } else {
      console.log(`  ❌ Missing: ${expectedName}`);
    }
  });
}

async function verifyAssemblies(): Promise<void> {
  console.log('🏗️ Verifying assemblies...');
  
  const assembliesRef = collection(firestore, 'assemblies');
  const q = query(assembliesRef, where('isActive', '==', true));
  const snapshot = await getDocs(q);
  
  const assemblies = snapshot.docs.map(doc => doc.data());
  
  const expectedAssemblies = [
    'Standard Staircase',
    'Toilet Installation', 
    'Bedroom Electrical Devices'
  ];
  
  console.log(`Found ${assemblies.length} assemblies`);
  
  expectedAssemblies.forEach(expectedName => {
    const found = assemblies.find(assembly => assembly.name === expectedName);
    if (found) {
      console.log(`  ✅ ${expectedName}`);
      
      // Show some details
      const assembly = found;
      console.log(`    Unit: ${assembly.unit}`);
      console.log(`    Total Cost: $${assembly.totalCost}`);
      console.log(`    Labor Hours: ${assembly.laborHours}`);
      console.log(`    Tags: ${assembly.tags.join(', ')}`);
    } else {
      console.log(`  ❌ Missing: ${expectedName}`);
    }
  });
}

async function verifyTasksAndMaterials(): Promise<void> {
  console.log('📋 Verifying tasks and materials...');
  
  // Check tasks
  const tasksRef = collection(firestore, 'tasks');
  const tasksQuery = query(tasksRef, where('isActive', '==', true));
  const tasksSnapshot = await getDocs(tasksQuery);
  
  console.log(`Found ${tasksSnapshot.size} tasks`);
  
  // Check materials
  const materialsRef = collection(firestore, 'materials');
  const materialsQuery = query(materialsRef, where('isActive', '==', true));
  const materialsSnapshot = await getDocs(materialsQuery);
  
  console.log(`Found ${materialsSnapshot.size} materials`);
}

// Run the verification
if (require.main === module) {
  verifyCatalogue()
    .then(() => {
      console.log('Verification completed successfully');
      process.exit(0);
    })
    .catch((error) => {
      console.error('Verification failed:', error);
      process.exit(1);
    });
}

export { verifyCatalogue };