import { sqliteTable, text, integer, real } from 'drizzle-orm/sqlite-core';

// Helper function to generate IDs
const generateId = () => Math.random().toString(36).substring(2) + Date.now().toString(36);

// Core business entities
export const projects = sqliteTable('projects', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  name: text('name').notNull(),
  description: text('description'),
  type: text('type').notNull(), // residential, commercial, industrial, etc.
  status: text('status').notNull().default('planning'), // planning, active, completed, on-hold
  startDate: text('start_date'),
  endDate: text('end_date'),
  estimatedCost: real('estimated_cost'),
  actualCost: real('actual_cost'),
  clientId: text('client_id').references(() => clients.id),
  addressLine1: text('address_line1'),
  addressLine2: text('address_line2'),
  city: text('city'),
  state: text('state'),
  zipCode: text('zip_code'),
  buildingCodes: text('building_codes'), // JSON array of applicable codes
  permits: text('permits'), // JSON array of permits
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
  updatedAt: text('updated_at').$defaultFn(() => new Date().toISOString()),
});

export const clients = sqliteTable('clients', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  name: text('name').notNull(),
  email: text('email'),
  phone: text('phone'),
  company: text('company'),
  addressLine1: text('address_line1'),
  addressLine2: text('address_line2'),
  city: text('city'),
  state: text('state'),
  zipCode: text('zip_code'),
  notes: text('notes'),
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
  updatedAt: text('updated_at').$defaultFn(() => new Date().toISOString()),
});

export const laborRates = sqliteTable('labor_rates', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  skillType: text('skill_type').notNull(), // electrician, plumber, carpenter, etc.
  level: text('level').notNull(), // apprentice, journeyman, master
  hourlyRate: real('hourly_rate').notNull(),
  overtimeRate: real('overtime_rate'),
  region: text('region'),
  effectiveDate: text('effective_date').notNull(),
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
});

export const tasks = sqliteTable('tasks', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  projectId: text('project_id').references(() => projects.id).notNull(),
  name: text('name').notNull(),
  description: text('description'),
  skillType: text('skill_type').notNull(),
  estimatedHours: real('estimated_hours'),
  actualHours: real('actual_hours'),
  status: text('status').notNull().default('pending'), // pending, in-progress, completed
  assignedTo: text('assigned_to'),
  dependencies: text('dependencies'), // JSON array of task IDs
  startDate: text('start_date'),
  endDate: text('end_date'),
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
  updatedAt: text('updated_at').$defaultFn(() => new Date().toISOString()),
});

export const buildingCodes = sqliteTable('building_codes', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  codeNumber: text('code_number').notNull(),
  title: text('title').notNull(),
  description: text('description'),
  category: text('category'), // structural, electrical, plumbing, fire safety, etc.
  jurisdiction: text('jurisdiction'), // local, state, federal
  effectiveDate: text('effective_date'),
  requirements: text('requirements'), // JSON object with requirements
  penalties: text('penalties'), // JSON object with penalty information
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
});

export const marketData = sqliteTable('market_data', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  region: text('region').notNull(),
  projectType: text('project_type').notNull(),
  averageCostPerSqFt: real('average_cost_per_sq_ft'),
  laborCostPercentage: real('labor_cost_percentage'),
  materialCostPercentage: real('material_cost_percentage'),
  permitCostAverage: real('permit_cost_average'),
  timelineAverage: integer('timeline_average'), // in days
  dataSource: text('data_source'),
  collectionDate: text('collection_date').notNull(),
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
});

// Call management
export const callLogs = sqliteTable('call_logs', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  phoneNumber: text('phone_number').notNull(),
  callerName: text('caller_name'),
  callType: text('call_type').notNull(), // incoming, outgoing
  status: text('status').notNull(), // answered, missed, blocked, screened
  duration: integer('duration'), // in seconds
  transcript: text('transcript'),
  sentiment: text('sentiment'), // positive, neutral, negative
  priority: text('priority').notNull().default('medium'), // low, medium, high, urgent
  clientId: text('client_id').references(() => clients.id),
  projectId: text('project_id').references(() => projects.id),
  aiScreeningResult: text('ai_screening_result'), // JSON object
  followUpRequired: integer('follow_up_required').default(0), // boolean as integer
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
});

// AI Models
export const aiModels = sqliteTable('ai_models', {
  id: text('id').primaryKey().$defaultFn(() => generateId()),
  name: text('name').notNull(),
  type: text('type').notNull(), // llm, speech-to-text, text-to-speech, etc.
  provider: text('provider').notNull(), // openai, anthropic, local, etc.
  modelId: text('model_id').notNull(),
  version: text('version'),
  status: text('status').notNull().default('inactive'), // active, inactive, downloading, failed
  capabilities: text('capabilities'), // JSON array
  configuration: text('configuration'), // JSON object
  downloadUrl: text('download_url'),
  fileSize: integer('file_size'),
  downloadProgress: integer('download_progress').default(0),
  lastUsed: text('last_used'),
  createdAt: text('created_at').$defaultFn(() => new Date().toISOString()),
  updatedAt: text('updated_at').$defaultFn(() => new Date().toISOString()),
});