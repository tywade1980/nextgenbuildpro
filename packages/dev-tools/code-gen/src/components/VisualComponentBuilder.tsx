import React, { useState, useRef, useCallback } from 'react'
import { DndProvider, useDrag, useDrop } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Separator } from '@/components/ui/separator'
import { ScrollArea } from '@/components/ui/scroll-area'
import { toast } from 'sonner'
import { 
  MousePointer, 
  Square, 
  Type, 
  Image, 
  Menu, 
  Grid3x3, 
  Layers,
  Settings,
  Code,
  Play,
  Download,
  Undo,
  Redo,
  Trash2,
  Copy,
  Eye,
  Smartphone,
  Tablet,
  Monitor
} from '@phosphor-icons/react'

interface ComponentDef {
  id: string
  type: string
  name: string
  icon: React.ReactNode
  category: string
  defaultProps: Record<string, any>
  allowedChildren?: string[]
}

interface CanvasComponent {
  id: string
  type: string
  props: Record<string, any>
  children: CanvasComponent[]
  position: { x: number; y: number }
  size: { width: number; height: number }
}

interface DragItem {
  type: string
  componentType: string
  id?: string
}

const COMPONENT_LIBRARY: ComponentDef[] = [
  {
    id: 'button',
    type: 'Button',
    name: 'Button',
    icon: <Square className="w-4 h-4" />,
    category: 'Basic',
    defaultProps: {
      children: 'Button',
      variant: 'default',
      size: 'default'
    }
  },
  {
    id: 'input',
    type: 'Input',
    name: 'Input',
    icon: <Type className="w-4 h-4" />,
    category: 'Form',
    defaultProps: {
      placeholder: 'Enter text...',
      type: 'text'
    }
  },
  {
    id: 'textarea',
    type: 'Textarea',
    name: 'Textarea',
    icon: <Menu className="w-4 h-4" />,
    category: 'Form',
    defaultProps: {
      placeholder: 'Enter text...',
      rows: 3
    }
  },
  {
    id: 'card',
    type: 'Card',
    name: 'Card',
    icon: <Layers className="w-4 h-4" />,
    category: 'Layout',
    defaultProps: {
      title: 'Card Title',
      description: 'Card description'
    },
    allowedChildren: ['Button', 'Input', 'Textarea', 'div']
  },
  {
    id: 'div',
    type: 'div',
    name: 'Container',
    icon: <Grid3x3 className="w-4 h-4" />,
    category: 'Layout',
    defaultProps: {
      className: 'p-4 border border-dashed border-gray-300 min-h-[100px]'
    },
    allowedChildren: ['Button', 'Input', 'Textarea', 'Card', 'div', 'img', 'p']
  },
  {
    id: 'img',
    type: 'img',
    name: 'Image',
    icon: <Image className="w-4 h-4" />,
    category: 'Media',
    defaultProps: {
      src: '/api/placeholder/300/200',
      alt: 'Placeholder image',
      className: 'rounded-md'
    }
  },
  {
    id: 'p',
    type: 'p',
    name: 'Text',
    icon: <Type className="w-4 h-4" />,
    category: 'Basic',
    defaultProps: {
      children: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
      className: 'text-sm text-muted-foreground'
    }
  }
]

const DEVICE_PRESETS = [
  { id: 'mobile', name: 'Mobile', icon: Smartphone, width: 375, height: 667 },
  { id: 'tablet', name: 'Tablet', icon: Tablet, width: 768, height: 1024 },
  { id: 'desktop', name: 'Desktop', icon: Monitor, width: 1440, height: 900 }
]

// Draggable component from library
const DraggableComponent: React.FC<{ component: ComponentDef }> = ({ component }) => {
  const [{ isDragging }, drag] = useDrag(() => ({
    type: 'COMPONENT',
    item: { type: 'COMPONENT', componentType: component.type, id: component.id },
    collect: (monitor) => ({
      isDragging: monitor.isDragging()
    })
  }))

  return (
    <div
      ref={drag}
      className={`p-3 border rounded-lg cursor-grab hover:bg-muted/50 transition-colors ${
        isDragging ? 'opacity-50' : ''
      }`}
    >
      <div className="flex items-center gap-2">
        {component.icon}
        <span className="text-sm font-medium">{component.name}</span>
      </div>
    </div>
  )
}

