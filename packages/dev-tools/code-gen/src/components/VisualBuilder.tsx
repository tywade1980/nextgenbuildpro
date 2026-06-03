import { useState, useRef } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FrameworkTemplate } from '@/templates/framework-templates'
import { 
  Layout, 
  Type, 
  Square, 
  Circle, 
  Image, 
  List, 
  Menu, 
  Grid3x3,
  ArrowRight,
  Smartphone,
  Monitor,
  Code,
  Plus
} from '@phosphor-icons/react'

interface VisualBuilderProps {
  project: {
    id: string
    name: string
    description: string
    type: string
    status: string
    lastModified: number
    codebase: {
      files: Record<string, string>
      dependencies: string[]
    }
  }
  onUpdateProject: (projectId: string, updates: any) => void
}

interface BuilderComponent {
  id: string
  type: 'container' | 'text' | 'button' | 'input' | 'image' | 'list' | 'card' | 'navigation'
  name: string
  icon: React.ReactNode
  properties: Record<string, any>
  children?: BuilderComponent[]
}

interface CanvasComponent extends BuilderComponent {
  x: number
  y: number
  width: number
  height: number
}

const componentLibrary: BuilderComponent[] = [
  {
    id: 'container',
    type: 'container',
    name: 'Container',
    icon: <Layout className="w-4 h-4" />,
    properties: { 
      backgroundColor: '#ffffff',
      padding: 16,
      margin: 0,
      borderRadius: 8
    }
  },
  {
    id: 'text',
    type: 'text',
    name: 'Text',
    icon: <Type className="w-4 h-4" />,
    properties: {
      content: 'Sample Text',
      fontSize: 16,
      fontWeight: 'normal',
      color: '#000000',
      textAlign: 'left'
    }
  },
  {
    id: 'button',
    type: 'button',
    name: 'Button',
    icon: <Square className="w-4 h-4" />,
    properties: {
      text: 'Button',
      backgroundColor: '#007AFF',
      textColor: '#ffffff',
      borderRadius: 8,
      padding: 12,
      action: 'onClick'
    }
  },
  {
    id: 'input',
    type: 'input',
    name: 'Input Field',
    icon: <Circle className="w-4 h-4" />,
    properties: {
      placeholder: 'Enter text...',
      type: 'text',
      borderColor: '#cccccc',
      borderRadius: 4,
      padding: 8
    }
  },
  {
    id: 'image',
    type: 'image',
    name: 'Image',
    icon: <Image className="w-4 h-4" />,
    properties: {
      src: 'https://via.placeholder.com/150',
      alt: 'Image',
      width: 150,
      height: 150,
      borderRadius: 0
    }
  },
  {
    id: 'list',
    type: 'list',
    name: 'List',
    icon: <List className="w-4 h-4" />,
    properties: {
      items: ['Item 1', 'Item 2', 'Item 3'],
      itemHeight: 48,
      separator: true
    }
  },
  {
    id: 'navigation',
    type: 'navigation',
    name: 'Navigation',
    icon: <Menu className="w-4 h-4" />,
    properties: {
      items: ['Home', 'About', 'Contact'],
      orientation: 'horizontal',
      backgroundColor: '#f8f9fa'
    }
  }
]

