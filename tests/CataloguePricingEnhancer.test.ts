/**
 * Integration tests for CataloguePricingEnhancer
 *
 * Tests the full pricing update flow: material pricing, labor cost updates,
 * assembly recalculation, and batch scope/trade updates.
 */

import { CataloguePricingEnhancer } from '../services/CataloguePricingEnhancer';
import { CatalogueDataService } from '../services/CatalogueDataService';
import { PricingWebSearchService } from '../services/PricingWebSearchService';
import { Assembly, Material, Task } from '../models/CatalogueSchema';

// ── Firebase mocks ──────────────────────────────────────────────────────────

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

// ── Fixtures ────────────────────────────────────────────────────────────────

function makeMaterial(overrides: Partial<Material> = {}): Material {
  return {
    id: 'mat-1',
    assemblyId: 'asm-1',
    name: '2x4 lumber',
    description: 'Dimensional lumber',
    quantity: 10,
    unit: 'each',
    unitCost: 5,
    totalCost: 50,
    waste: 10,
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    ...overrides,
  };
}

function makeTask(overrides: Partial<Task> = {}): Task {
  return {
    id: 'task-1',
    assemblyId: 'asm-1',
    name: 'Frame wall',
    description: 'Frame a standard wall section',
    sequence: 1,
    laborHours: 4,
    materialCost: 50,
    laborCost: 200,
    equipmentCost: 0,
    notes: '',
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    ...overrides,
  };
}

function makeAssembly(overrides: Partial<Assembly> = {}): Assembly {
  return {
    id: 'asm-1',
    scopeId: 'scope-1',
    name: 'Standard Wall Frame',
    description: 'Standard wall framing assembly',
    sequence: 1,
    unit: 'linear foot',
    laborHours: 4,
    materialCost: 50,
    laborCost: 200,
    equipmentCost: 25,
    subcontractorCost: 0,
    otherCost: 0,
    totalCost: 275,
    markupPercentage: 15,
    notes: '',
    tags: ['framing', 'wall'],
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    ...overrides,
  };
}

function makeDocSnap<T>(data: T | null) {
  return { exists: () => data !== null, data: () => data };
}

function makeQuerySnap<T>(items: T[]) {
  return { docs: items.map((d) => ({ data: () => d })), size: items.length };
}

// ── Tests ───────────────────────────────────────────────────────────────────

