/**
 * Construction Project Generator Component
 * 
 * Specialized project generator for construction industry projects
 * including Interior Finishes, Plumbing, Electrical, and Foundation work
 */

import React, { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { 
  constructionTemplates, 
  getConstructionTemplatesByCategory, 
  type ConstructionTemplate,
  type ConstructionCategory,
  type ConstructionAssembly,
  type ConstructionTask
} from '@/templates/construction-templates'
import { 
  Hammer, 
  Wrench, 
  Zap, 
  Home, 
  CheckCircle, 
  Clock, 
  AlertTriangle,
  Plus,
  Building,
  Gear,
  PaintBucket
} from '@phosphor-icons/react'
import { toast } from 'sonner'

interface ConstructionProjectGeneratorProps {
  onCreateProject: (project: any) => void
}

export function ConstructionProjectGenerator({ onCreateProject }: ConstructionProjectGeneratorProps) {
  const [projectName, setProjectName] = useState('')
  const [projectDescription, setProjectDescription] = useState('')
  const [selectedTemplate, setSelectedTemplate] = useState<ConstructionTemplate | null>(null)
  const [selectedCategory, setSelectedCategory] = useState<'all' | 'interior-finishes' | 'plumbing' | 'electrical' | 'foundation'>('all')
  const [projectType, setProjectType] = useState<'residential' | 'commercial' | 'industrial'>('residential')
  const [isGenerating, setIsGenerating] = useState(false)
  const [showTemplateDetails, setShowTemplateDetails] = useState(false)

  const categories = [
    { id: 'all', name: 'All Construction', icon: <Building className="w-4 h-4" /> },
    { id: 'interior-finishes', name: 'Interior Finishes', icon: <PaintBucket className="w-4 h-4" /> },
    { id: 'plumbing', name: 'Plumbing Systems', icon: <Wrench className="w-4 h-4" /> },
    { id: 'electrical', name: 'Electrical Systems', icon: <Zap className="w-4 h-4" /> },
    { id: 'foundation', name: 'Foundation & Basement', icon: <Home className="w-4 h-4" /> }
  ]

  const templates = selectedCategory === 'all' 
    ? constructionTemplates 
    : getConstructionTemplatesByCategory(selectedCategory)

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'interior-finishes': return <PaintBucket className="w-5 h-5" />
      case 'plumbing': return <Wrench className="w-5 h-5" />
      case 'electrical': return <Zap className="w-5 h-5" />
      case 'foundation': return <Home className="w-5 h-5" />
      default: return <Building className="w-5 h-5" />
    }
  }

  const getComplexityColor = (complexity: string) => {
    switch (complexity) {
      case 'basic': return 'bg-green-100 text-green-800 border-green-200'
      case 'intermediate': return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'advanced': return 'bg-red-100 text-red-800 border-red-200'
      default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const generateConstructionProject = async () => {
    if (!projectName || !selectedTemplate) {
      toast.error('Please provide project name and select a template')
      return
    }

    setIsGenerating(true)
    
    try {
      // Calculate total tasks and estimated time
      const totalTasks = selectedTemplate.categories.reduce((total, category) => {
        return total + category.scopes.reduce((scopeTotal, scope) => {
          return scopeTotal + scope.assemblies.reduce((assemblyTotal, assembly) => {
            return assemblyTotal + assembly.tasks.length
          }, 0)
        }, 0)
      }, 0)

      const totalEstimatedTime = selectedTemplate.categories.reduce((total, category) => {
        return total + category.scopes.reduce((scopeTotal, scope) => {
          return scopeTotal + scope.assemblies.reduce((assemblyTotal, assembly) => {
            return assemblyTotal + (assembly.estimatedTime || 0)
          }, 0)
        }, 0)
      }, 0)

      // Generate project structure
      const constructionProject = {
        id: Date.now().toString(),
        name: projectName,
        description: projectDescription || `${selectedTemplate.name} project`,
        type: 'construction' as const,
        template: selectedTemplate.id,
        status: 'development' as const,
        lastModified: Date.now(),
        constructionData: {
          template: selectedTemplate,
          projectType,
          totalTasks,
          completedTasks: 0,
          totalEstimatedTime,
          startDate: new Date().toISOString(),
          categories: selectedTemplate.categories.map(category => ({
            ...category,
            completed: false,
            progress: 0
          }))
        },
        codebase: {
          files: generateConstructionFiles(selectedTemplate, projectName),
          dependencies: []
        }
      }

      onCreateProject(constructionProject)
      toast.success(`Construction project "${projectName}" created successfully!`)
      
      // Reset form
      setProjectName('')
      setProjectDescription('')
      setSelectedTemplate(null)
      
    } catch (error) {
      toast.error('Failed to generate construction project')
      console.error('Construction project generation error:', error)
    } finally {
      setIsGenerating(false)
    }
  }

  const generateConstructionFiles = (template: ConstructionTemplate, projectName: string): Record<string, string> => {
    const files: Record<string, string> = {}

    // Project overview file
    files['PROJECT_OVERVIEW.md'] = `# ${projectName}

## Project Details
- **Type**: ${template.name}
- **Trade**: ${template.trade}
- **Estimated Duration**: ${template.estimatedDuration}
- **Complexity**: ${template.complexity}
- **Project Type**: ${template.projectType}

## Categories
${template.categories.map(category => `
### ${category.name}
**Trade**: ${category.trade}
**Description**: ${category.description}

${category.scopes.map(scope => `
#### ${scope.name}
${scope.description}

**Assemblies:**
${scope.assemblies.map(assembly => `
- **${assembly.name}** (${assembly.estimatedTime || 'N/A'} hours)
  - ${assembly.description}
  - Tasks: ${assembly.tasks.length}
`).join('')}
`).join('')}
`).join('')}

## Tags
${template.tags.map(tag => `- ${tag}`).join('\n')}
`

    // Task checklist file
    files['TASK_CHECKLIST.md'] = `# ${projectName} - Task Checklist

${template.categories.map(category => `
## ${category.name}

${category.scopes.map(scope => `
### ${scope.name}

${scope.assemblies.map(assembly => `
#### ${assembly.name}
**Estimated Time**: ${assembly.estimatedTime || 'N/A'} hours

${assembly.tasks.map((task, index) => `
- [ ] **${task.name}**
  - Description: ${task.description}
  - Dependencies: ${task.dependencies?.join(', ') || 'None'}
`).join('')}
`).join('')}
`).join('')}
`).join('')}
`

    // Progress tracking file
    files['PROGRESS_TRACKER.json'] = JSON.stringify({
      projectName,
      template: template.id,
      startDate: new Date().toISOString(),
      categories: template.categories.map(category => ({
        id: category.id,
        name: category.name,
        completed: false,
        progress: 0,
        scopes: category.scopes.map(scope => ({
          id: scope.id,
          name: scope.name,
          completed: false,
          assemblies: scope.assemblies.map(assembly => ({
            id: assembly.id,
            name: assembly.name,
            completed: false,
            estimatedTime: assembly.estimatedTime,
            tasks: assembly.tasks.map(task => ({
              id: task.id,
              name: task.name,
              completed: false
            }))
          }))
        }))
      }))
    }, null, 2)

    return files
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Hammer className="w-5 h-5" />
            Construction Project Generator
          </CardTitle>
          <CardDescription>
            Generate comprehensive construction projects with detailed task breakdowns and progress tracking
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="project-name">Project Name</Label>
              <Input
                id="project-name"
                placeholder="e.g., Downtown Office Renovation"
                value={projectName}
                onChange={(e) => setProjectName(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="project-type">Project Type</Label>
              <Select value={projectType} onValueChange={(value: any) => setProjectType(value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select project type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="residential">Residential</SelectItem>
                  <SelectItem value="commercial">Commercial</SelectItem>
                  <SelectItem value="industrial">Industrial</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="project-description">Project Description (Optional)</Label>
            <Textarea
              id="project-description"
              placeholder="Describe the project scope and requirements..."
              value={projectDescription}
              onChange={(e) => setProjectDescription(e.target.value)}
              rows={3}
            />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Select Construction Template</CardTitle>
          <CardDescription>Choose a specialized template for your construction project</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs value={selectedCategory} onValueChange={(value: any) => setSelectedCategory(value)}>
            <TabsList className="grid grid-cols-5 w-full">
              {categories.map(category => (
                <TabsTrigger key={category.id} value={category.id} className="flex items-center gap-1">
                  {category.icon}
                  <span className="hidden sm:inline">{category.name}</span>
                </TabsTrigger>
              ))}
            </TabsList>

            <TabsContent value={selectedCategory} className="mt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {templates.map(template => (
                  <Card 
                    key={template.id} 
                    className={`cursor-pointer transition-all ${
                      selectedTemplate?.id === template.id 
                        ? 'ring-2 ring-primary bg-primary/5' 
                        : 'hover:shadow-md'
                    }`}
                    onClick={() => setSelectedTemplate(template)}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-2">
                          {getCategoryIcon(template.category)}
                          <CardTitle className="text-lg">{template.name}</CardTitle>
                        </div>
                        <Badge className={getComplexityColor(template.complexity)}>
                          {template.complexity}
                        </Badge>
                      </div>
                      <CardDescription className="text-sm">
                        {template.description}
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="pt-0">
                      <div className="space-y-2">
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          <Gear className="w-4 h-4" />
                          <span>Trade: {template.trade}</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          <Clock className="w-4 h-4" />
                          <span>Duration: {template.estimatedDuration}</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          <Building className="w-4 h-4" />
                          <span>Categories: {template.categories.length}</span>
                        </div>
                        <div className="flex flex-wrap gap-1 pt-2">
                          {template.tags.slice(0, 3).map(tag => (
                            <Badge key={tag} variant="outline" className="text-xs">
                              {tag}
                            </Badge>
                          ))}
                          {template.tags.length > 3 && (
                            <Badge variant="outline" className="text-xs">
                              +{template.tags.length - 3} more
                            </Badge>
                          )}
                        </div>
                      </div>
                      
                      {selectedTemplate?.id === template.id && (
                        <div className="mt-3 pt-3 border-t">
                          <Button 
                            variant="outline" 
                            size="sm" 
                            onClick={(e) => {
                              e.stopPropagation()
                              setShowTemplateDetails(true)
                            }}
                          >
                            View Details
                          </Button>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </div>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {selectedTemplate && (
        <Card>
          <CardHeader>
            <CardTitle>Generate Construction Project</CardTitle>
            <CardDescription>
              Create a comprehensive project with task tracking and progress monitoring
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="font-medium">Selected: {selectedTemplate.name}</p>
                <p className="text-sm text-muted-foreground">
                  {selectedTemplate.categories.length} categories • {selectedTemplate.trade}
                </p>
              </div>
              <Button 
                onClick={generateConstructionProject}
                disabled={isGenerating || !projectName}
                className="min-w-[140px]"
              >
                {isGenerating ? (
                  <>
                    <Gear className="w-4 h-4 mr-2 animate-spin" />
                    Generating...
                  </>
                ) : (
                  <>
                    <Plus className="w-4 h-4 mr-2" />
                    Generate Project
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Template Details Dialog */}
      <Dialog open={showTemplateDetails} onOpenChange={setShowTemplateDetails}>
        <DialogContent className="max-w-4xl max-h-[80vh]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              {selectedTemplate && getCategoryIcon(selectedTemplate.category)}
              {selectedTemplate?.name} - Details
            </DialogTitle>
            <DialogDescription>
              Comprehensive breakdown of all categories, scopes, assemblies, and tasks
            </DialogDescription>
          </DialogHeader>
          
          {selectedTemplate && (
            <ScrollArea className="max-h-[60vh]">
              <div className="space-y-4">
                {selectedTemplate.categories.map(category => (
                  <Card key={category.id}>
                    <CardHeader className="pb-3">
                      <CardTitle className="text-lg">{category.name}</CardTitle>
                      <CardDescription>{category.description}</CardDescription>
                      <Badge variant="outline">Trade: {category.trade}</Badge>
                    </CardHeader>
                    <CardContent>
                      {category.scopes.map(scope => (
                        <div key={scope.id} className="mb-4 last:mb-0">
                          <h4 className="font-medium mb-2">{scope.name}</h4>
                          <p className="text-sm text-muted-foreground mb-3">{scope.description}</p>
                          
                          {scope.assemblies.map(assembly => (
                            <div key={assembly.id} className="ml-4 mb-3 p-3 border rounded-lg">
                              <div className="flex items-center justify-between mb-2">
                                <h5 className="font-medium">{assembly.name}</h5>
                                {assembly.estimatedTime && (
                                  <Badge variant="secondary">
                                    {assembly.estimatedTime}h
                                  </Badge>
                                )}
                              </div>
                              <p className="text-sm text-muted-foreground mb-2">{assembly.description}</p>
                              
                              <div className="space-y-1">
                                <p className="text-sm font-medium">Tasks ({assembly.tasks.length}):</p>
                                {assembly.tasks.map(task => (
                                  <div key={task.id} className="flex items-start gap-2 text-sm">
                                    <CheckCircle className="w-4 h-4 mt-0.5 text-muted-foreground" />
                                    <div>
                                      <span className="font-medium">{task.name}</span>
                                      <p className="text-muted-foreground">{task.description}</p>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </div>
                          ))}
                        </div>
                      ))}
                    </CardContent>
                  </Card>
                ))}
              </div>
            </ScrollArea>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}