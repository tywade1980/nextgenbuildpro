/**
 * Unit tests for CatalogueDataService
 *
 * These tests mock Firebase Firestore to validate the service logic
 * without requiring a live database connection.
 */

import { CatalogueDataService } from '../services/CatalogueDataService';
import {
  Category,
  Trade,
  Assembly,
} from '../models/CatalogueSchema';

// ── Firebase mocks ──────────────────────────────────────────────────
// We mock the entire firebase/firestore module so the service never
// touches a real database.

const mockSetDoc = jest.fn().mockResolvedValue(undefined);
const mockGetDoc = jest.fn();
const mockGetDocs = jest.fn();
const mockUpdateDoc = jest.fn().mockResolvedValue(undefined);

jest.mock('firebase/firestore', () => ({
  collection: jest.fn((_db: unknown, name: string) => ({ _name: name })),
  doc: jest.fn((_ref: unknown, id: string) => ({ _id: id })),
  setDoc: (...args: unknown[]) => mockSetDoc(...args),
  getDoc: (...args: unknown[]) => mockGetDoc(...args),
  getDocs: (...args: unknown[]) => mockGetDocs(...args),
  updateDoc: (...args: unknown[]) => mockUpdateDoc(...args),
  query: jest.fn((...args: unknown[]) => args),
  where: jest.fn((...args: unknown[]) => args),
  orderBy: jest.fn((...args: unknown[]) => args),
  limit: jest.fn((...args: unknown[]) => args),
}));

jest.mock('../firebase', () => ({
  firestore: {},
}));

// ── Helpers ─────────────────────────────────────────────────────────

function makeDocSnap<T>(data: T | null) {
  return {
    exists: () => data !== null,
    data: () => data,
  };
}

function makeQuerySnap<T>(items: T[]) {
  return {
    docs: items.map((d) => ({ data: () => d })),
    size: items.length,
  };
}

// ── Tests ───────────────────────────────────────────────────────────

