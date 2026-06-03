import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Smartphone, Code, Layers, Navigation, Settings } from '@phosphor-icons/react'
import { getTemplatesByFramework } from '@/templates/framework-templates'

interface AndroidTemplateSelectorProps {
  onCreateProject: (project: any) => void
}

export function AndroidTemplateSelector({ onCreateProject }: AndroidTemplateSelectorProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [selectedTemplate, setSelectedTemplate] = useState<string>('')
  const [projectName, setProjectName] = useState('')
  const [projectDescription, setProjectDescription] = useState('')

  const androidTemplates = getTemplatesByFramework('android')

  const handleCreateProject = () => {
    if (!selectedTemplate || !projectName) return

    const project = {
      name: projectName,
      description: projectDescription || 'Android application',
      type: 'android' as const,
      status: 'development' as const,
      template: selectedTemplate
    }

    onCreateProject(project)
    setIsOpen(false)
    setProjectName('')
    setProjectDescription('')
    setSelectedTemplate('')
  }

  const getTemplateIcon = (templateId: string) => {
    switch (templateId) {
      case 'android-navigation':
        return <Navigation className="w-6 h-6" />
      case 'android-kotlin':
        return <Code className="w-6 h-6" />
      case 'android-xml-layouts':
        return <Layers className="w-6 h-6" />
      default:
        return <Smartphone className="w-6 h-6" />
    }
  }

  const getTemplateColor = (templateId: string) => {
    switch (templateId) {
      case 'android-navigation':
        return 'bg-blue-100 text-blue-800 border-blue-200'
      case 'android-kotlin':
        return 'bg-green-100 text-green-800 border-green-200'
      case 'android-xml-layouts':
        return 'bg-purple-100 text-purple-800 border-purple-200'
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" className="gap-2">
          <Smartphone className="w-4 h-4" />
          Create Android App
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-2xl flex items-center gap-2">
            <Smartphone className="w-6 h-6 text-primary" />
            Create Android Application
          </DialogTitle>
          <DialogDescription>
            Choose an Android template and configure your mobile app project
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {/* Project Details */}
          <div className="space-y-4">
            <div>
              <Label htmlFor="projectName">Project Name</Label>
              <Input
                id="projectName"
                value={projectName}
                onChange={(e) => setProjectName(e.target.value)}
                placeholder="My Android App"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="projectDescription">Description (Optional)</Label>
              <Textarea
                id="projectDescription"
                value={projectDescription}
                onChange={(e) => setProjectDescription(e.target.value)}
                placeholder="Describe your Android application..."
                className="mt-1"
                rows={3}
              />
            </div>
          </div>

          {/* Template Selection */}
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold mb-3">Choose Android Template</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {androidTemplates.map(template => (
                  <Card 
                    key={template.id}
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      selectedTemplate === template.id ? 'ring-2 ring-primary' : ''
                    }`}
                    onClick={() => setSelectedTemplate(template.id)}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-3">
                          {getTemplateIcon(template.id)}
                          <div>
                            <CardTitle className="text-lg">{template.name}</CardTitle>
                            <Badge 
                              variant="outline" 
                              className={`mt-1 ${getTemplateColor(template.id)}`}
                            >
                              {template.framework}
                            </Badge>
                          </div>
                        </div>
                      </div>
                      <CardDescription className="mt-2">
                        {template.description}
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="pt-0">
                      <div className="space-y-3">
                        <div>
                          <h4 className="text-sm font-medium mb-2">Features:</h4>
                          <div className="flex flex-wrap gap-1">
                            {template.features.map(feature => (
                              <Badge key={feature} variant="secondary" className="text-xs">
                                {feature}
                              </Badge>
                            ))}
                          </div>
                        </div>
                        
                        {template.id === 'android-navigation' && (
                          <div className="text-sm text-muted-foreground">
                            <p className="font-medium text-foreground mb-1">Includes:</p>
                            <ul className="space-y-1">
                              <li>• Bottom Navigation with 3 tabs</li>
                              <li>• Complete navigation flow</li>
                              <li>• Material Design 3 components</li>
                              <li>• MVVM architecture pattern</li>
                              <li>• Ready-to-use screens</li>
                            </ul>
                          </div>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          </div>

          {/* Selected Template Details */}
          {selectedTemplate && (
            <div className="border-t pt-6">
              <h3 className="text-lg font-semibold mb-3">Template Details</h3>
              {(() => {
                const template = androidTemplates.find(t => t.id === selectedTemplate)
                if (!template) return null
                
                return (
                  <Card>
                    <CardContent className="pt-6">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                          <h4 className="font-medium mb-2">Project Structure</h4>
                          <div className="bg-muted/50 rounded-md p-3 font-mono text-sm">
                            {Object.keys(template.files).slice(0, 8).map(filename => (
                              <div key={filename} className="text-muted-foreground">
                                📄 {filename}
                              </div>
                            ))}
                            {Object.keys(template.files).length > 8 && (
                              <div className="text-muted-foreground">
                                ... and {Object.keys(template.files).length - 8} more files
                              </div>
                            )}
                          </div>
                        </div>
                        
                        <div>
                          <h4 className="font-medium mb-2">Key Components</h4>
                          <div className="space-y-2">
                            {template.id === 'android-navigation' && (
                              <>
                                <div className="flex items-center gap-2 text-sm">
                                  <div className="w-2 h-2 rounded-full bg-blue-500" />
                                  MainActivity with Navigation
                                </div>
                                <div className="flex items-center gap-2 text-sm">
                                  <div className="w-2 h-2 rounded-full bg-green-500" />
                                  Home, Profile, Settings Screens
                                </div>
                                <div className="flex items-center gap-2 text-sm">
                                  <div className="w-2 h-2 rounded-full bg-purple-500" />
                                  Details Screen with Navigation
                                </div>
                                <div className="flex items-center gap-2 text-sm">
                                  <div className="w-2 h-2 rounded-full bg-orange-500" />
                                  Material 3 Theme Components
                                </div>
                              </>
                            )}
                            {template.id === 'android-kotlin' && (
                              <>
                                <div className="flex items-center gap-2 text-sm">
                                  <div className="w-2 h-2 rounded-full bg-blue-500" />
                                  Basic Kotlin Activity
                                </div>
                                <div className="flex items-center gap-2 text-sm">
                                  <div className="w-2 h-2 rounded-full bg-green-500" />
                                  Jetpack Compose UI
                                </div>
                                <div className="flex items-center gap-2 text-sm">
                                  <div className="w-2 h-2 rounded-full bg-purple-500" />
                                  Material Design System
                                </div>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )
              })()}
            </div>
          )}

          {/* Create Button */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button variant="outline" onClick={() => setIsOpen(false)}>
              Cancel
            </Button>
            <Button 
              onClick={handleCreateProject}
              disabled={!selectedTemplate || !projectName.trim()}
              className="gap-2"
            >
              <Smartphone className="w-4 h-4" />
              Create Android Project
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}