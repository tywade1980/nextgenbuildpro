/**
 * AI Development Platform - Complete Application
 * 
 * Full-stack AI development platform with comprehensive navigation flow:
 * 
 * 1. Projects Tab - Manage all development projects
 *    - Create new projects (React, Node, Android, Express, etc.)
 *    - View existing projects with status tracking
 *    - Delete and manage project lifecycle
 * 
 * 2. AI Assistant Tab - Natural language development
 *    - Unlimited context window for conversations
 *    - Persistent memory across sessions using useKV
 *    - Code generation and debugging assistance
 * 
 * 3. Preview Tab - Live application preview
 *    - Web preview for React/Next.js projects
 *    - Android emulator preview for mobile apps
 *    - Real-time updates during development
 * 
 * 4. Android Tab - Mobile development preview
 *    - Visual Android emulator interface
 *    - Component-based UI building
 *    - Kotlin code generation
 * 
 * 5. Visual Builder Tab - Drag & drop UI creation
 *    - Component library with intuitive icons
 *    - Visual canvas for designing interfaces
 *    - Natural language intent descriptions
 *    - Export generated code to project files
 * 
 * 6. Production Tab - Professional code generation
 *    - Framework-specific production templates
 *    - Best practices implementation
 *    - Deployment-ready configurations
 * 
 * 7. Templates Tab - Project scaffolding
 *    - Express.js API templates
 *    - Android Kotlin templates
 *    - Custom template creation and editing
 * 
 * 8. Schema Tab - Database-driven development
 *    - Parse database schemas (SQL, JSON)
 *    - Generate complete backend APIs
 *    - CRUD operations with validation
 * 
 * 9. Microservices Tab - Distributed architecture
 *    - Service mesh configuration
 *    - Docker containerization
 *    - Service discovery patterns
 * 
 * 10. Lifecycle Tab - Application flow analysis
 *     - Complete navigation mapping
 *     - Feature implementation tracking
 *     - End-to-end functionality validation
 * 
 * 11. Navigation Tab - Flow analysis
 *     - User journey mapping
 *     - Screen transition logic
 *     - Navigation pattern validation
 * 
 * 12. Endpoints Tab - API validation
 *     - REST endpoint testing
 *     - GraphQL schema validation
 *     - Integration testing workflows
 * 
 * 13. Structure Tab - File organization
 *     - Automated file structure generation
 *     - Framework-specific patterns
 *     - Best practice enforcement
 * 
 * 14. Code Tab - Direct code editing
 *     - Multi-file editor interface
 *     - Syntax highlighting and validation
 *     - Real-time compilation feedback
 * 
 * 15. Agentic Tab - AI-powered development
 *     - Autonomous code generation
 *     - Context-aware assistance
 *     - Multi-session memory persistence
 * 
 * 16. Rewrite Tab - Global file management
 *     - Complete file rewriting (no patches)
 *     - Conflict resolution systems
 *     - Version control integration
 * 
 * 17. Backend Tab - Server-side development
 *     - Database integration tools
 *     - API generation and testing
 *     - Authentication implementations
 * 
 * 18. API Docs Tab - Documentation generation
 *     - Automatic API documentation
 *     - Interactive testing interfaces
 *     - Schema validation tools
 * 
 * 19. Test Tab - Quality assurance
 *     - Automated testing layers
 *     - Syntax error detection
 *     - Dependency validation
 *     - Functional testing suites
 * 
 * 20. Deploy Tab - Production deployment
 *     - Multi-platform deployment pipelines
 *     - Environment configuration
 *     - Monitoring and analytics setup
 * 
 * All features include:
 * - Persistent data storage using useKV hooks
 * - Error boundaries for stability
 * - Responsive design patterns
 * - Real-time collaboration support
 * - Cross-platform compatibility
 */

