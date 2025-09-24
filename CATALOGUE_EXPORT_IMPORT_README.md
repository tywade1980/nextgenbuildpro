# Catalogue Export/Import Utility

A comprehensive TypeScript utility for exporting and importing construction catalogue data in the NextGen BuildPro system. This utility provides backup, migration, and data management capabilities for the hierarchical catalogue structure.

## Overview

The `CatalogueExportImport` class provides a complete solution for managing catalogue data transfers between environments, creating backups, and performing bulk data operations on the construction catalogue system.

### Features

- **Complete Data Export**: Export all catalogue entities (categories, trades, scopes, assemblies, tasks, materials)
- **Active Data Export**: Export only active/enabled catalogue items
- **JSON Format**: Human-readable JSON format for easy inspection and modification
- **Bulk Import**: Import complete catalogue datasets with validation
- **Data Clearing**: Clear existing data before import for clean migrations
- **Statistics**: Get comprehensive statistics about catalogue data
- **Error Handling**: Comprehensive error handling and logging
- **Type Safety**: Full TypeScript type safety with existing data models

## Installation and Usage

### Basic Usage

```typescript
import { CatalogueExportImport } from './services/CatalogueExportImport';

const exportImport = new CatalogueExportImport();

// Export complete catalogue
await exportImport.exportCatalogue('./backup.json');

// Import catalogue data
await exportImport.importCatalogue('./backup.json', false);

// Get statistics
const stats = await exportImport.getCatalogueStats();
console.log(`Total categories: ${stats.categories}`);
```

### Advanced Usage

```typescript
// Export only active items
await exportImport.exportActiveCatalogue('./active-catalogue.json');

// Clear existing data and import fresh
await exportImport.importCatalogue('./new-data.json', true);

// Clear all catalogue data
await exportImport.clearExistingData();
```

## API Reference

### Methods

#### `exportCatalogue(outputPath: string): Promise<void>`

Exports the complete catalogue data to a JSON file.

- **Parameters:**
  - `outputPath`: File path where the JSON export will be saved
- **Returns:** Promise that resolves when export is complete
- **Exports:** All catalogue entities including inactive items

#### `importCatalogue(inputPath: string, clearExisting?: boolean): Promise<void>`

Imports catalogue data from a JSON file.

- **Parameters:**
  - `inputPath`: File path to the JSON file to import
  - `clearExisting`: Optional. If true, clears existing data before import (default: false)
- **Returns:** Promise that resolves when import is complete
- **Validates:** File structure before importing

#### `exportActiveCatalogue(outputPath: string): Promise<void>`

Exports only active catalogue data to a JSON file.

- **Parameters:**
  - `outputPath`: File path where the JSON export will be saved
- **Returns:** Promise that resolves when export is complete
- **Exports:** Only items where `isActive = true`

#### `clearExistingData(): Promise<void>`

Clears all existing catalogue data from all collections.

- **Returns:** Promise that resolves when all data is cleared
- **Warning:** This operation cannot be undone

#### `getCatalogueStats(): Promise<CatalogueStats>`

Returns comprehensive statistics about the catalogue data.

- **Returns:** Promise resolving to statistics object with counts for all entities

### Data Structure

#### CatalogueExportData Interface

```typescript
interface CatalogueExportData {
  categories: Category[];
  trades: Trade[];
  scopes: Scope[];
  assemblies: Assembly[];
  tasks: Task[];
  materials: Material[];
  exportDate: Date;
}
```

#### Statistics Interface

```typescript
interface CatalogueStats {
  categories: number;          // Total categories
  trades: number;             // Total trades
  scopes: number;             // Total scopes
  assemblies: number;         // Total assemblies
  tasks: number;              // Total tasks
  materials: number;          // Total materials
  activeCategories: number;   // Active categories
  activeTrades: number;       // Active trades
  activeScopes: number;       // Active scopes
  activeAssemblies: number;   // Active assemblies
  activeTasks: number;        // Active tasks
  activeMaterials: number;    // Active materials
}
```

## Use Cases

### 1. Data Backup

Create regular backups of your catalogue data:

```typescript
// Daily backup of active data
const timestamp = new Date().toISOString().split('T')[0];
await exportImport.exportActiveCatalogue(`./backups/catalogue-${timestamp}.json`);
```

### 2. Environment Migration

