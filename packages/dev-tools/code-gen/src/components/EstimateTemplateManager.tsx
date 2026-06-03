/**
 * Estimate Template Manager Component
 * 
 * Provides UI for creating, editing, and managing project estimate templates
 */

import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Separator } from '@/components/ui/separator'
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog'
import { EstimateTemplate, InMemoryTemplateManager, EstimateSection, EstimateItem } from '@/utils/EstimateTemplate'
import { Plus, Trash2, Edit, Calculator, FileText, DollarSign, Clock } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface EstimateTemplateManagerProps {
  onCreateProject?: (projectData: {
    name: string
    description: string
    type: 'estimate'
    template: string
  }) => void
}

export const EstimateTemplateManager: React.FC<EstimateTemplateManagerProps> = ({ onCreateProject }) => {
  const [templates, setTemplates] = useState<EstimateTemplate[]>([])
  const [selectedTemplate, setSelectedTemplate] = useState<EstimateTemplate | null>(null)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false)
  const [templateManager] = useState(() => new InMemoryTemplateManager())
  
  // Form state for creating/editing templates
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: '',
    projectType: '',
    defaultMarkup: 15,
    defaultTaxRate: 7,
    defaultCurrency: 'USD',
    termsAndConditions: '',
    notes: ''
  })

  // Load templates on component mount
  useEffect(() => {
    loadTemplates()
  }, [])

  const loadTemplates = async () => {
    try {
      // Create sample templates if none exist
      const existingTemplates = await templateManager.getAllTemplates()
      if (existingTemplates.length === 0) {
        await templateManager.createSampleTemplates()
      }
      
      const allTemplates = await templateManager.getAllTemplates()
      setTemplates(allTemplates)
    } catch (error) {
      console.error('Error loading templates:', error)
      toast.error('Failed to load templates')
    }
  }

  const handleCreateTemplate = async () => {
    try {
      const template = new EstimateTemplate({
        ...formData,
        createdBy: 'current-user' // In a real app, get from auth context
      })
      
      await templateManager.saveTemplate(template)
      await loadTemplates()
      
      setIsCreateDialogOpen(false)
      resetForm()
      toast.success('Template created successfully!')
    } catch (error) {
      console.error('Error creating template:', error)
      toast.error('Failed to create template')
    }
  }

  const handleEditTemplate = async () => {
    if (!selectedTemplate) return
    
    try {
      // Update template properties
      Object.assign(selectedTemplate, formData)
      
      await templateManager.saveTemplate(selectedTemplate)
      await loadTemplates()
      
      setIsEditDialogOpen(false)
      setSelectedTemplate(null)
      resetForm()
      toast.success('Template updated successfully!')
    } catch (error) {
      console.error('Error updating template:', error)
      toast.error('Failed to update template')
    }
  }

  const handleDeleteTemplate = async (template: EstimateTemplate) => {
    try {
      await templateManager.deleteTemplate(template.id)
      await loadTemplates()
      toast.success('Template deleted successfully!')
    } catch (error) {
      console.error('Error deleting template:', error)
      toast.error('Failed to delete template')
    }
  }

  const handleCreateEstimate = (template: EstimateTemplate) => {
    try {
      const estimate = template.createEstimate()
      
      if (onCreateProject) {
        onCreateProject({
          name: estimate.title,
          description: `Estimate created from template: ${template.name}`,
          type: 'estimate',
          template: template.id
        })
      }
      
      toast.success('Estimate created successfully!')
    } catch (error) {
      console.error('Error creating estimate:', error)
      toast.error('Failed to create estimate')
    }
  }

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      category: '',
      projectType: '',
      defaultMarkup: 15,
      defaultTaxRate: 7,
      defaultCurrency: 'USD',
      termsAndConditions: '',
      notes: ''
    })
  }

  const openEditDialog = (template: EstimateTemplate) => {
    setSelectedTemplate(template)
    setFormData({
      name: template.name,
      description: template.description,
      category: template.category,
      projectType: template.projectType,
      defaultMarkup: template.defaultMarkup,
      defaultTaxRate: template.defaultTaxRate,
      defaultCurrency: template.defaultCurrency,
      termsAndConditions: template.termsAndConditions,
      notes: template.notes
    })
    setIsEditDialogOpen(true)
  }

  const openViewDialog = (template: EstimateTemplate) => {
    setSelectedTemplate(template)
    setIsViewDialogOpen(true)
  }

  const calculateTemplateTotal = (template: EstimateTemplate): number => {
    return template.sections.reduce((sectionTotal, section) => {
      return sectionTotal + section.items.reduce((itemTotal, item) => {
        const costWithMarkup = item.unitCost * (1 + item.markup / 100)
        const totalCost = costWithMarkup * item.quantity
        return itemTotal + (item.taxable ? totalCost * (1 + template.defaultTaxRate / 100) : totalCost)
      }, 0)
    }, 0)
  }

  const getCategoryColor = (category: string) => {
    const colors: Record<string, string> = {
      'Web Development': 'bg-blue-100 text-blue-800',
      'Mobile Development': 'bg-green-100 text-green-800',
      'Desktop Development': 'bg-purple-100 text-purple-800',
      'Consulting': 'bg-orange-100 text-orange-800',
      'Design': 'bg-pink-100 text-pink-800',
      'Other': 'bg-gray-100 text-gray-800'
    }
    return colors[category] || colors['Other']
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Estimate Templates</h2>
          <p className="text-muted-foreground">
            Create and manage reusable project estimate templates
          </p>
        </div>
        
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button className="flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Create Template
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Create New Template</DialogTitle>
              <DialogDescription>
                Create a reusable template for project estimates
              </DialogDescription>
            </DialogHeader>
            
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name">Template Name</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                  placeholder="e.g., Web Development Project"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="category">Category</Label>
                <Select value={formData.category} onValueChange={(value) => setFormData(prev => ({ ...prev, category: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select category" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Web Development">Web Development</SelectItem>
                    <SelectItem value="Mobile Development">Mobile Development</SelectItem>
                    <SelectItem value="Desktop Development">Desktop Development</SelectItem>
                    <SelectItem value="Consulting">Consulting</SelectItem>
                    <SelectItem value="Design">Design</SelectItem>
                    <SelectItem value="Other">Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="projectType">Project Type</Label>
                <Input
                  id="projectType"
                  value={formData.projectType}
                  onChange={(e) => setFormData(prev => ({ ...prev, projectType: e.target.value }))}
                  placeholder="e.g., Website, Mobile App"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="defaultCurrency">Currency</Label>
                <Select value={formData.defaultCurrency} onValueChange={(value) => setFormData(prev => ({ ...prev, defaultCurrency: value }))}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USD">USD ($)</SelectItem>
                    <SelectItem value="EUR">EUR (€)</SelectItem>
                    <SelectItem value="GBP">GBP (£)</SelectItem>
                    <SelectItem value="CAD">CAD (C$)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="defaultMarkup">Default Markup (%)</Label>
                <Input
                  id="defaultMarkup"
                  type="number"
                  value={formData.defaultMarkup}
                  onChange={(e) => setFormData(prev => ({ ...prev, defaultMarkup: Number(e.target.value) }))}
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="defaultTaxRate">Default Tax Rate (%)</Label>
                <Input
                  id="defaultTaxRate"
                  type="number"
                  step="0.1"
                  value={formData.defaultTaxRate}
                  onChange={(e) => setFormData(prev => ({ ...prev, defaultTaxRate: Number(e.target.value) }))}
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                placeholder="Describe what this template is used for..."
                rows={3}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="terms">Terms and Conditions</Label>
              <Textarea
                id="terms"
                value={formData.termsAndConditions}
                onChange={(e) => setFormData(prev => ({ ...prev, termsAndConditions: e.target.value }))}
                placeholder="Default terms and conditions for estimates..."
                rows={3}
              />
            </div>
            
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsCreateDialogOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreateTemplate} disabled={!formData.name.trim()}>
                Create Template
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Templates Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {templates.map((template) => (
          <Card key={template.id} className="hover:shadow-md transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <CardTitle className="text-lg">{template.name}</CardTitle>
                  <CardDescription className="mt-1">
                    {template.description}
                  </CardDescription>
                </div>
                <Badge className={getCategoryColor(template.category)}>
                  {template.category}
                </Badge>
              </div>
            </CardHeader>
            
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div className="flex items-center gap-2">
                  <FileText className="w-4 h-4 text-muted-foreground" />
                  <span>{template.sections.length} sections</span>
                </div>
                <div className="flex items-center gap-2">
                  <Calculator className="w-4 h-4 text-muted-foreground" />
                  <span>{template.sections.reduce((acc, section) => acc + section.items.length, 0)} items</span>
                </div>
                <div className="flex items-center gap-2">
                  <DollarSign className="w-4 h-4 text-muted-foreground" />
                  <span>{template.defaultCurrency} {calculateTemplateTotal(template).toFixed(2)}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-muted-foreground" />
                  <span>{template.updatedAt.toLocaleDateString()}</span>
                </div>
              </div>
              
              <Separator />
              
              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  onClick={() => handleCreateEstimate(template)}
                  className="flex-1"
                >
                  Create Estimate
                </Button>
                
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => openViewDialog(template)}
                >
                  <FileText className="w-4 h-4" />
                </Button>
                
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => openEditDialog(template)}
                >
                  <Edit className="w-4 h-4" />
                </Button>
                
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button variant="outline" size="sm">
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Delete Template</AlertDialogTitle>
                      <AlertDialogDescription>
                        Are you sure you want to delete "{template.name}"? This action cannot be undone.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction onClick={() => handleDeleteTemplate(template)}>
                        Delete
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Edit Template Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Edit Template</DialogTitle>
            <DialogDescription>
              Update template details
            </DialogDescription>
          </DialogHeader>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="edit-name">Template Name</Label>
              <Input
                id="edit-name"
                value={formData.name}
                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="edit-category">Category</Label>
              <Select value={formData.category} onValueChange={(value) => setFormData(prev => ({ ...prev, category: value }))}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Web Development">Web Development</SelectItem>
                  <SelectItem value="Mobile Development">Mobile Development</SelectItem>
                  <SelectItem value="Desktop Development">Desktop Development</SelectItem>
                  <SelectItem value="Consulting">Consulting</SelectItem>
                  <SelectItem value="Design">Design</SelectItem>
                  <SelectItem value="Other">Other</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="edit-projectType">Project Type</Label>
              <Input
                id="edit-projectType"
                value={formData.projectType}
                onChange={(e) => setFormData(prev => ({ ...prev, projectType: e.target.value }))}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="edit-defaultCurrency">Currency</Label>
              <Select value={formData.defaultCurrency} onValueChange={(value) => setFormData(prev => ({ ...prev, defaultCurrency: value }))}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="USD">USD ($)</SelectItem>
                  <SelectItem value="EUR">EUR (€)</SelectItem>
                  <SelectItem value="GBP">GBP (£)</SelectItem>
                  <SelectItem value="CAD">CAD (C$)</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="edit-defaultMarkup">Default Markup (%)</Label>
              <Input
                id="edit-defaultMarkup"
                type="number"
                value={formData.defaultMarkup}
                onChange={(e) => setFormData(prev => ({ ...prev, defaultMarkup: Number(e.target.value) }))}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="edit-defaultTaxRate">Default Tax Rate (%)</Label>
              <Input
                id="edit-defaultTaxRate"
                type="number"
                step="0.1"
                value={formData.defaultTaxRate}
                onChange={(e) => setFormData(prev => ({ ...prev, defaultTaxRate: Number(e.target.value) }))}
              />
            </div>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="edit-description">Description</Label>
            <Textarea
              id="edit-description"
              value={formData.description}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              rows={3}
            />
          </div>
          
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleEditTemplate}>
              Update Template
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* View Template Dialog */}
      <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              {selectedTemplate?.name}
            </DialogTitle>
            <DialogDescription>
              {selectedTemplate?.description}
            </DialogDescription>
          </DialogHeader>
          
          {selectedTemplate && (
            <Tabs defaultValue="overview" className="w-full">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="overview">Overview</TabsTrigger>
                <TabsTrigger value="sections">Sections & Items</TabsTrigger>
                <TabsTrigger value="settings">Settings</TabsTrigger>
              </TabsList>
              
              <TabsContent value="overview" className="space-y-4">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <div className="text-2xl font-bold text-primary">
                      {selectedTemplate.sections.length}
                    </div>
                    <div className="text-sm text-muted-foreground">Sections</div>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <div className="text-2xl font-bold text-primary">
                      {selectedTemplate.sections.reduce((acc, section) => acc + section.items.length, 0)}
                    </div>
                    <div className="text-sm text-muted-foreground">Items</div>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <div className="text-2xl font-bold text-primary">
                      {selectedTemplate.defaultMarkup}%
                    </div>
                    <div className="text-sm text-muted-foreground">Markup</div>
                  </div>
                  <div className="text-center p-4 bg-muted rounded-lg">
                    <div className="text-2xl font-bold text-primary">
                      {selectedTemplate.defaultCurrency} {calculateTemplateTotal(selectedTemplate).toFixed(2)}
                    </div>
                    <div className="text-sm text-muted-foreground">Est. Total</div>
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label>Category & Type</Label>
                  <div className="flex gap-2">
                    <Badge className={getCategoryColor(selectedTemplate.category)}>
                      {selectedTemplate.category}
                    </Badge>
                    <Badge variant="outline">{selectedTemplate.projectType}</Badge>
                  </div>
                </div>
                
                {selectedTemplate.termsAndConditions && (
                  <div className="space-y-2">
                    <Label>Terms and Conditions</Label>
                    <div className="p-3 bg-muted rounded-lg text-sm">
                      {selectedTemplate.termsAndConditions}
                    </div>
                  </div>
                )}
              </TabsContent>
              
              <TabsContent value="sections" className="space-y-4">
                <div className="space-y-4 max-h-[60vh] overflow-y-auto">
                  {selectedTemplate.sections.map((section) => (
                    <Card key={section.id}>
                      <CardHeader className="pb-3">
                        <CardTitle className="text-lg">{section.name}</CardTitle>
                        {section.description && (
                          <CardDescription>{section.description}</CardDescription>
                        )}
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-2">
                          {section.items.map((item) => (
                            <div key={item.id} className="flex items-center justify-between p-2 bg-muted rounded">
                              <div className="flex-1">
                                <div className="font-medium">{item.assemblyName || item.description}</div>
                                <div className="text-sm text-muted-foreground">
                                  {item.quantity} {item.unit} × {selectedTemplate.defaultCurrency}{item.unitCost}
                                  {item.markup > 0 && ` (+${item.markup}% markup)`}
                                </div>
                              </div>
                              <div className="text-right">
                                <div className="font-medium">
                                  {selectedTemplate.defaultCurrency}{(item.unitCost * item.quantity * (1 + item.markup / 100)).toFixed(2)}
                                </div>
                                {item.taxable && (
                                  <div className="text-xs text-muted-foreground">+ tax</div>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </TabsContent>
              
              <TabsContent value="settings" className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label>Default Markup</Label>
                    <div className="text-lg font-semibold">{selectedTemplate.defaultMarkup}%</div>
                  </div>
                  <div>
                    <Label>Default Tax Rate</Label>
                    <div className="text-lg font-semibold">{selectedTemplate.defaultTaxRate}%</div>
                  </div>
                  <div>
                    <Label>Currency</Label>
                    <div className="text-lg font-semibold">{selectedTemplate.defaultCurrency}</div>
                  </div>
                  <div>
                    <Label>Created</Label>
                    <div className="text-lg font-semibold">{selectedTemplate.createdAt.toLocaleDateString()}</div>
                  </div>
                </div>
                
                {selectedTemplate.notes && (
                  <div className="space-y-2">
                    <Label>Notes</Label>
                    <div className="p-3 bg-muted rounded-lg text-sm">
                      {selectedTemplate.notes}
                    </div>
                  </div>
                )}
              </TabsContent>
            </Tabs>
          )}
          
          <div className="flex justify-end">
            <Button onClick={() => setIsViewDialogOpen(false)}>
              Close
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}

export default EstimateTemplateManager