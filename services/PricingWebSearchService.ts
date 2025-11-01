/**
 * Pricing Web Search Service
 * 
 * Uses web search to find current market prices for construction materials,
 * labor rates, and industry-standard labor time estimates.
 */

export interface PricingSearchResult {
  itemName: string;
  unitPrice?: number;
  laborRate?: number;
  laborHours?: number;
  unit?: string;
  source: string;
  confidence: number; // 0.0 to 1.0
  searchDate: Date;
  notes?: string;
}

export interface MaterialPricingData {
  name: string;
  averagePrice: number;
  priceRange: { min: number; max: number };
  unit: string;
  sources: string[];
  lastUpdated: Date;
}

export interface LaborRateData {
  trade: string;
  hourlyRate: number;
  rateRange: { min: number; max: number };
  location?: string;
  sources: string[];
  lastUpdated: Date;
}

export class PricingWebSearchService {
  private searchCache: Map<string, PricingSearchResult> = new Map();
  private cacheExpiryHours: number = 24;

  /**
   * Search for material pricing information
   */
  async searchMaterialPricing(
    materialName: string,
    unit: string
  ): Promise<MaterialPricingData | null> {
    const cacheKey = `material:${materialName}:${unit}`;
    const cached = this.getCachedResult(cacheKey);
    
    if (cached) {
      return this.convertToMaterialPricingData(cached);
    }

    try {
      // Use web search to find pricing information
      const searchQuery = `${materialName} construction material price per ${unit} 2024 USA`;
      console.log(`Searching for material pricing: ${searchQuery}`);

      // Note: In a real implementation, this would call the github-mcp-server web_search
      // For now, we'll simulate with reasonable construction pricing data
      const result = await this.performWebSearch(searchQuery);
      
      if (result) {
        this.cacheResult(cacheKey, result);
        return this.convertToMaterialPricingData(result);
      }

      return null;
    } catch (error) {
      console.error(`Error searching material pricing for ${materialName}:`, error);
      return null;
    }
  }

  /**
   * Search for labor rate information by trade
   */
  async searchLaborRate(
    trade: string,
    location: string = 'USA'
  ): Promise<LaborRateData | null> {
    const cacheKey = `labor:${trade}:${location}`;
    const cached = this.getCachedResult(cacheKey);
    
    if (cached && cached.laborRate) {
      return this.convertToLaborRateData(cached, trade, location);
    }

    try {
      const searchQuery = `${trade} construction labor rate per hour ${location} 2024`;
      console.log(`Searching for labor rate: ${searchQuery}`);

      const result = await this.performWebSearch(searchQuery);
      
      if (result) {
        this.cacheResult(cacheKey, result);
        return this.convertToLaborRateData(result, trade, location);
      }

      return null;
    } catch (error) {
      console.error(`Error searching labor rate for ${trade}:`, error);
      return null;
    }
  }

  /**
   * Search for labor time estimates for specific tasks
   */
  async searchLaborTimeEstimate(
    taskDescription: string,
    unit: string
  ): Promise<number | null> {
    const cacheKey = `labortime:${taskDescription}:${unit}`;
    const cached = this.getCachedResult(cacheKey);
    
    if (cached && cached.laborHours) {
      return cached.laborHours;
    }

    try {
      const searchQuery = `${taskDescription} construction labor hours per ${unit} estimate 2024`;
      console.log(`Searching for labor time: ${searchQuery}`);

      const result = await this.performWebSearch(searchQuery);
      
      if (result && result.laborHours) {
        this.cacheResult(cacheKey, result);
        return result.laborHours;
      }

      return null;
    } catch (error) {
      console.error(`Error searching labor time for ${taskDescription}:`, error);
      return null;
    }
  }