// Canvas component renderer
const CanvasComponentRenderer: React.FC<{
  component: CanvasComponent
  isSelected: boolean
  onSelect: (id: string) => void
  onUpdate: (id: string, updates: Partial<CanvasComponent>) => void
}> = ({ component, isSelected, onSelect, onUpdate }) => {
  const [{ isDragging }, drag] = useDrag(() => ({
    type: 'CANVAS_COMPONENT',
    item: { type: 'CANVAS_COMPONENT', id: component.id },
    collect: (monitor) => ({
      isDragging: monitor.isDragging()
    })
  }))

  const renderComponent = () => {
    const commonProps = {
      onClick: (e: React.MouseEvent) => {
        e.stopPropagation()
        onSelect(component.id)
      },
      className: `${component.props.className || ''} ${
        isSelected ? 'ring-2 ring-primary ring-offset-2' : ''
      }`,
      style: {
        position: 'absolute' as const,
        left: component.position.x,
        top: component.position.y,
        width: component.size.width,
        height: component.size.height
      }
    }

    switch (component.type) {
      case 'Button':
        return (
          <Button {...commonProps} variant={component.props.variant}>
            {component.props.children}
          </Button>
        )
      case 'Input':
        return <Input {...commonProps} placeholder={component.props.placeholder} />
      case 'Textarea':
        return <Textarea {...commonProps} placeholder={component.props.placeholder} rows={component.props.rows} />
      case 'Card':
        return (
          <Card {...commonProps}>
            <CardHeader>
              <CardTitle>{component.props.title}</CardTitle>
              <CardDescription>{component.props.description}</CardDescription>
            </CardHeader>
            <CardContent>
              {component.children.map(child => (
                <CanvasComponentRenderer
                  key={child.id}
                  component={child}
                  isSelected={false}
                  onSelect={onSelect}
                  onUpdate={onUpdate}
                />
              ))}
            </CardContent>
          </Card>
        )
      case 'img':
        return <img {...commonProps} src={component.props.src} alt={component.props.alt} />
      case 'p':
        return <p {...commonProps}>{component.props.children}</p>
      case 'div':
        return (
          <div {...commonProps}>
            {component.children.map(child => (
              <CanvasComponentRenderer
                key={child.id}
                component={child}
                isSelected={false}
                onSelect={onSelect}
                onUpdate={onUpdate}
              />
            ))}
          </div>
        )
      default:
        return <div {...commonProps}>Unknown component: {component.type}</div>
    }
  }

  return <div ref={drag}>{renderComponent()}</div>
}

