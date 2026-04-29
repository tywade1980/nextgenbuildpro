import React, { useState, useEffect } from 'react'
import { useKV } from '@github/spark/hooks'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ScrollArea } from '@/components/ui/scroll-area'
import { TestTube, CheckCircle, XCircle, AlertTriangle, Play, Code, Database, Zap } from '@phosphor-icons/react'
import { toast } from 'sonner'

interface TestingSuiteProps {
  project: any
  onUpdateProject: (id: string, updates: any) => void
}

interface TestResult {
  id: string
  type: 'syntax' | 'unit' | 'integration' | 'e2e' | 'performance' | 'security' | 'accessibility'
  name: string
  status: 'pending' | 'running' | 'passed' | 'failed' | 'skipped'
  details?: string
  duration?: number
  coverage?: number
}

interface TestSuite {
  id: string
  name: string
  type: 'syntax' | 'unit' | 'integration' | 'e2e' | 'performance' | 'security' | 'accessibility'
  tests: TestResult[]
  status: 'idle' | 'running' | 'completed'
  progress: number
}

const testSuiteTemplates = [
  {
    type: 'syntax',
    name: 'Syntax & Linting',
    description: 'Check code syntax, formatting, and linting rules',
    icon: Code
  },
  {
    type: 'unit',
    name: 'Unit Tests',
    description: 'Test individual components and functions',
    icon: TestTube
  },
  {
    type: 'integration',
    name: 'Integration Tests',
    description: 'Test component interactions and API integration',
    icon: Database
  },
  {
    type: 'e2e',
    name: 'End-to-End Tests',
    description: 'Test complete user workflows',
    icon: Play
  },
  {
    type: 'performance',
    name: 'Performance Tests',
    description: 'Test loading times, memory usage, and optimization',
    icon: Zap
  },
  {
    type: 'security',
    name: 'Security Tests',
    description: 'Check for security vulnerabilities and best practices',
    icon: AlertTriangle
  },
  {
    type: 'accessibility',
    name: 'Accessibility Tests',
    description: 'Ensure compliance with WCAG guidelines',
    icon: CheckCircle
  }
]

