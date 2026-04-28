/**
 * Estimate Template System
 * 
 * Provides functionality for creating and managing project estimate templates
 */

export interface EstimateItem {
  id: string
  assemblyId: string
  assemblyName: string
  description: string
  quantity: number
  unit: string
  unitCost: number
  markup: number
  taxable: boolean
  optional: boolean
  notes: string
  customFields: Record<string, any>
}

export interface EstimateSection {
  id: string
  name: string
  description: string
  sequence: number
  items: EstimateItem[]
  subtotalDisplay: boolean
}

export interface EstimateData {
  id: string
  title: string
  templateId?: string
  status: 'draft' | 'sent' | 'approved' | 'rejected'
  clientId?: string
  projectId?: string
  issueDate: Date
  expiryDate: Date
  sections: EstimateSection[]
  taxRate: number
  markup: number
  currency: string
  terms: string
  notes: string
  customFields: Record<string, any>
  createdAt: Date
  updatedAt: Date
}

export interface EstimateTemplateData {
  id: string
  name: string
  description: string
  category: string
  projectType: string
  createdBy: string
  createdAt: Date
  updatedAt: Date
  isActive: boolean
  isPublic: boolean
  sections: EstimateSection[]
  defaultMarkup: number
  defaultTaxRate: number
  defaultCurrency: string
  termsAndConditions: string
  notes: string
  customFields: Record<string, any>
}

export class EstimateTemplate {
  public id: string
  public name: string
  public description: string
  public category: string
  public projectType: string
  public createdBy: string
  public createdAt: Date
  public updatedAt: Date
  public isActive: boolean
  public isPublic: boolean
  public sections: EstimateSection[]
  public defaultMarkup: number
  public defaultTaxRate: number
  public defaultCurrency: string
  public termsAndConditions: string
  public notes: string
  public customFields: Record<string, any>

  constructor(data: Partial<EstimateTemplateData> = {}) {
    this.id = data.id || `template-${Date.now()}`
    this.name = data.name || 'New Template'
    this.description = data.description || ''
    this.category = data.category || ''
    this.projectType = data.projectType || ''
    this.createdBy = data.createdBy || ''
    this.createdAt = data.createdAt || new Date()
    this.updatedAt = data.updatedAt || new Date()
    this.isActive = data.isActive !== undefined ? data.isActive : true
    this.isPublic = data.isPublic !== undefined ? data.isPublic : false
    this.sections = data.sections || []
    this.defaultMarkup = data.defaultMarkup || 15
    this.defaultTaxRate = data.defaultTaxRate || 7
    this.defaultCurrency = data.defaultCurrency || 'USD'
    this.termsAndConditions = data.termsAndConditions || ''
    this.notes = data.notes || ''
    this.customFields = data.customFields || {}
  }

  // Add a section to the template
  addSection(name: string, description: string = '', sequence: number | null = null): EstimateSection {
    // If sequence not provided, add at the end
    if (sequence === null) {
      sequence = this.sections.length > 0 
        ? Math.max(...this.sections.map(s => s.sequence)) + 1 
        : 1
    }
    
    const section: EstimateSection = {
      id: `section-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      name,
      description,
      sequence,
      items: [],
      subtotalDisplay: true
    }
    
    this.sections.push(section)
    this.sections.sort((a, b) => a.sequence - b.sequence)
    this.updatedAt = new Date()
    
    return section
  }

  // Add an item to a section
  addItem(sectionId: string, itemData: Partial<EstimateItem>): EstimateItem {
    const section = this.sections.find(s => s.id === sectionId)
    if (!section) {
      throw new Error(`Section not found: ${sectionId}`)
    }
    
    const item: EstimateItem = {
      id: `item-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      assemblyId: itemData.assemblyId || '',
      assemblyName: itemData.assemblyName || '',
      description: itemData.description || '',
      quantity: itemData.quantity || 1,
      unit: itemData.unit || 'each',
      unitCost: itemData.unitCost || 0,
      markup: itemData.markup !== undefined ? itemData.markup : this.defaultMarkup,
      taxable: itemData.taxable !== undefined ? itemData.taxable : true,
      optional: itemData.optional !== undefined ? itemData.optional : false,
      notes: itemData.notes || '',
      customFields: itemData.customFields || {}
    }
    
    section.items.push(item)
    this.updatedAt = new Date()
    
    return item
  }

