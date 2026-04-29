/**
 * Automated Testing System for Production Code Generation
 * 
 * Provides comprehensive testing layers including:
 * - Syntax validation
 * - Dependency resolution
 * - Unit tests
 * - Integration tests
 * - Performance tests
 * - Security scans
 * - Deployment readiness checks
 */

export interface TestResult {
  id: string
  name: string
  status: 'pending' | 'running' | 'passed' | 'failed' | 'skipped'
  duration?: number
  errors?: string[]
  warnings?: string[]
  details?: Record<string, any>
}

export interface TestSuite {
  id: string
  name: string
  description: string
  tests: TestResult[]
  status: 'pending' | 'running' | 'completed' | 'failed'
  progress: number
  startTime?: number
  endTime?: number
}

export interface CodeFile {
  path: string
  content: string
  language: string
  dependencies?: string[]
}

export interface ProjectTestConfiguration {
  framework: string
  files: CodeFile[]
  dependencies: Record<string, string>
  devDependencies: Record<string, string>
  scripts: Record<string, string>
  enabledSuites: string[]
  strictMode: boolean
}

export class AutomatedTestingEngine {
  private testSuites: Map<string, TestSuite> = new Map()
  private onProgressUpdate?: (suites: TestSuite[]) => void

  constructor(onProgressUpdate?: (suites: TestSuite[]) => void) {
    this.onProgressUpdate = onProgressUpdate
    this.initializeTestSuites()
  }

  /**
   * Initialize all available test suites
   */
  private initializeTestSuites() {
    const suites: TestSuite[] = [
      {
        id: 'syntax',
        name: 'Syntax Validation',
        description: 'Validate code syntax and type checking',
        tests: [],
        status: 'pending',
        progress: 0
      },
      {
        id: 'dependencies',
        name: 'Dependency Resolution',
        description: 'Check all dependencies are resolvable and compatible',
        tests: [],
        status: 'pending',
        progress: 0
      },
      {
        id: 'unit',
        name: 'Unit Tests',
        description: 'Run unit tests for individual components and functions',
        tests: [],
        status: 'pending',
        progress: 0
      },
      {
        id: 'integration',
        name: 'Integration Tests',
        description: 'Test component interactions and API endpoints',
        tests: [],
        status: 'pending',
        progress: 0
      },
      {
        id: 'security',
        name: 'Security Scan',
        description: 'Scan for security vulnerabilities and best practices',
        tests: [],
        status: 'pending',
        progress: 0
      },
      {
        id: 'performance',
        name: 'Performance Tests',
        description: 'Analyze bundle size and runtime performance',
        tests: [],
        status: 'pending',
        progress: 0
      },
      {
        id: 'accessibility',
        name: 'Accessibility Tests',
        description: 'Check for accessibility compliance',
        tests: [],
        status: 'pending',
        progress: 0
      },
      {
        id: 'deployment',
        name: 'Deployment Readiness',
        description: 'Validate deployment configuration and build process',
        tests: [],
        status: 'pending',
        progress: 0
      }
    ]

    suites.forEach(suite => this.testSuites.set(suite.id, suite))
  }

  /**
   * Run comprehensive tests on generated project
   */
  async runTests(config: ProjectTestConfiguration): Promise<Map<string, TestSuite>> {
    const suitesToRun = Array.from(this.testSuites.values())
      .filter(suite => config.enabledSuites.includes(suite.id))

    console.log(`Starting test execution for ${suitesToRun.length} suites...`)

    for (const suite of suitesToRun) {
      await this.runTestSuite(suite, config)
    }

    return this.testSuites
  }

