import React, { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { PaintBrush, Calculator, Code2, Smartphone } from '@phosphor-icons/react'
import { TemplateSelector } from './TemplateSelector'
import { ExpressTemplateSelector } from './ExpressTemplateSelector'
import EstimateTemplateManager from './EstimateTemplateManager'

interface TemplateEditorProps {
  onCreateProject: (project: any) => void
}

export function TemplateEditor({ onCreateProject }: TemplateEditorProps) {
  const [activeTab, setActiveTab] = useState('estimate')

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold flex items-center gap-2">
          <PaintBrush className="w-6 h-6" />
          Template Management
        </h2>
        <p className="text-muted-foreground">
          Create, manage, and use project templates for faster project setup
        </p>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="estimate" className="flex items-center gap-2">
            <Calculator className="w-4 h-4" />
            Estimate Templates
            <Badge variant="secondary" className="text-xs">New</Badge>
          </TabsTrigger>
          <TabsTrigger value="framework" className="flex items-center gap-2">
            <Code2 className="w-4 h-4" />
            Framework Templates
          </TabsTrigger>
          <TabsTrigger value="express" className="flex items-center gap-2">
            <Code2 className="w-4 h-4" />
            Express API
          </TabsTrigger>
          <TabsTrigger value="mobile" className="flex items-center gap-2">
            <Smartphone className="w-4 h-4" />
            Mobile Apps
          </TabsTrigger>
        </TabsList>

        <TabsContent value="estimate" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calculator className="w-5 h-5" />
                Project Estimate Templates
              </CardTitle>
              <CardDescription>
                Create reusable templates for project cost estimation with detailed sections and line items
              </CardDescription>
            </CardHeader>
            <CardContent>
              <EstimateTemplateManager onCreateProject={onCreateProject} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="framework" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Code2 className="w-5 h-5" />
                Framework Templates
              </CardTitle>
              <CardDescription>
                Pre-built templates for popular frameworks and project types
              </CardDescription>
            </CardHeader>
            <CardContent>
              <TemplateSelector 
                onSelectTemplate={(template) => {
                  onCreateProject({
                    name: `${template.name} Project`,
                    description: template.description,
                    type: template.framework,
                    template: template.id
                  })
                }}
                onClose={() => {}}
              />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="express" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Code2 className="w-5 h-5" />
                Express.js API Templates
              </CardTitle>
              <CardDescription>
                Specialized templates for Express.js backend API development
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ExpressTemplateSelector onCreateProject={onCreateProject} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="mobile" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Smartphone className="w-5 h-5" />
                Mobile App Templates
              </CardTitle>
              <CardDescription>
                Templates for mobile application development
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <Smartphone className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
                <h3 className="text-lg font-semibold mb-2">Mobile Templates</h3>
                <p className="text-muted-foreground mb-4">
                  Mobile app templates for React Native, Flutter, and native development
                </p>
                <p className="text-sm text-muted-foreground">
                  Coming soon - integrate with existing Android templates
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}