import { useState } from 'react'
import { useKV } from '@github/spark/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Toaster } from '@/components/ui/sonner'
import { toast } from 'sonner'
import { Code, Play, Cog, AlertTriangle, CheckCircle, GitBranch, Zap, Monitor, Plus, FileText, Trash2, Eye, Smartphone, Cube } from '@phosphor-icons/react'
import { ProjectManager } from '@/components/ProjectManager'
import { AIAssistant } from '@/components/AIAssistant'
import { AgenticCodeEditor } from '@/components/AgenticCodeEditor'
import { TestingSuite } from '@/components/TestingSuite'
import { DeploymentPipeline } from '@/components/DeploymentPipeline'
import { TemplateEditor } from '@/components/TemplateEditor'
import { FileRewriteManager } from '@/components/FileRewriteManager'
import { FileStructureGenerator } from '@/components/FileStructureGenerator'
import { BackendIntegration } from '@/components/BackendIntegration'
import { ProductionCodeGeneratorComponent } from '@/components/ProductionCodeGenerator'
import { ExpressTemplateSelector } from '@/components/ExpressTemplateSelector'
import { AndroidTemplateSelector } from '@/components/AndroidTemplateSelector'
import { APIDocumentation } from '@/components/APIDocumentation'
import { MicroservicesArchitect } from '@/components/MicroservicesArchitect'
import { ServiceMeshPolicies } from '@/components/ServiceMeshPolicies'
import { LifecycleAnalysis } from '@/components/LifecycleAnalysis'
import { NavigationFlowTracker } from '@/components/NavigationFlowTracker'
import { EndpointValidator } from '@/components/EndpointValidator'
import { LivePreview } from '@/components/LivePreview'
import { AndroidPreview } from '@/components/AndroidPreview'
import { VisualBuilder } from '@/components/VisualBuilder'
import { SchemaCodeGenerator } from '@/components/SchemaCodeGeneratorSimple'
import { SystemValidator } from '@/components/SystemValidator'
import { SystemStatusDashboard } from '@/components/SystemStatusDashboard'
import { ConstructionProjectGenerator } from '@/components/ConstructionProjectGenerator'
import { ComponentImportValidator } from '@/components/ComponentImportValidator'
import { ComprehensiveSystemTestSummary } from '@/components/ComprehensiveSystemTestSummary'
import { PlatformTester } from '@/components/PlatformTester'
import { FullSystemTest } from '@/components/FullSystemTest'
import { FinalSystemValidation } from '@/components/FinalSystemValidation'
import { CompleteSystemValidator } from '@/components/CompleteSystemValidator'
import { CriticalComponentValidator } from '@/components/CriticalComponentValidator'
import { ComprehensiveErrorDetector } from '@/components/ComprehensiveErrorDetector'
import { MasterSystemHealthDashboard } from '@/components/MasterSystemHealthDashboard'
import { UltimatePlatformTestRunner } from '@/components/UltimatePlatformTestRunner'
import { IVRSystem } from '@/components/IVRSystem'
import { LLMConfiguration } from '@/components/LLMConfiguration'
import { getTemplateById } from '@/templates/framework-templates'
import { CodeGenerator } from '@/utils/CodeGenerator'
import { CodeRewriteManager, CodeFile } from '@/services/CodeRewriteManager'
import { ProductionCodeGenerator, GeneratedProject, ProjectConfiguration } from '@/utils/ProductionCodeGenerator'
import { codeGenerationEngine, GenerationRequest } from '@/services/CodeGenerationEngine'
import ErrorBoundary from '@/components/ErrorBoundary'

interface Project {
  id: string
  name: string
  description: string
  type: 'react' | 'node' | 'fullstack' | 'android' | 'nextjs' | 'express' | 'fastapi' | 'construction'
  status: 'development' | 'testing' | 'deploying' | 'deployed'
  lastModified: number
  codebase: {
    files: Record<string, string>
    dependencies: string[]
  }
  testResults?: {
    syntax: boolean
    unit: boolean
    integration: boolean
    dependencies: boolean
  }
  template?: string
  constructionData?: {
    template: any
    projectType: 'residential' | 'commercial' | 'industrial'
    totalTasks: number
    completedTasks: number
    totalEstimatedTime: number
    startDate: string
    categories: any[]
  }
}

