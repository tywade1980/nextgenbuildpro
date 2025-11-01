/**
 * Unit tests for PricingWebSearchService
 */

import { PricingWebSearchService } from '../services/PricingWebSearchService';

describe('PricingWebSearchService', () => {
  let service: PricingWebSearchService;

  beforeEach(() => {
    service = new PricingWebSearchService();
  });

  afterEach(() => {
    service.clearCache();
  });

  describe('searchMaterialPricing', () => {
    it('should return material pricing data for known materials', async () => {
      const result = await service.searchMaterialPricing('2x4 lumber', 'each');
      
      expect(result).not.toBeNull();
      expect(result?.name).toBeTruthy();
      expect(result?.averagePrice).toBeGreaterThan(0);
      expect(result?.unit).toBe('each');
      expect(result?.sources).toHaveLength(1);
    });

    it('should return null for unknown materials', async () => {
      const result = await service.searchMaterialPricing('unknown material xyz', 'each');
      
      // The service returns null when no pricing data is found
      expect(result).toBeNull();
    });

    it('should include price range', async () => {
      const result = await service.searchMaterialPricing('concrete', 'cubic yard');
      
      expect(result).not.toBeNull();
      if (result) {
        expect(result.priceRange).toBeDefined();
        expect(result.priceRange.min).toBeLessThan(result.priceRange.max);
        expect(result.priceRange.min).toBeCloseTo(result.averagePrice * 0.9);
        expect(result.priceRange.max).toBeCloseTo(result.averagePrice * 1.1);
      }
    });

    it('should cache results', async () => {
      const result1 = await service.searchMaterialPricing('toilet', 'each');
      const result2 = await service.searchMaterialPricing('toilet', 'each');
      
      expect(result1).toEqual(result2);
      
      const stats = service.getCacheStats();
      expect(stats.size).toBeGreaterThan(0);
    });
  });

  describe('searchLaborRate', () => {
    it('should return labor rate data for known trades', async () => {
      const result = await service.searchLaborRate('carpenter');
      
      expect(result).not.toBeNull();
      expect(result?.trade).toBe('carpenter');
      expect(result?.hourlyRate).toBeGreaterThan(0);
      expect(result?.sources).toHaveLength(1);
    });

    it('should return null for unknown trades', async () => {
      const result = await service.searchLaborRate('unknown trade xyz');
      
      expect(result).toBeNull();
    });

    it('should include rate range', async () => {
      const result = await service.searchLaborRate('electrician');
      
      expect(result).not.toBeNull();
      if (result) {
        expect(result.rateRange).toBeDefined();
        expect(result.rateRange.min).toBeLessThan(result.rateRange.max);
        expect(result.rateRange.min).toBeCloseTo(result.hourlyRate * 0.85);
        expect(result.rateRange.max).toBeCloseTo(result.hourlyRate * 1.15);
      }
    });

    it('should accept location parameter', async () => {
      const result = await service.searchLaborRate('plumber', 'California');
      
      expect(result).not.toBeNull();
      expect(result?.location).toBe('California');
    });

    it('should cache results', async () => {
      const result1 = await service.searchLaborRate('plumber', 'USA');
      const result2 = await service.searchLaborRate('plumber', 'USA');
      
      expect(result1).toEqual(result2);
      
      const stats = service.getCacheStats();
      expect(stats.size).toBeGreaterThan(0);
    });
  });

  describe('searchLaborTimeEstimate', () => {
    it('should return labor hours for known tasks', async () => {
      const result = await service.searchLaborTimeEstimate('install toilet', 'each');
      
      expect(result).not.toBeNull();
      expect(result).toBeGreaterThan(0);
    });

    it('should return null for unknown tasks', async () => {
      const result = await service.searchLaborTimeEstimate('unknown task xyz', 'each');
      
      expect(result).toBeNull();
    });

    it('should cache results', async () => {
      const result1 = await service.searchLaborTimeEstimate('install toilet', 'each');
      const result2 = await service.searchLaborTimeEstimate('install toilet', 'each');
      
      expect(result1).toBe(result2);
      
      const stats = service.getCacheStats();
      expect(stats.size).toBeGreaterThan(0);
    });
  });

  describe('cache management', () => {
    it('should track cache size', async () => {
      await service.searchMaterialPricing('2x4 lumber', 'each');
      await service.searchLaborRate('carpenter');
      
      const stats = service.getCacheStats();
      expect(stats.size).toBe(2);
    });

    it('should list cache keys', async () => {
      await service.searchMaterialPricing('2x4 lumber', 'each');
      await service.searchLaborRate('carpenter');
      
      const stats = service.getCacheStats();
      expect(stats.keys).toHaveLength(2);
      expect(stats.keys).toContain('material:2x4 lumber:each');
      expect(stats.keys).toContain('labor:carpenter:USA');
    });

    it('should clear cache', async () => {
      await service.searchMaterialPricing('2x4 lumber', 'each');
      
      let stats = service.getCacheStats();
      expect(stats.size).toBeGreaterThan(0);
      
      service.clearCache();
      
      stats = service.getCacheStats();
      expect(stats.size).toBe(0);
    });
  });

  describe('pricing data quality', () => {
    it('should return realistic material prices', async () => {
      const materials = [
        { name: '2x4 lumber', unit: 'each', expectedRange: [5, 15] },
        { name: 'concrete', unit: 'cubic yard', expectedRange: [100, 200] },
        { name: 'toilet', unit: 'each', expectedRange: [150, 400] },
      ];

      for (const material of materials) {
        const result = await service.searchMaterialPricing(material.name, material.unit);
        
        expect(result).not.toBeNull();
        expect(result?.averagePrice).toBeGreaterThanOrEqual(material.expectedRange[0]);
        expect(result?.averagePrice).toBeLessThanOrEqual(material.expectedRange[1]);
      }
    });

    it('should return realistic labor rates', async () => {
      const trades = [
        { name: 'carpenter', expectedRange: [40, 60] },
        { name: 'electrician', expectedRange: [55, 75] },
        { name: 'plumber', expectedRange: [60, 80] },
      ];

      for (const trade of trades) {
        const result = await service.searchLaborRate(trade.name);
        
        expect(result).not.toBeNull();
        expect(result?.hourlyRate).toBeGreaterThanOrEqual(trade.expectedRange[0]);
        expect(result?.hourlyRate).toBeLessThanOrEqual(trade.expectedRange[1]);
      }
    });

    it('should return realistic labor time estimates', async () => {
      const tasks = [
        { description: 'install toilet', unit: 'each', expectedRange: [1, 2] },
        { description: 'install light switch', unit: 'each', expectedRange: [0.4, 0.6] },
        { description: 'build staircase', unit: 'flight', expectedRange: [20, 30] },
      ];

      for (const task of tasks) {
        const result = await service.searchLaborTimeEstimate(task.description, task.unit);
        
        expect(result).not.toBeNull();
        expect(result).toBeGreaterThanOrEqual(task.expectedRange[0]);
        expect(result).toBeLessThanOrEqual(task.expectedRange[1]);
      }
    });
  });
});
