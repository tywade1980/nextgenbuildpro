import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Smartphone, Wifi, Battery, Signal, Clock, Home, Back, Menu, Plus } from '@phosphor-icons/react'

interface Project {
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

interface AndroidPreviewProps {
  project: Project
  onUpdateProject: (projectId: string, updates: Partial<Project>) => void
}

interface UIComponent {
  id: string
  type: 'text' | 'button' | 'input' | 'image' | 'card' | 'list' | 'toolbar'
  props: Record<string, any>
  x: number
  y: number
  width: number
  height: number
  children?: UIComponent[]
}

export function AndroidPreview({ project, onUpdateProject }: AndroidPreviewProps) {
  const [selectedComponent, setSelectedComponent] = useState<UIComponent | null>(null)
  const [components, setComponents] = useState<UIComponent[]>([
    {
      id: 'toolbar',
      type: 'toolbar',
      props: { title: project.name || 'Android App' },
      x: 0,
      y: 0,
      width: 360,
      height: 56
    },
    {
      id: 'welcome-text',
      type: 'text',
      props: { text: 'Welcome to ' + project.name, style: 'headline' },
      x: 16,
      y: 80,
      width: 328,
      height: 48
    },
    {
      id: 'counter-text',
      type: 'text',
      props: { text: 'Count: 0', style: 'body' },
      x: 16,
      y: 140,
      width: 100,
      height: 32
    },
    {
      id: 'increment-button',
      type: 'button',
      props: { text: 'Increment', variant: 'primary' },
      x: 16,
      y: 180,
      width: 120,
      height: 48
    }
  ])

  const handleComponentClick = (component: UIComponent) => {
    setSelectedComponent(component)
  }

  const renderComponent = (component: UIComponent) => {
    const baseStyle = {
      position: 'absolute' as const,
      left: component.x,
      top: component.y,
      width: component.width,
      height: component.height,
      cursor: 'pointer',
      border: selectedComponent?.id === component.id ? '2px solid #3b82f6' : '2px solid transparent',
      borderRadius: '4px'
    }

    switch (component.type) {
      case 'toolbar':
        return (
          <div
            key={component.id}
            style={baseStyle}
            className="bg-primary text-primary-foreground flex items-center px-4"
            onClick={() => handleComponentClick(component)}
          >
            <h3 className="font-semibold">{component.props.title}</h3>
          </div>
        )
      
      case 'text':
        const textStyle = component.props.style === 'headline' 
          ? 'text-2xl font-bold' 
          : 'text-base'
        return (
          <div
            key={component.id}
            style={baseStyle}
            className={`flex items-center ${textStyle} text-foreground`}
            onClick={() => handleComponentClick(component)}
          >
            {component.props.text}
          </div>
        )
      
      case 'button':
        const buttonStyle = component.props.variant === 'filled'
          ? 'bg-primary text-primary-foreground'
          : 'bg-secondary text-secondary-foreground'
        return (
          <div
            key={component.id}
            style={baseStyle}
            className={`${buttonStyle} rounded-md flex items-center justify-center font-medium`}
            onClick={() => handleComponentClick(component)}
          >
            {component.props.text}
          </div>
        )
      
      case 'input':
        return (
          <div
            key={component.id}
            style={baseStyle}
            className="border-2 border-border rounded-md px-3 py-2 text-sm"
            onClick={() => handleComponentClick(component)}
          >
            <span className="text-muted-foreground">{component.props.placeholder}</span>
          </div>
        )
      
      default:
        return (
          <div
            key={component.id}
            style={baseStyle}
            className="bg-muted border border-border rounded-md flex items-center justify-center"
            onClick={() => handleComponentClick(component)}
          >
            <span className="text-muted-foreground text-sm">{component.type}</span>
          </div>
        )
    }
  }

  const addComponent = (type: UIComponent['type']) => {
    const newComponent: UIComponent = {
      id: `${type}-${Date.now()}`,
      type,
      props: getDefaultProps(type),
      x: 16,
      y: 100 + components.length * 60,
      width: type === 'button' ? 120 : 328,
      height: type === 'button' ? 48 : 32
    }
    setComponents(prev => [...prev, newComponent])
  }

  const getDefaultProps = (type: UIComponent['type']) => {
    switch (type) {
      case 'text':
        return { text: 'New Text', style: 'body' }
      case 'button':
        return { text: 'Button', variant: 'filled' }
      case 'input':
        return { placeholder: 'Enter text...' }
      case 'toolbar':
        return { title: 'App Title' }
      default:
        return {}
    }
  }

  const updateComponentProps = (componentId: string, newProps: Record<string, any>) => {
    setComponents(prev => 
      prev.map(comp => 
        comp.id === componentId 
          ? { ...comp, props: { ...comp.props, ...newProps } }
          : comp
      )
    )
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Device Selection */}
      <Card className="lg:col-span-3 mb-4">
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Smartphone className="w-5 h-5" />
              Device Preview: {project.name}
            </CardTitle>
            <div className="flex items-center gap-4">
              <Badge variant="outline">{project.type}</Badge>
              <select className="px-3 py-2 border border-border rounded-md text-sm">
                <option value="pixel6">Pixel 6</option>
                <option value="galaxy">Galaxy S23</option>
                <option value="tablet">Tablet</option>
              </select>
            </div>
          </div>
        </CardHeader>
      </Card>

      {/* Phone Preview */}
      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Smartphone className="w-5 h-5" />
            Live Preview
          </CardTitle>
        </CardHeader>
        <CardContent className="flex justify-center">
          <div className="relative">
            {/* Phone Frame with enhanced graphics */}
            <div className="w-80 h-[640px] bg-gradient-to-b from-gray-800 to-gray-900 rounded-[2.5rem] p-3 shadow-2xl">
              {/* Screen */}
              <div className="w-full h-full bg-white rounded-[2rem] overflow-hidden relative shadow-inner">
                {/* Notch/Punch hole */}
                <div className="absolute top-2 left-1/2 transform -translate-x-1/2 w-16 h-4 bg-black rounded-full z-10"></div>
                
                {/* Status Bar */}
                <div className="h-8 bg-gradient-to-r from-gray-50 to-gray-100 flex items-center justify-between px-6 text-xs text-gray-700 pt-2">
                  <div className="flex items-center gap-2">
                    <Clock className="w-3 h-3" />
                    <span className="font-medium">9:41</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Signal className="w-3 h-3 text-green-600" />
                    <Wifi className="w-3 h-3 text-blue-600" />
                    <Battery className="w-3 h-3 text-green-600" />
                    <span className="text-xs">100%</span>
                  </div>
                </div>

                {/* App Content */}
                <div className="relative w-full h-[calc(100%-3rem)] bg-gradient-to-br from-blue-50 to-indigo-100 overflow-hidden">
                  {components.map(renderComponent)}
                  
                  {/* Floating Action Button */}
                  <div className="absolute bottom-20 right-6 w-14 h-14 bg-primary rounded-full shadow-lg flex items-center justify-center">
                    <Plus className="w-6 h-6 text-primary-foreground" />
                  </div>
                </div>

                {/* Navigation Bar */}
                <div className="absolute bottom-0 left-0 right-0 h-12 bg-gradient-to-r from-gray-100 to-gray-200 flex items-center justify-around">
                  <div className="w-8 h-1 bg-gray-400 rounded-full"></div>
                  <Back className="w-5 h-5 text-gray-600" />
                  <Home className="w-5 h-5 text-gray-800" />
                  <Menu className="w-5 h-5 text-gray-600" />
                </div>
              </div>
              
              {/* Power button */}
              <div className="absolute right-0 top-24 w-1 h-12 bg-gray-700 rounded-l"></div>
              {/* Volume buttons */}
              <div className="absolute right-0 top-40 w-1 h-8 bg-gray-700 rounded-l"></div>
              <div className="absolute right-0 top-52 w-1 h-8 bg-gray-700 rounded-l"></div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Component Panel */}
      <div className="space-y-4">
        {/* Component Library */}
        <Card>
          <CardHeader>
            <CardTitle>Component Library</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <Button 
              variant="outline" 
              className="w-full justify-start"
              onClick={() => addComponent('text')}
            >
              📝 Text
            </Button>
            <Button 
              variant="outline" 
              className="w-full justify-start"
              onClick={() => addComponent('button')}
            >
              🔘 Button
            </Button>
            <Button 
              variant="outline" 
              className="w-full justify-start"
              onClick={() => addComponent('input')}
            >
              📝 Input Field
            </Button>
            <Button 
              variant="outline" 
              className="w-full justify-start"
              onClick={() => addComponent('toolbar')}
            >
              📊 Toolbar
            </Button>
          </CardContent>
        </Card>

