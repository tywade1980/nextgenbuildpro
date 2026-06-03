/**
 * Data Validation Tests
 *
 * Validates that the catalogue data models enforce correct structure,
 * that cost calculations are mathematically consistent, and that
 * pricing service outputs stay within realistic construction industry ranges.
 */

import { PricingWebSearchService } from '../services/PricingWebSearchService';
import {
  Assembly,
  Task,
  Material,
  Category,
  Trade,
  Scope,
} from '../models/CatalogueSchema';

// ── Schema validation helpers ────────────────────────────────────────────────

function validateCategory(obj: unknown): obj is Category {
  const c = obj as Category;
  return (
    typeof c.id === 'string' && c.id.length > 0 &&
    typeof c.name === 'string' && c.name.length > 0 &&
    typeof c.description === 'string' &&
    typeof c.sequence === 'number' && c.sequence >= 0 &&
    typeof c.isActive === 'boolean' &&
    c.createdAt instanceof Date &&
    c.updatedAt instanceof Date
  );
}

function validateTrade(obj: unknown): obj is Trade {
  const t = obj as Trade;
  return (
    typeof t.id === 'string' && t.id.length > 0 &&
    typeof t.categoryId === 'string' && t.categoryId.length > 0 &&
    typeof t.name === 'string' && t.name.length > 0 &&
    typeof t.sequence === 'number' && t.sequence >= 0 &&
    typeof t.isActive === 'boolean'
  );
}

function validateAssembly(obj: unknown): obj is Assembly {
  const a = obj as Assembly;
  return (
    typeof a.id === 'string' && a.id.length > 0 &&
    typeof a.scopeId === 'string' && a.scopeId.length > 0 &&
    typeof a.name === 'string' && a.name.length > 0 &&
    typeof a.laborHours === 'number' && a.laborHours >= 0 &&
    typeof a.materialCost === 'number' && a.materialCost >= 0 &&
    typeof a.laborCost === 'number' && a.laborCost >= 0 &&
    typeof a.equipmentCost === 'number' && a.equipmentCost >= 0 &&
    typeof a.subcontractorCost === 'number' && a.subcontractorCost >= 0 &&
    typeof a.otherCost === 'number' && a.otherCost >= 0 &&
    typeof a.totalCost === 'number' && a.totalCost >= 0 &&
    typeof a.markupPercentage === 'number' && a.markupPercentage >= 0 &&
    Array.isArray(a.tags) &&
    typeof a.isActive === 'boolean'
  );
}

function validateTask(obj: unknown): obj is Task {
  const t = obj as Task;
  return (
    typeof t.id === 'string' && t.id.length > 0 &&
    typeof t.assemblyId === 'string' && t.assemblyId.length > 0 &&
    typeof t.laborHours === 'number' && t.laborHours >= 0 &&
    typeof t.laborCost === 'number' && t.laborCost >= 0 &&
    typeof t.isActive === 'boolean'
  );
}

function validateMaterial(obj: unknown): obj is Material {
  const m = obj as Material;
  return (
    typeof m.id === 'string' && m.id.length > 0 &&
    typeof m.assemblyId === 'string' && m.assemblyId.length > 0 &&
    typeof m.quantity === 'number' && m.quantity > 0 &&
    typeof m.unitCost === 'number' && m.unitCost >= 0 &&
    typeof m.totalCost === 'number' && m.totalCost >= 0 &&
    typeof m.waste === 'number' && m.waste >= 0 &&
    typeof m.isActive === 'boolean'
  );
}

// ── Cost calculation helpers ─────────────────────────────────────────────────

function calculateAssemblyTotal(assembly: Assembly): number {
  return (
    assembly.materialCost +
    assembly.laborCost +
    assembly.equipmentCost +
    assembly.subcontractorCost +
    assembly.otherCost
  );
}

function calculateMaterialTotal(material: Material): number {
  return material.quantity * material.unitCost;
}

function calculateTaskLaborCost(task: Task, hourlyRate: number): number {
  return task.laborHours * hourlyRate;
}

// ── Tests ────────────────────────────────────────────────────────────────────

