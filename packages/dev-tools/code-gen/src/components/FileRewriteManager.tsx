import React, { useState, useEffect } from 'react'
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
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Files, Wand, Download, Upload, RefreshCw, FileText, Code, CheckCircle, AlertTriangle } from '@phosphor-icons/react'
import { toast } from 'sonner'

export interface CodeFile {
  path: string
  content: string
  language: string
  lastModified: number
}

interface FileRewriteManagerProps {
  projectId: string
  files: CodeFile[]
  onFilesUpdated: (files: CodeFile[]) => void
}

interface RewriteRule {
  id: string
  name: string
  description: string
  pattern: string
  replacement: string
  fileTypes: string[]
  enabled: boolean
}

interface RewriteTask {
  id: string
  name: string
  description: string
  status: 'pending' | 'running' | 'completed' | 'failed'
  progress: number
  affectedFiles: string[]
  changes: {
    file: string
    before: string
    after: string
  }[]
}

const defaultRules: RewriteRule[] = [
  {
    id: 'remove-comments',
    name: 'Remove Comments',
    description: 'Remove single-line and multi-line comments',
    pattern: '(/\\*[\\s\\S]*?\\*/|//.*$)',
    replacement: '',
    fileTypes: ['js', 'ts', 'jsx', 'tsx'],
    enabled: false
  },
  {
    id: 'format-imports',
    name: 'Format Imports',
    description: 'Organize and format import statements',
    pattern: 'import\\s+.*from\\s+[\'"][^\'"]+[\'"]',
    replacement: 'organized imports',
    fileTypes: ['js', 'ts', 'jsx', 'tsx'],
    enabled: false
  },
  {
    id: 'add-typescript',
    name: 'Add TypeScript Types',
    description: 'Add TypeScript type annotations',
    pattern: 'function\\s+(\\w+)\\s*\\(',
    replacement: 'function $1(',
    fileTypes: ['js', 'jsx'],
    enabled: false
  }
]