        {/* Properties Panel */}
        {selectedComponent && (
          <Card>
            <CardHeader>
              <CardTitle>Properties</CardTitle>
              <p className="text-sm text-muted-foreground">
                {selectedComponent.type} • {selectedComponent.id}
              </p>
            </CardHeader>
            <CardContent className="space-y-4">
              {selectedComponent.type === 'text' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium">Text Content</label>
                  <input
                    type="text"
                    value={selectedComponent.props.text || ''}
                    onChange={(e) => updateComponentProps(selectedComponent.id, { text: e.target.value })}
                    className="w-full px-3 py-2 border border-border rounded-md text-sm"
                  />
                  <label className="text-sm font-medium">Style</label>
                  <select
                    value={selectedComponent.props.style || 'body'}
                    onChange={(e) => updateComponentProps(selectedComponent.id, { style: e.target.value })}
                    className="w-full px-3 py-2 border border-border rounded-md text-sm"
                  >
                    <option value="body">Body</option>
                    <option value="headline">Headline</option>
                  </select>
                </div>
              )}

              {selectedComponent.type === 'button' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium">Button Text</label>
                  <input
                    type="text"
                    value={selectedComponent.props.text || ''}
                    onChange={(e) => updateComponentProps(selectedComponent.id, { text: e.target.value })}
                    className="w-full px-3 py-2 border border-border rounded-md text-sm"
                  />
                  <label className="text-sm font-medium">Variant</label>
                  <select
                    value={selectedComponent.props.variant || 'filled'}
                    onChange={(e) => updateComponentProps(selectedComponent.id, { variant: e.target.value })}
                    className="w-full px-3 py-2 border border-border rounded-md text-sm"
                  >
                    <option value="filled">Filled</option>
                    <option value="outlined">Outlined</option>
                  </select>
                </div>
              )}

