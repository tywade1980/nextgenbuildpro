/**
 * Catalogue Pricing Enhancer Service
 * 
 * Enhances catalogue data with real-world pricing information from web search.
 * Updates materials, labor costs, and assembly totals based on current market data.
 */

import { CatalogueDataService } from './CatalogueDataService';
import { PricingWebSearchService } from './PricingWebSearchService';

export interface PricingUpdateResult {
  itemId: string;
  itemType: 'assembly' | 'material' | 'task';
  oldValue: number;
  newValue: number;
  source: string;
  updated: boolean;
  error?: string;
}

export interface BatchPricingUpdateResult {
  totalItems: number;
  updatedItems: number;
  failedItems: number;
  results: PricingUpdateResult[];
  summary: {
    totalOldCost: number;
    totalNewCost: number;
    percentChange: number;
  };
}

export class CataloguePricingEnhancer {
  private catalogueService: CatalogueDataService;
  private pricingSearchService: PricingWebSearchService;

  constructor(
    catalogueService?: CatalogueDataService,
    pricingSearchService?: PricingWebSearchService
  ) {
    this.catalogueService = catalogueService || new CatalogueDataService();
    this.pricingSearchService = pricingSearchService || new PricingWebSearchService();
  }

  /**
   * Update pricing for a single material
   */
  async updateMaterialPricing(materialId: string): Promise<PricingUpdateResult> {
    const result: PricingUpdateResult = {
      itemId: materialId,
      itemType: 'material',
      oldValue: 0,
      newValue: 0,
      source: '',
      updated: false,
    };

    try {
      const material = await this.catalogueService.getMaterial(materialId);
      
      if (!material) {
        result.error = 'Material not found';
        return result;
      }

      result.oldValue = material.unitCost;

      // Search for current pricing
      const pricingData = await this.pricingSearchService.searchMaterialPricing(
        material.name,
        material.unit
      );

      if (!pricingData) {
        result.error = 'No pricing data found';
        return result;
      }

      // Update material with new pricing
      const newUnitCost = pricingData.averagePrice;
      const newTotalCost = newUnitCost * material.quantity;

      await this.catalogueService.updateMaterial(materialId, {
        unitCost: newUnitCost,
        totalCost: newTotalCost,
      });

      result.newValue = newUnitCost;
      result.source = pricingData.sources.join(', ');
      result.updated = true;

      console.log(
        `Updated material ${material.name}: $${result.oldValue} -> $${result.newValue}`
      );

      return result;
    } catch (error) {
      result.error = error instanceof Error ? error.message : 'Unknown error';
      console.error(`Error updating material pricing for ${materialId}:`, error);
      return result;
    }
  }

  /**
   * Update labor costs for a single task
   */
  async updateTaskLaborCost(taskId: string, trade: string): Promise<PricingUpdateResult> {
    const result: PricingUpdateResult = {
      itemId: taskId,
      itemType: 'task',
      oldValue: 0,
      newValue: 0,
      source: '',
      updated: false,
    };

    try {
      const task = await this.catalogueService.getTask(taskId);
      
      if (!task) {
        result.error = 'Task not found';
        return result;
      }

      result.oldValue = task.laborCost;

      // Search for current labor rate
      const laborRateData = await this.pricingSearchService.searchLaborRate(trade);

      if (!laborRateData) {
        result.error = 'No labor rate data found';
        return result;
      }

      // Calculate new labor cost
      const newLaborCost = task.laborHours * laborRateData.hourlyRate;

      await this.catalogueService.updateTask(taskId, {
        laborCost: newLaborCost,
      });

      result.newValue = newLaborCost;
      result.source = laborRateData.sources.join(', ');
      result.updated = true;

      console.log(
        `Updated task ${task.name} labor cost: $${result.oldValue} -> $${result.newValue}`
      );

      return result;
    } catch (error) {
      result.error = error instanceof Error ? error.message : 'Unknown error';
      console.error(`Error updating task labor cost for ${taskId}:`, error);
      return result;
    }
  }

