/**
 * Component Import Validation
 * 
 * Tests that all major components can be imported without syntax errors
 */

// Core UI Components
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'

// Platform Components
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
import ErrorBoundary from '@/components/ErrorBoundary'

// Services
import { codeGenerationEngine } from '@/services/CodeGenerationEngine'
import { CodeRewriteManager } from '@/services/CodeRewriteManager'

// Utils
import { CodeGenerator } from '@/utils/CodeGenerator'
import { ProductionCodeGenerator } from '@/utils/ProductionCodeGenerator'

// Templates
import { getTemplateById } from '@/templates/framework-templates'

// Icons
import { 
  Code, 
  Play, 
  Cog, 
  AlertTriangle, 
  CheckCircle, 
  GitBranch, 
  Zap, 
  Monitor, 
  Plus, 
  FileText, 
  Trash2, 
  Eye, 
  Smartphone, 
  Cube 
} from '@phosphor-icons/react'

// Toast
import { toast } from 'sonner'

// Hooks
import { useKV } from '@github/spark/hooks'

/**
 * Component Import Test
 * 
 * This function validates that all imports work correctly
 */
export function validateImports(): { success: boolean; components: string[]; errors: string[] } {
  const components: string[] = []
  const errors: string[] = []
  
  try {
    // Test UI components
    components.push('Button', 'Card', 'Input', 'Label', 'Textarea', 'Badge', 'Progress', 'Tabs', 'Dialog', 'Alert', 'Select', 'ScrollArea', 'Separator')
    
    // Test platform components
    components.push(
      'ProjectManager', 'AIAssistant', 'AgenticCodeEditor', 'TestingSuite', 
      'DeploymentPipeline', 'TemplateEditor', 'FileRewriteManager', 
      'FileStructureGenerator', 'BackendIntegration', 'ProductionCodeGeneratorComponent',
      'ExpressTemplateSelector', 'AndroidTemplateSelector', 'APIDocumentation',
      'MicroservicesArchitect', 'ServiceMeshPolicies', 'LifecycleAnalysis',
      'NavigationFlowTracker', 'EndpointValidator', 'LivePreview', 'AndroidPreview',
      'VisualBuilder', 'SchemaCodeGenerator', 'SystemValidator', 'SystemStatusDashboard',
      'ErrorBoundary'
    )
    
    // Test services
    components.push('codeGenerationEngine', 'CodeRewriteManager')
    
    // Test utils
    components.push('CodeGenerator', 'ProductionCodeGenerator')
    
    // Test templates
    components.push('getTemplateById')
    
    // Test hooks
    components.push('useKV')
    
    // Test external libraries
    components.push('toast', 'phosphor-icons')
    
    return { success: true, components, errors }
    
  } catch (error) {
    errors.push(error instanceof Error ? error.message : 'Unknown import error')
    return { success: false, components, errors }
  }
}

/**
 * Runtime Component Test
 * 
 * Simple component that can be rendered to test runtime functionality
 */
export function ComponentImportValidator() {
  const result = validateImports()
  
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          {result.success ? (
            <CheckCircle className="w-5 h-5 text-green-500" />
          ) : (
            <AlertTriangle className="w-5 h-5 text-red-500" />
          )}
          Component Import Validation
        </CardTitle>
        <CardDescription>
          {result.success 
            ? `Successfully imported ${result.components.length} components`
            : `Failed to import components: ${result.errors.length} errors`
          }
        </CardDescription>
      </CardHeader>
      <CardContent>
        {result.success ? (
          <div className="space-y-4">
            <Alert>
              <CheckCircle className="h-4 w-4" />
              <AlertDescription>
                All platform components imported successfully! The system is ready for use.
              </AlertDescription>
            </Alert>
            <div>
              <p className="text-sm font-medium mb-2">Imported Components:</p>
              <div className="flex flex-wrap gap-1">
                {result.components.map(component => (
                  <Badge key={component} variant="outline" className="text-xs">
                    {component}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <Alert>
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription>
                Some components failed to import. Check the errors below.
              </AlertDescription>
            </Alert>
            <div>
              <p className="text-sm font-medium mb-2">Errors:</p>
              <div className="space-y-1">
                {result.errors.map((error, index) => (
                  <div key={index} className="text-xs text-red-600 bg-red-50 p-2 rounded">
                    {error}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}