              {selectedComponent.type === 'toolbar' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium">Title</label>
                  <input
                    type="text"
                    value={selectedComponent.props.title || ''}
                    onChange={(e) => updateComponentProps(selectedComponent.id, { title: e.target.value })}
                    className="w-full px-3 py-2 border border-border rounded-md text-sm"
                  />
                </div>
              )}

              <Separator />

              <div className="text-sm text-muted-foreground">
                <p><strong>Natural Language Prompt:</strong></p>
                <p className="mt-2 p-3 bg-muted rounded-md">
                  {selectedComponent.type === 'button' 
                    ? `"Make this button perform an action when tapped. When the user taps this button, I want it to ${selectedComponent.props.text?.toLowerCase() || 'perform an action'}."`
                    : selectedComponent.type === 'text'
                    ? `"This text should display: '${selectedComponent.props.text}'. Make it ${selectedComponent.props.style === 'headline' ? 'large and prominent' : 'normal size'}."`
                    : `"This ${selectedComponent.type} should help users interact with the app in an intuitive way."`
                  }
                </p>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Code Generation */}
        <Card>
          <CardHeader>
            <CardTitle>Generated Code</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="bg-gray-900 text-gray-100 p-4 rounded-md text-sm font-mono">
              <div className="text-green-400">// Kotlin Compose</div>
              <div className="mt-2">
                {selectedComponent?.type === 'button' && (
                  <>
                    <div>Button(</div>
                    <div className="ml-4">onClick = {"{ /* Action */ }"},</div>
                    <div className="ml-4">modifier = Modifier.padding(16.dp)</div>
                    <div>) {"{"}</div>
                    <div className="ml-4">Text("{selectedComponent.props.text}")</div>
                    <div>{"}"}</div>
                  </>
                )}
                {selectedComponent?.type === 'text' && (
                  <>
                    <div>Text(</div>
                    <div className="ml-4">text = "{selectedComponent.props.text}",</div>
                    <div className="ml-4">style = MaterialTheme.typography.{selectedComponent.props.style === 'headline' ? 'headlineMedium' : 'bodyLarge'}</div>
                    <div>)</div>
                  </>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}