  /**
   * Update all costs for an assembly (materials, labor, and totals)
   */
  async updateAssemblyPricing(
    assemblyId: string,
    trade: string
  ): Promise<PricingUpdateResult> {
    const result: PricingUpdateResult = {
      itemId: assemblyId,
      itemType: 'assembly',
      oldValue: 0,
      newValue: 0,
      source: 'Calculated from materials and tasks',
      updated: false,
    };

    try {
      const assembly = await this.catalogueService.getAssembly(assemblyId);
      
      if (!assembly) {
        result.error = 'Assembly not found';
        return result;
      }

      result.oldValue = assembly.totalCost;

      // Update all materials
      const materials = await this.catalogueService.getMaterialsByAssembly(assemblyId);
      let totalMaterialCost = 0;
      
      for (const material of materials) {
        const materialResult = await this.updateMaterialPricing(material.id);
        if (materialResult.updated) {
          totalMaterialCost += materialResult.newValue * material.quantity;
        } else {
          totalMaterialCost += material.totalCost;
        }
      }

      // Update all tasks
      const tasks = await this.catalogueService.getTasksByAssembly(assemblyId);
      let totalLaborCost = 0;
      
      for (const task of tasks) {
        const taskResult = await this.updateTaskLaborCost(task.id, trade);
        if (taskResult.updated) {
          totalLaborCost += taskResult.newValue;
        } else {
          totalLaborCost += task.laborCost;
        }
      }

      // Calculate new total cost
      const newTotalCost =
        totalMaterialCost +
        totalLaborCost +
        assembly.equipmentCost +
        assembly.subcontractorCost +
        assembly.otherCost;

      // Update assembly
      await this.catalogueService.updateAssembly(assemblyId, {
        materialCost: totalMaterialCost,
        laborCost: totalLaborCost,
        totalCost: newTotalCost,
      });

      result.newValue = newTotalCost;
      result.updated = true;

      console.log(
        `Updated assembly ${assembly.name}: $${result.oldValue} -> $${result.newValue}`
      );

      return result;
    } catch (error) {
      result.error = error instanceof Error ? error.message : 'Unknown error';
      console.error(`Error updating assembly pricing for ${assemblyId}:`, error);
      return result;
    }
  }

  /**
   * Update pricing for all assemblies in a scope
   */
  async updateScopePricing(scopeId: string, trade: string): Promise<BatchPricingUpdateResult> {
    const results: PricingUpdateResult[] = [];
    let totalOldCost = 0;
    let totalNewCost = 0;

    try {
      const assemblies = await this.catalogueService.getAssembliesByScope(scopeId);
      
      for (const assembly of assemblies) {
        const result = await this.updateAssemblyPricing(assembly.id, trade);
        results.push(result);
        
        if (result.updated) {
          totalOldCost += result.oldValue;
          totalNewCost += result.newValue;
        }
      }

      const updatedItems = results.filter(r => r.updated).length;
      const failedItems = results.filter(r => !r.updated).length;
      const percentChange =
        totalOldCost > 0 ? ((totalNewCost - totalOldCost) / totalOldCost) * 100 : 0;

      return {
        totalItems: results.length,
        updatedItems,
        failedItems,
        results,
        summary: {
          totalOldCost,
          totalNewCost,
          percentChange,
        },
      };
    } catch (error) {
      console.error(`Error updating scope pricing for ${scopeId}:`, error);
      throw error;
    }
  }