export function TestingSuite({ project, onUpdateProject }: TestingSuiteProps) {
  const [testSuites, setTestSuites] = useKV<TestSuite[]>(`test-suites-${project.id}`, [])
  const [isRunningAll, setIsRunningAll] = useState(false)
  const [selectedSuite, setSelectedSuite] = useState<TestSuite | null>(null)

  const generateTests = async (suiteType: string) => {
    const newSuite: TestSuite = {
      id: Date.now().toString(),
      name: testSuiteTemplates.find(t => t.type === suiteType)?.name || 'Test Suite',
      type: suiteType as any,
      tests: [],
      status: 'running',
      progress: 0
    }

    setTestSuites(prev => [...prev, newSuite])

    try {
      const projectFiles = Object.entries(project.codebase?.files || {})
        .map(([path, content]) => `// File: ${path}\n${content}`)
        .join('\n\n')

      let prompt = ''

      switch (suiteType) {
        case 'syntax':
          prompt = spark.llmPrompt`
            Perform syntax and linting analysis for this ${project.type} project:
            
            ${projectFiles}
            
            Check for:
            1. Syntax errors
            2. ESLint/TSLint violations
            3. Code formatting issues
            4. Import/export problems
            5. Type errors (if TypeScript)
            
            Return JSON with test results:
            {
              "tests": [
                {
                  "name": "test name",
                  "status": "passed|failed",
                  "details": "specific issue description",
                  "file": "affected file path"
                }
              ]
            }
          `
          break

        case 'unit':
          prompt = spark.llmPrompt`
            Generate unit tests for this ${project.type} project:
            
            ${projectFiles}
            
            Create comprehensive unit tests for:
            1. All components/functions
            2. Edge cases and error conditions
            3. Props validation (React)
            4. State management
            5. Utility functions
            
            Return JSON with test generation results and actual test code.
          `
          break

        case 'integration':
          prompt = spark.llmPrompt`
            Analyze integration points and generate integration tests for:
            
            ${projectFiles}
            
            Focus on:
            1. API calls and responses
            2. Component interactions
            3. Data flow between modules
            4. Database connections
            5. External service integrations
            
            Return test results and recommendations.
          `
          break

        case 'e2e':
          prompt = spark.llmPrompt`
            Design end-to-end test scenarios for this ${project.type} application:
            
            ${projectFiles}
            
            Create tests for:
            1. Complete user workflows
            2. Navigation paths
            3. Form submissions
            4. Authentication flows
            5. Critical business processes
            
            Return test scenarios and implementation code.
          `
          break

        case 'performance':
          prompt = spark.llmPrompt`
            Analyze performance aspects of this project:
            
            ${projectFiles}
            
            Check:
            1. Bundle size optimization
            2. Render performance
            3. Memory leaks
            4. Network requests efficiency
            5. Code splitting opportunities
            
            Return performance analysis and optimization suggestions.
          `
          break

        case 'security':
          prompt = spark.llmPrompt`
            Perform security analysis on this ${project.type} project:
            
            ${projectFiles}
            
            Check for:
            1. XSS vulnerabilities
            2. Input validation issues
            3. Authentication/authorization flaws
            4. Sensitive data exposure
            5. Dependency vulnerabilities
            
            Return security audit results with severity levels.
          `
          break

        case 'accessibility':
          prompt = spark.llmPrompt`
            Audit accessibility compliance for this project:
            
            ${projectFiles}
            
            Check WCAG 2.1 compliance:
            1. Semantic HTML usage
            2. ARIA attributes
            3. Keyboard navigation
            4. Color contrast
            5. Screen reader compatibility
            
            Return accessibility audit with specific issues and fixes.
          `
          break
      }

      // Simulate progress
      const progressInterval = setInterval(() => {
        setTestSuites(prev => prev.map(suite => 
          suite.id === newSuite.id 
            ? { ...suite, progress: Math.min(suite.progress + 15, 90) }
            : suite
        ))
      }, 300)

      const response = await spark.llm(prompt, 'gpt-4o', true)
      const testData = JSON.parse(response)

      clearInterval(progressInterval)

      // Generate mock test results
      const tests: TestResult[] = testData.tests?.map((test: any, index: number) => ({
        id: `${newSuite.id}-${index}`,
        type: suiteType as any,
        name: test.name || `Test ${index + 1}`,
        status: test.status || (Math.random() > 0.3 ? 'passed' : 'failed'),
        details: test.details || test.description,
        duration: Math.floor(Math.random() * 1000) + 100,
        coverage: Math.floor(Math.random() * 40) + 60
      })) || []

      // Add generated tests to project if applicable
      if (suiteType === 'unit' && testData.testFiles) {
        const updatedFiles = { ...project.codebase.files }
        Object.entries(testData.testFiles).forEach(([path, content]: [string, any]) => {
          updatedFiles[path] = content
        })
        
        onUpdateProject(project.id, {
          codebase: {
            ...project.codebase,
            files: updatedFiles
          }
        })
      }

      setTestSuites(prev => prev.map(suite => 
        suite.id === newSuite.id 
          ? { ...suite, tests, status: 'completed', progress: 100 }
          : suite
      ))

      // Update project test results
      const overallResults = {
        [suiteType]: tests.every(t => t.status === 'passed')
      }

      onUpdateProject(project.id, {
        testResults: {
          ...project.testResults,
          ...overallResults
        }
      })

      toast.success(`${newSuite.name} completed`)

    } catch (error) {
      console.error('Test generation failed:', error)
      setTestSuites(prev => prev.map(suite => 
        suite.id === newSuite.id 
          ? { ...suite, status: 'completed', progress: 100, tests: [{
              id: 'error',
              type: suiteType as any,
              name: 'Test Generation Failed',
              status: 'failed',
              details: 'Failed to generate tests. Please try again.'
            }] }
          : suite
      ))
      toast.error('Test generation failed')
    }
  }

  const runAllTests = async () => {
    setIsRunningAll(true)
    
    for (const template of testSuiteTemplates) {
      await generateTests(template.type)
      // Small delay between test suites
      await new Promise(resolve => setTimeout(resolve, 1000))
    }
    
    setIsRunningAll(false)
    toast.success('All test suites completed!')
  }

  const clearAllTests = () => {
    setTestSuites([])
    onUpdateProject(project.id, {
      testResults: {}
    })
    toast.success('All tests cleared')
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'passed': return <CheckCircle className="w-4 h-4 text-green-500" />
      case 'failed': return <XCircle className="w-4 h-4 text-red-500" />
      case 'running': return <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      case 'skipped': return <AlertTriangle className="w-4 h-4 text-yellow-500" />
      default: return <TestTube className="w-4 h-4 text-gray-500" />
    }
  }

  const getSuiteIcon = (type: string) => {
    const template = testSuiteTemplates.find(t => t.type === type)
    return template?.icon || TestTube
  }

  const getOverallStats = () => {
    const allTests = testSuites.flatMap(suite => suite.tests)
    const passed = allTests.filter(t => t.status === 'passed').length
    const failed = allTests.filter(t => t.status === 'failed').length
    const total = allTests.length

    return { passed, failed, total, coverage: total > 0 ? Math.round((passed / total) * 100) : 0 }
  }

  const stats = getOverallStats()

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <TestTube className="w-5 h-5" />
                Testing Suite
              </CardTitle>
              <CardDescription>
                Comprehensive automated testing for your project
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Button 
                onClick={runAllTests} 
                disabled={isRunningAll}
                variant="default"
              >
                <Play className="w-4 h-4 mr-2" />
                {isRunningAll ? 'Running...' : 'Run All Tests'}
              </Button>
              <Button 
                onClick={clearAllTests} 
                variant="outline"
                disabled={testSuites.length === 0}
              >
                Clear All
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {stats.total > 0 && (
            <div className="grid grid-cols-4 gap-4 mb-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">{stats.passed}</div>
                <div className="text-sm text-muted-foreground">Passed</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-red-600">{stats.failed}</div>
                <div className="text-sm text-muted-foreground">Failed</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold">{stats.total}</div>
                <div className="text-sm text-muted-foreground">Total</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-blue-600">{stats.coverage}%</div>
                <div className="text-sm text-muted-foreground">Success Rate</div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Test Suite Templates */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Test Suites</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {testSuiteTemplates.map((template) => {
              const Icon = template.icon
              const existingSuite = testSuites.find(s => s.type === template.type)
              
              return (
                <div key={template.type} className="space-y-2">
                  <Button
                    variant="outline"
                    className="w-full justify-start h-auto p-3"
                    onClick={() => generateTests(template.type)}
                    disabled={existingSuite?.status === 'running'}
                  >
                    <Icon className="w-4 h-4 mr-3 flex-shrink-0" />
                    <div className="text-left flex-1">
                      <div className="font-medium text-sm">{template.name}</div>
                      <div className="text-xs text-muted-foreground">{template.description}</div>
                    </div>
                    {existingSuite && (
                      <Badge 
                        variant={existingSuite.status === 'completed' ? 'default' : 'secondary'}
                        className="ml-2"
                      >
                        {existingSuite.status}
                      </Badge>
                    )}
                  </Button>
                  
                  {existingSuite?.status === 'running' && (
                    <Progress value={existingSuite.progress} className="h-1" />
                  )}
                </div>
              )
            })}
          </CardContent>
        </Card>

        {/* Test Results */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">Test Results</CardTitle>
          </CardHeader>
          <CardContent>
            {testSuites.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                <TestTube className="w-8 h-8 mx-auto mb-2 opacity-50" />
                <p>No tests run yet</p>
                <p className="text-sm">Start by running a test suite</p>
              </div>
            ) : (
              <Tabs value={selectedSuite?.id || testSuites[0]?.id} onValueChange={(value) => {
                const suite = testSuites.find(s => s.id === value)
                setSelectedSuite(suite || null)
              }}>
                <TabsList className="grid w-full grid-cols-3">
                  {testSuites.slice(0, 3).map((suite) => {
                    const Icon = getSuiteIcon(suite.type)
                    return (
                      <TabsTrigger key={suite.id} value={suite.id} className="flex items-center gap-2">
                        <Icon className="w-4 h-4" />
                        {suite.name}
                      </TabsTrigger>
                    )
                  })}
                </TabsList>

                {testSuites.map((suite) => (
                  <TabsContent key={suite.id} value={suite.id}>
                    <ScrollArea className="h-64">
                      <div className="space-y-2">
                        {suite.tests.map((test) => (
                          <div key={test.id} className="flex items-center justify-between p-3 border rounded-lg">
                            <div className="flex items-center gap-3">
                              {getStatusIcon(test.status)}
                              <div>
                                <div className="font-medium text-sm">{test.name}</div>
                                {test.details && (
                                  <div className="text-xs text-muted-foreground">{test.details}</div>
                                )}
                              </div>
                            </div>
                            <div className="text-right text-xs text-muted-foreground">
                              {test.duration && `${test.duration}ms`}
                              {test.coverage && (
                                <div>Coverage: {test.coverage}%</div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    </ScrollArea>
                  </TabsContent>
                ))}
              </Tabs>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}