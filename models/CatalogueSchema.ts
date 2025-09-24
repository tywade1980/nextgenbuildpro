/**
 * TypeScript interfaces for the Catalogue system
 * Based on the Kotlin models in app/src/main/java/com/nextgenbuildpro/pm/data/model/EnhancedCatalogueModels.kt
 */

export interface Category {
  id: string;
  name: string;
  description: string;
  sequence: number;
  imageUrl?: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface Trade {
  id: string;
  categoryId: string;
  name: string;
  description: string;
  sequence: number;
  imageUrl?: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface Scope {
  id: string;
  tradeId: string;
  name: string;
  description: string;
  sequence: number;
  imageUrl?: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface Assembly {
  id: string;
  scopeId: string;
  name: string;
  description: string;
  sequence: number;
  imageUrl?: string;
  unit: string;
  laborHours: number;
  materialCost: number;
  laborCost: number;
  equipmentCost: number;
  subcontractorCost: number;
  otherCost: number;
  totalCost: number;
  markupPercentage: number;
  notes: string;
  tags: string[];
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface Task {
  id: string;
  assemblyId: string;
  name: string;
  description: string;
  sequence: number;
  laborHours: number;
  materialCost: number;
  laborCost: number;
  equipmentCost: number;
  notes: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface Material {
  id: string;
  assemblyId: string;
  name: string;
  description: string;
  quantity: number;
  unit: string;
  unitCost: number;
  totalCost: number;
  supplier?: string;
  partNumber?: string;
  waste: number;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

// Additional interfaces for the complete system
export interface CategoryWithChildren {
  category: Category;
  trades: TradeWithChildren[];
}

export interface TradeWithChildren {
  trade: Trade;
  scopes: ScopeWithChildren[];
}

export interface ScopeWithChildren {
  scope: Scope;
  assemblies: AssemblyWithChildren[];
}

export interface AssemblyWithChildren {
  assembly: Assembly;
  tasks: TaskWithMaterials[];
  materials: Material[];
}

export interface TaskWithMaterials {
  task: Task;
  materials: Material[];
}

export interface SearchCriteria {
  keyword?: string;
  tradeType?: string;
  projectType?: string;
  minCost?: number;
  maxCost?: number;
  tags?: string[];
}

export interface AssemblySearchResult {
  assembly: Assembly;
  scope: Scope;
  trade: Trade;
  category: Category;
  tasks: Task[];
  materials: Material[];
}