  // Create an estimate from this template
  createEstimate(clientId: string | null = null, projectId: string | null = null): EstimateData {
    // Clone the template sections and items
    const sections: EstimateSection[] = this.sections.map(section => {
      return {
        ...section,
        id: `section-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
        items: section.items.map(item => {
          return {
            ...item,
            id: `item-${Date.now()}-${Math.floor(Math.random() * 1000)}`
          }
        })
      }
    })
    
    // Create the estimate
    const estimate: EstimateData = {
      id: `estimate-${Date.now()}`,
      title: `New Estimate - ${this.name}`,
      templateId: this.id,
      status: 'draft',
      clientId: clientId,
      projectId: projectId,
      issueDate: new Date(),
      expiryDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days from now
      sections,
      taxRate: this.defaultTaxRate,
      markup: this.defaultMarkup,
      currency: this.defaultCurrency,
      terms: this.termsAndConditions,
      notes: this.notes,
      customFields: {...this.customFields},
      createdAt: new Date(),
      updatedAt: new Date()
    }
    
    return estimate
  }

  // Remove a section
  removeSection(sectionId: string): boolean {
    const index = this.sections.findIndex(s => s.id === sectionId)
    if (index === -1) {
      return false
    }
    
    this.sections.splice(index, 1)
    this.updatedAt = new Date()
    return true
  }

  // Remove an item from a section
  removeItem(sectionId: string, itemId: string): boolean {
    const section = this.sections.find(s => s.id === sectionId)
    if (!section) {
      return false
    }
    
    const index = section.items.findIndex(i => i.id === itemId)
    if (index === -1) {
      return false
    }
    
    section.items.splice(index, 1)
    this.updatedAt = new Date()
    return true
  }

  // Get template data for serialization
  toJSON(): EstimateTemplateData {
    return {
      id: this.id,
      name: this.name,
      description: this.description,
      category: this.category,
      projectType: this.projectType,
      createdBy: this.createdBy,
      createdAt: this.createdAt,
      updatedAt: this.updatedAt,
      isActive: this.isActive,
      isPublic: this.isPublic,
      sections: this.sections,
      defaultMarkup: this.defaultMarkup,
      defaultTaxRate: this.defaultTaxRate,
      defaultCurrency: this.defaultCurrency,
      termsAndConditions: this.termsAndConditions,
      notes: this.notes,
      customFields: this.customFields
    }
  }
}

// Database interface (can be implemented with different storage backends)
export interface EstimateTemplateDatabase {
  collection(name: string): EstimateTemplateCollection
}

export interface EstimateTemplateCollection {
  doc(id: string): EstimateTemplateDocument
  where(field: string, operator: string, value: any): EstimateTemplateQuery
}

export interface EstimateTemplateDocument {
  set(data: EstimateTemplateData): Promise<void>
  get(): Promise<{ exists: boolean; data(): EstimateTemplateData | undefined }>
  update(data: Partial<EstimateTemplateData>): Promise<void>
}

export interface EstimateTemplateQuery {
  get(): Promise<{ docs: Array<{ data(): EstimateTemplateData }> }>
}

export class TemplateManager {
  private db: EstimateTemplateDatabase
  private templatesRef: EstimateTemplateCollection

  constructor(db: EstimateTemplateDatabase) {
    this.db = db
    this.templatesRef = db.collection('estimateTemplates')
  }

  // Save template to database
  async saveTemplate(template: EstimateTemplate): Promise<EstimateTemplate> {
    template.updatedAt = new Date()
    await this.templatesRef.doc(template.id).set(template.toJSON())
    return template
  }

  // Get template by ID
  async getTemplate(id: string): Promise<EstimateTemplate> {
    const doc = await this.templatesRef.doc(id).get()
    if (!doc.exists) {
      throw new Error(`Template not found: ${id}`)
    }
    const data = doc.data()
    if (!data) {
      throw new Error(`Template data not found: ${id}`)
    }
    return new EstimateTemplate(data)
  }

  // Get all templates
  async getAllTemplates(): Promise<EstimateTemplate[]> {
    const snapshot = await this.templatesRef.where('isActive', '==', true).get()
    return snapshot.docs.map(doc => new EstimateTemplate(doc.data()))
  }

  // Delete template (soft delete)
  async deleteTemplate(id: string): Promise<void> {
    await this.templatesRef.doc(id).update({
      isActive: false,
      updatedAt: new Date()
    })
  }

  // Get templates by category
  async getTemplatesByCategory(category: string): Promise<EstimateTemplate[]> {
    const allTemplates = await this.getAllTemplates()
    return allTemplates.filter(template => template.category === category)
  }

  // Get templates by project type
  async getTemplatesByProjectType(projectType: string): Promise<EstimateTemplate[]> {
    const allTemplates = await this.getAllTemplates()
    return allTemplates.filter(template => template.projectType === projectType)
  }
}

// In-memory implementation for development/testing
export class InMemoryTemplateManager extends TemplateManager {
  private templates: Map<string, EstimateTemplateData> = new Map()

  constructor() {
    // Create mock database interface
    const mockDb: EstimateTemplateDatabase = {
      collection: () => ({
        doc: (id: string) => ({
          set: async (data: EstimateTemplateData) => {
            this.templates.set(id, data)
          },
          get: async () => {
            const data = this.templates.get(id)
            return {
              exists: !!data,
              data: () => data
            }
          },
          update: async (updateData: Partial<EstimateTemplateData>) => {
            const existing = this.templates.get(id)
            if (existing) {
              this.templates.set(id, { ...existing, ...updateData })
            }
          }
        }),
        where: (field: string, operator: string, value: any) => ({
          get: async () => {
            const docs = Array.from(this.templates.values())
              .filter(data => {
                if (field === 'isActive' && operator === '==') {
                  return data.isActive === value
                }
                return true
              })
              .map(data => ({ data: () => data }))
            return { docs }
          }
        })
      })
    }
    
    super(mockDb)
  }

  // Add some sample templates for testing
  async createSampleTemplates(): Promise<void> {
    const webDevTemplate = new EstimateTemplate({
      name: 'Web Development Project',
      description: 'Standard web development project template',
      category: 'Web Development',
      projectType: 'Website',
      createdBy: 'system',
      defaultMarkup: 20,
      defaultTaxRate: 8.5,
      termsAndConditions: 'Payment terms: 50% upfront, 50% on completion'
    })

    const planningSection = webDevTemplate.addSection('Planning & Discovery', 'Initial project planning and requirements gathering')
    webDevTemplate.addItem(planningSection.id, {
      assemblyName: 'Requirements Analysis',
      description: 'Gather and document project requirements',
      quantity: 1,
      unit: 'phase',
      unitCost: 2500,
      notes: 'Includes stakeholder interviews and documentation'
    })

    const designSection = webDevTemplate.addSection('Design & UX', 'User interface and experience design')
    webDevTemplate.addItem(designSection.id, {
      assemblyName: 'UI/UX Design',
      description: 'Complete user interface design',
      quantity: 1,
      unit: 'phase',
      unitCost: 3500,
      notes: 'Includes wireframes, mockups, and prototypes'
    })

    const developmentSection = webDevTemplate.addSection('Development', 'Frontend and backend development')
    webDevTemplate.addItem(developmentSection.id, {
      assemblyName: 'Frontend Development',
      description: 'React/TypeScript frontend implementation',
      quantity: 1,
      unit: 'phase',
      unitCost: 5000
    })
    webDevTemplate.addItem(developmentSection.id, {
      assemblyName: 'Backend Development',
      description: 'Node.js/Express backend with database',
      quantity: 1,
      unit: 'phase',
      unitCost: 4500
    })

    await this.saveTemplate(webDevTemplate)

    // Create mobile app template
    const mobileAppTemplate = new EstimateTemplate({
      name: 'Mobile App Development',
      description: 'Cross-platform mobile app development template',
      category: 'Mobile Development',
      projectType: 'Mobile App',
      createdBy: 'system',
      defaultMarkup: 25,
      defaultTaxRate: 8.5,
      termsAndConditions: 'Payment terms: 30% upfront, 40% at milestone, 30% on completion'
    })

    const mobilePlanningSection = mobileAppTemplate.addSection('Planning & Architecture', 'Mobile app planning and technical architecture')
    mobileAppTemplate.addItem(mobilePlanningSection.id, {
      assemblyName: 'App Architecture',
      description: 'Define mobile app architecture and tech stack',
      quantity: 1,
      unit: 'phase',
      unitCost: 3000
    })

    const mobileDesignSection = mobileAppTemplate.addSection('Mobile UI/UX', 'Mobile-specific design and user experience')
    mobileAppTemplate.addItem(mobileDesignSection.id, {
      assemblyName: 'Mobile Design',
      description: 'iOS and Android UI design',
      quantity: 1,
      unit: 'phase',
      unitCost: 4000
    })

    const mobileDevelopmentSection = mobileAppTemplate.addSection('Development', 'Mobile app development')
    mobileAppTemplate.addItem(mobileDevelopmentSection.id, {
      assemblyName: 'React Native Development',
      description: 'Cross-platform mobile app development',
      quantity: 1,
      unit: 'phase',
      unitCost: 8000
    })

    await this.saveTemplate(mobileAppTemplate)
  }
}