describe('CataloguePricingEnhancer', () => {
  let enhancer: CataloguePricingEnhancer;
  let catalogueService: CatalogueDataService;
  let pricingService: PricingWebSearchService;

  beforeEach(() => {
    jest.clearAllMocks();
    catalogueService = new CatalogueDataService();
    pricingService = new PricingWebSearchService();
    enhancer = new CataloguePricingEnhancer(catalogueService, pricingService);
  });

  // ── updateMaterialPricing ─────────────────────────────────────────────────

  describe('updateMaterialPricing', () => {
    it('should update material unit cost and total cost from pricing data', async () => {
      const material = makeMaterial({ name: '2x4 lumber', quantity: 10, unitCost: 5 });
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(material));

      const result = await enhancer.updateMaterialPricing('mat-1');

      expect(result.updated).toBe(true);
      expect(result.newValue).toBeGreaterThan(0);
      expect(result.oldValue).toBe(5);
      expect(mockUpdateDoc).toHaveBeenCalledTimes(1);
      const updateArgs = mockUpdateDoc.mock.calls[0][1];
      expect(updateArgs.unitCost).toBeDefined();
      expect(updateArgs.totalCost).toBeDefined();
    });

    it('should return error result when material does not exist', async () => {
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(null));

      const result = await enhancer.updateMaterialPricing('nonexistent');

      expect(result.updated).toBe(false);
      expect(result.error).toBe('Material not found');
      expect(mockUpdateDoc).not.toHaveBeenCalled();
    });

    it('should return error result when pricing data is unavailable', async () => {
      const material = makeMaterial({ name: 'unknown material xyz abc' });
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(material));

      const result = await enhancer.updateMaterialPricing('mat-1');

      expect(result.updated).toBe(false);
      expect(result.error).toBe('No pricing data found');
    });

    it('should preserve itemType as material', async () => {
      const material = makeMaterial();
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(material));

      const result = await enhancer.updateMaterialPricing('mat-1');

      expect(result.itemType).toBe('material');
      expect(result.itemId).toBe('mat-1');
    });
  });

  // ── updateTaskLaborCost ───────────────────────────────────────────────────

  describe('updateTaskLaborCost', () => {
    it('should update task labor cost based on trade rate and hours', async () => {
      const task = makeTask({ laborHours: 4, laborCost: 200 });
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(task));

      const result = await enhancer.updateTaskLaborCost('task-1', 'carpenter');

      expect(result.updated).toBe(true);
      // carpenter rate is $50/hr, 4 hours = $200
      expect(result.newValue).toBeCloseTo(200);
      expect(result.oldValue).toBe(200);
      expect(mockUpdateDoc).toHaveBeenCalledTimes(1);
    });

    it('should return error when task does not exist', async () => {
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(null));

      const result = await enhancer.updateTaskLaborCost('nonexistent', 'carpenter');

      expect(result.updated).toBe(false);
      expect(result.error).toBe('Task not found');
    });

    it('should return error when trade is unknown', async () => {
      const task = makeTask();
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(task));

      const result = await enhancer.updateTaskLaborCost('task-1', 'unknown trade xyz');

      expect(result.updated).toBe(false);
      expect(result.error).toBe('No labor rate data found');
    });

    it('should handle electrician trade correctly', async () => {
      const task = makeTask({ laborHours: 2 });
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(task));

      const result = await enhancer.updateTaskLaborCost('task-1', 'electrician');

      expect(result.updated).toBe(true);
      // electrician rate is $65/hr, 2 hours = $130
      expect(result.newValue).toBeCloseTo(130);
    });
  });

  // ── updateAssemblyPricing ─────────────────────────────────────────────────

  describe('updateAssemblyPricing', () => {
    it('should update assembly total cost from materials and tasks', async () => {
      const assembly = makeAssembly({ totalCost: 275 });
      const materials = [makeMaterial({ name: '2x4 lumber', quantity: 10, unitCost: 5, totalCost: 50 })];
      const tasks = [makeTask({ laborHours: 4, laborCost: 200 })];

      // assembly fetch
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(assembly));
      // getMaterialsByAssembly
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(materials));
      // getMaterial (inside updateMaterialPricing)
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(materials[0]));
      // getTasksByAssembly
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(tasks));
      // getTask (inside updateTaskLaborCost)
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(tasks[0]));

      const result = await enhancer.updateAssemblyPricing('asm-1', 'carpenter');

      expect(result.updated).toBe(true);
      expect(result.itemType).toBe('assembly');
      expect(result.oldValue).toBe(275);
      expect(result.newValue).toBeGreaterThan(0);
    });

    it('should return error when assembly does not exist', async () => {
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(null));

      const result = await enhancer.updateAssemblyPricing('nonexistent', 'carpenter');

      expect(result.updated).toBe(false);
      expect(result.error).toBe('Assembly not found');
    });

    it('should include equipment, subcontractor, and other costs in total', async () => {
      const assembly = makeAssembly({
        equipmentCost: 100,
        subcontractorCost: 50,
        otherCost: 25,
        totalCost: 375,
      });

      mockGetDoc.mockResolvedValueOnce(makeDocSnap(assembly));
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([])); // no materials
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([])); // no tasks

      const result = await enhancer.updateAssemblyPricing('asm-1', 'carpenter');

      expect(result.updated).toBe(true);
      // No material/labor costs updated, so new total = 0 + 0 + 100 + 50 + 25 = 175
      expect(result.newValue).toBe(175);
    });
  });

  // ── updateScopePricing ────────────────────────────────────────────────────

  describe('updateScopePricing', () => {
    it('should return batch result for a scope with no assemblies', async () => {
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([]));

      const result = await enhancer.updateScopePricing('scope-empty', 'carpenter');

      expect(result.totalItems).toBe(0);
      expect(result.updatedItems).toBe(0);
      expect(result.failedItems).toBe(0);
      expect(result.summary.totalOldCost).toBe(0);
      expect(result.summary.totalNewCost).toBe(0);
      expect(result.summary.percentChange).toBe(0);
    });

    it('should aggregate results across multiple assemblies', async () => {
      const assemblies = [
        makeAssembly({ id: 'asm-1', totalCost: 275 }),
        makeAssembly({ id: 'asm-2', totalCost: 400 }),
      ];

      // getAssembliesByScope
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap(assemblies));

      // For asm-1: assembly fetch, materials, tasks
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(assemblies[0]));
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([]));
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([]));

      // For asm-2: assembly fetch, materials, tasks
      mockGetDoc.mockResolvedValueOnce(makeDocSnap(assemblies[1]));
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([]));
      mockGetDocs.mockResolvedValueOnce(makeQuerySnap([]));

      const result = await enhancer.updateScopePricing('scope-1', 'carpenter');

      expect(result.totalItems).toBe(2);
      expect(result.results).toHaveLength(2);
    });
  });

  // ── generatePricingReport ─────────────────────────────────────────────────

  describe('generatePricingReport', () => {
    it('should generate a report with summary section', () => {
      const batchResult = {
        totalItems: 5,
        updatedItems: 4,
        failedItems: 1,
        results: [
          {
            itemId: 'mat-fail',
            itemType: 'material' as const,
            oldValue: 0,
            newValue: 0,
            source: '',
            updated: false,
            error: 'Material not found',
          },
        ],
        summary: {
          totalOldCost: 1000,
          totalNewCost: 1050,
          percentChange: 5,
        },
      };

      const report = enhancer.generatePricingReport(batchResult);

      expect(report).toContain('CATALOGUE PRICING UPDATE REPORT');
      expect(report).toContain('Total Items Processed: 5');
      expect(report).toContain('Successfully Updated: 4');
      expect(report).toContain('Failed Updates: 1');
      expect(report).toContain('$1000.00');
      expect(report).toContain('$1050.00');
      expect(report).toContain('+5.00%');
      expect(report).toContain('FAILED UPDATES');
      expect(report).toContain('mat-fail');
    });

    it('should omit FAILED UPDATES section when all items succeed', () => {
      const batchResult = {
        totalItems: 3,
        updatedItems: 3,
        failedItems: 0,
        results: [],
        summary: { totalOldCost: 500, totalNewCost: 480, percentChange: -4 },
      };

      const report = enhancer.generatePricingReport(batchResult);

      expect(report).not.toContain('FAILED UPDATES');
      expect(report).toContain('-4.00%');
    });
  });
});
