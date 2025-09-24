/**
 * Script to seed the database with initial catalogue data
 * 
 * This TypeScript version mirrors the structure of the JavaScript seeding script
 * but uses the existing CatalogueDataService and TypeScript interfaces
 */

import { CatalogueDataService } from '../services/CatalogueDataService';
import { Category } from '../models/CatalogueSchema';

async function seedCatalogue(): Promise<void> {
  console.log('Starting construction catalogue seeding...');
  
  const catalogueService = new CatalogueDataService();
  
  try {
    // Create categories
    const categories = await createCategories(catalogueService);
    
    // Create trades, scopes, assemblies for each category
    await createInteriorFinishes(catalogueService, categories.find(c => c.name === 'Interior Finishes')!.id);
    await createPlumbing(catalogueService, categories.find(c => c.name === 'Plumbing')!.id);
    await createElectrical(catalogueService, categories.find(c => c.name === 'Electrical')!.id);
    
    console.log('Catalogue seeding completed successfully');
  } catch (error) {
    console.error('Error seeding catalogue:', error);
    throw error;
  }
}

async function createCategories(catalogueService: CatalogueDataService): Promise<Category[]> {
  console.log('Creating categories...');
  
  const categoryData = [
    {
      name: 'Pre-Construction',
      description: 'Activities before main construction begins',
      sequence: 1,
      isActive: true,
    },
    {
      name: 'Foundation',
      description: 'Structural foundation systems',
      sequence: 2,
      isActive: true,
    },
    {
      name: 'Structural',
      description: 'Framing and structural elements',
      sequence: 3,
      isActive: true,
    },
    {
      name: 'Exterior Envelope',
      description: 'Exterior elements and weatherproofing',
      sequence: 4,
      isActive: true,
    },
    {
      name: 'Plumbing',
      description: 'Plumbing systems and fixtures',
      sequence: 5,
      isActive: true,
    },
    {
      name: 'HVAC',
      description: 'Heating, ventilation, and air conditioning',
      sequence: 6,
      isActive: true,
    },
    {
      name: 'Electrical',
      description: 'Electrical systems and fixtures',
      sequence: 7,
      isActive: true,
    },
    {
      name: 'Interior Finishes',
      description: 'Interior finishing and trim work',
      sequence: 8,
      isActive: true,
    },
    {
      name: 'Specialty Areas',
      description: 'Specialty rooms and features',
      sequence: 9,
      isActive: true,
    },
    {
      name: 'Outdoor Spaces',
      description: 'Outdoor structures and landscaping',
      sequence: 10,
      isActive: true,
    }
  ];
  
  const categories: Category[] = [];
  
  for (const data of categoryData) {
    const category = await catalogueService.createCategory(data);
    categories.push(category);
    console.log(`Created category: ${category.name}`);
  }
  
  return categories;
}

// Create interior finishes category data
async function createInteriorFinishes(catalogueService: CatalogueDataService, categoryId: string): Promise<void> {
  console.log('Creating Interior Finishes category data...');
  
  // Create Finish Carpentry trade
  const finishCarpentryTrade = await catalogueService.createTrade({
    categoryId,
    name: 'Finish Carpentry',
    description: 'Fine woodwork and detailed carpentry',
    sequence: 1,
    isActive: true
  });
  
  // Create Stair Construction scope
  const stairScope = await catalogueService.createScope({
    tradeId: finishCarpentryTrade.id,
    name: 'Stair Construction',
    description: 'Building and finishing interior staircases',
    sequence: 1,
    isActive: true
  });
  
  // Create Standard Staircase assembly
  await catalogueService.createCompleteAssembly(
    stairScope.id,
    {
      scopeId: stairScope.id,
      name: 'Standard Staircase',
      description: 'Standard wooden staircase with handrail and balusters',
      sequence: 1,
      unit: 'flight',
      laborHours: 24,
      materialCost: 1250,
      laborCost: 1200,
      equipmentCost: 150,
      subcontractorCost: 0,
      otherCost: 50,
      totalCost: 2650,
      markupPercentage: 15,
      notes: 'Assumes standard 12-14 step staircase with oak treads and painted risers',
      tags: ['stair', 'wood', 'interior', 'carpentry'],
      isActive: true
    },
    [
      {
        name: 'Layout stair dimensions',
        description: 'Measure and mark stair locations and dimensions',
        sequence: 1,
        laborHours: 2,
        materialCost: 0,
        laborCost: 100,
        equipmentCost: 0,
        notes: 'Verify measurements against building code requirements',
        isActive: true
      },
      {
        name: 'Cut stringers',
        description: 'Cut and prepare stair stringers',
        sequence: 2,
        laborHours: 4,
        materialCost: 180,
        laborCost: 200,
        equipmentCost: 50,
        notes: 'Use 2x12 pressure treated lumber for stringers',
        isActive: true
      },
      {
        name: 'Install stringers',
        description: 'Install and secure stair stringers',
        sequence: 3,
        laborHours: 3,
        materialCost: 40,
        laborCost: 150,
        equipmentCost: 0,
        notes: 'Secure with lag bolts and joist hangers',
        isActive: true
      }
    ],
    [
      {
        name: 'Stair Stringers',
        description: '2x12 pressure treated lumber',
        quantity: 3,
        unit: 'each',
        unitCost: 60,
        totalCost: 180,
        waste: 10,
        isActive: true
      },
      {
        name: 'Stair Risers',
        description: '1x8 pine boards',
        quantity: 14,
        unit: 'each',
        unitCost: 8.50,
        totalCost: 119,
        waste: 5,
        isActive: true
      }
    ]
  );
  
  console.log('Created Interior Finishes data');
}