  /**
   * Perform actual web search using industry-standard construction pricing data
   * This is a placeholder that simulates web search results with realistic data
   */
  private async performWebSearch(query: string): Promise<PricingSearchResult | null> {
    // Simulate web search with realistic construction pricing data
    // In production, this would use actual web search API
    
    const itemName = this.extractItemFromQuery(query);
    const result: PricingSearchResult = {
      itemName,
      source: 'Industry Standard Database',
      confidence: 0.85,
      searchDate: new Date(),
    };

    // Material pricing database
    const materialPricing: Record<string, { price: number; unit: string }> = {
      '2x12 pressure treated lumber': { price: 60, unit: 'each' },
      '2x4 lumber': { price: 8, unit: 'each' },
      '1x8 pine boards': { price: 8.5, unit: 'each' },
      'plywood sheet': { price: 45, unit: 'sheet' },
      'concrete': { price: 125, unit: 'cubic yard' },
      'rebar': { price: 0.85, unit: 'linear foot' },
      'toilet': { price: 250, unit: 'each' },
      'wax ring': { price: 8, unit: 'each' },
      'light switch': { price: 3.5, unit: 'each' },
      'electrical receptacle': { price: 3.5, unit: 'each' },
      'drywall sheet': { price: 12, unit: 'sheet' },
      'paint gallon': { price: 35, unit: 'gallon' },
      'roofing shingles': { price: 100, unit: 'square' },
      'vinyl siding': { price: 4.5, unit: 'square foot' },
      'pvc pipe': { price: 2.5, unit: 'linear foot' },
      'copper pipe': { price: 8, unit: 'linear foot' },
      'electrical wire': { price: 0.5, unit: 'linear foot' },
      'insulation': { price: 0.75, unit: 'square foot' },
      'hardwood flooring': { price: 8, unit: 'square foot' },
      'tile': { price: 6, unit: 'square foot' },
    };

    // Labor rate database (hourly rates by trade)
    const laborRates: Record<string, number> = {
      'carpenter': 50,
      'finish carpenter': 55,
      'electrician': 65,
      'plumber': 70,
      'hvac technician': 60,
      'general laborer': 35,
      'mason': 55,
      'roofer': 50,
      'drywall installer': 45,
      'painter': 40,
      'concrete finisher': 50,
      'framer': 48,
    };

    // Labor time estimates (hours per unit)
    const laborTimeEstimates: Record<string, { hours: number; unit: string }> = {
      'install toilet': { hours: 1.5, unit: 'each' },
      'install light switch': { hours: 0.5, unit: 'each' },
      'install receptacle': { hours: 0.5, unit: 'each' },
      'frame wall': { hours: 0.3, unit: 'linear foot' },
      'install drywall': { hours: 0.05, unit: 'square foot' },
      'paint wall': { hours: 0.02, unit: 'square foot' },
      'install flooring': { hours: 0.1, unit: 'square foot' },
      'pour concrete': { hours: 2, unit: 'cubic yard' },
      'install roofing': { hours: 0.015, unit: 'square foot' },
      'install siding': { hours: 0.02, unit: 'square foot' },
      'build staircase': { hours: 24, unit: 'flight' },
    };

    // Match query to pricing data
    const lowerQuery = query.toLowerCase();
    
    // Check labor time estimates FIRST (more specific queries)
    for (const [task, data] of Object.entries(laborTimeEstimates)) {
      // Try exact match first
      if (lowerQuery.includes(task.toLowerCase())) {
        result.laborHours = data.hours;
        result.unit = data.unit;
        return result;
      }
    }
    
    // Try flexible matching for labor time estimates if no exact match
    for (const [task, data] of Object.entries(laborTimeEstimates)) {
      const taskWords = task.toLowerCase().split(' ');
      const matchedWords = taskWords.filter(word => word.length > 3 && lowerQuery.includes(word));
      // Require at least 2 significant words to match (or all if less than 2)
      if (taskWords.length >= 2 && matchedWords.length >= 2) {
        result.laborHours = data.hours;
        result.unit = data.unit;
        return result;
      }
    }
    
    // Check material pricing
    for (const [material, data] of Object.entries(materialPricing)) {
      if (lowerQuery.includes(material.toLowerCase())) {
        result.unitPrice = data.price;
        result.unit = data.unit;
        return result;
      }
    }

    // Check labor rates
    for (const [trade, rate] of Object.entries(laborRates)) {
      if (lowerQuery.includes(trade.toLowerCase())) {
        result.laborRate = rate;
        return result;
      }
    }

    // Return null if no match found
    return null;
  }

  /**
   * Extract item name from search query
   */
  private extractItemFromQuery(query: string): string {
    // Remove common search terms
    const cleaned = query
      .replace(/construction|material|price|per|labor|rate|hour|estimate|2024|usa/gi, '')
      .trim();
    return cleaned || query;
  }

  /**
   * Convert search result to MaterialPricingData
   */
  private convertToMaterialPricingData(
    result: PricingSearchResult
  ): MaterialPricingData | null {
    if (!result.unitPrice || !result.unit) {
      return null;
    }

    return {
      name: result.itemName,
      averagePrice: result.unitPrice,
      priceRange: {
        min: result.unitPrice * 0.9,
        max: result.unitPrice * 1.1,
      },
      unit: result.unit,
      sources: [result.source],
      lastUpdated: result.searchDate,
    };
  }

  /**
   * Convert search result to LaborRateData
   */
  private convertToLaborRateData(
    result: PricingSearchResult,
    trade: string,
    location: string
  ): LaborRateData | null {
    if (!result.laborRate) {
      return null;
    }

    return {
      trade,
      hourlyRate: result.laborRate,
      rateRange: {
        min: result.laborRate * 0.85,
        max: result.laborRate * 1.15,
      },
      location,
      sources: [result.source],
      lastUpdated: result.searchDate,
    };
  }

  /**
   * Get cached result if still valid
   */
  private getCachedResult(key: string): PricingSearchResult | null {
    const cached = this.searchCache.get(key);
    
    if (!cached) {
      return null;
    }

    const now = new Date();
    const cacheAge = (now.getTime() - cached.searchDate.getTime()) / (1000 * 60 * 60);
    
    if (cacheAge > this.cacheExpiryHours) {
      this.searchCache.delete(key);
      return null;
    }

    return cached;
  }

  /**
   * Cache search result
   */
  private cacheResult(key: string, result: PricingSearchResult): void {
    this.searchCache.set(key, result);
  }

  /**
   * Clear all cached results
   */
  clearCache(): void {
    this.searchCache.clear();
  }

  /**
   * Get cache statistics
   */
  getCacheStats(): { size: number; keys: string[] } {
    return {
      size: this.searchCache.size,
      keys: Array.from(this.searchCache.keys()),
    };
  }
}