export function VisualBuilder({ project, onUpdateProject }: VisualBuilderProps) {
  const [canvasComponents, setCanvasComponents] = useState<CanvasComponent[]>([])
  const [selectedComponent, setSelectedComponent] = useState<CanvasComponent | null>(null)
  const [activeView, setActiveView] = useState<'design' | 'code'>('design')
  const [isDragging, setIsDragging] = useState(false)
  const [draggedComponent, setDraggedComponent] = useState<BuilderComponent | null>(null)
  const canvasRef = useRef<HTMLDivElement>(null)

  const handleDragStart = (e: React.DragEvent, component: BuilderComponent) => {
    setIsDragging(true)
    setDraggedComponent(component)
    e.dataTransfer.effectAllowed = 'copy'
  }

  const handleDragEnd = () => {
    setIsDragging(false)
    setDraggedComponent(null)
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'copy'
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    
    if (!draggedComponent || !canvasRef.current) return
    
    const rect = canvasRef.current.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    
    const newComponent: CanvasComponent = {
      ...draggedComponent,
      id: `${draggedComponent.id}-${Date.now()}`,
      x: Math.max(0, x - 50),
      y: Math.max(0, y - 20),
      width: getDefaultWidth(draggedComponent.type),
      height: getDefaultHeight(draggedComponent.type)
    }
    
    setCanvasComponents(prev => [...prev, newComponent])
    setIsDragging(false)
    setDraggedComponent(null)
  }

  const getDefaultWidth = (type: BuilderComponent['type']): number => {
    switch (type) {
      case 'button': return 120
      case 'input': return 200
      case 'image': return 150
      case 'text': return 200
      case 'navigation': return 300
      case 'list': return 250
      case 'container': return 300
      default: return 150
    }
  }

  const getDefaultHeight = (type: BuilderComponent['type']): number => {
    switch (type) {
      case 'button': return 40
      case 'input': return 36
      case 'image': return 150
      case 'text': return 24
      case 'navigation': return 48
      case 'list': return 200
      case 'container': return 200
      default: return 40
    }
  }

  const addComponent = (component: BuilderComponent) => {
    const newComponent: CanvasComponent = {
      ...component,
      id: `${component.id}-${Date.now()}`,
      x: Math.random() * 200 + 50,
      y: Math.random() * 200 + 50,
      width: getDefaultWidth(component.type),
      height: getDefaultHeight(component.type)
    }
    setCanvasComponents(prev => [...prev, newComponent])
  }

  const renderCanvasComponent = (component: CanvasComponent) => {
    const isSelected = selectedComponent?.id === component.id
    const baseStyle = {
      position: 'absolute' as const,
      left: component.x,
      top: component.y,
      width: component.width,
      height: component.height,
      border: isSelected ? '2px solid #007AFF' : '1px solid #e0e0e0',
      borderRadius: component.properties.borderRadius || 4,
      cursor: 'pointer',
      padding: component.properties.padding || 8,
      backgroundColor: component.properties.backgroundColor || '#ffffff',
      color: component.properties.color || component.properties.textColor || '#000000'
    }

    const handleClick = (e: React.MouseEvent) => {
      e.stopPropagation()
      setSelectedComponent(component)
    }

    switch (component.type) {
      case 'text':
        return (
          <div
            key={component.id}
            style={{
              ...baseStyle,
              fontSize: component.properties.fontSize,
              fontWeight: component.properties.fontWeight,
              textAlign: component.properties.textAlign,
              display: 'flex',
              alignItems: 'center'
            }}
            onClick={handleClick}
          >
            {component.properties.content}
          </div>
        )
      
      case 'button':
        return (
          <div
            key={component.id}
            style={{
              ...baseStyle,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 'bold'
            }}
            onClick={handleClick}
          >
            {component.properties.text}
          </div>
        )
      
      case 'input':
        return (
          <input
            key={component.id}
            style={baseStyle}
            placeholder={component.properties.placeholder}
            type={component.properties.type}
            onClick={handleClick}
            readOnly
          />
        )
      
      case 'image':
        return (
          <div
            key={component.id}
            style={{
              ...baseStyle,
              backgroundImage: `url(${component.properties.src})`,
              backgroundSize: 'cover',
              backgroundPosition: 'center'
            }}
            onClick={handleClick}
          />
        )
      
      case 'list':
        return (
          <div
            key={component.id}
            style={baseStyle}
            onClick={handleClick}
          >
            {component.properties.items.map((item: string, index: number) => (
              <div
                key={index}
                style={{
                  height: component.properties.itemHeight,
                  display: 'flex',
                  alignItems: 'center',
                  borderBottom: component.properties.separator ? '1px solid #e0e0e0' : 'none'
                }}
              >
                {item}
              </div>
            ))}
          </div>
        )
      
      case 'navigation':
        return (
          <div
            key={component.id}
            style={{
              ...baseStyle,
              display: 'flex',
              flexDirection: component.properties.orientation === 'vertical' ? 'column' : 'row',
              alignItems: 'center',
              justifyContent: 'space-around'
            }}
            onClick={handleClick}
          >
            {component.properties.items.map((item: string, index: number) => (
              <span key={index}>{item}</span>
            ))}
          </div>
        )
      
      case 'container':
        return (
          <div
            key={component.id}
            style={baseStyle}
            onClick={handleClick}
          >
            <span className="text-gray-400 text-sm">Container</span>
          </div>
        )
      
      default:
        return (
          <div
            key={component.id}
            style={baseStyle}
            onClick={handleClick}
          >
            {component.name}
          </div>
        )
    }
  }

  const updateComponentProperty = (property: string, value: any) => {
    if (!selectedComponent) return
    
    setCanvasComponents(prev =>
      prev.map(comp =>
        comp.id === selectedComponent.id
          ? { ...comp, properties: { ...comp.properties, [property]: value } }
          : comp
      )
    )
    
    setSelectedComponent(prev => 
      prev ? { ...prev, properties: { ...prev.properties, [property]: value } } : null
    )
  }

  const exportCodeToProject = () => {
    const generatedCode = generateCode()
    const fileName = project.type === 'android' 
      ? 'src/main/java/com/example/app/GeneratedScreen.kt'
      : 'src/components/GeneratedComponent.tsx'
    
    const updatedFiles = {
      ...project.codebase.files,
      [fileName]: generatedCode
    }
    
    onUpdateProject(project.id, {
      codebase: {
        ...project.codebase,
        files: updatedFiles
      }
    })
  }

  const generateCode = () => {
    if (project.type === 'android') {
      return generateKotlinCode()
    } else if (project.type === 'react') {
      return generateReactCode()
    }
    return '// Code generation not implemented for this project type'
  }

  const generateKotlinCode = () => {
    const imports = `import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp`

    const components = canvasComponents.map(comp => {
      switch (comp.type) {
        case 'text':
          return `    Text(
        text = "${comp.properties.content}",
        fontSize = ${comp.properties.fontSize}.sp,
        modifier = Modifier.padding(${comp.properties.padding}.dp)
    )`
        case 'button':
          return `    Button(
        onClick = { /* TODO: Implement action */ },
        modifier = Modifier.padding(${comp.properties.padding}.dp)
    ) {
        Text("${comp.properties.text}")
    }`
        case 'input':
          return `    OutlinedTextField(
        value = "",
        onValueChange = { /* TODO: Handle input */ },
        placeholder = { Text("${comp.properties.placeholder}") },
        modifier = Modifier.padding(${comp.properties.padding}.dp)
    )`
        default:
          return `    // ${comp.type} component`
      }
    }).join('\n\n')

    return `${imports}

@Composable
fun GeneratedScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
${components}
    }
}`
  }

  const generateReactCode = () => {
    const components = canvasComponents.map(comp => {
      switch (comp.type) {
        case 'text':
          return `      <p style={{ fontSize: '${comp.properties.fontSize}px', color: '${comp.properties.color}' }}>
        ${comp.properties.content}
      </p>`
        case 'button':
          return `      <button 
        style={{ 
          backgroundColor: '${comp.properties.backgroundColor}', 
          color: '${comp.properties.textColor}',
          padding: '${comp.properties.padding}px',
          borderRadius: '${comp.properties.borderRadius}px',
          border: 'none',
          cursor: 'pointer'
        }}
        onClick={() => {/* TODO: Implement action */}}
      >
        ${comp.properties.text}
      </button>`
        case 'input':
          return `      <input 
        type="${comp.properties.type}"
        placeholder="${comp.properties.placeholder}"
        style={{ 
          padding: '${comp.properties.padding}px',
          borderRadius: '${comp.properties.borderRadius}px',
          border: '1px solid ${comp.properties.borderColor}'
        }}
      />`
        default:
          return `      {/* ${comp.type} component */}`
      }
    }).join('\n\n')

    return `import React from 'react'

function GeneratedComponent() {
  return (
    <div style={{ padding: '16px' }}>
${components}
    </div>
  )
}

export default GeneratedComponent`
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 h-[800px]">
      {/* Component Library */}
      <Card className="lg:col-span-1">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Grid3x3 className="w-5 h-5" />
            Components
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {componentLibrary.map((component) => (
              <div
                key={component.id}
                draggable
                onDragStart={(e) => handleDragStart(e, component)}
                onDragEnd={handleDragEnd}
                className="p-3 border rounded-lg cursor-move hover:bg-gray-50 transition-colors flex items-center justify-between"
              >
                <div className="flex items-center gap-2">
                  {component.icon}
                  <span className="text-sm font-medium">{component.name}</span>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => addComponent(component)}
                >
                  <Plus className="w-3 h-3" />
                </Button>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Canvas */}
      <Card className="lg:col-span-2">
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              {project.type === 'android' ? (
                <Smartphone className="w-5 h-5" />
              ) : (
                <Monitor className="w-5 h-5" />
              )}
              Design Canvas
            </CardTitle>
            <Tabs value={activeView} onValueChange={(v) => setActiveView(v as 'design' | 'code')}>
              <TabsList>
                <TabsTrigger value="design">Design</TabsTrigger>
                <TabsTrigger value="code">Code</TabsTrigger>
              </TabsList>
            </Tabs>
            {activeView === 'code' && (
              <Button onClick={exportCodeToProject} size="sm">
                <Code className="w-4 h-4 mr-2" />
                Export to Project
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="p-0">
          {activeView === 'design' ? (
            <div
              ref={canvasRef}
              onDragOver={handleDragOver}
              onDrop={handleDrop}
              className={`relative h-[600px] bg-gray-50 border-2 border-dashed transition-colors ${
                isDragging ? 'border-blue-400 bg-blue-50' : 'border-gray-300'
              }`}
              onClick={() => setSelectedComponent(null)}
            >
              {canvasComponents.map(renderCanvasComponent)}
              {canvasComponents.length === 0 && (
                <div className="absolute inset-0 flex items-center justify-center text-gray-400">
                  <div className="text-center">
                    <ArrowRight className="w-8 h-8 mx-auto mb-2" />
                    <p>Drag components here or click + to add</p>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="h-[600px] overflow-auto">
              <pre className="p-4 text-sm bg-gray-900 text-gray-100 h-full">
                <code>{generateCode()}</code>
              </pre>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Properties Panel */}
      <Card className="lg:col-span-1">
        <CardHeader>
          <CardTitle>Properties</CardTitle>
        </CardHeader>
        <CardContent>
          {selectedComponent ? (
            <div className="space-y-4">
              <div>
                <h4 className="font-semibold text-sm mb-2">{selectedComponent.name}</h4>
                <Badge variant="outline" className="text-xs">
                  {selectedComponent.type}
                </Badge>
              </div>

              {/* Common properties */}
              {selectedComponent.type === 'text' && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="text-content">Content</Label>
                    <Input
                      id="text-content"
                      value={selectedComponent.properties.content}
                      onChange={(e) => updateComponentProperty('content', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="font-size">Font Size</Label>
                    <Input
                      id="font-size"
                      type="number"
                      value={selectedComponent.properties.fontSize}
                      onChange={(e) => updateComponentProperty('fontSize', parseInt(e.target.value))}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="text-color">Color</Label>
                    <Input
                      id="text-color"
                      type="color"
                      value={selectedComponent.properties.color}
                      onChange={(e) => updateComponentProperty('color', e.target.value)}
                    />
                  </div>
                </>
              )}

              {selectedComponent.type === 'button' && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="button-text">Text</Label>
                    <Input
                      id="button-text"
                      value={selectedComponent.properties.text}
                      onChange={(e) => updateComponentProperty('text', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="bg-color">Background Color</Label>
                    <Input
                      id="bg-color"
                      type="color"
                      value={selectedComponent.properties.backgroundColor}
                      onChange={(e) => updateComponentProperty('backgroundColor', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="text-color">Text Color</Label>
                    <Input
                      id="text-color"
                      type="color"
                      value={selectedComponent.properties.textColor}
                      onChange={(e) => updateComponentProperty('textColor', e.target.value)}
                    />
                  </div>
                </>
              )}

              {selectedComponent.type === 'input' && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="placeholder">Placeholder</Label>
                    <Input
                      id="placeholder"
                      value={selectedComponent.properties.placeholder}
                      onChange={(e) => updateComponentProperty('placeholder', e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="input-type">Type</Label>
                    <Select
                      value={selectedComponent.properties.type}
                      onValueChange={(value) => updateComponentProperty('type', value)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="text">Text</SelectItem>
                        <SelectItem value="email">Email</SelectItem>
                        <SelectItem value="password">Password</SelectItem>
                        <SelectItem value="number">Number</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </>
              )}

              <div className="pt-4 border-t">
                <h5 className="font-semibold text-sm mb-2">Natural Language Intent</h5>
                <Textarea
                  placeholder="Describe what this component should do..."
                  className="text-sm"
                  rows={3}
                />
                <p className="text-xs text-muted-foreground mt-2">
                  Example: "When user taps this button, navigate to the settings screen"
                </p>
              </div>
            </div>
          ) : (
            <div className="text-center text-muted-foreground py-8">
              <Code className="w-8 h-8 mx-auto mb-2" />
              <p>Select a component to edit its properties</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}