describe('CatalogueDataService', () => {
  let service: CatalogueDataService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new CatalogueDataService();
  });

  // ─── Create operations ───────────────────────────────────────────

  describe('createCategory', () => {
    it('should create a category with generated id and timestamps', async () => {
      const input = {
        name: 'Interior Finishes',
        description: 'Interior finishing and trim work',
        sequence: 8,
        isActive: true,
      };

      const result = await service.createCategory(input);

      expect(result.id).toBeDefined();
      expect(result.name).toBe(input.name);
      expect(result.description).toBe(input.description);
      expect(result.sequence).toBe(input.sequence);
      expect(result.isActive).toBe(true);
      expect(result.createdAt).toBeInstanceOf(Date);
      expect(result.updatedAt).toBeInstanceOf(Date);
      expect(mockSetDoc).toHaveBeenCalledTimes(1);
    });
  });

  describe('createTrade', () => {
    it('should create a trade linked to a category', async () => {
      const input = {
        categoryId: 'cat-1',
        name: 'Finish Carpentry',
        description: 'Fine woodwork and detailed carpentry',
        sequence: 1,
        isActive: true,
      };

      const result = await service.createTrade(input);

      expect(result.id).toBeDefined();
      expect(result.categoryId).toBe('cat-1');
      expect(result.name).toBe(input.name);
      expect(mockSetDoc).toHaveBeenCalledTimes(1);
    });
  });

  describe('createScope', () => {
    it('should create a scope linked to a trade', async () => {
      const input = {
        tradeId: 'trade-1',
        name: 'Stair Construction',
        description: 'Building and finishing interior staircases',
        sequence: 1,
        isActive: true,
      };

      const result = await service.createScope(input);

      expect(result.id).toBeDefined();
      expect(result.tradeId).toBe('trade-1');
      expect(result.name).toBe(input.name);
      expect(mockSetDoc).toHaveBeenCalledTimes(1);
    });
  });

  describe('createAssembly', () => {
    it('should create an assembly with cost fields', async () => {
      const input = {
        scopeId: 'scope-1',
        name: 'Standard Staircase',
        description: 'Standard wooden staircase',
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
        notes: 'Oak treads and painted risers',
        tags: ['stair', 'wood', 'interior'],
        isActive: true,
      };

      const result = await service.createAssembly(input);

      expect(result.id).toBeDefined();
      expect(result.totalCost).toBe(2650);
      expect(result.tags).toContain('stair');
      expect(mockSetDoc).toHaveBeenCalledTimes(1);
    });
  });

  describe('createTask', () => {
    it('should create a task linked to an assembly', async () => {
      const input = {
        assemblyId: 'asm-1',
        name: 'Cut stringers',
        description: 'Cut and prepare stair stringers',
        sequence: 2,
        laborHours: 4,
        materialCost: 180,
        laborCost: 200,
        equipmentCost: 50,
        notes: 'Use 2x12 pressure treated lumber',
        isActive: true,
      };

      const result = await service.createTask(input);

      expect(result.id).toBeDefined();
      expect(result.assemblyId).toBe('asm-1');
      expect(result.laborHours).toBe(4);
      expect(mockSetDoc).toHaveBeenCalledTimes(1);
    });
  });

  describe('createMaterial', () => {
    it('should create a material with cost calculations', async () => {
      const input = {
        assemblyId: 'asm-1',
        name: 'Stair Stringers',
        description: '2x12 pressure treated lumber',
        quantity: 3,
        unit: 'each',
        unitCost: 60,
        totalCost: 180,
        waste: 10,
        isActive: true,
      };

      const result = await service.createMaterial(input);

      expect(result.id).toBeDefined();
      expect(result.assemblyId).toBe('asm-1');
      expect(result.unitCost).toBe(60);
      expect(result.totalCost).toBe(180);
      expect(mockSetDoc).toHaveBeenCalledTimes(1);
    });
  });

  // ─── Read operations ─────────────────────────────────────────────

  describe('getCategory', () => {
    it('should return a category when it exists', async () => {
      const cat: Category = {
        id: 'cat-1',
        name: 'Foundation',
        description: 'Structural foundation systems',
        sequence: 2,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      mockGetDoc.mockResolvedValueOnce(makeDocSnap(cat));

      const result = await service.getCategory('cat-1');

      expect(result).not.toBeNull();
      expect(result?.name).toBe('Foundation');
    });

    it('should return null when category does not exist', async () => {
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(null));

      const result = await service.getCategory('nonexistent');

      expect(result).toBeNull();
    });
  });

  describe('getTrade', () => {
    it('should return a trade when it exists', async () => {
      const trade: Trade = {
        id: 'trade-1',
        categoryId: 'cat-1',
        name: 'Finish Carpentry',
        description: 'Fine woodwork',
        sequence: 1,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      mockGetDoc.mockResolvedValueOnce(makeDocSnap(trade));

      const result = await service.getTrade('trade-1');

      expect(result).not.toBeNull();
      expect(result?.name).toBe('Finish Carpentry');
    });
  });

  describe('getAssembly', () => {
    it('should return null when assembly does not exist', async () => {
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(null));

      const result = await service.getAssembly('nonexistent');

      expect(result).toBeNull();
    });
  });

  // ─── List operations ─────────────────────────────────────────────

  describe('getCategories', () => {
    it('should return all active categories ordered by sequence', async () => {
      const cats: Category[] = [
        {
          id: 'cat-1',
          name: 'Foundation',
          description: '',
          sequence: 2,
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
        {
          id: 'cat-2',
          name: 'Interior Finishes',
          description: '',
          sequence: 8,
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      ];

      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(cats));

      const result = await service.getCategories();

      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('Foundation');
      expect(result[1].name).toBe('Interior Finishes');
    });
  });

  describe('getTradesByCategory', () => {
    it('should return trades for a given category', async () => {
      const trades: Trade[] = [
        {
          id: 'trade-1',
          categoryId: 'cat-8',
          name: 'Finish Carpentry',
          description: '',
          sequence: 1,
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      ];

      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(trades));

      const result = await service.getTradesByCategory('cat-8');

      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('Finish Carpentry');
    });
  });

  describe('getAssembliesByScope', () => {
    it('should return assemblies for a given scope', async () => {
      const assemblies: Assembly[] = [
        {
          id: 'asm-1',
          scopeId: 'scope-1',
          name: 'Standard Staircase',
          description: '',
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
          notes: '',
          tags: ['stair'],
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      ];

      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(assemblies));

      const result = await service.getAssembliesByScope('scope-1');

      expect(result).toHaveLength(1);
      expect(result[0].totalCost).toBe(2650);
    });
  });

  // ─── Update operations ───────────────────────────────────────────

  describe('updateCategory', () => {
    it('should call updateDoc with new data and updatedAt', async () => {
      await service.updateCategory('cat-1', { name: 'Updated Name' });

      expect(mockUpdateDoc).toHaveBeenCalledTimes(1);
      const callArgs = mockUpdateDoc.mock.calls[0][1];
      expect(callArgs.name).toBe('Updated Name');
      expect(callArgs.updatedAt).toBeInstanceOf(Date);
    });
  });

  describe('updateAssembly', () => {
    it('should update assembly cost fields', async () => {
      await service.updateAssembly('asm-1', {
        materialCost: 1500,
        totalCost: 3000,
      });

      expect(mockUpdateDoc).toHaveBeenCalledTimes(1);
      const callArgs = mockUpdateDoc.mock.calls[0][1];
      expect(callArgs.materialCost).toBe(1500);
      expect(callArgs.totalCost).toBe(3000);
    });
  });

  // ─── Delete operations (soft delete) ─────────────────────────────

  describe('deleteCategory', () => {
    it('should soft-delete by setting isActive to false', async () => {
      await service.deleteCategory('cat-1');

      expect(mockUpdateDoc).toHaveBeenCalledTimes(1);
      const callArgs = mockUpdateDoc.mock.calls[0][1];
      expect(callArgs.isActive).toBe(false);
    });
  });

  describe('deleteAssembly', () => {
    it('should soft-delete assembly', async () => {
      await service.deleteAssembly('asm-1');

      expect(mockUpdateDoc).toHaveBeenCalledTimes(1);
      const callArgs = mockUpdateDoc.mock.calls[0][1];
      expect(callArgs.isActive).toBe(false);
    });
  });

  // ─── Search operations ───────────────────────────────────────────

  describe('searchAssemblies', () => {
    it('should filter assemblies by name', async () => {
      const assemblies: Assembly[] = [
        {
          id: 'asm-1',
          scopeId: 'scope-1',
          name: 'Standard Staircase',
          description: 'Wooden staircase',
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
          notes: '',
          tags: ['stair'],
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
        {
          id: 'asm-2',
          scopeId: 'scope-2',
          name: 'Toilet Installation',
          description: 'Standard toilet install',
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
          notes: '',
          tags: ['plumbing', 'bathroom'],
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      ];

      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(assemblies));

      const result = await service.searchAssemblies('staircase');

      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('Standard Staircase');
    });

    it('should filter assemblies by tag', async () => {
      const assemblies: Assembly[] = [
        {
          id: 'asm-2',
          scopeId: 'scope-2',
          name: 'Toilet Installation',
          description: 'Standard toilet install',
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
          notes: '',
          tags: ['plumbing', 'bathroom'],
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      ];

      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(assemblies));

      const result = await service.searchAssemblies('plumbing');

      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('Toilet Installation');
    });

    it('should return empty array when no matches', async () => {
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([]));

      const result = await service.searchAssemblies('nonexistent');

      expect(result).toHaveLength(0);
    });
  });

  // ─── Batch / composite operations ────────────────────────────────

  describe('createCompleteAssembly', () => {
    it('should create assembly with tasks and materials', async () => {
      const assemblyData = {
        scopeId: 'scope-1',
        name: 'Test Assembly',
        description: 'Test',
        sequence: 1,
        unit: 'each',
        laborHours: 2,
        materialCost: 100,
        laborCost: 100,
        equipmentCost: 0,
        subcontractorCost: 0,
        otherCost: 0,
        totalCost: 200,
        markupPercentage: 15,
        notes: '',
        tags: ['test'],
        isActive: true,
      };

      const tasks = [
        {
          name: 'Task 1',
          description: 'First task',
          sequence: 1,
          laborHours: 1,
          materialCost: 50,
          laborCost: 50,
          equipmentCost: 0,
          notes: '',
          isActive: true,
        },
      ];

      const materials = [
        {
          name: 'Material 1',
          description: 'First material',
          quantity: 2,
          unit: 'each',
          unitCost: 25,
          totalCost: 50,
          waste: 5,
          isActive: true,
        },
      ];

      const result = await service.createCompleteAssembly(
        'scope-1',
        assemblyData,
        tasks,
        materials
      );

      expect(result.assembly).toBeDefined();
      expect(result.assembly.name).toBe('Test Assembly');
      expect(result.tasks).toHaveLength(1);
      expect(result.tasks[0].name).toBe('Task 1');
      expect(result.materials).toHaveLength(1);
      expect(result.materials[0].name).toBe('Material 1');
      // 1 assembly + 1 task + 1 material = 3 setDoc calls
      expect(mockSetDoc).toHaveBeenCalledTimes(3);
    });

    it('should link tasks and materials to the created assembly', async () => {
      const assemblyData = {
        scopeId: 'scope-1',
        name: 'Linked Assembly',
        description: '',
        sequence: 1,
        unit: 'each',
        laborHours: 1,
        materialCost: 50,
        laborCost: 50,
        equipmentCost: 0,
        subcontractorCost: 0,
        otherCost: 0,
        totalCost: 100,
        markupPercentage: 10,
        notes: '',
        tags: [],
        isActive: true,
      };

      const tasks = [
        {
          name: 'Linked Task',
          description: '',
          sequence: 1,
          laborHours: 1,
          materialCost: 0,
          laborCost: 50,
          equipmentCost: 0,
          notes: '',
          isActive: true,
        },
      ];

      const materials = [
        {
          name: 'Linked Material',
          description: '',
          quantity: 1,
          unit: 'each',
          unitCost: 50,
          totalCost: 50,
          waste: 0,
          isActive: true,
        },
      ];

      const result = await service.createCompleteAssembly(
        'scope-1',
        assemblyData,
        tasks,
        materials
      );

      // Tasks and materials should reference the created assembly's id
      expect(result.tasks[0].assemblyId).toBe(result.assembly.id);
      expect(result.materials[0].assemblyId).toBe(result.assembly.id);
    });
  });
});