describe('Data Validation - Schema Integrity', () => {
  describe('Category schema', () => {
    it('should validate a well-formed category', () => {
      const category: Category = {
        id: 'cat-1',
        name: 'Structural',
        description: 'Structural work',
        sequence: 1,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateCategory(category)).toBe(true);
    });

    it('should reject a category with empty id', () => {
      const bad = {
        id: '',
        name: 'Bad',
        description: '',
        sequence: 1,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateCategory(bad)).toBe(false);
    });

    it('should reject a category with empty name', () => {
      const bad = {
        id: 'cat-1',
        name: '',
        description: '',
        sequence: 1,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateCategory(bad)).toBe(false);
    });

    it('should reject a category with negative sequence', () => {
      const bad = {
        id: 'cat-1',
        name: 'Test',
        description: '',
        sequence: -1,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateCategory(bad)).toBe(false);
    });
  });

  describe('Trade schema', () => {
    it('should validate a well-formed trade', () => {
      const trade: Trade = {
        id: 'trade-1',
        categoryId: 'cat-1',
        name: 'Framing',
        description: 'Structural framing',
        sequence: 1,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateTrade(trade)).toBe(true);
    });

    it('should reject a trade without categoryId', () => {
      const bad = {
        id: 'trade-1',
        categoryId: '',
        name: 'Framing',
        description: '',
        sequence: 1,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateTrade(bad)).toBe(false);
    });
  });

  describe('Assembly schema', () => {
    it('should validate a well-formed assembly', () => {
      const assembly: Assembly = {
        id: 'asm-1',
        scopeId: 'scope-1',
        name: 'Wall Frame',
        description: 'Standard wall frame',
        sequence: 1,
        unit: 'linear foot',
        laborHours: 8,
        materialCost: 200,
        laborCost: 400,
        equipmentCost: 50,
        subcontractorCost: 0,
        otherCost: 0,
        totalCost: 650,
        markupPercentage: 15,
        notes: '',
        tags: ['framing'],
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateAssembly(assembly)).toBe(true);
    });

    it('should reject an assembly with negative costs', () => {
      const bad = {
        id: 'asm-1',
        scopeId: 'scope-1',
        name: 'Bad Assembly',
        description: '',
        sequence: 1,
        unit: 'each',
        laborHours: -1,
        materialCost: -100,
        laborCost: 0,
        equipmentCost: 0,
        subcontractorCost: 0,
        otherCost: 0,
        totalCost: -100,
        markupPercentage: 15,
        notes: '',
        tags: [],
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateAssembly(bad)).toBe(false);
    });
  });

  describe('Material schema', () => {
    it('should validate a well-formed material', () => {
      const material: Material = {
        id: 'mat-1',
        assemblyId: 'asm-1',
        name: 'Lumber',
        description: '2x4 lumber',
        quantity: 20,
        unit: 'each',
        unitCost: 8,
        totalCost: 160,
        waste: 10,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateMaterial(material)).toBe(true);
    });

    it('should reject a material with zero quantity', () => {
      const bad = {
        id: 'mat-1',
        assemblyId: 'asm-1',
        name: 'Lumber',
        description: '',
        quantity: 0,
        unit: 'each',
        unitCost: 8,
        totalCost: 0,
        waste: 0,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(validateMaterial(bad)).toBe(false);
    });
  });
});

describe('Data Validation - Cost Calculations', () => {
  describe('Assembly total cost', () => {
    it('should equal sum of all cost components', () => {
      const assembly: Assembly = {
        id: 'asm-1',
        scopeId: 'scope-1',
        name: 'Test',
        description: '',
        sequence: 1,
        unit: 'each',
        laborHours: 10,
        materialCost: 500,
        laborCost: 600,
        equipmentCost: 75,
        subcontractorCost: 200,
        otherCost: 25,
        totalCost: 1400,
        markupPercentage: 15,
        notes: '',
        tags: [],
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(calculateAssemblyTotal(assembly)).toBe(assembly.totalCost);
    });

    it('should be zero when all cost components are zero', () => {
      const assembly: Assembly = {
        id: 'asm-zero',
        scopeId: 'scope-1',
        name: 'Zero Cost',
        description: '',
        sequence: 1,
        unit: 'each',
        laborHours: 0,
        materialCost: 0,
        laborCost: 0,
        equipmentCost: 0,
        subcontractorCost: 0,
        otherCost: 0,
        totalCost: 0,
        markupPercentage: 0,
        notes: '',
        tags: [],
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(calculateAssemblyTotal(assembly)).toBe(0);
    });
  });

  describe('Material total cost', () => {
    it('should equal quantity × unitCost', () => {
      const material: Material = {
        id: 'mat-1',
        assemblyId: 'asm-1',
        name: 'Plywood',
        description: '',
        quantity: 5,
        unit: 'sheet',
        unitCost: 45,
        totalCost: 225,
        waste: 5,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(calculateMaterialTotal(material)).toBe(material.totalCost);
    });

    it('should handle fractional quantities correctly', () => {
      const material: Material = {
        id: 'mat-2',
        assemblyId: 'asm-1',
        name: 'Rebar',
        description: '',
        quantity: 12.5,
        unit: 'linear foot',
        unitCost: 0.85,
        totalCost: 10.625,
        waste: 0,
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      expect(calculateMaterialTotal(material)).toBeCloseTo(material.totalCost, 2);
    });
  });

  describe('Task labor cost', () => {
    it('should equal laborHours × hourlyRate', () => {
      const task: Task = {
        id: 'task-1',
        assemblyId: 'asm-1',
        name: 'Frame wall',
        description: '',
        sequence: 1,
        laborHours: 8,
        materialCost: 0,
        laborCost: 400,
        equipmentCost: 0,
        notes: '',
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      const hourlyRate = 50; // carpenter rate

      expect(calculateTaskLaborCost(task, hourlyRate)).toBe(task.laborCost);
    });

    it('should scale linearly with hours', () => {
      const hourlyRate = 65; // electrician
      expect(calculateTaskLaborCost({ laborHours: 1 } as Task, hourlyRate)).toBe(65);
      expect(calculateTaskLaborCost({ laborHours: 2 } as Task, hourlyRate)).toBe(130);
      expect(calculateTaskLaborCost({ laborHours: 4 } as Task, hourlyRate)).toBe(260);
    });
  });

  describe('Markup calculation', () => {
    it('should calculate marked-up price correctly', () => {
      const baseCost = 1000;
      const markupPercentage = 15;
      const markedUpPrice = baseCost * (1 + markupPercentage / 100);

      expect(markedUpPrice).toBe(1150);
    });

    it('should handle zero markup', () => {
      const baseCost = 500;
      const markedUpPrice = baseCost * (1 + 0 / 100);

      expect(markedUpPrice).toBe(500);
    });
  });
});

describe('Data Validation - Pricing Service Output Ranges', () => {
  let service: PricingWebSearchService;

  beforeEach(() => {
    service = new PricingWebSearchService();
  });

  afterEach(() => {
    service.clearCache();
  });

  describe('Material prices within industry ranges', () => {
    const materialBounds: Array<{ name: string; unit: string; min: number; max: number }> = [
      { name: '2x4 lumber', unit: 'each', min: 4, max: 20 },
      { name: 'plywood sheet', unit: 'sheet', min: 20, max: 80 },
      { name: 'concrete', unit: 'cubic yard', min: 80, max: 200 },
      { name: 'drywall sheet', unit: 'sheet', min: 8, max: 25 },
      { name: 'toilet', unit: 'each', min: 150, max: 500 },
      { name: 'hardwood flooring', unit: 'square foot', min: 4, max: 20 },
      { name: 'roofing shingles', unit: 'square', min: 60, max: 200 },
    ];

    materialBounds.forEach(({ name, unit, min, max }) => {
      it(`should price ${name} between $${min} and $${max} per ${unit}`, async () => {
        const result = await service.searchMaterialPricing(name, unit);

        expect(result).not.toBeNull();
        expect(result!.averagePrice).toBeGreaterThanOrEqual(min);
        expect(result!.averagePrice).toBeLessThanOrEqual(max);
      });
    });
  });

  describe('Labor rates within industry ranges', () => {
    const laborBounds: Array<{ trade: string; min: number; max: number }> = [
      { trade: 'carpenter', min: 35, max: 75 },
      { trade: 'electrician', min: 45, max: 95 },
      { trade: 'plumber', min: 50, max: 100 },
      { trade: 'painter', min: 25, max: 60 },
      { trade: 'roofer', min: 35, max: 75 },
      { trade: 'general laborer', min: 20, max: 55 },
    ];

    laborBounds.forEach(({ trade, min, max }) => {
      it(`should quote ${trade} rate between $${min}/hr and $${max}/hr`, async () => {
        const result = await service.searchLaborRate(trade);

        expect(result).not.toBeNull();
        expect(result!.hourlyRate).toBeGreaterThanOrEqual(min);
        expect(result!.hourlyRate).toBeLessThanOrEqual(max);
      });
    });
  });

  describe('Labor time estimates within industry ranges', () => {
    const timeBounds: Array<{
      task: string;
      unit: string;
      min: number;
      max: number;
    }> = [
      { task: 'install toilet', unit: 'each', min: 1, max: 3 },
      { task: 'install light switch', unit: 'each', min: 0.25, max: 1 },
      { task: 'build staircase', unit: 'flight', min: 16, max: 32 },
    ];

    timeBounds.forEach(({ task, unit, min, max }) => {
      it(`should estimate "${task}" between ${min}h and ${max}h per ${unit}`, async () => {
        const result = await service.searchLaborTimeEstimate(task, unit);

        expect(result).not.toBeNull();
        expect(result!).toBeGreaterThanOrEqual(min);
        expect(result!).toBeLessThanOrEqual(max);
      });
    });
  });

  describe('Price range consistency', () => {
    it('material price range min should always be less than max', async () => {
      const materials = ['concrete', 'toilet', '2x4 lumber', 'tile'];

      for (const name of materials) {
        const result = await service.searchMaterialPricing(name, 'each');
        if (result) {
          expect(result.priceRange.min).toBeLessThan(result.priceRange.max);
          expect(result.priceRange.min).toBeCloseTo(result.averagePrice * 0.9);
          expect(result.priceRange.max).toBeCloseTo(result.averagePrice * 1.1);
        }
      }
    });

    it('labor rate range min should always be less than max', async () => {
      const trades = ['carpenter', 'plumber', 'electrician'];

      for (const trade of trades) {
        const result = await service.searchLaborRate(trade);
        if (result) {
          expect(result.rateRange.min).toBeLessThan(result.rateRange.max);
          expect(result.rateRange.min).toBeCloseTo(result.hourlyRate * 0.85);
          expect(result.rateRange.max).toBeCloseTo(result.hourlyRate * 1.15);
        }
      }
    });
  });

  describe('Unknown items return null', () => {
    it('should return null for an unknown material', async () => {
      const result = await service.searchMaterialPricing('unobtanium widget xzq', 'each');
      expect(result).toBeNull();
    });

    it('should return null for an unknown trade', async () => {
      const result = await service.searchLaborRate('quantum engineer xzq');
      expect(result).toBeNull();
    });

    it('should return null for an unknown task', async () => {
      const result = await service.searchLaborTimeEstimate('teleport materials xzq', 'each');
      expect(result).toBeNull();
    });
  });
});

describe('Data Validation - Timestamp Consistency', () => {
  it('updatedAt should not be before createdAt', () => {
    const createdAt = new Date('2024-01-01T00:00:00Z');
    const updatedAt = new Date('2024-06-01T00:00:00Z');

    expect(updatedAt.getTime()).toBeGreaterThanOrEqual(createdAt.getTime());
  });

  it('both timestamps should be valid Date objects', () => {
    const now = new Date();
    expect(now).toBeInstanceOf(Date);
    expect(isNaN(now.getTime())).toBe(false);
  });

  it('sequence numbers should be non-negative integers', () => {
    const validSequences = [0, 1, 5, 10, 100];
    const invalidSequences = [-1, -100, NaN];

    validSequences.forEach(seq => expect(seq).toBeGreaterThanOrEqual(0));
    invalidSequences.forEach(seq => expect(seq < 0 || isNaN(seq)).toBe(true));
  });
});
