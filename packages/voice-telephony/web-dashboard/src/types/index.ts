// Type definitions for the construction business management system

export interface Project {
  id: string;
  name: string;
  description?: string;
  type: ProjectType;
  status: ProjectStatus;
  startDate?: string;
  endDate?: string;
  estimatedCost?: number;
  actualCost?: number;
  clientId?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  buildingCodes?: string[];
  permits?: string[];
  createdAt: string;
  updatedAt: string;
}

export type ProjectType = 'residential' | 'commercial' | 'industrial' | 'infrastructure' | 'renovation';
export type ProjectStatus = 'planning' | 'active' | 'completed' | 'on-hold' | 'cancelled';

export interface Client {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  company?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LaborRate {
  id: string;
  skillType: SkillType;
  level: SkillLevel;
  hourlyRate: number;
  overtimeRate?: number;
  region?: string;
  effectiveDate: string;
  createdAt: string;
}

export type SkillType = 'electrician' | 'plumber' | 'carpenter' | 'mason' | 'roofer' | 'hvac' | 'general_labor' | 'project_manager' | 'engineer' | 'architect';
export type SkillLevel = 'apprentice' | 'journeyman' | 'master' | 'supervisor' | 'foreman';

export interface Task {
  id: string;
  projectId: string;
  name: string;
  description?: string;
  skillType: SkillType;
  estimatedHours?: number;
  actualHours?: number;
  status: TaskStatus;
  assignedTo?: string;
  dependencies?: string[];
  startDate?: string;
  endDate?: string;
  createdAt: string;
  updatedAt: string;
}

export type TaskStatus = 'pending' | 'in-progress' | 'completed' | 'blocked' | 'cancelled';

export interface BuildingCode {
  id: string;
  codeNumber: string;
  title: string;
  description?: string;
  category?: CodeCategory;
  jurisdiction?: Jurisdiction;
  effectiveDate?: string;
  requirements?: Record<string, unknown>;
  penalties?: Record<string, unknown>;
  createdAt: string;
}

export type CodeCategory = 'structural' | 'electrical' | 'plumbing' | 'fire_safety' | 'accessibility' | 'energy_efficiency' | 'zoning';
export type Jurisdiction = 'local' | 'state' | 'federal' | 'international';

export interface MarketData {
  id: string;
  region: string;
  projectType: ProjectType;
  averageCostPerSqFt?: number;
  laborCostPercentage?: number;
  materialCostPercentage?: number;
  permitCostAverage?: number;
  timelineAverage?: number; // in days
  dataSource?: string;
  collectionDate: string;
  createdAt: string;
}

export interface CallLog {
  id: string;
  phoneNumber: string;
  callerName?: string;
  callType: CallType;
  status: CallStatus;
  duration?: number; // in seconds
  transcript?: string;
  sentiment?: Sentiment;
  priority: Priority;
  clientId?: string;
  projectId?: string;
  aiScreeningResult?: AIScreeningResult;
  followUpRequired: boolean;
  createdAt: string;
}

export type CallType = 'incoming' | 'outgoing';
export type CallStatus = 'answered' | 'missed' | 'blocked' | 'screened' | 'voicemail';
export type Sentiment = 'positive' | 'neutral' | 'negative';
export type Priority = 'low' | 'medium' | 'high' | 'urgent';

export interface AIScreeningResult {
  confidence: number;
  category: string;
  intent: string;
  urgency: Priority;
  suggestedAction: string;
  keyTopics: string[];
}

export interface AIModel {
  id: string;
  name: string;
  type: AIModelType;
  provider: AIProvider;
  modelId: string;
  version?: string;
  status: ModelStatus;
  capabilities?: string[];
  configuration?: Record<string, unknown>;
  downloadUrl?: string;
  fileSize?: number;
  downloadProgress: number;
  lastUsed?: string;
  createdAt: string;
  updatedAt: string;
}

export type AIModelType = 'llm' | 'speech-to-text' | 'text-to-speech' | 'embedding' | 'classification' | 'translation';
export type AIProvider = 'openai' | 'anthropic' | 'local' | 'huggingface' | 'google' | 'openrouter' | 'custom';
export type ModelStatus = 'active' | 'inactive' | 'downloading' | 'failed' | 'updating';

// API Response types
export interface APIResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface PaginatedResponse<T> {
  success: boolean;
  data: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
  error?: string;
}

// Form types
export interface ProjectForm {
  name: string;
  description?: string;
  type: ProjectType;
  startDate?: string;
  endDate?: string;
  estimatedCost?: number;
  clientId?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  zipCode?: string;
}

export interface ClientForm {
  name: string;
  email?: string;
  phone?: string;
  company?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  notes?: string;
}

export interface TaskForm {
  projectId: string;
  name: string;
  description?: string;
  skillType: SkillType;
  estimatedHours?: number;
  assignedTo?: string;
  startDate?: string;
  endDate?: string;
}