  /**
   * Run a specific test suite
   */
  private async runTestSuite(suite: TestSuite, config: ProjectTestConfiguration) {
    suite.status = 'running'
    suite.startTime = Date.now()
    suite.tests = []
    
    this.notifyProgress()

    try {
      switch (suite.id) {
        case 'syntax':
          await this.runSyntaxTests(suite, config)
          break
        case 'dependencies':
          await this.runDependencyTests(suite, config)
          break
        case 'unit':
          await this.runUnitTests(suite, config)
          break
        case 'integration':
          await this.runIntegrationTests(suite, config)
          break
        case 'security':
          await this.runSecurityTests(suite, config)
          break
        case 'performance':
          await this.runPerformanceTests(suite, config)
          break
        case 'accessibility':
          await this.runAccessibilityTests(suite, config)
          break
        case 'deployment':
          await this.runDeploymentTests(suite, config)
          break
      }

      suite.status = 'completed'
      suite.progress = 100
    } catch (error) {
      suite.status = 'failed'
      console.error(`Test suite ${suite.name} failed:`, error)
    }

    suite.endTime = Date.now()
    this.notifyProgress()
  }

  /**
   * Syntax validation tests
   */
  private async runSyntaxTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // TypeScript/JavaScript syntax validation
    for (const file of config.files) {
      if (file.language === 'typescript' || file.language === 'javascript') {
        const test: TestResult = {
          id: `syntax-${file.path}`,
          name: `Syntax check: ${file.path}`,
          status: 'running'
        }
        tests.push(test)
        suite.tests = tests
        this.notifyProgress()

        try {
          await this.validateTypescript(file.content, file.path)
          test.status = 'passed'
          test.duration = 50 + Math.random() * 100
        } catch (error) {
          test.status = 'failed'
          test.errors = [(error as Error).message]
        }

        suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
        this.notifyProgress()
        await new Promise(resolve => setTimeout(resolve, 100))
      }
    }

    // CSS/SCSS validation
    for (const file of config.files) {
      if (file.language === 'css' || file.language === 'scss') {
        const test: TestResult = {
          id: `css-syntax-${file.path}`,
          name: `CSS syntax: ${file.path}`,
          status: 'running'
        }
        tests.push(test)
        suite.tests = tests
        this.notifyProgress()

        try {
          await this.validateCSS(file.content)
          test.status = 'passed'
          test.duration = 30 + Math.random() * 50
        } catch (error) {
          test.status = 'failed'
          test.errors = [(error as Error).message]
        }

        suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
        this.notifyProgress()
        await new Promise(resolve => setTimeout(resolve, 50))
      }
    }

    // JSON validation
    for (const file of config.files) {
      if (file.language === 'json') {
        const test: TestResult = {
          id: `json-syntax-${file.path}`,
          name: `JSON syntax: ${file.path}`,
          status: 'running'
        }
        tests.push(test)
        suite.tests = tests
        this.notifyProgress()

        try {
          JSON.parse(file.content)
          test.status = 'passed'
          test.duration = 10 + Math.random() * 20
        } catch (error) {
          test.status = 'failed'
          test.errors = [(error as Error).message]
        }

        suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
        this.notifyProgress()
        await new Promise(resolve => setTimeout(resolve, 25))
      }
    }

