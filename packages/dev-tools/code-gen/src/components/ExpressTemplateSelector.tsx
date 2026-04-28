import React, { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Database, Server, Globe, Zap, Code, Shield, Activity, Users } from '@phosphor-icons/react'
import { getTemplatesByFramework } from '@/templates/framework-templates'
import { toast } from 'sonner'

interface ExpressTemplateSelectorProps {
  onCreateProject: (projectData: {
    name: string
    description: string
    type: 'express'
    template: string
  }) => void
}

export const ExpressTemplateSelector: React.FC<ExpressTemplateSelectorProps> = ({ onCreateProject }) => {
  const [isOpen, setIsOpen] = useState(false)
  const [selectedTemplate, setSelectedTemplate] = useState<string | null>(null)
  const [projectName, setProjectName] = useState('')
  const [projectDescription, setProjectDescription] = useState('')

  const expressTemplates = getTemplatesByFramework('express')

  const getTemplateIcon = (templateId: string) => {
    switch (templateId) {
      case 'express-api-basic':
        return <Code className="w-6 h-6" />
      case 'express-mongodb':
        return <Database className="w-6 h-6" />
      case 'express-postgresql':
        return <Server className="w-6 h-6" />
      case 'express-microservices':
        return <Globe className="w-6 h-6" />
      default:
        return <Zap className="w-6 h-6" />
    }
  }

  const getTemplateColor = (templateId: string) => {
    switch (templateId) {
      case 'express-api-basic':
        return 'bg-blue-500'
      case 'express-mongodb':
        return 'bg-green-500'
      case 'express-postgresql':
        return 'bg-purple-500'
      case 'express-microservices':
        return 'bg-orange-500'
      default:
        return 'bg-gray-500'
    }
  }

  const getComplexityLevel = (templateId: string) => {
    switch (templateId) {
      case 'express-api-basic':
        return { level: 'Beginner', color: 'bg-green-100 text-green-800' }
      case 'express-mongodb':
        return { level: 'Intermediate', color: 'bg-yellow-100 text-yellow-800' }
      case 'express-postgresql':
        return { level: 'Advanced', color: 'bg-orange-100 text-orange-800' }
      case 'express-microservices':
        return { level: 'Expert', color: 'bg-red-100 text-red-800' }
      default:
        return { level: 'Beginner', color: 'bg-green-100 text-green-800' }
    }
  }

  const handleCreateProject = () => {
    if (!selectedTemplate || !projectName.trim()) {
      toast.error('Please select a template and enter a project name')
      return
    }

    onCreateProject({
      name: projectName,
      description: projectDescription || `Express API project using ${selectedTemplate}`,
      type: 'express',
      template: selectedTemplate
    })

    setIsOpen(false)
    setSelectedTemplate(null)
    setProjectName('')
    setProjectDescription('')
    toast.success('Express API project created successfully!')
  }

  const selectedTemplateData = expressTemplates.find(t => t.id === selectedTemplate)

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button className="flex items-center gap-2">
          <Server className="w-4 h-4" />
          Create Express API
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Server className="w-5 h-5" />
            Create Express API Project
          </DialogTitle>
        </DialogHeader>

        <Tabs defaultValue="templates" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="templates">Choose Template</TabsTrigger>
            <TabsTrigger value="details" disabled={!selectedTemplate}>Project Details</TabsTrigger>
          </TabsList>

          <TabsContent value="templates" className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {expressTemplates.map((template) => {
                const complexity = getComplexityLevel(template.id)
                const isSelected = selectedTemplate === template.id

                return (
                  <Card 
                    key={template.id}
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      isSelected ? 'ring-2 ring-primary shadow-md' : ''
                    }`}
                    onClick={() => setSelectedTemplate(template.id)}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-3">
                          <div className={`w-10 h-10 ${getTemplateColor(template.id)} rounded-lg flex items-center justify-center text-white`}>
                            {getTemplateIcon(template.id)}
                          </div>
                          <div>
                            <CardTitle className="text-lg">{template.name}</CardTitle>
                            <Badge className={complexity.color} variant="secondary">
                              {complexity.level}
                            </Badge>
                          </div>
                        </div>
                        {isSelected && (
                          <div className="w-6 h-6 bg-primary rounded-full flex items-center justify-center">
                            <div className="w-3 h-3 bg-white rounded-full" />
                          </div>
                        )}
                      </div>
                      <CardDescription className="text-sm">{template.description}</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        <div>
                          <p className="text-sm font-medium mb-2">Key Features:</p>
                          <div className="flex flex-wrap gap-1">
                            {template.features.slice(0, 4).map((feature) => (
                              <Badge key={feature} variant="outline" className="text-xs">
                                {feature}
                              </Badge>
                            ))}
                            {template.features.length > 4 && (
                              <Badge variant="outline" className="text-xs">
                                +{template.features.length - 4} more
                              </Badge>
                            )}
                          </div>
                        </div>
                        
                        <div className="flex items-center justify-between text-xs text-muted-foreground">
                          <div className="flex items-center gap-4">
                            <div className="flex items-center gap-1">
                              <Database className="w-3 h-3" />
                              {template.id.includes('mongodb') ? 'MongoDB' : 
                               template.id.includes('postgresql') ? 'PostgreSQL' : 
                               template.id.includes('microservices') ? 'Multi-DB' : 'In-Memory'}
                            </div>
                            <div className="flex items-center gap-1">
                              <Shield className="w-3 h-3" />
                              {template.features.includes('JWT Auth') ? 'Auth' : 'Basic'}
                            </div>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )
              })}
            </div>

            {selectedTemplate && (
              <div className="border-t pt-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium">Selected: {selectedTemplateData?.name}</p>
                    <p className="text-sm text-muted-foreground">
                      {selectedTemplateData?.features.length} features included
                    </p>
                  </div>
                  <Button onClick={() => {
                    const tabsList = document.querySelector('[value="details"]') as HTMLElement
                    tabsList?.click()
                  }}>
                    Configure Project
                  </Button>
                </div>
              </div>
            )}
          </TabsContent>

          <TabsContent value="details" className="space-y-6">
            {selectedTemplateData && (
              <>
                <div className="border rounded-lg p-4 bg-muted/50">
                  <div className="flex items-center gap-3 mb-3">
                    <div className={`w-8 h-8 ${getTemplateColor(selectedTemplateData.id)} rounded-lg flex items-center justify-center text-white`}>
                      {getTemplateIcon(selectedTemplateData.id)}
                    </div>
                    <div>
                      <h3 className="font-semibold">{selectedTemplateData.name}</h3>
                      <p className="text-sm text-muted-foreground">{selectedTemplateData.description}</p>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
                    <div className="text-center">
                      <Database className="w-5 h-5 mx-auto mb-1 text-muted-foreground" />
                      <p className="text-xs font-medium">Database</p>
                      <p className="text-xs text-muted-foreground">
                        {selectedTemplateData.id.includes('mongodb') ? 'MongoDB' : 
                         selectedTemplateData.id.includes('postgresql') ? 'PostgreSQL' : 
                         selectedTemplateData.id.includes('microservices') ? 'Multi-DB' : 'In-Memory'}
                      </p>
                    </div>
                    
                    <div className="text-center">
                      <Shield className="w-5 h-5 mx-auto mb-1 text-muted-foreground" />
                      <p className="text-xs font-medium">Security</p>
                      <p className="text-xs text-muted-foreground">
                        {selectedTemplateData.features.includes('JWT Auth') ? 'JWT + RBAC' : 'Basic'}
                      </p>
                    </div>
                    
                    <div className="text-center">
                      <Activity className="w-5 h-5 mx-auto mb-1 text-muted-foreground" />
                      <p className="text-xs font-medium">Monitoring</p>
                      <p className="text-xs text-muted-foreground">
                        {selectedTemplateData.id.includes('microservices') ? 'Full Stack' : 'Basic'}
                      </p>
                    </div>
                    
                    <div className="text-center">
                      <Users className="w-5 h-5 mx-auto mb-1 text-muted-foreground" />
                      <p className="text-xs font-medium">Scalability</p>
                      <p className="text-xs text-muted-foreground">
                        {selectedTemplateData.id.includes('microservices') ? 'Microservices' : 'Monolith'}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="projectName">Project Name *</Label>
                      <Input
                        id="projectName"
                        value={projectName}
                        onChange={(e) => setProjectName(e.target.value)}
                        placeholder="my-express-api"
                      />
                    </div>
                    
                    <div>
                      <Label htmlFor="projectDescription">Description</Label>
                      <Textarea
                        id="projectDescription"
                        value={projectDescription}
                        onChange={(e) => setProjectDescription(e.target.value)}
                        placeholder="Describe your API project..."
                        rows={3}
                      />
                    </div>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <Label>Included Features</Label>
                      <div className="mt-2 space-y-2 max-h-32 overflow-y-auto">
                        {selectedTemplateData.features.map((feature) => (
                          <div key={feature} className="flex items-center gap-2 text-sm">
                            <div className="w-2 h-2 bg-green-500 rounded-full" />
                            {feature}
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t">
                  <Button 
                    variant="outline" 
                    onClick={() => {
                      const tabsList = document.querySelector('[value="templates"]') as HTMLElement
                      tabsList?.click()
                    }}
                  >
                    Back to Templates
                  </Button>
                  <Button onClick={handleCreateProject} disabled={!projectName.trim()}>
                    Create Express API Project
                  </Button>
                </div>
              </>
            )}
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  )
}