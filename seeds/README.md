# Construction Catalogue Seeding

This directory contains scripts to seed the Firebase Firestore database with initial construction catalogue data.

## Overview

The seeding script creates a comprehensive construction catalogue with:

- **10 Construction Categories**: From Pre-Construction to Outdoor Spaces
- **Sample Trades**: Specialized trade types within each category  
- **Sample Scopes**: Specific work scopes within each trade
- **Complete Assemblies**: Detailed work assemblies with tasks and materials

## Files

- `seedCatalogue.ts` - Main TypeScript seeding script

- `verifyCatalogue.ts` - Verification script to check seeded data


- `runSeeder.js` - Node.js runner script  
- `README.md` - This documentation

## Usage

### Prerequisites

1. Ensure Firebase is configured with valid `google-services.json`
2. Firebase project should have Firestore enabled
3. Dependencies installed: `npm install`

### Running the Seeder

**Option 1: Using npm script (Recommended)**
```bash
npm run seed:catalogue
```

**Option 2: Using the runner script**
```bash
npm run seed:run
```

**Option 3: Direct execution**
```bash
npx ts-node seeds/seedCatalogue.ts
```


### Verifying the Seeded Data

After running the seeder, verify the data was created correctly:

```bash
npm run seed:verify
```

This will check for:
- All 10 expected categories
- Sample trades (Finish Carpentry, Finish Plumbing, Finish Electrical)
- Sample assemblies (Standard Staircase, Toilet Installation, Bedroom Electrical Devices)
- Tasks and materials counts



## What Gets Created

### Categories (10 total)
1. Pre-Construction
2. Foundation  
3. Structural
4. Exterior Envelope
5. Plumbing
6. HVAC
7. Electrical
8. Interior Finishes
9. Specialty Areas
10. Outdoor Spaces

### Sample Data Examples

**Interior Finishes Category:**
- Trade: Finish Carpentry
- Scope: Stair Construction  
- Assembly: Standard Staircase
  - 3 Tasks (Layout, Cut stringers, Install stringers)
  - 2 Materials (Stair Stringers, Stair Risers)

**Plumbing Category:**
- Trade: Finish Plumbing
- Scope: Bathroom Fixture Installation
- Assembly: Toilet Installation
  - 2 Tasks (Install wax ring, Set toilet on flange)
  - 2 Materials (Toilet, Wax Ring)

**Electrical Category:**
- Trade: Finish Electrical  
- Scope: Room Device Installation
- Assembly: Bedroom Electrical Devices
  - 2 Tasks (Install light switches, Install receptacles)
  - 2 Materials (Light Switches, Receptacles)

## Data Structure

The seeding follows this hierarchy:
```
Category
└── Trade
    └── Scope
        └── Assembly
            ├── Tasks[]
            └── Materials[]
```

## Error Handling

The script includes comprehensive error handling:
- Logs progress for each step
- Catches and reports any failures
- Provides detailed error messages
- Graceful exit on completion or failure

## Testing

Run the seeding tests:
```bash
npm test -- --testPathPattern=seedCatalogue
```

## Customization

To modify the seed data:

1. Edit the category definitions in `createCategories()`
2. Modify specific trade/scope/assembly data in the respective functions:
   - `createInteriorFinishes()`
   - `createPlumbing()`  
   - `createElectrical()`
3. Add new categories by creating additional functions following the same pattern

## Firebase Collections

The script creates data in these Firestore collections:
- `categories` - Construction categories
- `trades` - Trade specializations
- `scopes` - Work scopes  
- `assemblies` - Complete work assemblies
- `tasks` - Individual tasks within assemblies
- `materials` - Materials required for assemblies

## Notes

- The script is idempotent but will create duplicate data if run multiple times
- All created records have `isActive: true` by default
- Costs and quantities are sample values for demonstration
- The script uses the existing `CatalogueDataService` for all database operations