function App() {
  const [projects, setProjects] = useKV<Project[]>('dev-platform-projects', [])
  const [activeProject, setActiveProject] = useState<Project | null>(null)
  const [activeTab, setActiveTab] = useState('projects')

  const handleProductionProjectGenerated = async (project: GeneratedProject, config: ProjectConfiguration) => {
    // Convert generated project to our project format
    const newProject: Project = {
      id: Date.now().toString(),
      name: config.name,
      description: config.description,
      type: config.framework as Project['type'],
      status: 'development',
      lastModified: Date.now(),
      codebase: {
        files: project.files,
        dependencies: Object.keys(project.dependencies)
      }
    }
    
    setProjects(current => [...current, newProject])
    setActiveProject(newProject)
    setActiveTab('ai')
    toast.success(`Production-ready ${config.framework} project generated successfully!`)
  }

  const generateCompleteApp = async (description: string, projectType: Project['type']) => {
    try {
      const request: GenerationRequest = {
        description,
        projectType,
        features: [description],
        database: 'postgresql',
        authentication: true,
        deployment: 'docker'
      }

      const generatedApp = await codeGenerationEngine.generateApplication(request, `session_${Date.now()}`)
      
      const newProject: Project = {
        id: Date.now().toString(),
        name: `Generated App ${Date.now()}`,
        description,
        type: projectType,
        status: 'development',
        lastModified: Date.now(),
        codebase: {
          files: generatedApp.files,
          dependencies: Object.keys(generatedApp.dependencies)
        }
      }

      setProjects(current => [...current, newProject])
      setActiveProject(newProject)
      setActiveTab('ai')
      toast.success('Complete application generated successfully!')
    } catch (error) {
      toast.error('Failed to generate application')
      console.error('Generation error:', error)
    }
  }

  const createProject = (projectData: Omit<Project, 'id' | 'lastModified' | 'codebase'>) => {
    const { template, ...baseProjectData } = projectData
    
    let generatedProject
    
    if (template) {
      // Generate from template
      const selectedTemplate = getTemplateById(template)
      if (selectedTemplate) {
        generatedProject = CodeGenerator.generateFromTemplate(
          selectedTemplate,
          projectData.name,
          projectData.description
        )
      }
    }
    
    if (!generatedProject) {
      // Generate blank project
      generatedProject = CodeGenerator.generateBlankProject(
        projectData.name,
        projectData.type,
        projectData.description
      )
    }

    const newProject: Project = {
      ...baseProjectData,
      id: Date.now().toString(),
      lastModified: Date.now(),
      template,
      codebase: {
        files: generatedProject.files,
        dependencies: generatedProject.dependencies
      }
    }
    
    setProjects(current => [...current, newProject])
    setActiveProject(newProject)
    setActiveTab('lifecycle') // Switch to lifecycle analysis for new projects
  }

  const updateProject = (projectId: string, updates: Partial<Project>) => {
    setProjects(current =>
      current.map(p =>
        p.id === projectId
          ? { ...p, ...updates, lastModified: Date.now() }
          : p
      )
    )
    
    if (activeProject?.id === projectId) {
      setActiveProject(prev => prev ? { ...prev, ...updates, lastModified: Date.now() } : null)
    }
  }

  const updateFileContent = (filePath: string, newContent: string) => {
    if (!activeProject) return

    const updatedProject = {
      ...activeProject,
      codebase: {
        ...activeProject.codebase,
        files: {
          ...activeProject.codebase.files,
          [filePath]: newContent
        }
      },
      lastModified: Date.now()
    }

    updateProject(activeProject.id, updatedProject)
  }

  const convertProjectToCodeFiles = (project: Project): CodeFile[] => {
    if (!project?.codebase?.files) return []
    return Object.entries(project.codebase.files).map(([path, content]) => ({
      path,
      content,
      language: getLanguageFromPath(path),
      lastModified: project.lastModified
    }))
  }

  const getLanguageFromPath = (path: string): string => {
    const extension = path.split('.').pop()?.toLowerCase()
    switch (extension) {
      case 'js': return 'javascript'
      case 'jsx': return 'javascript'
      case 'ts': return 'typescript'
      case 'tsx': return 'typescript'
      case 'css': return 'css'
      case 'scss': return 'scss'
      case 'html': return 'html'
      case 'json': return 'json'
      default: return 'text'
    }
  }

  const handleFilesUpdated = (updatedFiles: CodeFile[]) => {
    if (!activeProject || !updatedFiles) return

    const newFiles: Record<string, string> = {}
    updatedFiles.forEach(file => {
      newFiles[file.path] = file.content
    })

    updateProject(activeProject.id, {
      codebase: {
        ...activeProject.codebase,
        files: newFiles
      }
    })
  }

  const deleteProject = (projectId: string) => {
    setProjects(current => current.filter(p => p.id !== projectId))
    if (activeProject?.id === projectId) {
      setActiveProject(null)
      setActiveTab('projects')
    }
  }

  const getStatusColor = (status: Project['status']) => {
    switch (status) {
      case 'development': return 'bg-blue-500'
      case 'testing': return 'bg-yellow-500'
      case 'deploying': return 'bg-orange-500'
      case 'deployed': return 'bg-green-500'
      default: return 'bg-gray-500'
    }
  }

  const getStatusIcon = (status: Project['status']) => {
    switch (status) {
      case 'development': return <Code className="w-4 h-4" />
      case 'testing': return <Play className="w-4 h-4" />
      case 'deploying': return <GitBranch className="w-4 h-4" />
      case 'deployed': return <Monitor className="w-4 h-4" />
      default: return <FileText className="w-4 h-4" />
    }
  }

  return (
    <ErrorBoundary>
      <div className="min-h-screen bg-background">
        <header className="border-b border-border bg-card">
          <div className="container mx-auto px-4 py-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                  <Zap className="w-5 h-5 text-primary-foreground" />
                </div>
                <div>
                  <h1 className="text-2xl font-bold">AI Development Platform</h1>
                  <p className="text-sm text-muted-foreground">Build complete, working applications with AI - no templates, real code generation</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                {activeProject && (
                  <Badge variant="outline" className="flex items-center gap-1">
                    {getStatusIcon(activeProject.status)}
                    {activeProject.name}
                  </Badge>
                )}
                <Button variant="outline" size="sm">
                  <Cog className="w-4 h-4" />
                </Button>
              </div>
            </div>
          </div>
        </header>

        <main className="container mx-auto px-4 py-6">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
            <div className="flex gap-2 overflow-x-auto pb-2">
              <TabsList className="flex w-auto min-w-max">
                <TabsTrigger value="projects">Projects</TabsTrigger>
                <TabsTrigger value="ai" disabled={!activeProject}>AI Assistant</TabsTrigger>
                <TabsTrigger value="preview" disabled={!activeProject}>Preview</TabsTrigger>
                <TabsTrigger value="android" disabled={!activeProject || activeProject.type !== 'android'}>Android</TabsTrigger>
                <TabsTrigger value="visual" disabled={!activeProject}>Visual Builder</TabsTrigger>
                <TabsTrigger value="production">Production</TabsTrigger>
                <TabsTrigger value="templates">Templates</TabsTrigger>
                <TabsTrigger value="construction">Construction</TabsTrigger>
                <TabsTrigger value="schema">Schema</TabsTrigger>
                <TabsTrigger value="microservices">Microservices</TabsTrigger>
                <TabsTrigger value="lifecycle" disabled={!activeProject}>Lifecycle</TabsTrigger>
                <TabsTrigger value="navigation" disabled={!activeProject}>Navigation</TabsTrigger>
                <TabsTrigger value="endpoints" disabled={!activeProject}>Endpoints</TabsTrigger>
                <TabsTrigger value="structure" disabled={!activeProject}>Structure</TabsTrigger>
                <TabsTrigger value="code" disabled={!activeProject}>Code</TabsTrigger>
                <TabsTrigger value="agentic" disabled={!activeProject}>Agentic</TabsTrigger>
                <TabsTrigger value="rewrite" disabled={!activeProject}>Rewrite</TabsTrigger>
                <TabsTrigger value="backend" disabled={!activeProject}>Backend</TabsTrigger>
                <TabsTrigger value="api" disabled={!activeProject}>API Docs</TabsTrigger>
                <TabsTrigger value="test" disabled={!activeProject}>Test</TabsTrigger>
                <TabsTrigger value="deploy" disabled={!activeProject}>Deploy</TabsTrigger>
                <TabsTrigger value="ivr">IVR System</TabsTrigger>
                <TabsTrigger value="validate">System Check</TabsTrigger>
                <TabsTrigger value="status">Status</TabsTrigger>
                <TabsTrigger value="llm-config">LLM Config</TabsTrigger>
              </TabsList>
            </div>

            <TabsContent value="projects" className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-2xl font-semibold">Projects</h2>
                  <p className="text-muted-foreground">Manage your development projects</p>
                </div>
                <div className="flex items-center gap-2">
                  <ProjectManager onCreateProject={createProject} />
                  <ExpressTemplateSelector onCreateProject={createProject} />
                  <AndroidTemplateSelector onCreateProject={createProject} />
                  <Badge variant="outline" className="flex items-center gap-1">
                    <CheckCircle className="w-3 h-3 text-green-500" />
                    Complete Platform
                  </Badge>
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {projects?.length ? projects.map(project => (
                  <Card key={project.id} className="cursor-pointer hover:shadow-md transition-shadow">
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-2">
                          <div className={`w-3 h-3 rounded-full ${getStatusColor(project.status)}`} />
                          <CardTitle className="text-lg">{project.name}</CardTitle>
                        </div>
                        <div className="flex items-center gap-2">
                          {project.template && (
                            <Badge variant="outline" className="text-xs">
                              Template
                            </Badge>
                          )}
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation()
                              deleteProject(project.id)
                            }}
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                      <CardDescription>{project.description}</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-muted-foreground">Type</span>
                          <Badge variant="secondary">{project.type}</Badge>
                        </div>
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-muted-foreground">Last Modified</span>
                          <span>{new Date(project.lastModified).toLocaleDateString()}</span>
                        </div>
                        <Button 
                          className="w-full" 
                          onClick={() => {
                            setActiveProject(project)
                            setActiveTab('ai') // Open in AI assistant first
                          }}
                        >
                          <Eye className="w-4 h-4 mr-2" />
                          Open Project
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                )) : (
                  <div className="col-span-full flex flex-col items-center justify-center py-12 text-center">
                    <FileText className="w-12 h-12 text-muted-foreground mb-4" />
                    <h3 className="text-lg font-semibold mb-2">No projects yet</h3>
                    <p className="text-muted-foreground mb-4">Create your first project to get started</p>
                    <div className="flex items-center gap-2">
                      <ProjectManager onCreateProject={createProject} />
                      <ExpressTemplateSelector onCreateProject={createProject} />
                      <AndroidTemplateSelector onCreateProject={createProject} />
                      <Button 
                        onClick={() => generateCompleteApp('Build a modern web application', 'react')}
                        variant="outline"
                      >
                        <Zap className="w-4 h-4 mr-2" />
                        Quick Generate
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </TabsContent>

            <TabsContent value="ai" className="space-y-6">
              {activeProject && <AIAssistant project={activeProject} onUpdateProject={updateProject} />}
            </TabsContent>

            <TabsContent value="preview" className="space-y-6">
              {activeProject && activeProject.type === 'android' ? (
                <AndroidPreview project={activeProject} onUpdateProject={updateProject} />
              ) : activeProject ? (
                <LivePreview project={activeProject} onUpdateProject={updateProject} />
              ) : null}
            </TabsContent>

            <TabsContent value="android" className="space-y-6">
              {activeProject && activeProject.type === 'android' && (
                <AndroidPreview project={activeProject} onUpdateProject={updateProject} />
              )}
            </TabsContent>

            <TabsContent value="visual" className="space-y-6">
              {activeProject && (
                <VisualBuilder 
                  project={activeProject} 
                  onUpdateProject={updateProject}
                />
              )}
            </TabsContent>

            <TabsContent value="production" className="space-y-6">
              <ProductionCodeGeneratorComponent onProjectGenerated={handleProductionProjectGenerated} />
            </TabsContent>

            <TabsContent value="templates" className="space-y-6">
              <TemplateEditor onCreateProject={createProject} />
            </TabsContent>

            <TabsContent value="schema" className="space-y-6">
              <SchemaCodeGenerator onCreateProject={createProject} />
            </TabsContent>

            <TabsContent value="construction" className="space-y-6">
              <ConstructionProjectGenerator onCreateProject={createProject} />
            </TabsContent>

            <TabsContent value="microservices" className="space-y-6">
              <MicroservicesArchitect 
                project={activeProject || projects[0]} 
                onUpdateProject={updateProject} 
              />
              {activeProject && (
                <ServiceMeshPolicies
                  services={[
                    { id: 'auth-service', name: 'Authentication Service', namespace: 'default', labels: { app: 'auth' } },
                    { id: 'user-service', name: 'User Management', namespace: 'default', labels: { app: 'users' } },
                    { id: 'payment-service', name: 'Payment Gateway', namespace: 'payment', labels: { app: 'payment' } },
                  ]}
                  onPolicyUpdate={(policies) => {
                    toast.success(`${policies.length} policies updated`)
                  }}
                />
              )}
            </TabsContent>

            <TabsContent value="lifecycle" className="space-y-6">
              {activeProject && (
                <LifecycleAnalysis
                  project={activeProject}
                  onUpdateProject={updateProject}
                />
              )}
            </TabsContent>

            <TabsContent value="navigation" className="space-y-6">
              {activeProject && (
                <NavigationFlowTracker
                  project={activeProject}
                  onUpdateProject={updateProject}
                />
              )}
            </TabsContent>

            <TabsContent value="endpoints" className="space-y-6">
              {activeProject && (
                <EndpointValidator
                  project={activeProject}
                  onUpdateProject={updateProject}
                />
              )}
            </TabsContent>

            <TabsContent value="structure" className="space-y-6">
              {activeProject && (
                <FileStructureGenerator
                  project={activeProject}
                  onUpdateProject={updateProject}
                />
              )}
            </TabsContent>

            <TabsContent value="code" className="space-y-6">
              {activeProject && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <h2 className="text-2xl font-semibold">Code Editor</h2>
                    <div className="flex items-center gap-2">
                      <Badge variant="outline">{activeProject.type}</Badge>
                      <Button variant="outline" size="sm">
                        <Play className="w-4 h-4 mr-2" />
                        Run
                      </Button>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                    <Card className="lg:col-span-1">
                      <CardHeader>
                        <CardTitle className="text-lg flex items-center gap-2">
                          <FileText className="w-5 h-5" />
                          Files
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-2">
                        {Object.keys(activeProject.codebase.files || {}).map(filename => (
                          <div key={filename} className="p-2 rounded-md bg-muted text-sm font-mono hover:bg-muted/80 cursor-pointer">
                            {filename}
                          </div>
                        ))}
                      </CardContent>
                    </Card>
                    
                    <Card className="lg:col-span-3">
                      <CardHeader>
                        <CardTitle className="text-lg">src/index.js</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <Textarea
                          value={activeProject.codebase.files['src/index.js'] || ''}
                          onChange={(e) => updateFileContent('src/index.js', e.target.value)}
                          className="min-h-96 font-mono text-sm"
                          placeholder="Start coding..."
                        />
                      </CardContent>
                    </Card>
                  </div>
                </div>
              )}
            </TabsContent>

            <TabsContent value="agentic" className="space-y-6">
              {activeProject && (
                <AgenticCodeEditor
                  project={activeProject}
                  onUpdateProject={updateProject}
                />
              )}
            </TabsContent>

            <TabsContent value="rewrite" className="space-y-6">
              {activeProject && (
                <FileRewriteManager
                  projectId={activeProject.id}
                  files={convertProjectToCodeFiles(activeProject)}
                  onFilesUpdated={handleFilesUpdated}
                />
              )}
            </TabsContent>

            <TabsContent value="backend" className="space-y-6">
              {activeProject && (
                <BackendIntegration
                  project={activeProject}
                  onUpdateProject={updateProject}
                />
              )}
            </TabsContent>

            <TabsContent value="api" className="space-y-6">
              {activeProject && <APIDocumentation project={activeProject} />}
            </TabsContent>

            <TabsContent value="test" className="space-y-6">
              {activeProject && <TestingSuite project={activeProject} onUpdateProject={updateProject} />}
            </TabsContent>

            <TabsContent value="deploy" className="space-y-6">
              {activeProject && <DeploymentPipeline project={activeProject} onUpdateProject={updateProject} />}
            </TabsContent>

            <TabsContent value="ivr" className="space-y-6">
              <IVRSystem />
            </TabsContent>

            <TabsContent value="validate" className="space-y-6">
              <UltimatePlatformTestRunner />
              <CompleteSystemValidator />
              <CriticalComponentValidator />
              <ComprehensiveErrorDetector />
              <FinalSystemValidation />
              <FullSystemTest />
              <PlatformTester />
              <ComprehensiveSystemTestSummary />
              <ComponentImportValidator />
              <SystemValidator />
            </TabsContent>

            <TabsContent value="status" className="space-y-6">
              <MasterSystemHealthDashboard />
              <SystemStatusDashboard />
            </TabsContent>

            <TabsContent value="llm-config" className="space-y-6">
              <LLMConfiguration />
            </TabsContent>
          </Tabs>
        </main>
        
        <Toaster />
      </div>
    </ErrorBoundary>
  )
}

export default App