// Create plumbing category data
async function createPlumbing(catalogueService: CatalogueDataService, categoryId: string): Promise<void> {
  console.log('Creating Plumbing category data...');
  
  // Create Finish Plumbing trade
  const finishPlumbingTrade = await catalogueService.createTrade({
    categoryId,
    name: 'Finish Plumbing',
    description: 'Installation of plumbing fixtures and connections',
    sequence: 1,
    isActive: true
  });
  
  // Create Bathroom Fixture Installation scope
  const bathroomFixtureScope = await catalogueService.createScope({
    tradeId: finishPlumbingTrade.id,
    name: 'Bathroom Fixture Installation',
    description: 'Installation of toilets, sinks, and other bathroom fixtures',
    sequence: 1,
    isActive: true
  });
  
  // Create Toilet Installation assembly
  await catalogueService.createCompleteAssembly(
    bathroomFixtureScope.id,
    {
      scopeId: bathroomFixtureScope.id,
      name: 'Toilet Installation',
      description: 'Standard toilet installation including wax ring and supply line',
      sequence: 1,
      unit: 'each',
      laborHours: 1.5,
      materialCost: 325,
      laborCost: 75,
      equipmentCost: 0,
      subcontractorCost: 0,
      otherCost: 0,
      totalCost: 400,
      markupPercentage: 15,
      notes: 'Includes standard two-piece toilet with seat',
      tags: ['plumbing', 'bathroom', 'toilet', 'fixture'],
      isActive: true
    },
    [
      {
        name: 'Install wax ring',
        description: 'Apply wax ring to toilet flange',
        sequence: 1,
        laborHours: 0.25,
        materialCost: 8,
        laborCost: 12.50,
        equipmentCost: 0,
        notes: 'Ensure flange is clean before applying',
        isActive: true
      },
      {
        name: 'Set toilet on flange',
        description: 'Position toilet on wax ring and flange',
        sequence: 2,
        laborHours: 0.25,
        materialCost: 0,
        laborCost: 12.50,
        equipmentCost: 0,
        notes: 'Align bolt holes with flange bolts',
        isActive: true
      }
    ],
    [
      {
        name: 'Toilet',
        description: 'Two-piece toilet',
        quantity: 1,
        unit: 'each',
        unitCost: 250,
        totalCost: 250,
        waste: 0,
        isActive: true
      },
      {
        name: 'Wax Ring',
        description: 'Toilet wax ring with sleeve',
        quantity: 1,
        unit: 'each',
        unitCost: 8,
        totalCost: 8,
        waste: 0,
        isActive: true
      }
    ]
  );
  
  console.log('Created Plumbing data');
}

// Create electrical category data
async function createElectrical(catalogueService: CatalogueDataService, categoryId: string): Promise<void> {
  console.log('Creating Electrical category data...');
  
  // Create Finish Electrical trade
  const finishElectricalTrade = await catalogueService.createTrade({
    categoryId,
    name: 'Finish Electrical',
    description: 'Installation of electrical fixtures and devices',
    sequence: 1,
    isActive: true
  });
  
  // Create Room Device Installation scope
  const roomDeviceScope = await catalogueService.createScope({
    tradeId: finishElectricalTrade.id,
    name: 'Room Device Installation',
    description: 'Installation of electrical devices and fixtures by room',
    sequence: 1,
    isActive: true
  });
  
  // Create Bedroom Devices assembly
  await catalogueService.createCompleteAssembly(
    roomDeviceScope.id,
    {
      scopeId: roomDeviceScope.id,
      name: 'Bedroom Electrical Devices',
      description: 'Installation of all electrical devices for a standard bedroom',
      sequence: 1,
      unit: 'room',
      laborHours: 3.5,
      materialCost: 215,
      laborCost: 175,
      equipmentCost: 0,
      subcontractorCost: 0,
      otherCost: 0,
      totalCost: 390,
      markupPercentage: 15,
      notes: 'Includes switches, receptacles, ceiling fan/light, and smoke detector for standard bedroom',
      tags: ['electrical', 'bedroom', 'devices', 'fixtures'],
      isActive: true
    },
    [
      {
        name: 'Install light switches',
        description: 'Install single-pole and three-way switches',
        sequence: 1,
        laborHours: 0.5,
        materialCost: 15,
        laborCost: 25,
        equipmentCost: 0,
        notes: 'Typically 2 switches per bedroom',
        isActive: true
      },
      {
        name: 'Install receptacles',
        description: 'Install electrical outlets',
        sequence: 2,
        laborHours: 1.5,
        materialCost: 60,
        laborCost: 75,
        equipmentCost: 0,
        notes: 'Typically 6 receptacles per bedroom',
        isActive: true
      }
    ],
    [
      {
        name: 'Light Switches',
        description: 'Single-pole and three-way switches',
        quantity: 2,
        unit: 'each',
        unitCost: 3.50,
        totalCost: 7,
        waste: 0,
        isActive: true
      },
      {
        name: 'Receptacles',
        description: 'Standard 15A receptacles',
        quantity: 6,
        unit: 'each',
        unitCost: 3.50,
        totalCost: 21,
        waste: 0,
        isActive: true
      }
    ]
  );
  
  console.log('Created Electrical data');
}

// Run the seeder
if (require.main === module) {
  seedCatalogue()
    .then(() => {
      console.log('Seeding completed successfully');
      process.exit(0);
    })
    .catch((error) => {
      console.error('Seeding failed:', error);
      process.exit(1);
    });
}

export { seedCatalogue };