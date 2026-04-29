import React, { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { toast } from 'sonner'
import { Copy, Download, Eye, Code, Palette, FileCode } from '@phosphor-icons/react'
import { VisualComponentBuilder, CanvasComponent } from '@/components/VisualComponentBuilder'
import { VisualCodeGenerator } from '@/utils/VisualCodeGenerator'

interface GeneratedCodeOutput {
  react: string
  vue: string
  angular: string
  css: string
  json: string
}

export const VisualBuilderWorkspace: React.FC<{
  project?: any
  onUpdateProject?: (id: string, updates: any) => void
}> = ({ project, onUpdateProject }) => {
  const [generatedCode, setGeneratedCode] = useState<GeneratedCodeOutput | null>(null)
  const [activeCodeTab, setActiveCodeTab] = useState('react')
  const [showPreview, setShowPreview] = useState(false)

  const handleGenerateCode = (components: CanvasComponent[]) => {
    const output: GeneratedCodeOutput = {
      react: VisualCodeGenerator.generateReactCode(components),
      vue: VisualCodeGenerator.generateVueCode(components),
      angular: VisualCodeGenerator.generateAngularCode(components),
      css: VisualCodeGenerator.generateCSS(components),
      json: VisualCodeGenerator.generateJSON(components)
    }
    
    setGeneratedCode(output)
    toast.success('Code generated for all frameworks!')

    // Update project with generated code if project exists
    if (project && onUpdateProject) {
      onUpdateProject(project.id, {
        codebase: {
          ...project.codebase,
          files: {
            ...project.codebase.files,
            'src/GeneratedComponent.tsx': output.react,
            'src/GeneratedComponent.vue': output.vue,
            'src/GeneratedComponent.ts': output.angular,
            'src/generated.css': output.css,
            'src/components.json': output.json
          }
        }
      })
    }
  }

  const copyToClipboard = async (code: string, framework: string) => {
    try {
      await navigator.clipboard.writeText(code)
      toast.success(`${framework} code copied to clipboard!`)
    } catch (err) {
      toast.error('Failed to copy code')
    }
  }

  const downloadCode = (code: string, filename: string) => {
    const blob = new Blob([code], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    toast.success(`${filename} downloaded!`)
  }

  const getFileExtension = (framework: string): string => {
    switch (framework) {
      case 'react': return 'tsx'
      case 'vue': return 'vue'
      case 'angular': return 'ts'
      case 'css': return 'css'
      case 'json': return 'json'
      default: return 'txt'
    }
  }

  const getLanguageLabel = (framework: string): string => {
    switch (framework) {
      case 'react': return 'React/TSX'
      case 'vue': return 'Vue.js'
      case 'angular': return 'Angular'
      case 'css': return 'CSS'
      case 'json': return 'JSON'
      default: return framework
    }
  }

  const renderCodeBlock = (code: string, language: string) => (
    <ScrollArea className="h-96 w-full">
      <pre className="p-4 bg-muted rounded-md text-sm font-mono overflow-x-auto">
        <code>{code}</code>
      </pre>
    </ScrollArea>
  )

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">Visual Component Builder</h2>
          <p className="text-muted-foreground">
            Drag and drop components to build your UI, then generate production-ready code
          </p>
        </div>
        <div className="flex items-center gap-2">
          {generatedCode && (
            <>
              <Dialog open={showPreview} onOpenChange={setShowPreview}>
                <DialogTrigger asChild>
                  <Button variant="outline" size="sm">
                    <Eye className="w-4 h-4 mr-2" />
                    Preview
                  </Button>
                </DialogTrigger>
                <DialogContent className="max-w-4xl">
                  <DialogHeader>
                    <DialogTitle>Component Preview</DialogTitle>
                    <DialogDescription>
                      Preview of your generated component structure
                    </DialogDescription>
                  </DialogHeader>
                  <div className="p-4 border rounded-lg bg-background">
                    <div dangerouslySetInnerHTML={{ __html: generatedCode.react.replace(/import.*\n/g, '') }} />
                  </div>
                </DialogContent>
              </Dialog>
              <Badge variant="outline" className="flex items-center gap-1">
                <Code className="w-3 h-3" />
                Code Generated
              </Badge>
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Visual Builder */}
        <div className="xl:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Palette className="w-5 h-5" />
                Visual Builder
              </CardTitle>
              <CardDescription>
                Drag components from the library to create your interface
              </CardDescription>
            </CardHeader>
            <CardContent className="p-0">
              <VisualComponentBuilder onGenerateCode={handleGenerateCode} />
            </CardContent>
          </Card>
        </div>

        {/* Generated Code Panel */}
        <div className="xl:col-span-1">
          <Card className="h-full">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileCode className="w-5 h-5" />
                Generated Code
              </CardTitle>
              <CardDescription>
                Production-ready code in multiple frameworks
              </CardDescription>
            </CardHeader>
            <CardContent>
              {generatedCode ? (
                <Tabs value={activeCodeTab} onValueChange={setActiveCodeTab} className="space-y-4">
                  <TabsList className="grid w-full grid-cols-3">
                    <TabsTrigger value="react">React</TabsTrigger>
                    <TabsTrigger value="vue">Vue</TabsTrigger>
                    <TabsTrigger value="angular">Angular</TabsTrigger>
                  </TabsList>
                  
                  {(['react', 'vue', 'angular'] as const).map(framework => (
                    <TabsContent key={framework} value={framework} className="space-y-4">
                      <div className="flex items-center justify-between">
                        <Badge variant="outline">
                          {getLanguageLabel(framework)}
                        </Badge>
                        <div className="flex items-center gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => copyToClipboard(generatedCode[framework], framework)}
                          >
                            <Copy className="w-3 h-3" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => downloadCode(
                              generatedCode[framework], 
                              `GeneratedComponent.${getFileExtension(framework)}`
                            )}
                          >
                            <Download className="w-3 h-3" />
                          </Button>
                        </div>
                      </div>
                      {renderCodeBlock(generatedCode[framework], framework)}
                    </TabsContent>
                  ))}

                  {/* Additional tabs for CSS and JSON */}
                  <div className="grid grid-cols-2 gap-2 mt-4">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => copyToClipboard(generatedCode.css, 'CSS')}
                      className="flex items-center gap-2"
                    >
                      <Copy className="w-3 h-3" />
                      Copy CSS
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => downloadCode(generatedCode.css, 'generated.css')}
                      className="flex items-center gap-2"
                    >
                      <Download className="w-3 h-3" />
                      CSS File
                    </Button>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => copyToClipboard(generatedCode.json, 'JSON')}
                      className="flex items-center gap-2"
                    >
                      <Copy className="w-3 h-3" />
                      Copy JSON
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => downloadCode(generatedCode.json, 'components.json')}
                      className="flex items-center gap-2"
                    >
                      <Download className="w-3 h-3" />
                      JSON File
                    </Button>
                  </div>
                </Tabs>
              ) : (
                <div className="text-center py-12 text-muted-foreground">
                  <FileCode className="w-12 h-12 mx-auto mb-3 opacity-50" />
                  <p className="text-sm">Build your interface and click "Generate Code" to see the output</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Quick Tips */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Quick Tips</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div className="space-y-2">
              <h4 className="font-medium">Drag & Drop</h4>
              <p className="text-muted-foreground">
                Drag components from the left panel onto the canvas to add them to your design
              </p>
            </div>
            <div className="space-y-2">
              <h4 className="font-medium">Properties</h4>
              <p className="text-muted-foreground">
                Select any component to edit its properties, styling, and positioning in the right panel
              </p>
            </div>
            <div className="space-y-2">
              <h4 className="font-medium">Responsive Design</h4>
              <p className="text-muted-foreground">
                Switch between device presets to see how your design looks on different screen sizes
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}