  /**
   * Update pricing for all assemblies in a trade
   */
  async updateTradePricing(tradeId: string, tradeName: string): Promise<BatchPricingUpdateResult> {
    const results: PricingUpdateResult[] = [];
    let totalOldCost = 0;
    let totalNewCost = 0;

    try {
      const scopes = await this.catalogueService.getScopesByTrade(tradeId);
      
      for (const scope of scopes) {
        const scopeResult = await this.updateScopePricing(scope.id, tradeName);
        results.push(...scopeResult.results);
        totalOldCost += scopeResult.summary.totalOldCost;
        totalNewCost += scopeResult.summary.totalNewCost;
      }

      const updatedItems = results.filter(r => r.updated).length;
      const failedItems = results.filter(r => !r.updated).length;
      const percentChange =
        totalOldCost > 0 ? ((totalNewCost - totalOldCost) / totalOldCost) * 100 : 0;

      return {
        totalItems: results.length,
        updatedItems,
        failedItems,
        results,
        summary: {
          totalOldCost,
          totalNewCost,
          percentChange,
        },
      };
    } catch (error) {
      console.error(`Error updating trade pricing for ${tradeId}:`, error);
      throw error;
    }
  }

  /**
   * Update pricing for all items in the entire catalogue
   */
  async updateAllCataloguePricing(): Promise<BatchPricingUpdateResult> {
    const results: PricingUpdateResult[] = [];
    let totalOldCost = 0;
    let totalNewCost = 0;

    try {
      console.log('Starting full catalogue pricing update...');
      
      const categories = await this.catalogueService.getCategories();
      
      for (const category of categories) {
        console.log(`Updating category: ${category.name}`);
        const trades = await this.catalogueService.getTradesByCategory(category.id);
        
        for (const trade of trades) {
          console.log(`  Updating trade: ${trade.name}`);
          const tradeResult = await this.updateTradePricing(trade.id, trade.name);
          results.push(...tradeResult.results);
          totalOldCost += tradeResult.summary.totalOldCost;
          totalNewCost += tradeResult.summary.totalNewCost;
        }
      }

      const updatedItems = results.filter(r => r.updated).length;
      const failedItems = results.filter(r => !r.updated).length;
      const percentChange =
        totalOldCost > 0 ? ((totalNewCost - totalOldCost) / totalOldCost) * 100 : 0;

      console.log('Full catalogue pricing update completed');
      console.log(`Updated: ${updatedItems}, Failed: ${failedItems}`);
      console.log(`Total cost change: ${percentChange.toFixed(2)}%`);

      return {
        totalItems: results.length,
        updatedItems,
        failedItems,
        results,
        summary: {
          totalOldCost,
          totalNewCost,
          percentChange,
        },
      };
    } catch (error) {
      console.error('Error updating all catalogue pricing:', error);
      throw error;
    }
  }

  /**
   * Generate a pricing update report
   */
  generatePricingReport(result: BatchPricingUpdateResult): string {
    const lines: string[] = [];
    
    lines.push('='.repeat(60));
    lines.push('CATALOGUE PRICING UPDATE REPORT');
    lines.push('='.repeat(60));
    lines.push('');
    
    lines.push('SUMMARY:');
    lines.push(`  Total Items Processed: ${result.totalItems}`);
    lines.push(`  Successfully Updated: ${result.updatedItems}`);
    lines.push(`  Failed Updates: ${result.failedItems}`);
    lines.push('');
    
    lines.push('COST ANALYSIS:');
    lines.push(`  Previous Total Cost: $${result.summary.totalOldCost.toFixed(2)}`);
    lines.push(`  Updated Total Cost: $${result.summary.totalNewCost.toFixed(2)}`);
    lines.push(
      `  Cost Change: ${result.summary.percentChange > 0 ? '+' : ''}${result.summary.percentChange.toFixed(2)}%`
    );
    lines.push('');
    
    if (result.failedItems > 0) {
      lines.push('FAILED UPDATES:');
      result.results
        .filter(r => !r.updated && r.error)
        .forEach(r => {
          lines.push(`  - ${r.itemType} ${r.itemId}: ${r.error}`);
        });
      lines.push('');
    }
    
    lines.push('='.repeat(60));
    
    return lines.join('\n');
  }
}
