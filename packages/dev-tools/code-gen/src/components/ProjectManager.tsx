import React, { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Plus, Code, Smartphone, Globe, Server, Database, Cloud } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface ProjectManagerProps {
  onCreateProject: (project: any) => void
}

const projectTypes = [
  { id: 'react', name: 'React App', icon: Code, description: 'Single-page application with React' },
  { id: 'nextjs', name: 'Next.js App', icon: Globe, description: 'Full-stack React framework with SSR' },
  { id: 'node', name: 'Node.js API', icon: Server, description: 'Backend API with Node.js' },
  { id: 'express', name: 'Express Server', icon: Database, description: 'REST API with Express.js' },
  { id: 'fastapi', name: 'FastAPI', icon: Cloud, description: 'Python API with FastAPI' },
  { id: 'android', name: 'Android App', icon: Smartphone, description: 'Native Android application' },
  { id: 'fullstack', name: 'Full Stack', icon: Code, description: 'Complete frontend and backend solution' }
]

const templates = [
  { id: 'blank', name: 'Blank Project', description: 'Start from scratch' },
  { id: 'crud', name: 'CRUD Application', description: 'Create, Read, Update, Delete operations' },
  { id: 'auth', name: 'Authentication App', description: 'User login and registration' },
  { id: 'dashboard', name: 'Dashboard', description: 'Admin dashboard with charts' },
  { id: 'ecommerce', name: 'E-commerce', description: 'Online store template' },
  { id: 'blog', name: 'Blog', description: 'Content management system' },
  { id: 'portfolio', name: 'Portfolio', description: 'Personal portfolio website' },
  { id: 'landing', name: 'Landing Page', description: 'Marketing landing page' }
]

export function ProjectManager({ onCreateProject }: ProjectManagerProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    type: '',
    template: 'blank'
  })
  const [step, setStep] = useState(1)

  const handleCreateProject = () => {
    if (!formData.name.trim()) {
      toast.error('Project name is required')
      return
    }
    
    if (!formData.type) {
      toast.error('Please select a project type')
      return
    }

    const project = {
      name: formData.name,
      description: formData.description || `A new ${formData.type} project`,
      type: formData.type,
      status: 'development',
      template: formData.template !== 'blank' ? formData.template : undefined
    }
    
    onCreateProject(project)
    
    // Reset form and close dialog
    setFormData({ name: '', description: '', type: '', template: 'blank' })
    setStep(1)
    setIsOpen(false)
    
    toast.success(`${formData.name} project created successfully!`)
  }

  const nextStep = () => {
    if (step === 1 && !formData.name.trim()) {
      toast.error('Project name is required')
      return
    }
    setStep(prev => Math.min(prev + 1, 3))
  }

  const prevStep = () => {
    setStep(prev => Math.max(prev - 1, 1))
  }

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="w-4 h-4 mr-2" />
          New Project
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Create New Project</DialogTitle>
          <DialogDescription>
            Step {step} of 3: {step === 1 ? 'Project Details' : step === 2 ? 'Project Type' : 'Template Selection'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {step === 1 && (
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Project Name</Label>
                <Input
                  id="name"
                  placeholder="My Awesome Project"
                  value={formData.name}
                  onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">Description (Optional)</Label>
                <Textarea
                  id="description"
                  placeholder="Describe your project..."
                  value={formData.description}
                  onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                />
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Choose Project Type</h3>
              <div className="grid grid-cols-2 gap-3">
                {projectTypes.map((type) => (
                  <Card
                    key={type.id}
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      formData.type === type.id ? 'ring-2 ring-primary' : ''
                    }`}
                    onClick={() => setFormData(prev => ({ ...prev, type: type.id }))}
                  >
                    <CardContent className="p-4">
                      <div className="flex items-center gap-3">
                        <type.icon className="w-6 h-6 text-primary" />
                        <div className="flex-1">
                          <h4 className="font-medium">{type.name}</h4>
                          <p className="text-sm text-muted-foreground">{type.description}</p>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Select Template</h3>
              <div className="grid grid-cols-2 gap-3">
                {templates.map((template) => (
                  <Card
                    key={template.id}
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      formData.template === template.id ? 'ring-2 ring-primary' : ''
                    }`}
                    onClick={() => setFormData(prev => ({ ...prev, template: template.id }))}
                  >
                    <CardContent className="p-4">
                      <div className="space-y-2">
                        <div className="flex items-center justify-between">
                          <h4 className="font-medium">{template.name}</h4>
                          {template.id === 'blank' && <Badge variant="secondary">Default</Badge>}
                        </div>
                        <p className="text-sm text-muted-foreground">{template.description}</p>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          )}

          <div className="flex items-center justify-between pt-4 border-t">
            <div className="flex items-center gap-1">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className={`w-2 h-2 rounded-full ${
                    i <= step ? 'bg-primary' : 'bg-muted'
                  }`}
                />
              ))}
            </div>
            <div className="flex items-center gap-2">
              {step > 1 && (
                <Button variant="outline" onClick={prevStep}>
                  Previous
                </Button>
              )}
              {step < 3 ? (
                <Button onClick={nextStep}>
                  Next
                </Button>
              ) : (
                <Button onClick={handleCreateProject}>
                  Create Project
                </Button>
              )}
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}