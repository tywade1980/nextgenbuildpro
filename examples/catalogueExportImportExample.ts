/**
 * Example usage of the CatalogueExportImport utility
 * 
 * This demonstrates how to use the export/import functionality
 * for catalogue data backup, migration, and restoration.
 */

import { CatalogueExportImport } from '../services/CatalogueExportImport';
import * as path from 'path';

async function exampleUsage() {
  // Initialize the export/import utility
  const catalogueExportImport = new CatalogueExportImport();

  try {
    console.log('=== Catalogue Export/Import Example ===\n');

    // 1. Get current catalogue statistics
    console.log('1. Getting current catalogue statistics...');
    const stats = await catalogueExportImport.getCatalogueStats();
    console.log('Current Catalogue Statistics:');
    console.log(`  Categories: ${stats.categories} (${stats.activeCategories} active)`);
    console.log(`  Trades: ${stats.trades} (${stats.activeTrades} active)`);
    console.log(`  Scopes: ${stats.scopes} (${stats.activeScopes} active)`);
    console.log(`  Assemblies: ${stats.assemblies} (${stats.activeAssemblies} active)`);
    console.log(`  Tasks: ${stats.tasks} (${stats.activeTasks} active)`);
    console.log(`  Materials: ${stats.materials} (${stats.activeMaterials} active)\n`);

    // 2. Export all catalogue data (including inactive items)
    const allDataExportPath = path.join(__dirname, '../exports/complete-catalogue.json');
    console.log('2. Exporting complete catalogue data...');
    await catalogueExportImport.exportCatalogue(allDataExportPath);
    console.log(`Complete catalogue exported to: ${allDataExportPath}\n`);

    // 3. Export only active catalogue data
    const activeDataExportPath = path.join(__dirname, '../exports/active-catalogue.json');
    console.log('3. Exporting active catalogue data only...');
    await catalogueExportImport.exportActiveCatalogue(activeDataExportPath);
    console.log(`Active catalogue exported to: ${activeDataExportPath}\n`);

    // 4. Import catalogue data (example with active data)
    console.log('4. Importing catalogue data (without clearing existing)...');
    await catalogueExportImport.importCatalogue(activeDataExportPath, false);
    console.log('Import completed without clearing existing data.\n');

    // 5. Clear existing data and import fresh data
    console.log('5. Clearing existing data and importing fresh catalogue...');
    await catalogueExportImport.importCatalogue(allDataExportPath, true);
    console.log('Fresh import completed with existing data cleared.\n');

    // 6. Clear all catalogue data
    console.log('6. Clearing all catalogue data...');
    await catalogueExportImport.clearExistingData();
    console.log('All catalogue data cleared.\n');

    // 7. Restore from backup
    console.log('7. Restoring from backup...');
    await catalogueExportImport.importCatalogue(allDataExportPath, false);
    console.log('Catalogue restored from backup.\n');

    // 8. Final statistics
    console.log('8. Final catalogue statistics...');
    const finalStats = await catalogueExportImport.getCatalogueStats();
    console.log('Final Catalogue Statistics:');
    console.log(`  Categories: ${finalStats.categories} (${finalStats.activeCategories} active)`);
    console.log(`  Trades: ${finalStats.trades} (${finalStats.activeTrades} active)`);
    console.log(`  Scopes: ${finalStats.scopes} (${finalStats.activeScopes} active)`);
    console.log(`  Assemblies: ${finalStats.assemblies} (${finalStats.activeAssemblies} active)`);
    console.log(`  Tasks: ${finalStats.tasks} (${finalStats.activeTasks} active)`);
    console.log(`  Materials: ${finalStats.materials} (${finalStats.activeMaterials} active)\n`);

    console.log('=== Example completed successfully! ===');

  } catch (error) {
    console.error('Error during catalogue export/import example:', error);
  }
}

/**
 * Automated backup function
 * Can be run on a schedule to create regular backups
 */
async function createScheduledBackup() {
  const catalogueExportImport = new CatalogueExportImport();
  
  try {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const backupPath = path.join(__dirname, `../backups/catalogue-backup-${timestamp}.json`);
    
    console.log(`Creating scheduled backup: ${backupPath}`);
    await catalogueExportImport.exportActiveCatalogue(backupPath);
    console.log('Scheduled backup completed successfully');
    
    return backupPath;
  } catch (error) {
    console.error('Error creating scheduled backup:', error);
    throw error;
  }
}

/**
 * Migration function for moving data between environments
 */
async function migrateToNewEnvironment(sourcePath: string) {
  const catalogueExportImport = new CatalogueExportImport();
  
  try {
    console.log('Starting migration to new environment...');
    
    // Clear existing data and import from source
    await catalogueExportImport.importCatalogue(sourcePath, true);
    
    // Verify migration
    const stats = await catalogueExportImport.getCatalogueStats();
    console.log('Migration completed. New environment statistics:');
    console.log(`  Total items: ${stats.categories + stats.trades + stats.scopes + stats.assemblies + stats.tasks + stats.materials}`);
    console.log(`  Active items: ${stats.activeCategories + stats.activeTrades + stats.activeScopes + stats.activeAssemblies + stats.activeTasks + stats.activeMaterials}`);
    
    return stats;
  } catch (error) {
    console.error('Error during migration:', error);
    throw error;
  }
}

// Export functions for use in other modules
export {
  exampleUsage,
  createScheduledBackup,
  migrateToNewEnvironment
};

// Run the example if this file is executed directly
if (require.main === module) {
  exampleUsage().catch(console.error);
}