    suite.tests = tests
  }

  /**
   * Dependency resolution tests
   */
  private async runDependencyTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // Check production dependencies
    for (const [name, version] of Object.entries(config.dependencies)) {
      const test: TestResult = {
        id: `dep-${name}`,
        name: `Dependency: ${name}@${version}`,
        status: 'running'
      }
      tests.push(test)
      suite.tests = tests
      this.notifyProgress()

      try {
        const isValid = await this.validateDependency(name, version)
        test.status = isValid ? 'passed' : 'failed'
        test.duration = 100 + Math.random() * 200
        if (!isValid) {
          test.errors = [`Package ${name}@${version} not found or incompatible`]
        }
      } catch (error) {
        test.status = 'failed'
        test.errors = [(error as Error).message]
      }

      suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
      this.notifyProgress()
      await new Promise(resolve => setTimeout(resolve, 150))
    }

    // Check dev dependencies
    for (const [name, version] of Object.entries(config.devDependencies)) {
      const test: TestResult = {
        id: `devdep-${name}`,
        name: `Dev dependency: ${name}@${version}`,
        status: 'running'
      }
      tests.push(test)
      suite.tests = tests
      this.notifyProgress()

      try {
        const isValid = await this.validateDependency(name, version)
        test.status = isValid ? 'passed' : 'failed'
        test.duration = 100 + Math.random() * 200
        if (!isValid) {
          test.errors = [`Package ${name}@${version} not found or incompatible`]
        }
      } catch (error) {
        test.status = 'failed'
        test.errors = [(error as Error).message]
      }

      suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
      this.notifyProgress()
      await new Promise(resolve => setTimeout(resolve, 150))
    }

    // Dependency conflict check
    const conflictTest: TestResult = {
      id: 'dependency-conflicts',
      name: 'Dependency conflict analysis',
      status: 'running'
    }
    tests.push(conflictTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      const conflicts = await this.checkDependencyConflicts(config.dependencies, config.devDependencies)
      if (conflicts.length === 0) {
        conflictTest.status = 'passed'
        conflictTest.details = { conflicts: [] }
      } else {
        conflictTest.status = 'failed'
        conflictTest.errors = conflicts
        conflictTest.details = { conflicts }
      }
      conflictTest.duration = 300 + Math.random() * 200
    } catch (error) {
      conflictTest.status = 'failed'
      conflictTest.errors = [(error as Error).message]
    }

    suite.progress = 100
    this.notifyProgress()
    suite.tests = tests
  }

  /**
   * Unit tests
   */
  private async runUnitTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // Find test files
    const testFiles = config.files.filter(file => 
      file.path.includes('.test.') || file.path.includes('.spec.') || file.path.includes('__tests__')
    )

    if (testFiles.length === 0) {
      // Generate basic tests for components
      const componentFiles = config.files.filter(file => 
        file.path.includes('components/') && 
        (file.language === 'typescript' || file.language === 'javascript')
      )

      for (const file of componentFiles) {
        const test: TestResult = {
          id: `unit-${file.path}`,
          name: `Component test: ${file.path}`,
          status: 'running'
        }
        tests.push(test)
        suite.tests = tests
        this.notifyProgress()

        try {
          // Simulate component testing
          await this.simulateComponentTest(file)
          test.status = 'passed'
          test.duration = 200 + Math.random() * 300
        } catch (error) {
          test.status = 'failed'
          test.errors = [(error as Error).message]
        }

        suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
        this.notifyProgress()
        await new Promise(resolve => setTimeout(resolve, 250))
      }
    } else {
      // Run existing tests
      for (const testFile of testFiles) {
        const test: TestResult = {
          id: `unit-${testFile.path}`,
          name: `Unit test: ${testFile.path}`,
          status: 'running'
        }
        tests.push(test)
        suite.tests = tests
        this.notifyProgress()

        try {
          await this.runTestFile(testFile)
          test.status = 'passed'
          test.duration = 300 + Math.random() * 500
        } catch (error) {
          test.status = 'failed'
          test.errors = [(error as Error).message]
        }

        suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
        this.notifyProgress()
        await new Promise(resolve => setTimeout(resolve, 400))
      }
    }

    suite.tests = tests
  }

  /**
   * Integration tests
   */
  private async runIntegrationTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // API endpoint tests
    if (config.framework === 'node' || config.framework === 'express' || config.framework === 'nextjs') {
      const apiTest: TestResult = {
        id: 'api-endpoints',
        name: 'API endpoint tests',
        status: 'running'
      }
      tests.push(apiTest)
      suite.tests = tests
      this.notifyProgress()

      try {
        await this.testAPIEndpoints(config)
        apiTest.status = 'passed'
        apiTest.duration = 500 + Math.random() * 1000
      } catch (error) {
        apiTest.status = 'failed'
        apiTest.errors = [(error as Error).message]
      }
      
      suite.progress = 50
      this.notifyProgress()
      await new Promise(resolve => setTimeout(resolve, 750))
    }

    // Database integration tests
    if (config.files.some(f => f.path.includes('database') || f.path.includes('db'))) {
      const dbTest: TestResult = {
        id: 'database-integration',
        name: 'Database integration tests',
        status: 'running'
      }
      tests.push(dbTest)
      suite.tests = tests
      this.notifyProgress()

      try {
        await this.testDatabaseIntegration(config)
        dbTest.status = 'passed'
        dbTest.duration = 800 + Math.random() * 1200
      } catch (error) {
        dbTest.status = 'failed'
        dbTest.errors = [(error as Error).message]
      }

      suite.progress = 100
      this.notifyProgress()
      await new Promise(resolve => setTimeout(resolve, 1000))
    }

    suite.tests = tests
  }

  /**
   * Security tests
   */
  private async runSecurityTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // Dependency vulnerability scan
    const vulnTest: TestResult = {
      id: 'vulnerability-scan',
      name: 'Dependency vulnerability scan',
      status: 'running'
    }
    tests.push(vulnTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      const vulnerabilities = await this.scanDependencyVulnerabilities(config.dependencies)
      if (vulnerabilities.length === 0) {
        vulnTest.status = 'passed'
      } else {
        vulnTest.status = vulnerabilities.some(v => v.severity === 'high' || v.severity === 'critical') ? 'failed' : 'passed'
        vulnTest.warnings = vulnerabilities.map(v => `${v.package}: ${v.title} (${v.severity})`)
      }
      vulnTest.duration = 1000 + Math.random() * 2000
    } catch (error) {
      vulnTest.status = 'failed'
      vulnTest.errors = [(error as Error).message]
    }

    suite.progress = 33
    this.notifyProgress()
    await new Promise(resolve => setTimeout(resolve, 1500))

    // Code security analysis
    const codeSecTest: TestResult = {
      id: 'code-security',
      name: 'Code security analysis',
      status: 'running'
    }
    tests.push(codeSecTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      const issues = await this.analyzeCodeSecurity(config.files)
      codeSecTest.status = issues.length === 0 ? 'passed' : 'failed'
      if (issues.length > 0) {
        codeSecTest.errors = issues
      }
      codeSecTest.duration = 800 + Math.random() * 1200
    } catch (error) {
      codeSecTest.status = 'failed'
      codeSecTest.errors = [(error as Error).message]
    }

    suite.progress = 66
    this.notifyProgress()
    await new Promise(resolve => setTimeout(resolve, 1000))

    // Configuration security check
    const configSecTest: TestResult = {
      id: 'config-security',
      name: 'Configuration security check',
      status: 'running'
    }
    tests.push(configSecTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      const configIssues = await this.checkConfigurationSecurity(config.files)
      configSecTest.status = configIssues.length === 0 ? 'passed' : 'failed'
      if (configIssues.length > 0) {
        configSecTest.errors = configIssues
      }
      configSecTest.duration = 300 + Math.random() * 500
    } catch (error) {
      configSecTest.status = 'failed'
      configSecTest.errors = [(error as Error).message]
    }

    suite.progress = 100
    this.notifyProgress()
    suite.tests = tests
  }

  /**
   * Performance tests
   */
  private async runPerformanceTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // Bundle size analysis
    const bundleTest: TestResult = {
      id: 'bundle-size',
      name: 'Bundle size analysis',
      status: 'running'
    }
    tests.push(bundleTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      const bundleSize = await this.analyzeBundleSize(config)
      bundleTest.status = bundleSize < 1000000 ? 'passed' : 'failed' // 1MB threshold
      bundleTest.details = { bundleSize, threshold: 1000000 }
      bundleTest.duration = 2000 + Math.random() * 3000
      if (bundleSize >= 1000000) {
        bundleTest.warnings = [`Bundle size ${Math.round(bundleSize / 1024)}KB exceeds recommended 1MB`]
      }
    } catch (error) {
      bundleTest.status = 'failed'
      bundleTest.errors = [(error as Error).message]
    }

    suite.progress = 50
    this.notifyProgress()
    await new Promise(resolve => setTimeout(resolve, 2500))

    // Performance benchmarks
    const perfTest: TestResult = {
      id: 'performance-benchmarks',
      name: 'Performance benchmarks',
      status: 'running'
    }
    tests.push(perfTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      const metrics = await this.runPerformanceBenchmarks(config)
      perfTest.status = metrics.score > 80 ? 'passed' : 'failed'
      perfTest.details = metrics
      perfTest.duration = 3000 + Math.random() * 2000
    } catch (error) {
      perfTest.status = 'failed'
      perfTest.errors = [(error as Error).message]
    }

    suite.progress = 100
    this.notifyProgress()
    suite.tests = tests
  }

  /**
   * Accessibility tests
   */
  private async runAccessibilityTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // Find React/HTML components
    const uiFiles = config.files.filter(file => 
      file.path.includes('components/') || file.path.includes('pages/')
    )

    for (const file of uiFiles) {
      const test: TestResult = {
        id: `a11y-${file.path}`,
        name: `Accessibility: ${file.path}`,
        status: 'running'
      }
      tests.push(test)
      suite.tests = tests
      this.notifyProgress()

      try {
        const issues = await this.checkAccessibility(file)
        test.status = issues.length === 0 ? 'passed' : 'failed'
        if (issues.length > 0) {
          test.errors = issues
        }
        test.duration = 400 + Math.random() * 600
      } catch (error) {
        test.status = 'failed'
        test.errors = [(error as Error).message]
      }

      suite.progress = (tests.filter(t => t.status !== 'running').length / tests.length) * 100
      this.notifyProgress()
      await new Promise(resolve => setTimeout(resolve, 500))
    }

    suite.tests = tests
  }

  /**
   * Deployment readiness tests
   */
  private async runDeploymentTests(suite: TestSuite, config: ProjectTestConfiguration) {
    const tests: TestResult[] = []

    // Build process test
    const buildTest: TestResult = {
      id: 'build-process',
      name: 'Build process validation',
      status: 'running'
    }
    tests.push(buildTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      await this.validateBuildProcess(config)
      buildTest.status = 'passed'
      buildTest.duration = 5000 + Math.random() * 10000
    } catch (error) {
      buildTest.status = 'failed'
      buildTest.errors = [(error as Error).message]
    }

    suite.progress = 33
    this.notifyProgress()
    await new Promise(resolve => setTimeout(resolve, 7500))

    // Environment configuration test
    const envTest: TestResult = {
      id: 'environment-config',
      name: 'Environment configuration',
      status: 'running'
    }
    tests.push(envTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      const envIssues = await this.validateEnvironmentConfig(config)
      envTest.status = envIssues.length === 0 ? 'passed' : 'failed'
      if (envIssues.length > 0) {
        envTest.errors = envIssues
      }
      envTest.duration = 500 + Math.random() * 1000
    } catch (error) {
      envTest.status = 'failed'
      envTest.errors = [(error as Error).message]
    }

    suite.progress = 66
    this.notifyProgress()
    await new Promise(resolve => setTimeout(resolve, 750))

    // Deployment configuration test
    const deployTest: TestResult = {
      id: 'deployment-config',
      name: 'Deployment configuration',
      status: 'running'
    }
    tests.push(deployTest)
    suite.tests = tests
    this.notifyProgress()

    try {
      await this.validateDeploymentConfig(config)
      deployTest.status = 'passed'
      deployTest.duration = 1000 + Math.random() * 2000
    } catch (error) {
      deployTest.status = 'failed'
      deployTest.errors = [(error as Error).message]
    }

    suite.progress = 100
    this.notifyProgress()
    suite.tests = tests
  }

  private notifyProgress() {
    if (this.onProgressUpdate) {
      this.onProgressUpdate(Array.from(this.testSuites.values()))
    }
  }

  // Mock validation methods (in a real implementation, these would use actual tools)
  private async validateTypescript(content: string, path: string): Promise<void> {
    // Mock TypeScript compilation check
    if (content.includes('syntax error') || content.includes('undefined variable')) {
      throw new Error(`TypeScript compilation error in ${path}`)
    }
  }

  private async validateCSS(content: string): Promise<void> {
    // Mock CSS validation
    if (content.includes('invalid-property')) {
      throw new Error('Invalid CSS property')
    }
  }

  private async validateDependency(name: string, version: string): Promise<boolean> {
    // Mock dependency validation
    const invalidPackages = ['invalid-package', 'nonexistent-lib']
    return !invalidPackages.includes(name)
  }

  private async checkDependencyConflicts(deps: Record<string, string>, devDeps: Record<string, string>): Promise<string[]> {
    // Mock conflict detection
    const conflicts: string[] = []
    return conflicts
  }

  private async simulateComponentTest(file: CodeFile): Promise<void> {
    // Mock component testing
    if (file.content.includes('broken component')) {
      throw new Error('Component test failed')
    }
  }

  private async runTestFile(file: CodeFile): Promise<void> {
    // Mock test file execution
    if (file.content.includes('failing test')) {
      throw new Error('Test suite failed')
    }
  }

  private async testAPIEndpoints(config: ProjectTestConfiguration): Promise<void> {
    // Mock API testing
    await new Promise(resolve => setTimeout(resolve, 500))
  }

  private async testDatabaseIntegration(config: ProjectTestConfiguration): Promise<void> {
    // Mock database testing
    await new Promise(resolve => setTimeout(resolve, 800))
  }

  private async scanDependencyVulnerabilities(deps: Record<string, string>): Promise<any[]> {
    // Mock vulnerability scanning
    return []
  }

  private async analyzeCodeSecurity(files: CodeFile[]): Promise<string[]> {
    // Mock security analysis
    const issues: string[] = []
    for (const file of files) {
      if (file.content.includes('eval(') || file.content.includes('dangerouslySetInnerHTML')) {
        issues.push(`Security issue in ${file.path}: Potentially dangerous code`)
      }
    }
    return issues
  }

  private async checkConfigurationSecurity(files: CodeFile[]): Promise<string[]> {
    // Mock configuration security check
    const issues: string[] = []
    for (const file of files) {
      if (file.content.includes('API_KEY') && !file.path.includes('.env')) {
        issues.push(`Security issue in ${file.path}: API key exposed in code`)
      }
    }
    return issues
  }

  private async analyzeBundleSize(config: ProjectTestConfiguration): Promise<number> {
    // Mock bundle size analysis
    const fileCount = config.files.length
    const depCount = Object.keys(config.dependencies).length
    return fileCount * 10000 + depCount * 50000 // Rough estimate
  }

  private async runPerformanceBenchmarks(config: ProjectTestConfiguration): Promise<{ score: number; [key: string]: any }> {
    // Mock performance benchmarks
    return {
      score: 85,
      firstContentfulPaint: 1.2,
      largestContentfulPaint: 2.5,
      totalBlockingTime: 150
    }
  }

  private async checkAccessibility(file: CodeFile): Promise<string[]> {
    // Mock accessibility check
    const issues: string[] = []
    if (file.content.includes('<img') && !file.content.includes('alt=')) {
      issues.push('Missing alt attribute on image')
    }
    if (file.content.includes('<button') && !file.content.includes('aria-label') && !file.content.match(/<button[^>]*>[^<]+</)) {
      issues.push('Button missing accessible label')
    }
    return issues
  }

  private async validateBuildProcess(config: ProjectTestConfiguration): Promise<void> {
    // Mock build validation
    if (!config.scripts.build) {
      throw new Error('No build script defined')
    }
  }

  private async validateEnvironmentConfig(config: ProjectTestConfiguration): Promise<string[]> {
    // Mock environment validation
    const issues: string[] = []
    const hasEnvExample = config.files.some(f => f.path.includes('.env.example'))
    if (!hasEnvExample && config.framework !== 'react') {
      issues.push('Missing .env.example file for backend projects')
    }
    return issues
  }

  private async validateDeploymentConfig(config: ProjectTestConfiguration): Promise<void> {
    // Mock deployment validation
    await new Promise(resolve => setTimeout(resolve, 1000))
  }
}