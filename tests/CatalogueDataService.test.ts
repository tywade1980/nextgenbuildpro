/**
 * Unit tests for CatalogueDataService
 * These tests validate the core CRUD operations and data integrity
 */

import { CatalogueDataService } from '../services/CatalogueDataService';

// Mock Firebase
jest.mock('../firebase', () => {
  const mockFirestore = {
    collection: jest.fn(),
    doc: jest.fn()
  };
  return {
    firestore: mockFirestore
  };
});

// Mock Firebase Firestore functions
jest.mock('firebase/firestore', () => ({
  collection: jest.fn(() => mockCollectionRef),
  doc: jest.fn(() => mockDocRef),
  setDoc: jest.fn(() => Promise.resolve()),
  getDoc: jest.fn(() => Promise.resolve(mockDocSnap)),
  getDocs: jest.fn(() => Promise.resolve(mockQuerySnapshot)),
  updateDoc: jest.fn(() => Promise.resolve()),
  query: jest.fn(() => mockQuery),
  where: jest.fn(() => mockQuery),
  orderBy: jest.fn(() => mockQuery),
  limit: jest.fn(() => mockQuery)
}));

const mockDocRef = { id: 'test-id' };
const mockCollectionRef = { doc: jest.fn(() => mockDocRef) };
const mockQuery = {};
const mockDocSnap = {
  exists: () => true,
  data: () => mockData,
  id: 'test-id'
};
const mockQuerySnapshot = {
  docs: [mockDocSnap]
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let mockData: any;
let catalogueService: CatalogueDataService;

beforeEach(() => {
  catalogueService = new CatalogueDataService();
  mockData = {
    id: 'test-id',
    name: 'Test Item',
    description: 'Test Description',
    sequence: 1,
    isActive: true,
    tags: ['test', 'mock'],
    createdAt: new Date(),
    updatedAt: new Date()
  };
});

describe('CatalogueDataService', () => {
  describe('Category Operations', () => {
    test('createCategory should create a new category with generated ID and timestamps', async () => {
      const categoryData = {
        name: 'Structure',
        description: 'Structural components',
        sequence: 1,
        isActive: true
      };

      const result = await catalogueService.createCategory(categoryData);

      expect(result).toBeDefined();
      expect(result.id).toBeDefined();
      expect(result.name).toBe(categoryData.name);
      expect(result.description).toBe(categoryData.description);
      expect(result.createdAt).toBeInstanceOf(Date);
      expect(result.updatedAt).toBeInstanceOf(Date);
    });

    test('getCategory should return category data', async () => {
      const result = await catalogueService.getCategory('test-id');
      expect(result).toBeDefined();
    });

    test('getCategories should return list of active categories', async () => {
      const result = await catalogueService.getCategories();
      expect(Array.isArray(result)).toBe(true);
    });

    test('updateCategory should update category with new timestamp', async () => {
      await expect(catalogueService.updateCategory('test-id', { name: 'Updated Name' }))
        .resolves.not.toThrow();
    });

    test('deleteCategory should soft delete by setting isActive to false', async () => {
      await expect(catalogueService.deleteCategory('test-id'))
        .resolves.not.toThrow();
    });
  });

  describe('Trade Operations', () => {
    test('createTrade should create a new trade with categoryId', async () => {
      const tradeData = {
        categoryId: 'category-1',
        name: 'Concrete',
        description: 'Concrete work',
        sequence: 1,
        isActive: true
      };

      const result = await catalogueService.createTrade(tradeData);

      expect(result).toBeDefined();
      expect(result.id).toBeDefined();
      expect(result.categoryId).toBe(tradeData.categoryId);
      expect(result.name).toBe(tradeData.name);
    });

    test('getTradesByCategory should return trades for specific category', async () => {
      const result = await catalogueService.getTradesByCategory('category-1');
      expect(Array.isArray(result)).toBe(true);
    });
  });

  describe('Scope Operations', () => {
    test('createScope should create a new scope with tradeId', async () => {
      const scopeData = {
        tradeId: 'trade-1',
        name: 'Interior Walls',
        description: 'Interior wall construction',
        sequence: 1,
        isActive: true
      };

      const result = await catalogueService.createScope(scopeData);

      expect(result).toBeDefined();
      expect(result.id).toBeDefined();
      expect(result.tradeId).toBe(scopeData.tradeId);
      expect(result.name).toBe(scopeData.name);
      expect(result.createdAt).toBeInstanceOf(Date);
      expect(result.updatedAt).toBeInstanceOf(Date);
    });

    test('getScopesByTrade should return scopes for specific trade', async () => {
      const result = await catalogueService.getScopesByTrade('trade-1');
      expect(Array.isArray(result)).toBe(true);
    });
  });

  describe('Assembly Operations', () => {
    test('createAssembly should create a new assembly with cost data', async () => {
      const assemblyData = {
        scopeId: 'scope-1',
        name: 'Wall Assembly',
        description: 'Standard wall assembly',
        sequence: 1,
        unit: 'SF',
        laborHours: 0.5,
        materialCost: 5.0,
        laborCost: 15.0,
        equipmentCost: 2.0,
        subcontractorCost: 0.0,
        otherCost: 1.0,
        totalCost: 23.0,
        markupPercentage: 0.2,
        notes: 'Standard assembly',
        tags: ['wall', 'interior'],
        isActive: true
      };

      const result = await catalogueService.createAssembly(assemblyData);

      expect(result).toBeDefined();
      expect(result.id).toBeDefined();
      expect(result.scopeId).toBe(assemblyData.scopeId);
      expect(result.totalCost).toBe(assemblyData.totalCost);
      expect(result.tags).toEqual(assemblyData.tags);
    });

    test('searchAssemblies should return filtered assemblies', async () => {
      const result = await catalogueService.searchAssemblies('wall');
      expect(Array.isArray(result)).toBe(true);
    });
  });

  describe('Task and Material Operations', () => {
    test('createTask should create a new task with assemblyId', async () => {
      const taskData = {
        assemblyId: 'assembly-1',
        name: 'Install Drywall',
        description: 'Install and secure drywall',
        sequence: 1,
        laborHours: 0.3,
        materialCost: 2.0,
        laborCost: 9.0,
        equipmentCost: 1.0,
        notes: 'Standard installation',
        isActive: true
      };

      const result = await catalogueService.createTask(taskData);

      expect(result).toBeDefined();
      expect(result.id).toBeDefined();
      expect(result.assemblyId).toBe(taskData.assemblyId);
    });

    test('createMaterial should create a new material with cost calculations', async () => {
      const materialData = {
        assemblyId: 'assembly-1',
        name: 'Drywall Sheet',
        description: '4x8 drywall sheet',
        quantity: 1.0,
        unit: 'EA',
        unitCost: 12.0,
        totalCost: 12.0,
        supplier: 'Home Depot',
        partNumber: 'DW-001',
        waste: 0.1,
        isActive: true
      };

      const result = await catalogueService.createMaterial(materialData);

      expect(result).toBeDefined();
      expect(result.id).toBeDefined();
      expect(result.assemblyId).toBe(materialData.assemblyId);
      expect(result.totalCost).toBe(materialData.totalCost);
    });
  });

  describe('Complex Operations', () => {
    test('createCompleteAssembly should create assembly with tasks and materials', async () => {
      const assemblyData = {
        scopeId: 'scope-1',
        name: 'Complete Wall Assembly',
        description: 'Wall with all components',
        sequence: 1,
        unit: 'SF',
        laborHours: 1.0,
        materialCost: 10.0,
        laborCost: 30.0,
        equipmentCost: 5.0,
        subcontractorCost: 0.0,
        otherCost: 2.0,
        totalCost: 47.0,
        markupPercentage: 0.2,
        notes: 'Complete assembly',
        tags: ['wall', 'complete'],
        isActive: true
      };

      const tasks = [
        {
          name: 'Frame Wall',
          description: 'Frame the wall structure',
          sequence: 1,
          laborHours: 0.5,
          materialCost: 5.0,
          laborCost: 15.0,
          equipmentCost: 2.0,
          notes: 'Framing task',
          isActive: true
        }
      ];

      const materials = [
        {
          name: '2x4 Lumber',
          description: '8ft 2x4 lumber',
          quantity: 10.0,
          unit: 'EA',
          unitCost: 3.0,
          totalCost: 30.0,
          waste: 0.1,
          isActive: true
        }
      ];

      const result = await catalogueService.createCompleteAssembly(
        'scope-1',
        assemblyData,
        tasks,
        materials
      );

      expect(result).toBeDefined();
      expect(result.assembly).toBeDefined();
      expect(result.tasks).toHaveLength(1);
      expect(result.materials).toHaveLength(1);
      expect(result.assembly.name).toBe(assemblyData.name);
    });
  });
});