// Drop zone canvas
const DesignCanvas: React.FC<{
  components: CanvasComponent[]
  selectedComponent: string | null
  onSelectComponent: (id: string | null) => void
  onAddComponent: (componentType: string, position: { x: number; y: number }) => void
  onUpdateComponent: (id: string, updates: Partial<CanvasComponent>) => void
  devicePreset: typeof DEVICE_PRESETS[0]
}> = ({ 
  components, 
  selectedComponent, 
  onSelectComponent, 
  onAddComponent, 
  onUpdateComponent,
  devicePreset 
}) => {
  const [{ isOver }, drop] = useDrop(() => ({
    accept: ['COMPONENT', 'CANVAS_COMPONENT'],
    drop: (item: DragItem, monitor) => {
      const offset = monitor.getClientOffset()
      const canvasRect = canvasRef.current?.getBoundingClientRect()
      
      if (offset && canvasRect) {
        const x = offset.x - canvasRect.left
        const y = offset.y - canvasRect.top
        
        if (item.type === 'COMPONENT') {
          onAddComponent(item.componentType, { x, y })
        }
      }
    },
    collect: (monitor) => ({
      isOver: monitor.isOver()
    })
  }))

  const canvasRef = useRef<HTMLDivElement>(null)

  return (
    <div className="flex-1 flex flex-col">
      <div className="flex items-center justify-center p-4 border-b">
        <div 
          className="bg-white border-2 border-gray-300 rounded-lg shadow-lg overflow-hidden"
          style={{ 
            width: devicePreset.width, 
            height: devicePreset.height,
            transform: 'scale(0.8)',
            transformOrigin: 'top center'
          }}
        >
          <div
            ref={(el) => {
              drop(el)
              canvasRef.current = el
            }}
            className={`w-full h-full relative bg-background ${
              isOver ? 'bg-blue-50 border-blue-300' : ''
            }`}
            onClick={() => onSelectComponent(null)}
          >
            {components.map(component => (
              <CanvasComponentRenderer
                key={component.id}
                component={component}
                isSelected={selectedComponent === component.id}
                onSelect={onSelectComponent}
                onUpdate={onUpdateComponent}
              />
            ))}
            
            {components.length === 0 && (
              <div className="absolute inset-0 flex items-center justify-center text-muted-foreground">
                <div className="text-center">
                  <Grid3x3 className="w-12 h-12 mx-auto mb-2 opacity-50" />
                  <p>Drag components here to start building</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

// Property editor
const PropertyEditor: React.FC<{
  component: CanvasComponent | null
  onUpdate: (updates: Partial<CanvasComponent>) => void
}> = ({ component, onUpdate }) => {
  if (!component) {
    return (
      <div className="p-4 text-center text-muted-foreground">
        <Settings className="w-8 h-8 mx-auto mb-2 opacity-50" />
        <p>Select a component to edit properties</p>
      </div>
    )
  }

  const updateProps = (key: string, value: any) => {
    onUpdate({
      props: {
        ...component.props,
        [key]: value
      }
    })
  }

  const updatePosition = (axis: 'x' | 'y', value: number) => {
    onUpdate({
      position: {
        ...component.position,
        [axis]: value
      }
    })
  }

  const updateSize = (dimension: 'width' | 'height', value: number) => {
    onUpdate({
      size: {
        ...component.size,
        [dimension]: value
      }
    })
  }

  return (
    <ScrollArea className="h-full">
      <div className="p-4 space-y-6">
        <div>
          <h3 className="font-semibold mb-3">Component Properties</h3>
          <div className="space-y-3">
            <div>
              <Label className="text-xs font-medium text-muted-foreground">Type</Label>
              <div className="text-sm font-medium">{component.type}</div>
            </div>
            
            {/* Common properties based on component type */}
            {component.type === 'Button' && (
              <>
                <div>
                  <Label htmlFor="button-text">Text</Label>
                  <Input
                    id="button-text"
                    value={component.props.children || ''}
                    onChange={(e) => updateProps('children', e.target.value)}
                  />
                </div>
                <div>
                  <Label htmlFor="button-variant">Variant</Label>
                  <Select value={component.props.variant} onValueChange={(value) => updateProps('variant', value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="default">Default</SelectItem>
                      <SelectItem value="destructive">Destructive</SelectItem>
                      <SelectItem value="outline">Outline</SelectItem>
                      <SelectItem value="secondary">Secondary</SelectItem>
                      <SelectItem value="ghost">Ghost</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </>
            )}

            {(component.type === 'Input' || component.type === 'Textarea') && (
              <div>
                <Label htmlFor="input-placeholder">Placeholder</Label>
                <Input
                  id="input-placeholder"
                  value={component.props.placeholder || ''}
                  onChange={(e) => updateProps('placeholder', e.target.value)}
                />
              </div>
            )}

            {component.type === 'Card' && (
              <>
                <div>
                  <Label htmlFor="card-title">Title</Label>
                  <Input
                    id="card-title"
                    value={component.props.title || ''}
                    onChange={(e) => updateProps('title', e.target.value)}
                  />
                </div>
                <div>
                  <Label htmlFor="card-description">Description</Label>
                  <Input
                    id="card-description"
                    value={component.props.description || ''}
                    onChange={(e) => updateProps('description', e.target.value)}
                  />
                </div>
              </>
            )}

            {component.type === 'p' && (
              <div>
                <Label htmlFor="text-content">Content</Label>
                <Textarea
                  id="text-content"
                  value={component.props.children || ''}
                  onChange={(e) => updateProps('children', e.target.value)}
                />
              </div>
            )}

            {component.type === 'img' && (
              <>
                <div>
                  <Label htmlFor="img-src">Source URL</Label>
                  <Input
                    id="img-src"
                    value={component.props.src || ''}
                    onChange={(e) => updateProps('src', e.target.value)}
                  />
                </div>
                <div>
                  <Label htmlFor="img-alt">Alt Text</Label>
                  <Input
                    id="img-alt"
                    value={component.props.alt || ''}
                    onChange={(e) => updateProps('alt', e.target.value)}
                  />
                </div>
              </>
            )}
          </div>
        </div>

        <Separator />

        <div>
          <h3 className="font-semibold mb-3">Position & Size</h3>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <Label htmlFor="pos-x">X Position</Label>
              <Input
                id="pos-x"
                type="number"
                value={component.position.x}
                onChange={(e) => updatePosition('x', parseInt(e.target.value) || 0)}
              />
            </div>
            <div>
              <Label htmlFor="pos-y">Y Position</Label>
              <Input
                id="pos-y"
                type="number"
                value={component.position.y}
                onChange={(e) => updatePosition('y', parseInt(e.target.value) || 0)}
              />
            </div>
            <div>
              <Label htmlFor="size-width">Width</Label>
              <Input
                id="size-width"
                type="number"
                value={component.size.width}
                onChange={(e) => updateSize('width', parseInt(e.target.value) || 0)}
              />
            </div>
            <div>
              <Label htmlFor="size-height">Height</Label>
              <Input
                id="size-height"
                type="number"
                value={component.size.height}
                onChange={(e) => updateSize('height', parseInt(e.target.value) || 0)}
              />
            </div>
          </div>
        </div>

        <Separator />

        <div>
          <h3 className="font-semibold mb-3">Styling</h3>
          <div>
            <Label htmlFor="custom-classes">CSS Classes</Label>
            <Textarea
              id="custom-classes"
              value={component.props.className || ''}
              onChange={(e) => updateProps('className', e.target.value)}
              placeholder="Add Tailwind classes..."
              rows={3}
            />
          </div>
        </div>
      </div>
    </ScrollArea>
  )
}

export const VisualComponentBuilder: React.FC<{
  onGenerateCode: (components: CanvasComponent[]) => void
}> = ({ onGenerateCode }) => {
  const [components, setComponents] = useState<CanvasComponent[]>([])
  const [selectedComponent, setSelectedComponent] = useState<string | null>(null)
  const [selectedDevice, setSelectedDevice] = useState(DEVICE_PRESETS[2]) // Desktop by default
  const [history, setHistory] = useState<CanvasComponent[][]>([])
  const [historyIndex, setHistoryIndex] = useState(-1)

  const addToHistory = useCallback((newComponents: CanvasComponent[]) => {
    const newHistory = history.slice(0, historyIndex + 1)
    newHistory.push([...newComponents])
    setHistory(newHistory)
    setHistoryIndex(newHistory.length - 1)
  }, [history, historyIndex])

  const undo = () => {
    if (historyIndex > 0) {
      setHistoryIndex(historyIndex - 1)
      setComponents([...history[historyIndex - 1]])
    }
  }

  const redo = () => {
    if (historyIndex < history.length - 1) {
      setHistoryIndex(historyIndex + 1)
      setComponents([...history[historyIndex + 1]])
    }
  }

  const addComponent = (componentType: string, position: { x: number; y: number }) => {
    const componentDef = COMPONENT_LIBRARY.find(c => c.type === componentType)
    if (!componentDef) return

    const newComponent: CanvasComponent = {
      id: `${componentType}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      type: componentType,
      props: { ...componentDef.defaultProps },
      children: [],
      position,
      size: { width: 200, height: 40 }
    }

    const newComponents = [...components, newComponent]
    setComponents(newComponents)
    addToHistory(newComponents)
    setSelectedComponent(newComponent.id)
    toast.success(`Added ${componentDef.name} to canvas`)
  }

  const updateComponent = (id: string, updates: Partial<CanvasComponent>) => {
    const newComponents = components.map(comp =>
      comp.id === id ? { ...comp, ...updates } : comp
    )
    setComponents(newComponents)
    addToHistory(newComponents)
  }

  const deleteComponent = (id: string) => {
    const newComponents = components.filter(comp => comp.id !== id)
    setComponents(newComponents)
    addToHistory(newComponents)
    setSelectedComponent(null)
    toast.success('Component deleted')
  }

  const selectedComponentData = selectedComponent ? 
    components.find(c => c.id === selectedComponent) : null

  const generateCode = () => {
    onGenerateCode(components)
    toast.success('Code generated successfully!')
  }

  const clearCanvas = () => {
    const newComponents: CanvasComponent[] = []
    setComponents(newComponents)
    addToHistory(newComponents)
    setSelectedComponent(null)
    toast.success('Canvas cleared')
  }

  const groupedComponents = COMPONENT_LIBRARY.reduce((acc, comp) => {
    if (!acc[comp.category]) acc[comp.category] = []
    acc[comp.category].push(comp)
    return acc
  }, {} as Record<string, ComponentDef[]>)

  return (
    <DndProvider backend={HTML5Backend}>
      <div className="h-screen flex flex-col">
        {/* Toolbar */}
        <div className="flex items-center justify-between p-4 border-b bg-card">
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={undo} disabled={historyIndex <= 0}>
              <Undo className="w-4 h-4" />
            </Button>
            <Button variant="outline" size="sm" onClick={redo} disabled={historyIndex >= history.length - 1}>
              <Redo className="w-4 h-4" />
            </Button>
            <Separator orientation="vertical" className="h-6" />
            <div className="flex items-center gap-1">
              {DEVICE_PRESETS.map(device => {
                const Icon = device.icon
                return (
                  <Button
                    key={device.id}
                    variant={selectedDevice.id === device.id ? "default" : "outline"}
                    size="sm"
                    onClick={() => setSelectedDevice(device)}
                  >
                    <Icon className="w-4 h-4" />
                  </Button>
                )
              })}
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm">
              <Eye className="w-4 h-4 mr-2" />
              Preview
            </Button>
            <Button variant="outline" size="sm" onClick={generateCode}>
              <Code className="w-4 h-4 mr-2" />
              Generate Code
            </Button>
            <Button variant="outline" size="sm" onClick={clearCanvas}>
              <Trash2 className="w-4 h-4 mr-2" />
              Clear
            </Button>
          </div>
        </div>

        <div className="flex-1 flex">
          {/* Component Library */}
          <div className="w-64 border-r bg-card">
            <div className="p-4">
              <h2 className="font-semibold mb-4">Components</h2>
              <Tabs defaultValue="Basic" className="w-full">
                <TabsList className="grid w-full grid-cols-3">
                  <TabsTrigger value="Basic">Basic</TabsTrigger>
                  <TabsTrigger value="Form">Form</TabsTrigger>
                  <TabsTrigger value="Layout">Layout</TabsTrigger>
                </TabsList>
                
                {Object.entries(groupedComponents).map(([category, components]) => (
                  <TabsContent key={category} value={category} className="space-y-2 mt-4">
                    {components.map(component => (
                      <DraggableComponent key={component.id} component={component} />
                    ))}
                  </TabsContent>
                ))}
              </Tabs>
            </div>
          </div>

          {/* Canvas */}
          <DesignCanvas
            components={components}
            selectedComponent={selectedComponent}
            onSelectComponent={setSelectedComponent}
            onAddComponent={addComponent}
            onUpdateComponent={updateComponent}
            devicePreset={selectedDevice}
          />

          {/* Properties Panel */}
          <div className="w-80 border-l bg-card">
            <div className="p-4 border-b">
              <h2 className="font-semibold">Properties</h2>
            </div>
            <PropertyEditor
              component={selectedComponentData}
              onUpdate={(updates) => {
                if (selectedComponent) {
                  updateComponent(selectedComponent, updates)
                }
              }}
            />
          </div>
        </div>
      </div>
    </DndProvider>
  )
}