Move catalogue data between development, staging, and production:

```typescript
// Export from source environment
await exportImport.exportCatalogue('./migration-data.json');

// Import to target environment (clearing existing)
await exportImport.importCatalogue('./migration-data.json', true);
```

### 3. Data Restoration

Restore from a backup file:

```typescript
// Clear corrupted data and restore from backup
await exportImport.clearExistingData();
await exportImport.importCatalogue('./backup-2024-01-15.json');
```

### 4. Data Analysis

Get insights into your catalogue structure:

```typescript
const stats = await exportImport.getCatalogueStats();
console.log(`Active/Total ratio: ${stats.activeAssemblies}/${stats.assemblies} assemblies`);
```

## Error Handling

The utility includes comprehensive error handling:

```typescript
try {
  await exportImport.exportCatalogue('./backup.json');
  console.log('Export successful');
} catch (error) {
  console.error('Export failed:', error.message);
  // Handle error appropriately
}
```

Common error scenarios:
- **File system errors**: Invalid paths, permission issues
- **Firebase errors**: Network issues, authentication problems
- **Data validation errors**: Invalid JSON structure during import
- **Memory errors**: Large datasets exceeding available memory

## Performance Considerations

### Large Datasets

For large catalogues:
- Use `exportActiveCatalogue()` instead of `exportCatalogue()` when possible
- Monitor memory usage during operations
- Consider chunked processing for very large datasets

### Network Optimization

- Export/import operations are network-intensive
- Run during off-peak hours for production systems
- Ensure stable network connection for large operations

### Storage Requirements

- JSON exports are human-readable but larger than binary formats
- Estimated sizes:
  - Small catalogue (100 assemblies): ~500KB
  - Medium catalogue (1000 assemblies): ~5MB
  - Large catalogue (10000 assemblies): ~50MB

## Security Considerations

### Data Sensitivity

- Exported JSON files contain complete catalogue data
- Store export files securely
- Use appropriate file permissions
- Consider encryption for sensitive data

### Access Control

- Restrict access to export/import functions in production
- Log all export/import operations
- Implement user authentication for these operations

## Integration Examples

### With Existing Services

```typescript
import { CatalogueDataService } from './CatalogueDataService';
import { CatalogueExportImport } from './CatalogueExportImport';

// Create new items, then backup
const dataService = new CatalogueDataService();
const exportImport = new CatalogueExportImport();

await dataService.createCategory({ name: 'New Category', /* ... */ });
await exportImport.exportActiveCatalogue('./post-update-backup.json');
```

### Scheduled Operations

```typescript
// Example cron job integration
import cron from 'node-cron';

cron.schedule('0 2 * * *', async () => {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
  await exportImport.exportActiveCatalogue(`./daily-backups/catalogue-${timestamp}.json`);
  console.log('Daily backup completed');
});
```

## Testing

The utility includes comprehensive unit tests:

```bash
npm test -- --testPathPattern=CatalogueExportImport.test.ts
```

Test coverage includes:
- Export functionality
- Import functionality with validation
- Error handling scenarios
- Data clearing operations
- Statistics generation

## Troubleshooting

### Common Issues

1. **"Invalid catalogue file structure" error**
   - Ensure JSON file contains all required fields: categories, trades, scopes, assemblies, tasks, materials
   - Validate JSON syntax

2. **Firebase permission errors**
   - Check Firebase authentication
   - Verify Firestore security rules
   - Ensure proper collection permissions

3. **Memory issues with large datasets**
   - Use `exportActiveCatalogue()` instead of full export
   - Process data in smaller chunks
   - Increase Node.js memory limit if needed

### Debug Mode

Enable detailed logging by setting console log level or adding debug statements:

```typescript
// Add debugging
console.log('Starting export operation...');
const stats = await exportImport.getCatalogueStats();
console.log('Pre-export stats:', stats);
```

## Version History

- **v1.0.0**: Initial release with core export/import functionality
- Added TypeScript implementation based on JavaScript requirements
- Comprehensive test suite
- Full Firebase Firestore integration
- Statistics and monitoring capabilities

## Contributing

When contributing to this utility:
1. Maintain TypeScript type safety
2. Add tests for new functionality
3. Update documentation
4. Follow existing error handling patterns
5. Consider backward compatibility

## License

This utility is part of the NextGen BuildPro project and follows the same licensing terms.