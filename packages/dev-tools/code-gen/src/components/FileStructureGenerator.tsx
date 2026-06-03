import React, { useState } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { FolderOpen, File, Plus, Wand, Download, Upload, Trash2 } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface FileStructureGeneratorProps {
  project: any
  onUpdateProject: (id: string, updates: any) => void
}

interface FileNode {
  id: string
  name: string
  type: 'file' | 'folder'
  content?: string
  children?: FileNode[]
  path: string
}

export function FileStructureGenerator({ project, onUpdateProject }: FileStructureGeneratorProps) {
  const [aiDescription, setAiDescription] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)
  const [generationProgress, setGenerationProgress] = useState(0)
  const [fileStructure, setFileStructure] = useState<FileNode[]>([])
  const [selectedFile, setSelectedFile] = useState<FileNode | null>(null)
  const [newFileName, setNewFileName] = useState('')
  const [newFolderName, setNewFolderName] = useState('')

  // Initialize file structure from project
  React.useEffect(() => {
    if (project?.codebase?.files) {
      const structure = convertFilesToStructure(project.codebase.files)
      setFileStructure(structure)
    }
  }, [project])

  const convertFilesToStructure = (files: Record<string, string>): FileNode[] => {
    const structure: FileNode[] = []
    const pathMap = new Map<string, FileNode>()

    Object.entries(files).forEach(([path, content]) => {
      const parts = path.split('/')
      let currentPath = ''

      parts.forEach((part, index) => {
        const previousPath = currentPath
        currentPath = currentPath ? `${currentPath}/${part}` : part
        
        if (!pathMap.has(currentPath)) {
          const node: FileNode = {
            id: currentPath,
            name: part,
            type: index === parts.length - 1 ? 'file' : 'folder',
            path: currentPath,
            content: index === parts.length - 1 ? content : undefined,
            children: index === parts.length - 1 ? undefined : []
          }

          pathMap.set(currentPath, node)

          if (previousPath) {
            const parent = pathMap.get(previousPath)
            if (parent && parent.children) {
              parent.children.push(node)
            }
          } else {
            structure.push(node)
          }
        }
      })
    })

    return structure
  }

  const generateFromAI = async () => {
    if (!aiDescription.trim()) {
      toast.error('Please describe the file structure you want to generate')
      return
    }

    setIsGenerating(true)
    setGenerationProgress(0)

    try {
      // Simulate progress
      const progressInterval = setInterval(() => {
        setGenerationProgress(prev => Math.min(prev + 10, 90))
      }, 200)

      const prompt = spark.llmPrompt`
        Generate a complete file structure for a ${project.type} project based on this description: ${aiDescription}
        
        Project type: ${project.type}
        Project name: ${project.name}
        
        Return a JSON object with this structure:
        {
          "files": {
            "path/to/file.ext": "file content here",
            ...
          },
          "folders": ["path/to/folder", ...],
          "dependencies": ["package1", "package2", ...]
        }
        
        Include realistic file contents for the project type. For React projects, include components, styles, and configuration. For Node.js, include routes, middleware, and package.json. For Android, include Kotlin files, manifests, and resource files.
      `

      const response = await spark.llm(prompt, 'gpt-4o', true)
      const generatedStructure = JSON.parse(response)

      clearInterval(progressInterval)
      setGenerationProgress(100)

      // Convert generated structure to file nodes
      const newStructure = convertFilesToStructure(generatedStructure.files)
      setFileStructure(newStructure)

      // Update project with generated files
      onUpdateProject(project.id, {
        codebase: {
          files: generatedStructure.files,
          dependencies: [...(project.codebase?.dependencies || []), ...(generatedStructure.dependencies || [])]
        }
      })

      toast.success('File structure generated successfully!')
      setAiDescription('')
    } catch (error) {
      console.error('Generation failed:', error)
      toast.error('Failed to generate file structure')
    } finally {
      setIsGenerating(false)
      setTimeout(() => setGenerationProgress(0), 1000)
    }
  }

  const addFile = (parentPath?: string) => {
    if (!newFileName.trim()) {
      toast.error('Please enter a file name')
      return
    }

    const fullPath = parentPath ? `${parentPath}/${newFileName}` : newFileName
    const newFile: FileNode = {
      id: fullPath,
      name: newFileName,
      type: 'file',
      path: fullPath,
      content: ''
    }

    // Add to project files
    const updatedFiles = {
      ...project.codebase.files,
      [fullPath]: ''
    }

    onUpdateProject(project.id, {
      codebase: {
        ...project.codebase,
        files: updatedFiles
      }
    })

    setNewFileName('')
    toast.success(`File ${newFileName} created`)
  }

  const addFolder = (parentPath?: string) => {
    if (!newFolderName.trim()) {
      toast.error('Please enter a folder name')
      return
    }

    const fullPath = parentPath ? `${parentPath}/${newFolderName}` : newFolderName
    
    // Create a placeholder file to represent the folder
    const placeholderFile = `${fullPath}/.gitkeep`
    const updatedFiles = {
      ...project.codebase.files,
      [placeholderFile]: ''
    }

    onUpdateProject(project.id, {
      codebase: {
        ...project.codebase,
        files: updatedFiles
      }
    })

    setNewFolderName('')
    toast.success(`Folder ${newFolderName} created`)
  }

  const deleteNode = (nodePath: string) => {
    const updatedFiles = { ...project.codebase.files }
    
    // Delete all files that start with this path
    Object.keys(updatedFiles).forEach(filePath => {
      if (filePath.startsWith(nodePath)) {
        delete updatedFiles[filePath]
      }
    })

    onUpdateProject(project.id, {
      codebase: {
        ...project.codebase,
        files: updatedFiles
      }
    })

    toast.success('Item deleted successfully')
  }

  const updateFileContent = (filePath: string, content: string) => {
    const updatedFiles = {
      ...project.codebase.files,
      [filePath]: content
    }

    onUpdateProject(project.id, {
      codebase: {
        ...project.codebase,
        files: updatedFiles
      }
    })
  }

  const renderFileTree = (nodes: FileNode[], level = 0) => {
    return nodes.map(node => (
      <div key={node.id} className="space-y-1">
        <div
          className={`flex items-center gap-2 p-2 rounded-md cursor-pointer hover:bg-muted ${
            selectedFile?.id === node.id ? 'bg-muted' : ''
          }`}
          style={{ paddingLeft: `${level * 20 + 8}px` }}
          onClick={() => node.type === 'file' ? setSelectedFile(node) : null}
        >
          {node.type === 'folder' ? (
            <FolderOpen className="w-4 h-4 text-blue-500" />
          ) : (
            <File className="w-4 h-4 text-gray-500" />
          )}
          <span className="text-sm font-mono">{node.name}</span>
          <Button
            variant="ghost"
            size="sm"
            className="ml-auto opacity-0 group-hover:opacity-100"
            onClick={(e) => {
              e.stopPropagation()
              deleteNode(node.path)
            }}
          >
            <Trash2 className="w-3 h-3" />
          </Button>
        </div>
        {node.children && renderFileTree(node.children, level + 1)}
      </div>
    ))
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FolderOpen className="w-5 h-5" />
            File Structure Generator
          </CardTitle>
          <CardDescription>
            Generate complete file structures from natural language descriptions
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="ai-description">Describe the structure you want to create</Label>
            <Textarea
              id="ai-description"
              placeholder="Create a React dashboard with components for charts, tables, and user management. Include routing, state management, and API integration..."
              value={aiDescription}
              onChange={(e) => setAiDescription(e.target.value)}
              className="min-h-24"
            />
          </div>
          
          {isGenerating && (
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span>Generating file structure...</span>
                <span>{generationProgress}%</span>
              </div>
              <Progress value={generationProgress} />
            </div>
          )}

          <Button 
            onClick={generateFromAI} 
            disabled={isGenerating || !aiDescription.trim()}
            className="w-full"
          >
            <Wand className="w-4 h-4 mr-2" />
            {isGenerating ? 'Generating...' : 'Generate with AI'}
          </Button>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg">File Tree</CardTitle>
              <div className="flex items-center gap-2">
                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm">
                      <Plus className="w-4 h-4" />
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Add File or Folder</DialogTitle>
                    </DialogHeader>
                    <Tabs defaultValue="file">
                      <TabsList className="grid w-full grid-cols-2">
                        <TabsTrigger value="file">File</TabsTrigger>
                        <TabsTrigger value="folder">Folder</TabsTrigger>
                      </TabsList>
                      <TabsContent value="file" className="space-y-4">
                        <div className="space-y-2">
                          <Label htmlFor="filename">File Name</Label>
                          <Input
                            id="filename"
                            placeholder="component.tsx"
                            value={newFileName}
                            onChange={(e) => setNewFileName(e.target.value)}
                          />
                        </div>
                        <Button onClick={() => addFile()}>Create File</Button>
                      </TabsContent>
                      <TabsContent value="folder" className="space-y-4">
                        <div className="space-y-2">
                          <Label htmlFor="foldername">Folder Name</Label>
                          <Input
                            id="foldername"
                            placeholder="components"
                            value={newFolderName}
                            onChange={(e) => setNewFolderName(e.target.value)}
                          />
                        </div>
                        <Button onClick={() => addFolder()}>Create Folder</Button>
                      </TabsContent>
                    </Tabs>
                  </DialogContent>
                </Dialog>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-1 max-h-96 overflow-y-auto">
              {fileStructure.length > 0 ? (
                renderFileTree(fileStructure)
              ) : (
                <div className="text-center py-8 text-muted-foreground">
                  <FolderOpen className="w-8 h-8 mx-auto mb-2 opacity-50" />
                  <p>No files yet. Generate or create some files to get started.</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">
              {selectedFile ? `Editing: ${selectedFile.name}` : 'File Editor'}
            </CardTitle>
          </CardHeader>
          <CardContent>
            {selectedFile ? (
              <div className="space-y-4">
                <div className="flex items-center gap-2">
                  <Badge variant="outline">{selectedFile.path}</Badge>
                </div>
                <Textarea
                  value={selectedFile.content || ''}
                  onChange={(e) => {
                    setSelectedFile(prev => prev ? { ...prev, content: e.target.value } : null)
                    updateFileContent(selectedFile.path, e.target.value)
                  }}
                  className="min-h-96 font-mono text-sm"
                  placeholder="File content..."
                />
              </div>
            ) : (
              <div className="text-center py-12 text-muted-foreground">
                <File className="w-8 h-8 mx-auto mb-2 opacity-50" />
                <p>Select a file from the tree to edit its contents</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}