export function FileRewriteManager({ projectId, files, onFilesUpdated }: FileRewriteManagerProps) {
  const [rules, setRules] = useKV<RewriteRule[]>(`rewrite-rules-${projectId}`, defaultRules)
  const [tasks, setTasks] = useKV<RewriteTask[]>(`rewrite-tasks-${projectId}`, [])
  const [selectedFiles, setSelectedFiles] = useState<string[]>([])
  const [customPrompt, setCustomPrompt] = useState('')
  const [newRule, setNewRule] = useState<Partial<RewriteRule>>({
    enabled: true,
    fileTypes: ['js', 'ts', 'jsx', 'tsx']
  })
  const [isRewriting, setIsRewriting] = useState(false)

  const executeGlobalRewrite = async (prompt: string, targetFiles?: string[]) => {
    const filesToProcess = targetFiles || selectedFiles
    
    if (filesToProcess.length === 0) {
      toast.error('Please select files to rewrite')
      return
    }

    const newTask: RewriteTask = {
      id: Date.now().toString(),
      name: 'AI Global Rewrite',
      description: prompt,
      status: 'running',
      progress: 0,
      affectedFiles: filesToProcess,
      changes: []
    }

    setTasks(prev => [...prev, newTask])
    setIsRewriting(true)

    try {
      const filesToRewrite = files.filter(f => filesToProcess.includes(f.path))
      const updatedFiles: CodeFile[] = []
      let totalFiles = filesToRewrite.length
      let processedFiles = 0

      for (const file of filesToRewrite) {
        try {
          const rewritePrompt = spark.llmPrompt`
            Rewrite this ${file.language} file based on the following instruction: ${prompt}
            
            File: ${file.path}
            Current content:
            ${file.content}
            
            Instructions:
            1. Perform a COMPLETE rewrite of the entire file
            2. DO NOT use patches or snippets
            3. Return the FULL, complete file content
            4. Maintain functionality while applying the requested changes
            5. Follow best practices for ${file.language}
            6. Ensure syntax is correct and complete
            
            Return ONLY the complete file content, no explanations or markdown formatting.
          `

          const rewrittenContent = await spark.llm(rewritePrompt, 'gpt-4o')

          const updatedFile: CodeFile = {
            ...file,
            content: rewrittenContent.trim(),
            lastModified: Date.now()
          }

          updatedFiles.push(updatedFile)
          
          // Track changes
          newTask.changes.push({
            file: file.path,
            before: file.content.substring(0, 100) + '...',
            after: rewrittenContent.substring(0, 100) + '...'
          })

          processedFiles++
          const progress = (processedFiles / totalFiles) * 100

          setTasks(prev => prev.map(task => 
            task.id === newTask.id 
              ? { ...task, progress, changes: [...newTask.changes] }
              : task
          ))

        } catch (error) {
          console.error(`Failed to rewrite ${file.path}:`, error)
          toast.error(`Failed to rewrite ${file.path}`)
        }
      }

      // Update all files with rewritten content
      const finalFiles = files.map(file => {
        const updated = updatedFiles.find(uf => uf.path === file.path)
        return updated || file
      })

      onFilesUpdated(finalFiles)

      setTasks(prev => prev.map(task => 
        task.id === newTask.id 
          ? { ...task, status: 'completed', progress: 100 }
          : task
      ))

      toast.success(`Successfully rewrote ${updatedFiles.length} files`)
      setCustomPrompt('')

    } catch (error) {
      console.error('Global rewrite failed:', error)
      setTasks(prev => prev.map(task => 
        task.id === newTask.id 
          ? { ...task, status: 'failed' }
          : task
      ))
      toast.error('Global rewrite failed')
    } finally {
      setIsRewriting(false)
    }
  }

  const applyRules = async () => {
    const enabledRules = rules.filter(rule => rule.enabled)
    
    if (enabledRules.length === 0) {
      toast.error('Please enable at least one rewrite rule')
      return
    }

    const newTask: RewriteTask = {
      id: Date.now().toString(),
      name: 'Apply Rewrite Rules',
      description: `Applying ${enabledRules.length} rules`,
      status: 'running',
      progress: 0,
      affectedFiles: files.map(f => f.path),
      changes: []
    }

    setTasks(prev => [...prev, newTask])

    try {
      const updatedFiles = files.map(file => {
        let content = file.content
        const fileExt = file.path.split('.').pop()?.toLowerCase()

        enabledRules.forEach(rule => {
          if (rule.fileTypes.includes(fileExt || '')) {
            try {
              const regex = new RegExp(rule.pattern, 'gm')
              const newContent = content.replace(regex, rule.replacement)
              
              if (newContent !== content) {
                newTask.changes.push({
                  file: file.path,
                  before: content.substring(0, 50) + '...',
                  after: newContent.substring(0, 50) + '...'
                })
                content = newContent
              }
            } catch (error) {
              console.error(`Rule ${rule.name} failed on ${file.path}:`, error)
            }
          }
        })

        return {
          ...file,
          content,
          lastModified: Date.now()
        }
      })

      onFilesUpdated(updatedFiles)

      setTasks(prev => prev.map(task => 
        task.id === newTask.id 
          ? { ...task, status: 'completed', progress: 100, changes: newTask.changes }
          : task
      ))

      toast.success(`Applied ${enabledRules.length} rewrite rules`)

    } catch (error) {
      console.error('Rule application failed:', error)
      setTasks(prev => prev.map(task => 
        task.id === newTask.id 
          ? { ...task, status: 'failed' }
          : task
      ))
      toast.error('Failed to apply rewrite rules')
    }
  }

  const addCustomRule = () => {
    if (!newRule.name || !newRule.pattern) {
      toast.error('Please provide rule name and pattern')
      return
    }

    const rule: RewriteRule = {
      id: Date.now().toString(),
      name: newRule.name!,
      description: newRule.description || '',
      pattern: newRule.pattern!,
      replacement: newRule.replacement || '',
      fileTypes: newRule.fileTypes || [],
      enabled: true
    }

    setRules(prev => [...prev, rule])
    setNewRule({ enabled: true, fileTypes: ['js', 'ts', 'jsx', 'tsx'] })
    toast.success('Custom rule added')
  }

  const toggleRule = (ruleId: string) => {
    setRules(prev => prev.map(rule => 
      rule.id === ruleId ? { ...rule, enabled: !rule.enabled } : rule
    ))
  }

  const deleteRule = (ruleId: string) => {
    setRules(prev => prev.filter(rule => rule.id !== ruleId))
    toast.success('Rule deleted')
  }

  const toggleFileSelection = (filePath: string) => {
    setSelectedFiles(prev => 
      prev.includes(filePath)
        ? prev.filter(f => f !== filePath)
        : [...prev, filePath]
    )
  }

  const selectAllFiles = () => {
    setSelectedFiles(files.map(f => f.path))
  }

  const clearSelection = () => {
    setSelectedFiles([])
  }

  const exportRules = () => {
    const rulesData = {
      rules,
      exportDate: new Date().toISOString()
    }
    
    const blob = new Blob([JSON.stringify(rulesData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `rewrite-rules-${projectId}.json`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Files className="w-5 h-5" />
            File Rewrite Manager
          </CardTitle>
          <CardDescription>
            Global file rewriting system for clean, complete code updates without patches or snippets
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Alert>
            <AlertTriangle className="w-4 h-4" />
            <AlertDescription>
              This system performs complete file rewrites. No patching or snippet injection - entire files are regenerated.
            </AlertDescription>
          </Alert>

          <div className="space-y-2">
            <Label>AI Rewrite Instruction</Label>
            <Textarea
              placeholder="Describe how you want to rewrite the selected files... (e.g., 'Convert to TypeScript with proper type annotations', 'Optimize for performance', 'Add comprehensive error handling')"
              value={customPrompt}
              onChange={(e) => setCustomPrompt(e.target.value)}
              className="min-h-20"
            />
          </div>

          <div className="flex items-center gap-2">
            <Button 
              onClick={() => executeGlobalRewrite(customPrompt)}
              disabled={isRewriting || !customPrompt.trim() || selectedFiles.length === 0}
              className="flex-1"
            >
              <Wand className="w-4 h-4 mr-2" />
              {isRewriting ? 'Rewriting Files...' : 'Execute Global Rewrite'}
            </Button>
            <Button 
              onClick={applyRules}
              variant="outline"
              disabled={isRewriting}
            >
              <RefreshCw className="w-4 h-4 mr-2" />
              Apply Rules
            </Button>
          </div>
        </CardContent>
      </Card>

      <Tabs defaultValue="files" className="space-y-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="files">File Selection</TabsTrigger>
          <TabsTrigger value="rules">Rewrite Rules</TabsTrigger>
          <TabsTrigger value="history">Task History</TabsTrigger>
        </TabsList>

        <TabsContent value="files" className="space-y-4">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg">File Selection</CardTitle>
                <div className="flex items-center gap-2">
                  <Badge variant="outline">
                    {selectedFiles.length} of {files.length} selected
                  </Badge>
                  <Button variant="outline" size="sm" onClick={selectAllFiles}>
                    Select All
                  </Button>
                  <Button variant="outline" size="sm" onClick={clearSelection}>
                    Clear
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-64">
                <div className="space-y-2">
                  {files.map(file => (
                    <div 
                      key={file.path}
                      className={`flex items-center justify-between p-3 rounded-md border cursor-pointer ${
                        selectedFiles.includes(file.path) ? 'bg-primary/10 border-primary' : 'hover:bg-muted'
                      }`}
                      onClick={() => toggleFileSelection(file.path)}
                    >
                      <div className="flex items-center gap-3">
                        <input
                          type="checkbox"
                          checked={selectedFiles.includes(file.path)}
                          onChange={() => {}}
                          className="w-4 h-4"
                        />
                        <FileText className="w-4 h-4" />
                        <div>
                          <span className="font-mono text-sm">{file.path}</span>
                          <div className="text-xs text-muted-foreground">
                            {file.language} • {file.content.length} chars
                          </div>
                        </div>
                      </div>
                      <Badge variant="secondary">{file.language}</Badge>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="rules" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">Rewrite Rules</CardTitle>
                  <Button variant="outline" size="sm" onClick={exportRules}>
                    <Download className="w-4 h-4" />
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-64">
                  <div className="space-y-3">
                    {rules.map(rule => (
                      <div key={rule.id} className="border rounded-lg p-3">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <input
                              type="checkbox"
                              checked={rule.enabled}
                              onChange={() => toggleRule(rule.id)}
                              className="w-4 h-4"
                            />
                            <h4 className="font-medium">{rule.name}</h4>
                          </div>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => deleteRule(rule.id)}
                          >
                            ×
                          </Button>
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">{rule.description}</p>
                        <div className="space-y-1">
                          <div className="text-xs">
                            <span className="font-medium">Pattern:</span> 
                            <code className="ml-1 bg-muted px-1 rounded">{rule.pattern}</code>
                          </div>
                          <div className="flex items-center gap-1">
                            {rule.fileTypes.map(type => (
                              <Badge key={type} variant="outline" className="text-xs">
                                {type}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Add Custom Rule</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Rule Name</Label>
                  <Input
                    placeholder="My Custom Rule"
                    value={newRule.name || ''}
                    onChange={(e) => setNewRule(prev => ({ ...prev, name: e.target.value }))}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Description</Label>
                  <Input
                    placeholder="What this rule does..."
                    value={newRule.description || ''}
                    onChange={(e) => setNewRule(prev => ({ ...prev, description: e.target.value }))}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Pattern (Regex)</Label>
                  <Input
                    placeholder="console\.log\(.*\)"
                    value={newRule.pattern || ''}
                    onChange={(e) => setNewRule(prev => ({ ...prev, pattern: e.target.value }))}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Replacement</Label>
                  <Input
                    placeholder=""
                    value={newRule.replacement || ''}
                    onChange={(e) => setNewRule(prev => ({ ...prev, replacement: e.target.value }))}
                  />
                </div>

                <Button onClick={addCustomRule} className="w-full">
                  Add Rule
                </Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="history" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Rewrite Task History</CardTitle>
            </CardHeader>
            <CardContent>
              {tasks.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  <Files className="w-8 h-8 mx-auto mb-2 opacity-50" />
                  <p>No rewrite tasks yet</p>
                </div>
              ) : (
                <ScrollArea className="h-64">
                  <div className="space-y-3">
                    {tasks.map(task => (
                      <div key={task.id} className="border rounded-lg p-3">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium">{task.name}</h4>
                          <div className="flex items-center gap-2">
                            {task.status === 'completed' && <CheckCircle className="w-4 h-4 text-green-500" />}
                            {task.status === 'failed' && <AlertTriangle className="w-4 h-4 text-red-500" />}
                            <Badge variant="outline">{task.status}</Badge>
                          </div>
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">{task.description}</p>
                        
                        {task.status === 'running' && (
                          <Progress value={task.progress} className="mb-2" />
                        )}
                        
                        <div className="text-xs text-muted-foreground">
                          {task.affectedFiles.length} files • {task.changes.length} changes
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}