import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { frameworkTemplates, getTemplatesByCategory, type FrameworkTemplate } from '@/templates/framework-templates'
import { Code2, Layers, Globe, Smartphone, Sparkles, CheckCircle } from '@phosphor-icons/react'

interface TemplateSelectorProps {
  onSelectTemplate: (template: FrameworkTemplate) => void
  onClose: () => void
}

export function TemplateSelector({ onSelectTemplate, onClose }: TemplateSelectorProps) {
  const [selectedCategory, setSelectedCategory] = useState<string>('all')
  const [selectedTemplate, setSelectedTemplate] = useState<FrameworkTemplate | null>(null)

  const categories = [
    { id: 'all', name: 'All Templates', icon: <Layers className="w-4 h-4" /> },
    { id: 'frontend', name: 'Frontend', icon: <Globe className="w-4 h-4" /> },
    { id: 'backend', name: 'Backend', icon: <Code2 className="w-4 h-4" /> },
    { id: 'fullstack', name: 'Full Stack', icon: <Sparkles className="w-4 h-4" /> },
    { id: 'mobile', name: 'Mobile', icon: <Smartphone className="w-4 h-4" /> }
  ]

  const templates = selectedCategory === 'all' 
    ? (frameworkTemplates || []) 
    : (getTemplatesByCategory(selectedCategory) || [])

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'frontend': return 'bg-blue-100 text-blue-800 border-blue-200'
      case 'backend': return 'bg-green-100 text-green-800 border-green-200'
      case 'fullstack': return 'bg-purple-100 text-purple-800 border-purple-200'
      case 'mobile': return 'bg-orange-100 text-orange-800 border-orange-200'
      default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const handleUseTemplate = () => {
    if (selectedTemplate) {
      onSelectTemplate(selectedTemplate)
      onClose()
    }
  }

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="max-w-6xl max-h-[90vh] p-0">
        <DialogHeader className="p-6 pb-2">
          <DialogTitle className="text-2xl flex items-center gap-2">
            <Sparkles className="w-6 h-6 text-primary" />
            Choose a Framework Template
          </DialogTitle>
          <DialogDescription>
            Start your project with a pre-configured template featuring best practices and modern tooling
          </DialogDescription>
        </DialogHeader>

        <div className="flex flex-1 overflow-hidden">
          {/* Category Sidebar */}
          <div className="w-64 border-r bg-muted/30 p-4">
            <h3 className="font-semibold mb-3 text-sm text-muted-foreground uppercase tracking-wide">
              Categories
            </h3>
            <div className="space-y-1">
              {categories.map(category => (
                <Button
                  key={category.id}
                  variant={selectedCategory === category.id ? "secondary" : "ghost"}
                  className="w-full justify-start gap-2"
                  onClick={() => setSelectedCategory(category.id)}
                >
                  {category.icon}
                  {category.name}
                </Button>
              ))}
            </div>
            
            <Separator className="my-4" />
            
            <div className="text-xs text-muted-foreground">
              <p className="font-medium mb-1">Template Features:</p>
              <ul className="space-y-1">
                <li>• TypeScript support</li>
                <li>• Modern tooling</li>
                <li>• Best practices</li>
                <li>• Ready to deploy</li>
              </ul>
            </div>
          </div>

          {/* Templates Grid */}
          <div className="flex-1 p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 max-h-[60vh] overflow-y-auto">
              {templates.map(template => (
                <Card 
                  key={template.id} 
                  className={`cursor-pointer transition-all hover:shadow-md ${
                    selectedTemplate?.id === template.id ? 'ring-2 ring-primary' : ''
                  }`}
                  onClick={() => setSelectedTemplate(template)}
                >
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div className="text-2xl">{template.icon}</div>
                      <Badge 
                        variant="outline" 
                        className={getCategoryColor(template.category)}
                      >
                        {template.category}
                      </Badge>
                    </div>
                    <CardTitle className="text-lg">{template.name}</CardTitle>
                    <CardDescription className="text-sm">
                      {template.description}
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="flex flex-wrap gap-1">
                      {(template.features || []).slice(0, 3).map(feature => (
                        <Badge key={feature} variant="secondary" className="text-xs">
                          {feature}
                        </Badge>
                      ))}
                      {(template.features || []).length > 3 && (
                        <Badge variant="outline" className="text-xs">
                          +{(template.features || []).length - 3} more
                        </Badge>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Template Details */}
            {selectedTemplate && (
              <div className="mt-6 border-t pt-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="text-3xl">{selectedTemplate.icon}</div>
                    <div>
                      <h3 className="text-xl font-semibold">{selectedTemplate.name}</h3>
                      <p className="text-muted-foreground">{selectedTemplate.description}</p>
                    </div>
                  </div>
                  <Button onClick={handleUseTemplate} className="gap-2">
                    <CheckCircle className="w-4 h-4" />
                    Use This Template
                  </Button>
                </div>

                <Tabs defaultValue="features" className="w-full">
                  <TabsList>
                    <TabsTrigger value="features">Features</TabsTrigger>
                    <TabsTrigger value="structure">File Structure</TabsTrigger>
                    <TabsTrigger value="dependencies">Dependencies</TabsTrigger>
                  </TabsList>
                  
                  <TabsContent value="features" className="mt-4">
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                      {selectedTemplate.features.map(feature => (
                        <div key={feature} className="flex items-center gap-2 p-2 rounded-md bg-muted/50">
                          <CheckCircle className="w-4 h-4 text-green-600" />
                          <span className="text-sm">{feature}</span>
                        </div>
                      ))}
                    </div>
                  </TabsContent>
                  
                  <TabsContent value="structure" className="mt-4">
                    <ScrollArea className="h-48 w-full rounded-md border p-4">
                      <div className="space-y-1">
                        {Object.keys(selectedTemplate.files).map(filename => (
                          <div key={filename} className="font-mono text-sm text-muted-foreground">
                            📄 {filename}
                          </div>
                        ))}
                      </div>
                    </ScrollArea>
                  </TabsContent>
                  
                  <TabsContent value="dependencies" className="mt-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <h4 className="font-medium mb-2">Dependencies</h4>
                        <div className="space-y-1">
                          {selectedTemplate.dependencies.map(dep => (
                            <Badge key={dep} variant="outline" className="mr-1 mb-1">
                              {dep}
                            </Badge>
                          ))}
                        </div>
                      </div>
                      {selectedTemplate.devDependencies && (
                        <div>
                          <h4 className="font-medium mb-2">Dev Dependencies</h4>
                          <div className="space-y-1">
                            {selectedTemplate.devDependencies.map(dep => (
                              <Badge key={dep} variant="secondary" className="mr-1 mb-1">
                                {dep}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  </TabsContent>
                </Tabs>
              </div>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}