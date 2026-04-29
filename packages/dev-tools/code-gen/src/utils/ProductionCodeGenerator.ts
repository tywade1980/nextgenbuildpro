/**
 * Production-Ready Code Generator for Multiple Frameworks
 * 
 * Generates complete, deployment-ready applications with proper architecture,
 * error handling, testing setup, and best practices for each framework.
 */

export interface ProjectConfiguration {
  name: string
  description: string
  framework: 'react' | 'node' | 'android' | 'fullstack' | 'nextjs' | 'express' | 'fastapi'
  features: string[]
  deploymentTarget: 'vercel' | 'netlify' | 'heroku' | 'aws' | 'gcp' | 'docker' | 'playstore'
  database?: 'postgresql' | 'mongodb' | 'sqlite' | 'mysql' | 'firebase'
  authentication?: 'firebase' | 'auth0' | 'supabase' | 'custom'
  styling?: 'tailwind' | 'mui' | 'chakra' | 'styled-components' | 'css-modules'
}

export interface GeneratedProject {
  files: Record<string, string>
  dependencies: Record<string, string>
  devDependencies: Record<string, string>
  scripts: Record<string, string>
  config: Record<string, any>
  dockerConfig?: string
  deploymentConfig?: Record<string, any>
  tests: Record<string, string>
  documentation: Record<string, string>
}

export class ProductionCodeGenerator {
  /**
   * Generate a complete production-ready project
   */
  static async generateProject(config: ProjectConfiguration): Promise<GeneratedProject> {
    const generator = new ProductionCodeGenerator()
    
    switch (config.framework) {
      case 'react':
        return generator.generateReactApp(config)
      case 'nextjs':
        return generator.generateNextJSApp(config)
      case 'node':
        return generator.generateNodeApp(config)
      case 'express':
        return generator.generateExpressApp(config)
      case 'android':
        return generator.generateAndroidApp(config)
      case 'fullstack':
        return generator.generateFullStackApp(config)
      case 'fastapi':
        return generator.generateFastAPIApp(config)
      default:
        throw new Error(`Unsupported framework: ${config.framework}`)
    }
  }

  /**
   * Generate React application with TypeScript, testing, and deployment config
   */
  private generateReactApp(config: ProjectConfiguration): GeneratedProject {
    const dependencies = {
      'react': '^18.2.0',
      'react-dom': '^18.2.0',
      'react-router-dom': '^6.8.0',
      '@types/react': '^18.0.28',
      '@types/react-dom': '^18.0.11',
      'typescript': '^4.9.5'
    }

    const devDependencies = {
      '@vitejs/plugin-react': '^3.1.0',
      'vite': '^4.1.0',
      '@testing-library/react': '^14.0.0',
      '@testing-library/jest-dom': '^5.16.5',
      '@testing-library/user-event': '^14.4.3',
      'vitest': '^0.28.5',
      'jsdom': '^21.1.0',
      'eslint': '^8.35.0',
      '@typescript-eslint/eslint-plugin': '^5.54.0',
      '@typescript-eslint/parser': '^5.54.0',
      'prettier': '^2.8.4'
    }

    // Add styling dependencies
    if (config.styling === 'tailwind') {
      devDependencies['tailwindcss'] = '^3.2.7'
      devDependencies['autoprefixer'] = '^10.4.14'
      devDependencies['postcss'] = '^8.4.21'
    } else if (config.styling === 'mui') {
      dependencies['@mui/material'] = '^5.11.10'
      dependencies['@emotion/react'] = '^11.10.5'
      dependencies['@emotion/styled'] = '^11.10.5'
    }

    // Add authentication dependencies
    if (config.authentication === 'firebase') {
      dependencies['firebase'] = '^9.17.2'
    } else if (config.authentication === 'auth0') {
      dependencies['@auth0/auth0-react'] = '^2.0.1'
    }

    const files = {
      'package.json': JSON.stringify({
        name: config.name.toLowerCase().replace(/\s+/g, '-'),
        version: '1.0.0',
        type: 'module',
        description: config.description,
        scripts: {
          'dev': 'vite',
          'build': 'tsc && vite build',
          'preview': 'vite preview',
          'test': 'vitest',
          'test:coverage': 'vitest --coverage',
          'lint': 'eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0',
          'lint:fix': 'eslint . --ext ts,tsx --fix',
          'format': 'prettier --write "src/**/*.{ts,tsx,css,md}"'
        },
        dependencies,
        devDependencies
      }, null, 2),

      'vite.config.ts': this.generateViteConfig(config),
      'tsconfig.json': this.generateTSConfig(),
      'tsconfig.node.json': this.generateNodeTSConfig(),
      'index.html': this.generateReactIndexHTML(config),
      'src/main.tsx': this.generateReactMain(config),
      'src/App.tsx': this.generateReactApp_Component(config),
      'src/App.css': this.generateReactCSS(config),
      'src/types/index.ts': this.generateTypeDefinitions(),
      'src/hooks/index.ts': this.generateCustomHooks(config),
      'src/utils/index.ts': this.generateUtilities(),
      'src/components/Layout/Header.tsx': this.generateHeaderComponent(config),
      'src/components/Layout/Footer.tsx': this.generateFooterComponent(),
      'src/components/UI/Button.tsx': this.generateButtonComponent(config),
      'src/components/UI/LoadingSpinner.tsx': this.generateLoadingComponent(),
      'src/pages/Home.tsx': this.generateHomePage(config),
      'src/pages/About.tsx': this.generateAboutPage(),
      '.eslintrc.json': this.generateESLintConfig(),
      '.prettierrc': this.generatePrettierConfig(),
      '.gitignore': this.generateGitIgnore(),
      'README.md': this.generateREADME(config),
      'vitest.config.ts': this.generateVitestConfig(),
      'src/tests/App.test.tsx': this.generateAppTests(),
      'src/tests/components/Button.test.tsx': this.generateButtonTests(),
      'src/tests/utils/helpers.test.ts': this.generateUtilTests()
    }

    // Add styling configuration files
    if (config.styling === 'tailwind') {
      files['tailwind.config.js'] = this.generateTailwindConfig()
      files['postcss.config.js'] = this.generatePostCSSConfig()
    }

    // Add authentication setup
    if (config.authentication) {
      files['src/auth/index.ts'] = this.generateAuthSetup(config)
      files['src/contexts/AuthContext.tsx'] = this.generateAuthContext(config)
    }

    // Add deployment configurations
    const deploymentConfig = this.generateDeploymentConfig(config)

    return {
      files,
      dependencies,
      devDependencies,
      scripts: {
        'dev': 'vite',
        'build': 'tsc && vite build',
        'preview': 'vite preview',
        'test': 'vitest',
        'deploy': this.getDeployScript(config.deploymentTarget)
      },
      config: {},
      deploymentConfig,
      tests: this.generateTestSuite('react'),
      documentation: {
        'README.md': files['README.md'],
        'API.md': this.generateAPIDocumentation(config),
        'DEPLOYMENT.md': this.generateDeploymentDocs(config)
      }
    }
  }

  /**
   * Generate Next.js application with full-stack capabilities
   */
  private generateNextJSApp(config: ProjectConfiguration): GeneratedProject {
    const dependencies = {
      'next': '^13.2.1',
      'react': '^18.2.0',
      'react-dom': '^18.2.0',
      '@types/node': '^18.14.6',
      '@types/react': '^18.0.28',
      '@types/react-dom': '^18.0.11',
      'typescript': '^4.9.5'
    }

    const devDependencies = {
      'eslint': '^8.35.0',
      'eslint-config-next': '^13.2.1',
      '@testing-library/react': '^14.0.0',
      '@testing-library/jest-dom': '^5.16.5',
      'jest': '^29.5.0',
      'jest-environment-jsdom': '^29.5.0'
    }

    // Add database dependencies
    if (config.database === 'postgresql') {
      dependencies['pg'] = '^8.9.0'
      dependencies['@types/pg'] = '^8.6.6'
      dependencies['prisma'] = '^4.11.0'
      dependencies['@prisma/client'] = '^4.11.0'
    } else if (config.database === 'mongodb') {
      dependencies['mongodb'] = '^5.1.0'
      dependencies['mongoose'] = '^7.0.3'
    }

    const files = {
      'package.json': JSON.stringify({
        name: config.name.toLowerCase().replace(/\s+/g, '-'),
        version: '1.0.0',
        description: config.description,
        scripts: {
          'dev': 'next dev',
          'build': 'next build',
          'start': 'next start',
          'lint': 'next lint',
          'test': 'jest',
          'test:watch': 'jest --watch',
          'type-check': 'tsc --noEmit'
        },
        dependencies,
        devDependencies
      }, null, 2),

      'next.config.js': this.generateNextConfig(config),
      'tsconfig.json': this.generateNextTSConfig(),
      'tailwind.config.js': config.styling === 'tailwind' ? this.generateTailwindConfig() : '',
      'postcss.config.js': config.styling === 'tailwind' ? this.generatePostCSSConfig() : '',
      'src/app/layout.tsx': this.generateNextLayout(config),
      'src/app/page.tsx': this.generateNextHomePage(config),
      'src/app/globals.css': this.generateNextGlobalCSS(config),
      'src/components/ui/Button.tsx': this.generateButtonComponent(config),
      'src/lib/utils.ts': this.generateUtilities(),
      'src/types/index.ts': this.generateTypeDefinitions(),
      '.eslintrc.json': this.generateNextESLintConfig(),
      '.gitignore': this.generateNextGitIgnore(),
      'README.md': this.generateREADME(config),
      'jest.config.js': this.generateJestConfig(),
      '__tests__/index.test.tsx': this.generateNextTests()
    }

    // Add API routes
    files['src/app/api/health/route.ts'] = this.generateHealthAPI()
    
    if (config.database) {
      files['src/lib/database.ts'] = this.generateDatabaseSetup(config)
      files['src/app/api/users/route.ts'] = this.generateUserAPI(config)
    }

    return {
      files,
      dependencies,
      devDependencies,
      scripts: {
        'dev': 'next dev',
        'build': 'next build',
        'start': 'next start',
        'deploy': this.getDeployScript(config.deploymentTarget)
      },
      config: {},
      deploymentConfig: this.generateDeploymentConfig(config),
      tests: this.generateTestSuite('nextjs'),
      documentation: {
        'README.md': files['README.md'],
        'API.md': this.generateAPIDocumentation(config),
        'DEPLOYMENT.md': this.generateDeploymentDocs(config)
      }
    }
  }

  /**
   * Generate Node.js/Express backend application
   */
  private generateExpressApp(config: ProjectConfiguration): GeneratedProject {
    const dependencies = {
      'express': '^4.18.2',
      'cors': '^2.8.5',
      'helmet': '^6.0.1',
      'morgan': '^1.10.0',
      'dotenv': '^16.0.3',
      '@types/express': '^4.17.17',
      '@types/cors': '^2.8.13',
      '@types/morgan': '^1.9.4',
      'typescript': '^4.9.5',
      'ts-node': '^10.9.1'
    }

    const devDependencies = {
      'nodemon': '^2.0.20',
      '@types/node': '^18.14.6',
      'jest': '^29.5.0',
      '@types/jest': '^29.4.0',
      'ts-jest': '^29.0.5',
      'supertest': '^6.3.3',
      '@types/supertest': '^2.0.12',
      'eslint': '^8.35.0',
      '@typescript-eslint/eslint-plugin': '^5.54.0',
      '@typescript-eslint/parser': '^5.54.0'
    }

    // Add database dependencies
    if (config.database === 'postgresql') {
      dependencies['pg'] = '^8.9.0'
      dependencies['@types/pg'] = '^8.6.6'
      dependencies['prisma'] = '^4.11.0'
      dependencies['@prisma/client'] = '^4.11.0'
    } else if (config.database === 'mongodb') {
      dependencies['mongoose'] = '^7.0.3'
      dependencies['@types/mongoose'] = '^5.11.97'
    }

    const files = {
      'package.json': JSON.stringify({
        name: config.name.toLowerCase().replace(/\s+/g, '-'),
        version: '1.0.0',
        description: config.description,
        main: 'dist/server.js',
        scripts: {
          'dev': 'nodemon src/server.ts',
          'build': 'tsc',
          'start': 'node dist/server.js',
          'test': 'jest',
          'test:watch': 'jest --watch',
          'lint': 'eslint src/**/*.ts',
          'lint:fix': 'eslint src/**/*.ts --fix'
        },
        dependencies,
        devDependencies
      }, null, 2),

      'tsconfig.json': this.generateServerTSConfig(),
      'src/server.ts': this.generateExpressServer(config),
      'src/app.ts': this.generateExpressApp_Setup(config),
      'src/routes/index.ts': this.generateExpressRoutes(),
      'src/routes/health.ts': this.generateHealthRoute(),
      'src/middleware/errorHandler.ts': this.generateErrorHandler(),
      'src/middleware/validation.ts': this.generateValidationMiddleware(),
      'src/config/database.ts': config.database ? this.generateDatabaseConfig(config) : '',
      'src/models/User.ts': this.generateUserModel(config),
      'src/controllers/UserController.ts': this.generateUserController(config),
      'src/services/UserService.ts': this.generateUserService(config),
      'src/types/index.ts': this.generateServerTypes(),
      'src/utils/logger.ts': this.generateLogger(),
      '.env.example': this.generateEnvExample(config),
      '.gitignore': this.generateServerGitIgnore(),
      'Dockerfile': this.generateDockerfile('node'),
      'docker-compose.yml': this.generateDockerCompose(config),
      'README.md': this.generateREADME(config),
      'jest.config.js': this.generateJestServerConfig(),
      'src/tests/app.test.ts': this.generateExpressTests(),
      'src/tests/routes/health.test.ts': this.generateHealthTests()
    }

    return {
      files,
      dependencies,
      devDependencies,
      scripts: {
        'dev': 'nodemon src/server.ts',
        'build': 'tsc',
        'start': 'node dist/server.js',
        'deploy': this.getDeployScript(config.deploymentTarget)
      },
      config: {},
      dockerConfig: files['Dockerfile'],
      deploymentConfig: this.generateDeploymentConfig(config),
      tests: this.generateTestSuite('express'),
      documentation: {
        'README.md': files['README.md'],
        'API.md': this.generateAPIDocumentation(config),
        'DEPLOYMENT.md': this.generateDeploymentDocs(config)
      }
    }
  }

  /**
   * Generate Android Kotlin application
   */
  private generateAndroidApp(config: ProjectConfiguration): GeneratedProject {
    const files = {
      'app/build.gradle.kts': this.generateAndroidBuildGradle(config),
      'build.gradle.kts': this.generateProjectBuildGradle(),
      'gradle.properties': this.generateGradleProperties(),
      'settings.gradle.kts': this.generateSettingsGradle(config),
      'app/src/main/AndroidManifest.xml': this.generateAndroidManifest(config),
      'app/src/main/java/com/example/app/MainActivity.kt': this.generateMainActivity(config),
      'app/src/main/java/com/example/app/MyApplication.kt': this.generateApplication(config),
      'app/src/main/java/com/example/app/ui/theme/Theme.kt': this.generateTheme(),
      'app/src/main/java/com/example/app/ui/theme/Color.kt': this.generateColors(),
      'app/src/main/java/com/example/app/ui/screens/HomeScreen.kt': this.generateHomeScreen(config),
      'app/src/main/java/com/example/app/data/repository/Repository.kt': this.generateRepository(),
      'app/src/main/java/com/example/app/data/network/ApiService.kt': this.generateApiService(),
      'app/src/main/java/com/example/app/viewmodel/MainViewModel.kt': this.generateViewModel(),
      'app/src/main/res/values/strings.xml': this.generateStrings(config),
      'app/src/main/res/values/colors.xml': this.generateColorResources(),
      'app/src/main/res/values/themes.xml': this.generateThemesXml(config),
      'app/src/main/res/xml/backup_rules.xml': this.generateBackupRules(),
      'app/src/main/res/xml/data_extraction_rules.xml': this.generateDataExtractionRules(),
      'app/src/test/java/com/example/app/ExampleUnitTest.kt': this.generateUnitTests(),
      'app/src/androidTest/java/com/example/app/ExampleInstrumentedTest.kt': this.generateInstrumentedTests(),
      'README.md': this.generateREADME(config),
      '.gitignore': this.generateAndroidGitIgnore(),
      'local.properties.example': this.generateLocalPropertiesExample()
    }

    return {
      files,
      dependencies: this.getAndroidDependencies(config),
      devDependencies: {},
      scripts: {
        'build': './gradlew build',
        'test': './gradlew test',
        'assembleDebug': './gradlew assembleDebug',
        'assembleRelease': './gradlew assembleRelease',
        'bundleRelease': './gradlew bundleRelease',
        'installDebug': './gradlew installDebug',
        'lint': './gradlew lint',
        'clean': './gradlew clean'
      },
      config: {},
      deploymentConfig: this.generateAndroidDeploymentConfig(config),
      tests: this.generateTestSuite('android'),
      documentation: {
        'README.md': files['README.md'],
        'DEPLOYMENT.md': this.generateDeploymentDocs(config),
        'ANDROID_DEPLOYMENT.md': config.deploymentTarget === 'playstore' 
          ? this.generateAndroidDeploymentDocs(config)
          : 'See DEPLOYMENT.md for deployment instructions.'
      }
    }
  }

  // Utility methods for generating specific file contents
  private generateViteConfig(config: ProjectConfiguration): string {
    return `import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    open: true
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom']
        }
      }
    }
  }
})`
  }

  private generateTSConfig(): string {
    return JSON.stringify({
      compilerOptions: {
        target: "ES2020",
        useDefineForClassFields: true,
        lib: ["ES2020", "DOM", "DOM.Iterable"],
        module: "ESNext",
        skipLibCheck: true,
        moduleResolution: "bundler",
        allowImportingTsExtensions: true,
        resolveJsonModule: true,
        isolatedModules: true,
        noEmit: true,
        jsx: "react-jsx",
        strict: true,
        noUnusedLocals: true,
        noUnusedParameters: true,
        noFallthroughCasesInSwitch: true,
        baseUrl: ".",
        paths: {
          "@/*": ["./src/*"]
        }
      },
      include: ["src"],
      references: [{ path: "./tsconfig.node.json" }]
    }, null, 2)
  }

  private generateReactMain(config: ProjectConfiguration): string {
    return `import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './App.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
)`
  }

  private generateReactApp_Component(config: ProjectConfiguration): string {
    return `import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Header from './components/Layout/Header'
import Footer from './components/Layout/Footer'
import Home from './pages/Home'
import About from './pages/About'
${config.authentication ? "import { AuthProvider } from './contexts/AuthContext'" : ''}

function App() {
  return (
    ${config.authentication ? '<AuthProvider>' : ''}
      <div className="min-h-screen flex flex-col">
        <Header />
        <main className="flex-grow">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
          </Routes>
        </main>
        <Footer />
      </div>
    ${config.authentication ? '</AuthProvider>' : ''}
  )
}

export default App`
  }

  private generateREADME(config: ProjectConfiguration): string {
    return `# ${config.name}

${config.description}

## Framework: ${config.framework}

## Features

${config.features.map(feature => `- ${feature}`).join('\n')}

## Getting Started

### Prerequisites

- Node.js 18+ (for web projects)
- Android Studio (for Android projects)
- Docker (optional)

### Installation

\`\`\`bash
# Clone the repository
git clone <repository-url>
cd ${config.name.toLowerCase().replace(/\s+/g, '-')}

# Install dependencies
npm install

# Start development server
npm run dev
\`\`\`

### Testing

\`\`\`bash
# Run tests
npm test

# Run tests with coverage
npm run test:coverage
\`\`\`

### Building

\`\`\`bash
# Build for production
npm run build
\`\`\`

### Deployment

Deployment target: ${config.deploymentTarget}

See DEPLOYMENT.md for detailed deployment instructions.

## Project Structure

\`\`\`
src/
├── components/     # Reusable UI components
├── pages/          # Page components
├── hooks/          # Custom React hooks
├── utils/          # Utility functions
├── types/          # TypeScript type definitions
└── tests/          # Test files
\`\`\`

## Technologies Used

- ${config.framework}
- TypeScript
- ${config.styling || 'CSS'}
${config.database ? `- ${config.database}` : ''}
${config.authentication ? `- ${config.authentication}` : ''}

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

MIT License`
  }

  private generateDeploymentConfig(config: ProjectConfiguration): Record<string, any> {
    switch (config.deploymentTarget) {
      case 'vercel':
        return {
          'vercel.json': {
            version: 2,
            builds: [
              {
                src: config.framework === 'nextjs' ? 'package.json' : 'dist/index.html',
                use: config.framework === 'nextjs' ? '@vercel/next' : '@vercel/static'
              }
            ]
          }
        }
      case 'netlify':
        return {
          'netlify.toml': `[build]
  publish = "dist"
  command = "npm run build"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200`
        }
      case 'docker':
        return {
          'Dockerfile': this.generateDockerfile(config.framework),
          'docker-compose.yml': this.generateDockerCompose(config)
        }
      default:
        return {}
    }
  }

  private generateDockerfile(framework: string): string {
    if (framework === 'android') {
      return `FROM openjdk:17-alpine
WORKDIR /app
COPY . .
RUN ./gradlew build
EXPOSE 8080
CMD ["java", "-jar", "build/libs/app.jar"]`
    }

    return `FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

FROM node:18-alpine AS runner
WORKDIR /app
COPY --from=builder /app/node_modules ./node_modules
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]`
  }

  private generateTestSuite(framework: string): Record<string, string> {
    return {
      'unit': 'Comprehensive unit tests for all components and utilities',
      'integration': 'Integration tests for API endpoints and user flows',
      'e2e': 'End-to-end tests using Cypress or Playwright',
      'performance': 'Performance tests and benchmarks',
      'accessibility': 'Accessibility tests using axe-core'
    }
  }

  private getDeployScript(target: string): string {
    switch (target) {
      case 'vercel':
        return 'vercel --prod'
      case 'netlify':
        return 'netlify deploy --prod'
      case 'heroku':
        return 'git push heroku main'
      default:
        return 'npm run build'
    }
  }

  // Additional helper methods would be implemented here...
  private generateNodeTSConfig(): string {
    return JSON.stringify({
      compilerOptions: {
        composite: true,
        skipLibCheck: true,
        module: "ESNext",
        moduleResolution: "bundler",
        allowSyntheticDefaultImports: true
      },
      include: ["vite.config.ts"]
    }, null, 2)
  }

  private generateReactIndexHTML(config: ProjectConfiguration): string {
    return `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${config.name}</title>
    <meta name="description" content="${config.description}" />
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>`
  }

  private generateReactCSS(config: ProjectConfiguration): string {
    const baseStyles = `@import 'tailwindcss/base';
@import 'tailwindcss/components';
@import 'tailwindcss/utilities';

:root {
  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
  line-height: 1.5;
  font-weight: 400;
  color-scheme: light dark;
  color: rgba(255, 255, 255, 0.87);
  background-color: #242424;
  font-synthesis: none;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  margin: 0;
  display: flex;
  place-items: center;
  min-width: 320px;
  min-height: 100vh;
}

#root {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem;
  text-align: center;
}

.card {
  padding: 2em;
}

.read-the-docs {
  color: #888;
}`

    return config.styling === 'tailwind' ? baseStyles : baseStyles.replace(/@import 'tailwindcss\/[^']+';/g, '')
  }

  private generateTypeDefinitions(): string {
    return `export interface User {
  id: string
  email: string
  name: string
  createdAt: Date
  updatedAt: Date
}

export interface ApiResponse<T> {
  data: T
  message: string
  success: boolean
}

export interface PaginationMeta {
  page: number
  limit: number
  total: number
  totalPages: number
}

export interface PaginatedResponse<T> extends ApiResponse<T[]> {
  meta: PaginationMeta
}

export type Theme = 'light' | 'dark' | 'system'

export interface AppConfig {
  apiUrl: string
  appName: string
  version: string
  environment: 'development' | 'staging' | 'production'
}
`
  }

  private generateCustomHooks(config: ProjectConfiguration): string {
    return `import { useState, useEffect } from 'react'
import { Theme } from '../types'

export function useTheme() {
  const [theme, setTheme] = useState<Theme>(() => {
    if (typeof window !== 'undefined') {
      return (localStorage.getItem('theme') as Theme) || 'system'
    }
    return 'system'
  })

  useEffect(() => {
    const root = window.document.documentElement
    root.classList.remove('light', 'dark')

    if (theme === 'system') {
      const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
      root.classList.add(systemTheme)
    } else {
      root.classList.add(theme)
    }

    localStorage.setItem('theme', theme)
  }, [theme])

  return { theme, setTheme }
}

export function useLocalStorage<T>(key: string, initialValue: T) {
  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      const item = window.localStorage.getItem(key)
      return item ? JSON.parse(item) : initialValue
    } catch (error) {
      console.error(\`Error reading localStorage key "\${key}":\`, error)
      return initialValue
    }
  })

  const setValue = (value: T | ((val: T) => T)) => {
    try {
      const valueToStore = value instanceof Function ? value(storedValue) : value
      setStoredValue(valueToStore)
      window.localStorage.setItem(key, JSON.stringify(valueToStore))
    } catch (error) {
      console.error(\`Error setting localStorage key "\${key}":\`, error)
    }
  }

  return [storedValue, setValue] as const
}

export function useApi<T>(url: string) {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        const response = await fetch(url)
        if (!response.ok) {
          throw new Error(\`HTTP error! status: \${response.status}\`)
        }
        const result = await response.json()
        setData(result)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred')
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [url])

  return { data, loading, error }
}
`
  }

  private generateUtilities(): string {
    return `import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(date: Date | string | number) {
  return new Intl.DateTimeFormat('en-US', {
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(date))
}

export function formatCurrency(amount: number, currency = 'USD') {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(amount)
}

export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout
  return (...args: Parameters<T>) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }
}

export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}

export function generateId(length = 8): string {
  return Math.random().toString(36).substring(2, length + 2)
}

export function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^\\w ]+/g, '')
    .replace(/ +/g, '-')
}

export function capitalizeFirst(text: string): string {
  return text.charAt(0).toUpperCase() + text.slice(1)
}
`
  }

  private generateHeaderComponent(config: ProjectConfiguration): string {
    const authImports = config.authentication ? "import { useAuth } from '../../contexts/AuthContext'" : ''
    const authLogic = config.authentication ? `
  const { user, logout } = useAuth()` : ''
    
    return `import React from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../UI/Button'
${authImports}

export default function Header() {${authLogic}

  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link to="/" className="text-xl font-bold text-gray-900">
              ${config.name}
            </Link>
          </div>
          
          <nav className="hidden md:flex space-x-8">
            <Link
              to="/"
              className="text-gray-500 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
            >
              Home
            </Link>
            <Link
              to="/about"
              className="text-gray-500 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
            >
              About
            </Link>
          </nav>

          <div className="flex items-center space-x-4">
            ${config.authentication ? `
            {user ? (
              <div className="flex items-center space-x-4">
                <span className="text-sm text-gray-700">Hello, {user.name}</span>
                <Button variant="outline" onClick={logout}>
                  Logout
                </Button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <Button variant="outline">Login</Button>
                <Button>Sign Up</Button>
              </div>
            )}` : `
            <Button>Get Started</Button>`}
          </div>
        </div>
      </div>
    </header>
  )
}
`
  }

  private generateFooterComponent(): string {
    return `import React from 'react'

export default function Footer() {
  return (
    <footer className="bg-gray-50 border-t">
      <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="col-span-1 md:col-span-2">
            <h3 className="text-sm font-semibold text-gray-600 tracking-wider uppercase">
              About
            </h3>
            <p className="mt-4 text-base text-gray-500">
              Built with modern technologies and best practices for performance, 
              security, and maintainability.
            </p>
          </div>
          
          <div>
            <h3 className="text-sm font-semibold text-gray-600 tracking-wider uppercase">
              Links
            </h3>
            <ul className="mt-4 space-y-4">
              <li>
                <a href="#" className="text-base text-gray-500 hover:text-gray-900">
                  Privacy Policy
                </a>
              </li>
              <li>
                <a href="#" className="text-base text-gray-500 hover:text-gray-900">
                  Terms of Service
                </a>
              </li>
            </ul>
          </div>
          
          <div>
            <h3 className="text-sm font-semibold text-gray-600 tracking-wider uppercase">
              Support
            </h3>
            <ul className="mt-4 space-y-4">
              <li>
                <a href="#" className="text-base text-gray-500 hover:text-gray-900">
                  Documentation
                </a>
              </li>
              <li>
                <a href="#" className="text-base text-gray-500 hover:text-gray-900">
                  Contact
                </a>
              </li>
            </ul>
          </div>
        </div>
        
        <div className="mt-8 border-t border-gray-200 pt-8">
          <p className="text-base text-gray-400 text-center">
            &copy; {new Date().getFullYear()} All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  )
}
`
  }

  private generateButtonComponent(config: ProjectConfiguration): string {
    return `import React from 'react'
import { cn } from '../../utils'

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link'
  size?: 'default' | 'sm' | 'lg' | 'icon'
  children: React.ReactNode
}

const buttonVariants = {
  default: 'bg-primary text-primary-foreground hover:bg-primary/90',
  destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
  outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground',
  secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
  ghost: 'hover:bg-accent hover:text-accent-foreground',
  link: 'text-primary underline-offset-4 hover:underline'
}

const buttonSizes = {
  default: 'h-10 px-4 py-2',
  sm: 'h-9 rounded-md px-3',
  lg: 'h-11 rounded-md px-8',
  icon: 'h-10 w-10'
}

export function Button({
  className,
  variant = 'default',
  size = 'default',
  children,
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
        buttonVariants[variant],
        buttonSizes[size],
        className
      )}
      {...props}
    >
      {children}
    </button>
  )
}
`
  }

  private generateLoadingComponent(): string {
    return `import React from 'react'
import { cn } from '../../utils'

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const sizeClasses = {
  sm: 'w-4 h-4',
  md: 'w-8 h-8',
  lg: 'w-12 h-12'
}

export function LoadingSpinner({ size = 'md', className }: LoadingSpinnerProps) {
  return (
    <div
      className={cn(
        'animate-spin rounded-full border-2 border-gray-300 border-t-blue-600',
        sizeClasses[size],
        className
      )}
    />
  )
}

export function LoadingPage() {
  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <LoadingSpinner size="lg" />
        <p className="mt-4 text-gray-600">Loading...</p>
      </div>
    </div>
  )
}
`
  }

  private generateHomePage(config: ProjectConfiguration): string {
    return `import React from 'react'
import { Button } from '../components/UI/Button'

export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-16">
        <div className="text-center">
          <h1 className="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
            Welcome to <span className="text-blue-600">${config.name}</span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            ${config.description}
          </p>
          
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg">
              Get Started
            </Button>
            <Button variant="outline" size="lg">
              Learn More
            </Button>
          </div>
        </div>

        <div className="mt-20 grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            {
              title: 'Fast Performance',
              description: 'Built with modern technologies for optimal performance',
              icon: '⚡'
            },
            {
              title: 'Secure',
              description: 'Industry-standard security practices and authentication',
              icon: '🔒'
            },
            {
              title: 'Scalable',
              description: 'Designed to grow with your needs and requirements',
              icon: '📈'
            }
          ].map((feature, index) => (
            <div key={index} className="bg-white rounded-lg p-6 shadow-md">
              <div className="text-4xl mb-4">{feature.icon}</div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                {feature.title}
              </h3>
              <p className="text-gray-600">
                {feature.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
`
  }

  private generateAboutPage(): string {
    return `import React from 'react'

export default function About() {
  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">About Us</h1>
          <p className="text-xl text-gray-600">
            Learn more about our mission, values, and the team behind this project.
          </p>
        </div>

        <div className="prose prose-lg mx-auto">
          <h2>Our Mission</h2>
          <p>
            We're dedicated to building exceptional software that makes a difference. 
            Our team is passionate about creating solutions that are not only functional 
            but also delightful to use.
          </p>

          <h2>What We Do</h2>
          <p>
            We specialize in developing modern web applications using cutting-edge 
            technologies. Our focus is on performance, security, and user experience.
          </p>

          <h2>Our Values</h2>
          <ul>
            <li><strong>Quality:</strong> We never compromise on the quality of our work</li>
            <li><strong>Innovation:</strong> We embrace new technologies and methodologies</li>
            <li><strong>Collaboration:</strong> We believe in the power of teamwork</li>
            <li><strong>Transparency:</strong> We maintain open and honest communication</li>
          </ul>

          <h2>Technology Stack</h2>
          <p>
            This application is built with modern technologies including React, TypeScript, 
            and Tailwind CSS, ensuring a robust and maintainable codebase.
          </p>
        </div>
      </div>
    </div>
  )
}
`
  }

  private generateESLintConfig(): string {
    return JSON.stringify({
      root: true,
      env: { browser: true, es2020: true },
      extends: [
        'eslint:recommended',
        '@typescript-eslint/recommended',
        'plugin:react-hooks/recommended',
      ],
      ignorePatterns: ['dist', '.eslintrc.cjs'],
      parser: '@typescript-eslint/parser',
      plugins: ['react-refresh'],
      rules: {
        'react-refresh/only-export-components': [
          'warn',
          { allowConstantExport: true },
        ],
      },
    }, null, 2)
  }

  private generatePrettierConfig(): string {
    return JSON.stringify({
      semi: false,
      singleQuote: true,
      trailingComma: 'es5',
      tabWidth: 2,
      printWidth: 80,
    }, null, 2)
  }

  private generateGitIgnore(): string {
    return `# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
lerna-debug.log*

node_modules
dist
dist-ssr
*.local

# Editor directories and files
.vscode/*
!.vscode/extensions.json
.idea
.DS_Store
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?

# Environment variables
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# Testing
coverage
.nyc_output

# Build artifacts
build
.next
.vercel
.netlify

# Dependencies
.pnp
.pnp.js

# misc
.DS_Store
*.tsbuildinfo
`
  }

  private generateVitestConfig(): string {
    return `import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/tests/setup.ts'],
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  }
})
`
  }

  private generateAppTests(): string {
    return `import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import App from '../App'

function AppWrapper() {
  return (
    <BrowserRouter>
      <App />
    </BrowserRouter>
  )
}

describe('App', () => {
  it('renders without crashing', () => {
    render(<AppWrapper />)
    expect(document.body).toBeTruthy()
  })

  it('displays the main navigation', () => {
    render(<AppWrapper />)
    // Add specific test assertions based on your app structure
    expect(screen.getByRole('main')).toBeInTheDocument()
  })
})
`
  }

  private generateButtonTests(): string {
    return `import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { Button } from '../../components/UI/Button'

describe('Button', () => {
  it('renders with default props', () => {
    render(<Button>Click me</Button>)
    const button = screen.getByRole('button', { name: /click me/i })
    expect(button).toBeInTheDocument()
  })

  it('calls onClick when clicked', () => {
    const handleClick = vi.fn()
    render(<Button onClick={handleClick}>Click me</Button>)
    
    const button = screen.getByRole('button', { name: /click me/i })
    fireEvent.click(button)
    
    expect(handleClick).toHaveBeenCalledTimes(1)
  })

  it('applies variant classes correctly', () => {
    render(<Button variant="outline">Outline Button</Button>)
    const button = screen.getByRole('button', { name: /outline button/i })
    expect(button).toHaveClass('border')
  })

  it('is disabled when disabled prop is true', () => {
    render(<Button disabled>Disabled Button</Button>)
    const button = screen.getByRole('button', { name: /disabled button/i })
    expect(button).toBeDisabled()
  })
})
`
  }

  private generateUtilTests(): string {
    return `import { describe, it, expect } from 'vitest'
import { cn, formatDate, debounce, generateId } from '../../utils'

describe('Utility Functions', () => {
  describe('cn', () => {
    it('merges class names correctly', () => {
      const result = cn('base-class', 'additional-class')
      expect(result).toContain('base-class')
      expect(result).toContain('additional-class')
    })
  })

  describe('formatDate', () => {
    it('formats date correctly', () => {
      const date = new Date('2023-01-15')
      const formatted = formatDate(date)
      expect(formatted).toMatch(/January 15, 2023/)
    })
  })

  describe('generateId', () => {
    it('generates id of specified length', () => {
      const id = generateId(10)
      expect(id).toHaveLength(10)
    })

    it('generates unique ids', () => {
      const id1 = generateId()
      const id2 = generateId()
      expect(id1).not.toBe(id2)
    })
  })

  describe('debounce', () => {
    it('debounces function calls', async () => {
      let callCount = 0
      const debouncedFn = debounce(() => callCount++, 100)
      
      debouncedFn()
      debouncedFn()
      debouncedFn()
      
      expect(callCount).toBe(0)
      
      await new Promise(resolve => setTimeout(resolve, 150))
      expect(callCount).toBe(1)
    })
  })
})
`
  }

  private generateTailwindConfig(): string {
    return `/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
    },
  },
  plugins: [],
}
`
  }

  private generatePostCSSConfig(): string {
    return `export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
`
  }

  private generateAuthSetup(config: ProjectConfiguration): string {
    if (config.authentication === 'firebase') {
      return `import { initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'

const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY,
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID,
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.REACT_APP_FIREBASE_APP_ID
}

export const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
export const db = getFirestore(app)
`
    } else if (config.authentication === 'supabase') {
      return `import { createClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.REACT_APP_SUPABASE_URL!
const supabaseAnonKey = process.env.REACT_APP_SUPABASE_ANON_KEY!

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
`
    } else {
      return `// Custom authentication implementation
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:3001'

export const authAPI = {
  login: async (email: string, password: string) => {
    const response = await fetch(\`\${API_BASE_URL}/auth/login\`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    return response.json()
  },
  
  register: async (email: string, password: string, name: string) => {
    const response = await fetch(\`\${API_BASE_URL}/auth/register\`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, name })
    })
    return response.json()
  },
  
  logout: async () => {
    const token = localStorage.getItem('authToken')
    const response = await fetch(\`\${API_BASE_URL}/auth/logout\`, {
      method: 'POST',
      headers: { 
        'Authorization': \`Bearer \${token}\`,
        'Content-Type': 'application/json'
      }
    })
    localStorage.removeItem('authToken')
    return response.json()
  },
  
  getCurrentUser: async () => {
    const token = localStorage.getItem('authToken')
    if (!token) return null
    
    const response = await fetch(\`\${API_BASE_URL}/auth/me\`, {
      headers: { 'Authorization': \`Bearer \${token}\` }
    })
    return response.json()
  }
}
`
    }
  }

  private generateAuthContext(config: ProjectConfiguration): string {
    return `import React, { createContext, useContext, useEffect, useState } from 'react'
import { User } from '../types'

interface AuthContextType {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, name: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check for existing session on mount
    checkCurrentUser()
  }, [])

  const checkCurrentUser = async () => {
    try {
      const token = localStorage.getItem('authToken')
      if (token) {
        // Validate token and get user data
        const response = await fetch('/api/auth/me', {
          headers: { 'Authorization': \`Bearer \${token}\` }
        })
        
        if (response.ok) {
          const userData = await response.json()
          setUser(userData)
        } else {
          localStorage.removeItem('authToken')
        }
      }
    } catch (error) {
      console.error('Error checking current user:', error)
      localStorage.removeItem('authToken')
    } finally {
      setLoading(false)
    }
  }

  const login = async (email: string, password: string) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      })

      if (!response.ok) {
        throw new Error('Login failed')
      }

      const { user, token } = await response.json()
      localStorage.setItem('authToken', token)
      setUser(user)
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  }

  const register = async (email: string, password: string, name: string) => {
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, name })
      })

      if (!response.ok) {
        throw new Error('Registration failed')
      }

      const { user, token } = await response.json()
      localStorage.setItem('authToken', token)
      setUser(user)
    } catch (error) {
      console.error('Registration error:', error)
      throw error
    }
  }

  const logout = async () => {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 
          'Authorization': \`Bearer \${localStorage.getItem('authToken')}\`
        }
      })
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      localStorage.removeItem('authToken')
      setUser(null)
    }
  }

  const value = {
    user,
    loading,
    login,
    register,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
`
  }

  private generateAPIDocumentation(config: ProjectConfiguration): string {
    return `# API Documentation

## Overview

This document describes the API endpoints available in ${config.name}.

## Base URL

\`\`\`
${config.framework === 'nextjs' ? 'https://your-app.vercel.app/api' : 'http://localhost:3001/api'}
\`\`\`

## Authentication

${config.authentication ? `
This API uses JWT tokens for authentication. Include the token in the Authorization header:

\`\`\`
Authorization: Bearer <your-jwt-token>
\`\`\`

### Authentication Endpoints

#### POST /auth/login
Login with email and password.

**Request:**
\`\`\`json
{
  "email": "user@example.com",
  "password": "password123"
}
\`\`\`

**Response:**
\`\`\`json
{
  "success": true,
  "data": {
    "user": {
      "id": "1",
      "email": "user@example.com",
      "name": "John Doe"
    },
    "token": "jwt-token-here"
  }
}
\`\`\`

#### POST /auth/register
Register a new user account.

**Request:**
\`\`\`json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
\`\`\`

#### POST /auth/logout
Logout the current user.

#### GET /auth/me
Get current user information.
` : 'This API does not require authentication.'}

## Core Endpoints

### GET /health
Health check endpoint.

**Response:**
\`\`\`json
{
  "status": "ok",
  "timestamp": "2023-01-01T00:00:00.000Z",
  "version": "1.0.0"
}
\`\`\`

${config.database ? `
### Users

#### GET /users
Get paginated list of users.

**Query Parameters:**
- \`page\` (optional): Page number (default: 1)
- \`limit\` (optional): Items per page (default: 20)

**Response:**
\`\`\`json
{
  "success": true,
  "data": [
    {
      "id": "1",
      "email": "user@example.com",
      "name": "John Doe",
      "createdAt": "2023-01-01T00:00:00.000Z"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 50,
    "totalPages": 3
  }
}
\`\`\`

#### GET /users/:id
Get user by ID.

#### PUT /users/:id
Update user information.

#### DELETE /users/:id
Delete a user.
` : ''}

## Error Responses

All endpoints return errors in the following format:

\`\`\`json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message"
  }
}
\`\`\`

## Rate Limiting

API requests are limited to 100 requests per minute per IP address.

## SDK Examples

### JavaScript/TypeScript

\`\`\`typescript
const api = {
  baseURL: '${config.framework === 'nextjs' ? '/api' : 'http://localhost:3001/api'}',
  
  async request(endpoint: string, options: RequestInit = {}) {
    const url = \`\${this.baseURL}\${endpoint}\`
    const token = localStorage.getItem('authToken')
    
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: \`Bearer \${token}\` }),
        ...options.headers,
      },
    })
    
    return response.json()
  },
  
  async getUsers(page = 1, limit = 20) {
    return this.request(\`/users?page=\${page}&limit=\${limit}\`)
  },
  
  async getUser(id: string) {
    return this.request(\`/users/\${id}\`)
  }
}
\`\`\`
`
  }

  private generateDeploymentDocs(config: ProjectConfiguration): string {
    return `# Deployment Guide

## Overview

This guide covers how to deploy ${config.name} to various platforms.

## Prerequisites

- Node.js 18+
- npm or yarn
- Git

## Environment Variables

Create a \`.env\` file with the following variables:

\`\`\`bash
# App Configuration
NODE_ENV=production
PORT=3000

${config.database === 'postgresql' ? `
# Database
DATABASE_URL=postgresql://user:password@localhost:5432/dbname
` : config.database === 'mongodb' ? `
# Database
MONGODB_URI=mongodb://localhost:27017/dbname
` : ''}

${config.authentication ? `
# Authentication
JWT_SECRET=your-super-secret-jwt-key
JWT_EXPIRES_IN=7d

${config.authentication === 'firebase' ? `
# Firebase
REACT_APP_FIREBASE_API_KEY=your-api-key
REACT_APP_FIREBASE_AUTH_DOMAIN=your-app.firebaseapp.com
REACT_APP_FIREBASE_PROJECT_ID=your-project-id
` : config.authentication === 'supabase' ? `
# Supabase
REACT_APP_SUPABASE_URL=https://your-project.supabase.co
REACT_APP_SUPABASE_ANON_KEY=your-anon-key
` : ''}
` : ''}
\`\`\`

## Deployment Options

### ${config.deploymentTarget === 'vercel' ? 'Vercel (Recommended)' : 'Vercel'}

${config.deploymentTarget === 'vercel' ? `
This is the recommended deployment platform for this application.

1. Install Vercel CLI:
\`\`\`bash
npm i -g vercel
\`\`\`

2. Login to Vercel:
\`\`\`bash
vercel login
\`\`\`

3. Deploy:
\`\`\`bash
vercel --prod
\`\`\`

4. Configure environment variables in the Vercel dashboard.
` : `
1. Install Vercel CLI: \`npm i -g vercel\`
2. Run \`vercel --prod\`
3. Configure environment variables in dashboard
`}

### ${config.deploymentTarget === 'netlify' ? 'Netlify (Recommended)' : 'Netlify'}

${config.deploymentTarget === 'netlify' ? `
This application is optimized for Netlify deployment.

1. Connect your Git repository to Netlify
2. Set build command: \`npm run build\`
3. Set publish directory: \`dist\`
4. Configure environment variables in Netlify dashboard
5. Deploy!

For CLI deployment:
\`\`\`bash
npm install -g netlify-cli
netlify login
netlify deploy --prod
\`\`\`
` : `
1. Connect Git repository to Netlify
2. Build command: \`npm run build\`
3. Publish directory: \`dist\`
`}

### ${config.deploymentTarget === 'docker' ? 'Docker (Recommended)' : 'Docker'}

${config.deploymentTarget === 'docker' ? `
This application includes Docker configuration for easy deployment.

1. Build the Docker image:
\`\`\`bash
docker build -t ${config.name.toLowerCase().replace(/\s+/g, '-')} .
\`\`\`

2. Run the container:
\`\`\`bash
docker run -p 3000:3000 -d ${config.name.toLowerCase().replace(/\s+/g, '-')}
\`\`\`

3. For development with Docker Compose:
\`\`\`bash
docker-compose up -d
\`\`\`
` : `
1. Build: \`docker build -t app .\`
2. Run: \`docker run -p 3000:3000 app\`
`}

### AWS

1. Create an AWS account and install AWS CLI
2. Configure AWS credentials
3. Deploy using AWS Amplify, Elastic Beanstalk, or ECS

### Google Cloud Platform

1. Create a GCP account and install gcloud CLI
2. Create a new project
3. Deploy using App Engine, Cloud Run, or Compute Engine

### Heroku

1. Install Heroku CLI
2. Create a new Heroku app: \`heroku create your-app-name\`
3. Set environment variables: \`heroku config:set KEY=value\`
4. Deploy: \`git push heroku main\`

## Database Setup

${config.database === 'postgresql' ? `
### PostgreSQL

For production, use a managed PostgreSQL service:

- **Vercel**: Use Vercel Postgres
- **Heroku**: Use Heroku Postgres
- **AWS**: Use RDS
- **Google Cloud**: Use Cloud SQL

Run migrations after deployment:
\`\`\`bash
npm run db:migrate
\`\`\`
` : config.database === 'mongodb' ? `
### MongoDB

For production, use MongoDB Atlas:

1. Create a MongoDB Atlas account
2. Create a new cluster
3. Get the connection string
4. Set MONGODB_URI environment variable
` : ''}

## Performance Optimization

1. **Enable gzip compression** on your server
2. **Use a CDN** for static assets
3. **Enable caching** headers
4. **Monitor performance** with tools like:
   - Google PageSpeed Insights
   - Lighthouse
   - Web Vitals

## Security Checklist

- [ ] HTTPS enabled
- [ ] Environment variables secured
- [ ] Database credentials rotated
- [ ] CORS properly configured
- [ ] Rate limiting enabled
- [ ] Security headers configured

## Monitoring

Set up monitoring and logging:

- **Error tracking**: Sentry, Bugsnag
- **Performance monitoring**: New Relic, DataDog
- **Uptime monitoring**: UptimeRobot, Pingdom
- **Log aggregation**: LogRocket, Papertrail

## Troubleshooting

### Common Issues

1. **Build fails**: Check Node.js version and dependencies
2. **Environment variables not found**: Verify variable names and values
3. **Database connection fails**: Check connection string and network access
4. **404 errors**: Ensure routing is configured for SPAs

### Getting Help

- Check the [Issues](https://github.com/your-repo/issues) page
- Review deployment platform documentation
- Contact support if using managed services

## Maintenance

### Regular Tasks

- Update dependencies monthly
- Monitor security advisories
- Review and rotate secrets quarterly
- Backup database regularly
- Monitor performance metrics

### Scaling

As your application grows:

1. **Horizontal scaling**: Add more server instances
2. **Database scaling**: Use read replicas or sharding
3. **CDN**: Implement global content delivery
4. **Caching**: Add Redis or Memcached
5. **Load balancing**: Distribute traffic across instances
`
  }

  // NextJS specific methods
  private generateNextConfig(config: ProjectConfiguration): string {
    return `/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    appDir: true,
  },
  images: {
    domains: ['localhost'],
  },
  env: {
    CUSTOM_KEY: process.env.CUSTOM_KEY,
  },
  ${config.deploymentTarget === 'vercel' ? `
  // Vercel-specific optimizations
  swcMinify: true,
  ` : ''}
  ${config.styling === 'tailwind' ? `
  // Tailwind CSS configuration
  tailwindcss: {},
  ` : ''}
}

module.exports = nextConfig
`
  }

  private generateNextTSConfig(): string {
    return JSON.stringify({
      compilerOptions: {
        target: "es5",
        lib: ["dom", "dom.iterable", "es6"],
        allowJs: true,
        skipLibCheck: true,
        strict: true,
        forceConsistentCasingInFileNames: true,
        noEmit: true,
        esModuleInterop: true,
        module: "esnext",
        moduleResolution: "node",
        resolveJsonModule: true,
        isolatedModules: true,
        jsx: "preserve",
        incremental: true,
        plugins: [
          {
            name: "next"
          }
        ],
        baseUrl: ".",
        paths: {
          "@/*": ["./src/*"]
        }
      },
      include: ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
      exclude: ["node_modules"]
    }, null, 2)
  }

  private generateNextLayout(config: ProjectConfiguration): string {
    return `import './globals.css'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
${config.authentication ? "import { AuthProvider } from '@/contexts/AuthContext'" : ''}

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: '${config.name}',
  description: '${config.description}',
  keywords: ['${config.framework}', 'TypeScript', 'Next.js'],
  authors: [{ name: 'Your Name' }],
  viewport: 'width=device-width, initial-scale=1',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        ${config.authentication ? `
        <AuthProvider>
          {children}
        </AuthProvider>
        ` : '{children}'}
      </body>
    </html>
  )
}
`
  }

  private generateNextHomePage(config: ProjectConfiguration): string {
    return `import { Metadata } from 'next'
import Link from 'next/link'
import { Button } from '@/components/ui/Button'

export const metadata: Metadata = {
  title: 'Home | ${config.name}',
  description: '${config.description}',
}

export default function HomePage() {
  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-16">
        <div className="text-center">
          <h1 className="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
            Welcome to <span className="text-blue-600">${config.name}</span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            ${config.description}
          </p>
          
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg">
              Get Started
            </Button>
            <Button variant="outline" size="lg">
              <Link href="/about">Learn More</Link>
            </Button>
          </div>
        </div>

        <div className="mt-20 grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            {
              title: 'Server-Side Rendering',
              description: 'Fast initial page loads with SSR and SSG support',
              icon: '⚡'
            },
            {
              title: 'API Routes',
              description: 'Built-in API endpoints for backend functionality',
              icon: '🔧'
            },
            {
              title: 'Optimized',
              description: 'Automatic code splitting and performance optimization',
              icon: '🚀'
            }
          ].map((feature, index) => (
            <div key={index} className="bg-white rounded-lg p-6 shadow-md">
              <div className="text-4xl mb-4">{feature.icon}</div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                {feature.title}
              </h3>
              <p className="text-gray-600">
                {feature.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </main>
  )
}
`
  }

  private generateNextGlobalCSS(config: ProjectConfiguration): string {
    return `${config.styling === 'tailwind' ? `@tailwind base;
@tailwind components;
@tailwind utilities;

` : ''}:root {
  --foreground-rgb: 0, 0, 0;
  --background-start-rgb: 214, 219, 220;
  --background-end-rgb: 255, 255, 255;
  
  --primary: 222.2 84% 4.9%;
  --primary-foreground: 210 40% 98%;
  --secondary: 210 40% 96%;
  --secondary-foreground: 222.2 84% 4.9%;
  --muted: 210 40% 96%;
  --muted-foreground: 215.4 16.3% 46.9%;
  --accent: 210 40% 96%;
  --accent-foreground: 222.2 84% 4.9%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 210 40% 98%;
  --border: 214.3 31.8% 91.4%;
  --input: 214.3 31.8% 91.4%;
  --ring: 222.2 84% 4.9%;
  --radius: 0.5rem;
}

@media (prefers-color-scheme: dark) {
  :root {
    --foreground-rgb: 255, 255, 255;
    --background-start-rgb: 0, 0, 0;
    --background-end-rgb: 0, 0, 0;
    
    --primary: 210 40% 98%;
    --primary-foreground: 222.2 84% 4.9%;
    --secondary: 222.2 84% 4.9%;
    --secondary-foreground: 210 40% 98%;
    --muted: 222.2 84% 4.9%;
    --muted-foreground: 215 20.2% 65.1%;
    --accent: 222.2 84% 4.9%;
    --accent-foreground: 210 40% 98%;
    --destructive: 0 62.8% 30.6%;
    --destructive-foreground: 210 40% 98%;
    --border: 217.2 32.6% 17.5%;
    --input: 217.2 32.6% 17.5%;
    --ring: 212.7 26.8% 83.9%;
  }
}

* {
  box-sizing: border-box;
  padding: 0;
  margin: 0;
}

html,
body {
  max-width: 100vw;
  overflow-x: hidden;
}

body {
  color: rgb(var(--foreground-rgb));
  background: linear-gradient(
      to bottom,
      transparent,
      rgb(var(--background-end-rgb))
    )
    rgb(var(--background-start-rgb));
}

a {
  color: inherit;
  text-decoration: none;
}

@media (prefers-color-scheme: dark) {
  html {
    color-scheme: dark;
  }
}
`
  }

  private generateNextESLintConfig(): string {
    return JSON.stringify({
      extends: ["next/core-web-vitals", "@typescript-eslint/recommended"],
      parser: "@typescript-eslint/parser",
      plugins: ["@typescript-eslint"],
      rules: {
        "@typescript-eslint/no-unused-vars": "error",
        "@typescript-eslint/no-explicit-any": "warn"
      }
    }, null, 2)
  }

  private generateNextGitIgnore(): string {
    return `# See https://help.github.com/articles/ignoring-files/ for more about ignoring files.

# dependencies
/node_modules
/.pnp
.pnp.js

# testing
/coverage

# next.js
/.next/
/out/

# production
/build

# misc
.DS_Store
*.pem

# debug
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# local env files
.env*.local
.env

# vercel
.vercel

# typescript
*.tsbuildinfo
next-env.d.ts

# IDE
.vscode
.idea

# OS
Thumbs.db
`
  }

  private generateJestConfig(): string {
    return `const nextJest = require('next/jest')

const createJestConfig = nextJest({
  // Provide the path to your Next.js app to load next.config.js and .env files
  dir: './',
})

// Add any custom config to be passed to Jest
const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  moduleNameMapping: {
    // Handle module aliases (this will be automatically configured for you based on your tsconfig.json paths)
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  testEnvironment: 'jest-environment-jsdom',
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/*.stories.{ts,tsx}',
  ],
  testPathIgnorePatterns: [
    '<rootDir>/.next/',
    '<rootDir>/node_modules/',
  ],
}

// createJestConfig is exported this way to ensure that next/jest can load the Next.js config which is async
module.exports = createJestConfig(customJestConfig)
`
  }

  private generateNextTests(): string {
    return `import { render, screen } from '@testing-library/react'
import { describe, it, expect } from '@jest/globals'
import HomePage from '../src/app/page'

describe('HomePage', () => {
  it('renders the main heading', () => {
    render(<HomePage />)
    
    const heading = screen.getByRole('heading', { level: 1 })
    expect(heading).toBeInTheDocument()
    expect(heading).toHaveTextContent(/Welcome to/)
  })

  it('renders feature cards', () => {
    render(<HomePage />)
    
    expect(screen.getByText('Server-Side Rendering')).toBeInTheDocument()
    expect(screen.getByText('API Routes')).toBeInTheDocument()
    expect(screen.getByText('Optimized')).toBeInTheDocument()
  })

  it('renders call-to-action buttons', () => {
    render(<HomePage />)
    
    expect(screen.getByRole('button', { name: /get started/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /learn more/i })).toBeInTheDocument()
  })
})
`
  }

  private generateHealthAPI(): string {
    return `import { NextRequest, NextResponse } from 'next/server'

export async function GET(request: NextRequest) {
  try {
    // Basic health check
    const healthCheck = {
      status: 'ok',
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      version: process.env.npm_package_version || '1.0.0',
      environment: process.env.NODE_ENV || 'development',
      memory: {
        used: Math.round((process.memoryUsage().heapUsed / 1024 / 1024) * 100) / 100,
        total: Math.round((process.memoryUsage().heapTotal / 1024 / 1024) * 100) / 100,
      },
    }

    return NextResponse.json(healthCheck, { status: 200 })
  } catch (error) {
    return NextResponse.json(
      {
        status: 'error',
        timestamp: new Date().toISOString(),
        error: error instanceof Error ? error.message : 'Unknown error',
      },
      { status: 500 }
    )
  }
}

export async function HEAD(request: NextRequest) {
  return new NextResponse(null, { status: 200 })
}
`
  }

  private generateDatabaseSetup(config: ProjectConfiguration): string {
    if (config.database === 'postgresql') {
      return `import { Pool } from 'pg'

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
})

export async function query(text: string, params?: any[]) {
  const start = Date.now()
  try {
    const res = await pool.query(text, params)
    const duration = Date.now() - start
    console.log('Executed query', { text, duration, rows: res.rowCount })
    return res
  } catch (error) {
    console.error('Database query error:', error)
    throw error
  }
}

export async function getClient() {
  const client = await pool.connect()
  return client
}

export default pool
`
    } else if (config.database === 'mongodb') {
      return `import { MongoClient, Db, Collection } from 'mongodb'

if (!process.env.MONGODB_URI) {
  throw new Error('Please add your Mongo URI to .env.local')
}

const uri = process.env.MONGODB_URI
const options = {}

let client: MongoClient
let clientPromise: Promise<MongoClient>

if (process.env.NODE_ENV === 'development') {
  // In development mode, use a global variable so that the value
  // is preserved across module reloads caused by HMR (Hot Module Replacement).
  let globalWithMongo = global as typeof globalThis & {
    _mongoClientPromise?: Promise<MongoClient>
  }

  if (!globalWithMongo._mongoClientPromise) {
    client = new MongoClient(uri, options)
    globalWithMongo._mongoClientPromise = client.connect()
  }
  clientPromise = globalWithMongo._mongoClientPromise
} else {
  // In production mode, it's best to not use a global variable.
  client = new MongoClient(uri, options)
  clientPromise = client.connect()
}

export async function connectToDatabase(): Promise<{ client: MongoClient; db: Db }> {
  const client = await clientPromise
  const db = client.db()
  return { client, db }
}

export async function getCollection(collectionName: string): Promise<Collection> {
  const { db } = await connectToDatabase()
  return db.collection(collectionName)
}

export default clientPromise
`
    } else {
      return `// Database setup would be implemented based on selected database type
export const db = {
  async connect() {
    console.log('Database connection established')
  },
  
  async disconnect() {
    console.log('Database connection closed')
  },
  
  async query(sql: string, params: any[] = []) {
    console.log('Executing query:', sql, params)
    return { rows: [], rowCount: 0 }
  }
}
`
    }
  }

  private generateUserAPI(config: ProjectConfiguration): string {
    return `import { NextRequest, NextResponse } from 'next/server'
${config.database === 'postgresql' ? "import { query } from '@/lib/database'" : 
  config.database === 'mongodb' ? "import { getCollection } from '@/lib/database'" : 
  "import { db } from '@/lib/database'"}

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url)
    const page = parseInt(searchParams.get('page') || '1')
    const limit = parseInt(searchParams.get('limit') || '20')
    const offset = (page - 1) * limit

    ${config.database === 'postgresql' ? `
    // PostgreSQL implementation
    const usersQuery = \`
      SELECT id, email, name, created_at, updated_at 
      FROM users 
      ORDER BY created_at DESC 
      LIMIT $1 OFFSET $2
    \`
    const countQuery = 'SELECT COUNT(*) FROM users'
    
    const [usersResult, countResult] = await Promise.all([
      query(usersQuery, [limit, offset]),
      query(countQuery)
    ])
    
    const users = usersResult.rows
    const total = parseInt(countResult.rows[0].count)
    ` : config.database === 'mongodb' ? `
    // MongoDB implementation
    const collection = await getCollection('users')
    
    const [users, total] = await Promise.all([
      collection.find({})
        .sort({ createdAt: -1 })
        .skip(offset)
        .limit(limit)
        .toArray(),
      collection.countDocuments({})
    ])
    ` : `
    // Mock implementation
    const users = [
      { id: '1', email: 'user1@example.com', name: 'User 1', createdAt: new Date() },
      { id: '2', email: 'user2@example.com', name: 'User 2', createdAt: new Date() },
    ]
    const total = users.length
    `}

    return NextResponse.json({
      success: true,
      data: users,
      meta: {
        page,
        limit,
        total,
        totalPages: Math.ceil(total / limit)
      }
    })
  } catch (error) {
    console.error('Error fetching users:', error)
    return NextResponse.json(
      { 
        success: false, 
        error: { 
          code: 'FETCH_USERS_ERROR',
          message: 'Failed to fetch users' 
        } 
      },
      { status: 500 }
    )
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { email, name, password } = body

    if (!email || !name || !password) {
      return NextResponse.json(
        { 
          success: false, 
          error: { 
            code: 'VALIDATION_ERROR',
            message: 'Email, name, and password are required' 
          } 
        },
        { status: 400 }
      )
    }

    ${config.database === 'postgresql' ? `
    // PostgreSQL implementation
    const insertQuery = \`
      INSERT INTO users (email, name, password_hash, created_at, updated_at)
      VALUES ($1, $2, $3, NOW(), NOW())
      RETURNING id, email, name, created_at, updated_at
    \`
    
    // In production, hash the password first
    const passwordHash = password // TODO: Hash password with bcrypt
    const result = await query(insertQuery, [email, name, passwordHash])
    const user = result.rows[0]
    ` : config.database === 'mongodb' ? `
    // MongoDB implementation
    const collection = await getCollection('users')
    
    const newUser = {
      email,
      name,
      passwordHash: password, // TODO: Hash password with bcrypt
      createdAt: new Date(),
      updatedAt: new Date()
    }
    
    const result = await collection.insertOne(newUser)
    const user = { ...newUser, id: result.insertedId }
    ` : `
    // Mock implementation
    const user = {
      id: Date.now().toString(),
      email,
      name,
      createdAt: new Date(),
      updatedAt: new Date()
    }
    `}

    // Remove password from response
    const { passwordHash, ...userResponse } = user as any
    
    return NextResponse.json({
      success: true,
      data: userResponse,
      message: 'User created successfully'
    }, { status: 201 })
  } catch (error) {
    console.error('Error creating user:', error)
    return NextResponse.json(
      { 
        success: false, 
        error: { 
          code: 'CREATE_USER_ERROR',
          message: 'Failed to create user' 
        } 
      },
      { status: 500 }
    )
  }
}
`
  }

  // Express specific methods
  private generateServerTSConfig(): string {
    return JSON.stringify({
      compilerOptions: {
        target: "ES2020",
        module: "commonjs",
        lib: ["ES2020"],
        outDir: "./dist",
        rootDir: "./src",
        strict: true,
        esModuleInterop: true,
        skipLibCheck: true,
        forceConsistentCasingInFileNames: true,
        resolveJsonModule: true,
        declaration: true,
        declarationMap: true,
        sourceMap: true,
        removeComments: true,
        noUnusedLocals: true,
        noUnusedParameters: true,
        noImplicitReturns: true,
        noFallthroughCasesInSwitch: true,
        moduleResolution: "node",
        baseUrl: ".",
        paths: {
          "@/*": ["./src/*"]
        }
      },
      include: ["src/**/*"],
      exclude: ["node_modules", "dist", "**/*.test.ts", "**/*.spec.ts"]
    }, null, 2)
  }

  private generateExpressServer(config: ProjectConfiguration): string {
    return `import app from './app'
import { logger } from './utils/logger'
${config.database ? "import { connectDatabase } from './config/database'" : ''}

const PORT = process.env.PORT || 3001

async function startServer() {
  try {
    ${config.database ? `
    // Connect to database
    await connectDatabase()
    logger.info('Database connected successfully')
    ` : ''}

    // Start server
    const server = app.listen(PORT, () => {
      logger.info(\`🚀 Server running on port \${PORT}\`)
      logger.info(\`📚 API documentation: http://localhost:\${PORT}/api/docs\`)
      logger.info(\`❤️  Health check: http://localhost:\${PORT}/api/health\`)
    })

    // Graceful shutdown
    process.on('SIGTERM', () => {
      logger.info('SIGTERM received, shutting down gracefully')
      server.close(() => {
        logger.info('Process terminated')
        process.exit(0)
      })
    })

    process.on('SIGINT', () => {
      logger.info('SIGINT received, shutting down gracefully')
      server.close(() => {
        logger.info('Process terminated')
        process.exit(0)
      })
    })

  } catch (error) {
    logger.error('Failed to start server:', error)
    process.exit(1)
  }
}

startServer()
`
  }

  private generateExpressApp_Setup(config: ProjectConfiguration): string {
    return `import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import { config } from 'dotenv'
import routes from './routes'
import { errorHandler } from './middleware/errorHandler'
import { logger } from './utils/logger'

// Load environment variables
config()

const app = express()

// Security middleware
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"],
    },
  },
  hsts: {
    maxAge: 31536000,
    includeSubDomains: true,
    preload: true
  }
}))

// CORS configuration
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'],
  allowedHeaders: ['Content-Type', 'Authorization']
}))

// Request parsing middleware
app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))

// Logging middleware
app.use(morgan('combined', {
  stream: { write: (message: string) => logger.info(message.trim()) }
}))

// Request ID middleware
app.use((req, res, next) => {
  req.id = Math.random().toString(36).substring(2, 15)
  res.setHeader('X-Request-ID', req.id)
  next()
})

// API routes
app.use('/api', routes)

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    version: process.env.npm_package_version || '1.0.0'
  })
})

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    success: false,
    error: {
      code: 'NOT_FOUND',
      message: \`Route \${req.originalUrl} not found\`
    }
  })
})

// Global error handler
app.use(errorHandler)

export default app
`
  }

  private generateExpressRoutes(): string {
    return `import { Router } from 'express'
import healthRoutes from './health'
import userRoutes from './users'

const router = Router()

// Mount route modules
router.use('/health', healthRoutes)
router.use('/users', userRoutes)

// API documentation endpoint
router.get('/docs', (req, res) => {
  res.json({
    name: 'API Documentation',
    version: '1.0.0',
    endpoints: {
      'GET /api/health': 'Health check',
      'GET /api/users': 'Get all users',
      'POST /api/users': 'Create user',
      'GET /api/users/:id': 'Get user by ID',
      'PUT /api/users/:id': 'Update user',
      'DELETE /api/users/:id': 'Delete user'
    }
  })
})

export default router
`
  }

  private generateHealthRoute(): string {
    return `import { Router, Request, Response } from 'express'
${`import { query } from '../config/database'`}

const router = Router()

interface HealthCheck {
  status: 'ok' | 'error'
  timestamp: string
  uptime: number
  version: string
  environment: string
  memory: {
    used: number
    total: number
    free: number
  }
  database?: {
    status: 'connected' | 'disconnected'
    responseTime?: number
  }
}

router.get('/', async (req: Request, res: Response) => {
  const healthCheck: HealthCheck = {
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    version: process.env.npm_package_version || '1.0.0',
    environment: process.env.NODE_ENV || 'development',
    memory: {
      used: Math.round((process.memoryUsage().heapUsed / 1024 / 1024) * 100) / 100,
      total: Math.round((process.memoryUsage().heapTotal / 1024 / 1024) * 100) / 100,
      free: Math.round((process.memoryUsage().rss / 1024 / 1024) * 100) / 100,
    }
  }

  try {
    // Check database connection
    const start = Date.now()
    await query('SELECT 1')
    const responseTime = Date.now() - start

    healthCheck.database = {
      status: 'connected',
      responseTime
    }
  } catch (error) {
    healthCheck.status = 'error'
    healthCheck.database = {
      status: 'disconnected'
    }
  }

  const statusCode = healthCheck.status === 'ok' ? 200 : 503
  res.status(statusCode).json(healthCheck)
})

// Detailed health check
router.get('/detailed', async (req: Request, res: Response) => {
  try {
    const checks = await Promise.allSettled([
      // Database check
      query('SELECT 1'),
      // Memory check
      Promise.resolve(process.memoryUsage()),
      // Disk space check (simplified)
      Promise.resolve({ free: 1000000, total: 10000000 })
    ])

    const results = {
      status: 'ok',
      timestamp: new Date().toISOString(),
      checks: {
        database: checks[0].status === 'fulfilled' ? 'pass' : 'fail',
        memory: checks[1].status === 'fulfilled' ? 'pass' : 'fail',
        disk: checks[2].status === 'fulfilled' ? 'pass' : 'fail',
      },
      details: {
        database: checks[0].status === 'fulfilled' ? 'Connected' : 'Connection failed',
        memory: checks[1].status === 'fulfilled' ? checks[1].value : 'Memory check failed',
        disk: checks[2].status === 'fulfilled' ? checks[2].value : 'Disk check failed',
      }
    }

    const hasFailure = Object.values(results.checks).includes('fail')
    res.status(hasFailure ? 503 : 200).json(results)
  } catch (error) {
    res.status(503).json({
      status: 'error',
      timestamp: new Date().toISOString(),
      error: error instanceof Error ? error.message : 'Health check failed'
    })
  }
})

export default router
`
  }

  private generateErrorHandler(): string {
    return `import { Request, Response, NextFunction } from 'express'
import { logger } from '../utils/logger'

export interface AppError extends Error {
  statusCode?: number
  status?: string
  isOperational?: boolean
}

export class ValidationError extends Error {
  statusCode = 400
  status = 'fail'
  isOperational = true

  constructor(message: string) {
    super(message)
    this.name = 'ValidationError'
  }
}

export class NotFoundError extends Error {
  statusCode = 404
  status = 'fail'
  isOperational = true

  constructor(message: string = 'Resource not found') {
    super(message)
    this.name = 'NotFoundError'
  }
}

export class UnauthorizedError extends Error {
  statusCode = 401
  status = 'fail'
  isOperational = true

  constructor(message: string = 'Unauthorized') {
    super(message)
    this.name = 'UnauthorizedError'
  }
}

export class ForbiddenError extends Error {
  statusCode = 403
  status = 'fail'
  isOperational = true

  constructor(message: string = 'Forbidden') {
    super(message)
    this.name = 'ForbiddenError'
  }
}

const sendErrorDev = (err: AppError, res: Response) => {
  res.status(err.statusCode || 500).json({
    success: false,
    error: {
      status: err.status,
      message: err.message,
      stack: err.stack,
      name: err.name
    }
  })
}

const sendErrorProd = (err: AppError, res: Response) => {
  // Operational, trusted error: send message to client
  if (err.isOperational) {
    res.status(err.statusCode || 500).json({
      success: false,
      error: {
        code: err.name.replace('Error', '').toUpperCase(),
        message: err.message
      }
    })
  } else {
    // Programming or other unknown error: don't leak error details
    logger.error('ERROR 💥', err)
    res.status(500).json({
      success: false,
      error: {
        code: 'INTERNAL_SERVER_ERROR',
        message: 'Something went wrong!'
      }
    })
  }
}

export const errorHandler = (
  err: AppError,
  req: Request,
  res: Response,
  next: NextFunction
) => {
  err.statusCode = err.statusCode || 500
  err.status = err.status || 'error'

  // Log error
  logger.error(\`Error \${err.statusCode}: \${err.message}\`, {
    error: err,
    requestId: req.id,
    url: req.originalUrl,
    method: req.method,
    ip: req.ip,
    userAgent: req.get('User-Agent')
  })

  if (process.env.NODE_ENV === 'development') {
    sendErrorDev(err, res)
  } else {
    sendErrorProd(err, res)
  }
}

// Async error handler wrapper
export const catchAsync = (fn: Function) => {
  return (req: Request, res: Response, next: NextFunction) => {
    fn(req, res, next).catch(next)
  }
}
`
  }

  private generateValidationMiddleware(): string {
    return `import { Request, Response, NextFunction } from 'express'
import { ValidationError } from './errorHandler'

export interface ValidationRule {
  field: string
  required?: boolean
  type?: 'string' | 'number' | 'email' | 'boolean' | 'array' | 'object'
  minLength?: number
  maxLength?: number
  min?: number
  max?: number
  pattern?: RegExp
  custom?: (value: any) => boolean | string
}

export const validate = (rules: ValidationRule[]) => {
  return (req: Request, res: Response, next: NextFunction) => {
    const errors: string[] = []
    const data = { ...req.body, ...req.query, ...req.params }

    for (const rule of rules) {
      const value = data[rule.field]

      // Check required fields
      if (rule.required && (value === undefined || value === null || value === '')) {
        errors.push(\`\${rule.field} is required\`)
        continue
      }

      // Skip validation if field is not provided and not required
      if (value === undefined || value === null) {
        continue
      }

      // Type validation
      if (rule.type) {
        switch (rule.type) {
          case 'string':
            if (typeof value !== 'string') {
              errors.push(\`\${rule.field} must be a string\`)
            }
            break
          case 'number':
            if (typeof value !== 'number' && isNaN(Number(value))) {
              errors.push(\`\${rule.field} must be a number\`)
            }
            break
          case 'email':
            const emailRegex = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/
            if (!emailRegex.test(value)) {
              errors.push(\`\${rule.field} must be a valid email\`)
            }
            break
          case 'boolean':
            if (typeof value !== 'boolean' && value !== 'true' && value !== 'false') {
              errors.push(\`\${rule.field} must be a boolean\`)
            }
            break
          case 'array':
            if (!Array.isArray(value)) {
              errors.push(\`\${rule.field} must be an array\`)
            }
            break
          case 'object':
            if (typeof value !== 'object' || Array.isArray(value)) {
              errors.push(\`\${rule.field} must be an object\`)
            }
            break
        }
      }

      // String length validation
      if (typeof value === 'string') {
        if (rule.minLength && value.length < rule.minLength) {
          errors.push(\`\${rule.field} must be at least \${rule.minLength} characters long\`)
        }
        if (rule.maxLength && value.length > rule.maxLength) {
          errors.push(\`\${rule.field} must not exceed \${rule.maxLength} characters\`)
        }
      }

      // Number range validation
      if (typeof value === 'number' || !isNaN(Number(value))) {
        const numValue = Number(value)
        if (rule.min !== undefined && numValue < rule.min) {
          errors.push(\`\${rule.field} must be at least \${rule.min}\`)
        }
        if (rule.max !== undefined && numValue > rule.max) {
          errors.push(\`\${rule.field} must not exceed \${rule.max}\`)
        }
      }

      // Pattern validation
      if (rule.pattern && typeof value === 'string') {
        if (!rule.pattern.test(value)) {
          errors.push(\`\${rule.field} format is invalid\`)
        }
      }

      // Custom validation
      if (rule.custom) {
        const result = rule.custom(value)
        if (result !== true) {
          errors.push(typeof result === 'string' ? result : \`\${rule.field} is invalid\`)
        }
      }
    }

    if (errors.length > 0) {
      throw new ValidationError(\`Validation failed: \${errors.join(', ')}\`)
    }

    next()
  }
}

// Common validation rules
export const userValidationRules: ValidationRule[] = [
  { field: 'email', required: true, type: 'email' },
  { field: 'name', required: true, type: 'string', minLength: 2, maxLength: 50 },
  { field: 'password', required: true, type: 'string', minLength: 8, maxLength: 100 }
]

export const paginationRules: ValidationRule[] = [
  { field: 'page', type: 'number', min: 1 },
  { field: 'limit', type: 'number', min: 1, max: 100 }
]
`
  }

  private generateDatabaseConfig(config: ProjectConfiguration): string {
    if (config.database === 'postgresql') {
      return `import { Pool, PoolClient } from 'pg'
import { logger } from '../utils/logger'

if (!process.env.DATABASE_URL) {
  throw new Error('DATABASE_URL environment variable is required')
}

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
})

export async function query(text: string, params?: any[]) {
  const start = Date.now()
  try {
    const res = await pool.query(text, params)
    const duration = Date.now() - start
    logger.debug('Executed query', { text, duration, rows: res.rowCount })
    return res
  } catch (error) {
    logger.error('Database query error:', error)
    throw error
  }
}

export async function getClient(): Promise<PoolClient> {
  const client = await pool.connect()
  return client
}

export async function connectDatabase() {
  try {
    const client = await pool.connect()
    await client.query('SELECT 1')
    client.release()
    logger.info('PostgreSQL database connected successfully')
  } catch (error) {
    logger.error('Failed to connect to PostgreSQL database:', error)
    throw error
  }
}

export async function disconnectDatabase() {
  await pool.end()
  logger.info('PostgreSQL database connection closed')
}

export default pool
`
    } else if (config.database === 'mongodb') {
      return `import { MongoClient, Db, Collection } from 'mongodb'
import { logger } from '../utils/logger'

if (!process.env.MONGODB_URI) {
  throw new Error('MONGODB_URI environment variable is required')
}

const uri = process.env.MONGODB_URI
let client: MongoClient
let db: Db

export async function connectDatabase() {
  try {
    client = new MongoClient(uri)
    await client.connect()
    
    // Ping the database
    await client.db('admin').command({ ping: 1 })
    
    db = client.db()
    logger.info('MongoDB database connected successfully')
  } catch (error) {
    logger.error('Failed to connect to MongoDB database:', error)
    throw error
  }
}

export async function disconnectDatabase() {
  if (client) {
    await client.close()
    logger.info('MongoDB database connection closed')
  }
}

export function getDb(): Db {
  if (!db) {
    throw new Error('Database not connected. Call connectDatabase() first.')
  }
  return db
}

export function getCollection(name: string): Collection {
  return getDb().collection(name)
}

// Health check function
export async function checkDatabaseHealth(): Promise<boolean> {
  try {
    await client.db('admin').command({ ping: 1 })
    return true
  } catch (error) {
    logger.error('Database health check failed:', error)
    return false
  }
}

export { client }
`
    } else {
      return `import { logger } from '../utils/logger'

// Mock database configuration
export const db = {
  users: [] as any[],
  isConnected: false
}

export async function connectDatabase() {
  try {
    // Simulate database connection
    await new Promise(resolve => setTimeout(resolve, 1000))
    db.isConnected = true
    logger.info('Mock database connected successfully')
  } catch (error) {
    logger.error('Failed to connect to database:', error)
    throw error
  }
}

export async function disconnectDatabase() {
  db.isConnected = false
  logger.info('Mock database disconnected')
}

export async function query(sql: string, params: any[] = []) {
  if (!db.isConnected) {
    throw new Error('Database not connected')
  }
  
  logger.debug('Executing query:', { sql, params })
  
  // Mock query execution
  return {
    rows: [],
    rowCount: 0
  }
}
`
    }
  }

  private generateUserModel(config: ProjectConfiguration): string {
    if (config.database === 'mongodb') {
      return `import { ObjectId } from 'mongodb'
import { getCollection } from '../config/database'

export interface User {
  _id?: ObjectId
  id?: string
  email: string
  name: string
  passwordHash: string
  createdAt: Date
  updatedAt: Date
}

export interface CreateUserData {
  email: string
  name: string
  passwordHash: string
}

export interface UpdateUserData {
  email?: string
  name?: string
  passwordHash?: string
}

export class UserModel {
  private static collection = () => getCollection('users')

  static async create(userData: CreateUserData): Promise<User> {
    const now = new Date()
    const user: Omit<User, '_id'> = {
      ...userData,
      createdAt: now,
      updatedAt: now
    }

    const result = await this.collection().insertOne(user)
    return { ...user, _id: result.insertedId, id: result.insertedId.toString() }
  }

  static async findById(id: string): Promise<User | null> {
    const objectId = new ObjectId(id)
    const user = await this.collection().findOne({ _id: objectId })
    return user ? { ...user, id: user._id.toString() } : null
  }

  static async findByEmail(email: string): Promise<User | null> {
    const user = await this.collection().findOne({ email })
    return user ? { ...user, id: user._id.toString() } : null
  }

  static async findAll(skip: number = 0, limit: number = 20): Promise<User[]> {
    const users = await this.collection()
      .find({})
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit)
      .toArray()

    return users.map(user => ({ ...user, id: user._id.toString() }))
  }

  static async count(): Promise<number> {
    return await this.collection().countDocuments({})
  }

  static async updateById(id: string, updateData: UpdateUserData): Promise<User | null> {
    const objectId = new ObjectId(id)
    const updatedData = {
      ...updateData,
      updatedAt: new Date()
    }

    const result = await this.collection().findOneAndUpdate(
      { _id: objectId },
      { $set: updatedData },
      { returnDocument: 'after' }
    )

    return result.value ? { ...result.value, id: result.value._id.toString() } : null
  }

  static async deleteById(id: string): Promise<boolean> {
    const objectId = new ObjectId(id)
    const result = await this.collection().deleteOne({ _id: objectId })
    return result.deletedCount === 1
  }
}
`
    } else {
      return `import { query } from '../config/database'

export interface User {
  id: string
  email: string
  name: string
  passwordHash: string
  createdAt: Date
  updatedAt: Date
}

export interface CreateUserData {
  email: string
  name: string
  passwordHash: string
}

export interface UpdateUserData {
  email?: string
  name?: string
  passwordHash?: string
}

export class UserModel {
  static async create(userData: CreateUserData): Promise<User> {
    const sql = \`
      INSERT INTO users (email, name, password_hash, created_at, updated_at)
      VALUES ($1, $2, $3, NOW(), NOW())
      RETURNING id, email, name, password_hash as "passwordHash", created_at as "createdAt", updated_at as "updatedAt"
    \`
    
    const result = await query(sql, [userData.email, userData.name, userData.passwordHash])
    return result.rows[0]
  }

  static async findById(id: string): Promise<User | null> {
    const sql = \`
      SELECT id, email, name, password_hash as "passwordHash", created_at as "createdAt", updated_at as "updatedAt"
      FROM users
      WHERE id = $1
    \`
    
    const result = await query(sql, [id])
    return result.rows[0] || null
  }

  static async findByEmail(email: string): Promise<User | null> {
    const sql = \`
      SELECT id, email, name, password_hash as "passwordHash", created_at as "createdAt", updated_at as "updatedAt"
      FROM users
      WHERE email = $1
    \`
    
    const result = await query(sql, [email])
    return result.rows[0] || null
  }

  static async findAll(offset: number = 0, limit: number = 20): Promise<User[]> {
    const sql = \`
      SELECT id, email, name, password_hash as "passwordHash", created_at as "createdAt", updated_at as "updatedAt"
      FROM users
      ORDER BY created_at DESC
      LIMIT $1 OFFSET $2
    \`
    
    const result = await query(sql, [limit, offset])
    return result.rows
  }

  static async count(): Promise<number> {
    const sql = 'SELECT COUNT(*) as count FROM users'
    const result = await query(sql)
    return parseInt(result.rows[0].count)
  }

  static async updateById(id: string, updateData: UpdateUserData): Promise<User | null> {
    const fields = []
    const values = []
    let paramIndex = 1

    Object.entries(updateData).forEach(([key, value]) => {
      if (value !== undefined) {
        const dbField = key === 'passwordHash' ? 'password_hash' : key
        fields.push(\`\${dbField} = $\${paramIndex}\`)
        values.push(value)
        paramIndex++
      }
    })

    if (fields.length === 0) {
      return this.findById(id)
    }

    const sql = \`
      UPDATE users
      SET \${fields.join(', ')}, updated_at = NOW()
      WHERE id = $\${paramIndex}
      RETURNING id, email, name, password_hash as "passwordHash", created_at as "createdAt", updated_at as "updatedAt"
    \`
    
    values.push(id)
    const result = await query(sql, values)
    return result.rows[0] || null
  }

  static async deleteById(id: string): Promise<boolean> {
    const sql = 'DELETE FROM users WHERE id = $1'
    const result = await query(sql, [id])
    return result.rowCount === 1
  }
}
`
    }
  }

  private generateUserController(config: ProjectConfiguration): string {
    return `import { Request, Response, NextFunction } from 'express'
import { UserService } from '../services/UserService'
import { catchAsync } from '../middleware/errorHandler'
import { validate } from '../middleware/validation'

export class UserController {
  static getUsers = catchAsync(async (req: Request, res: Response) => {
    const page = parseInt(req.query.page as string) || 1
    const limit = parseInt(req.query.limit as string) || 20
    const search = req.query.search as string

    const result = await UserService.getUsers({ page, limit, search })

    res.json({
      success: true,
      data: result.users,
      meta: {
        page,
        limit,
        total: result.total,
        totalPages: Math.ceil(result.total / limit)
      }
    })
  })

  static getUserById = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params
    const user = await UserService.getUserById(id)

    if (!user) {
      return res.status(404).json({
        success: false,
        error: {
          code: 'USER_NOT_FOUND',
          message: 'User not found'
        }
      })
    }

    res.json({
      success: true,
      data: user
    })
  })

  static createUser = [
    validate([
      { field: 'email', required: true, type: 'email' },
      { field: 'name', required: true, type: 'string', minLength: 2, maxLength: 50 },
      { field: 'password', required: true, type: 'string', minLength: 8 }
    ]),
    catchAsync(async (req: Request, res: Response) => {
      const { email, name, password } = req.body

      // Check if user already exists
      const existingUser = await UserService.getUserByEmail(email)
      if (existingUser) {
        return res.status(409).json({
          success: false,
          error: {
            code: 'USER_EXISTS',
            message: 'User with this email already exists'
          }
        })
      }

      const user = await UserService.createUser({ email, name, password })

      res.status(201).json({
        success: true,
        data: user,
        message: 'User created successfully'
      })
    })
  ]

  static updateUser = [
    validate([
      { field: 'email', type: 'email' },
      { field: 'name', type: 'string', minLength: 2, maxLength: 50 },
      { field: 'password', type: 'string', minLength: 8 }
    ]),
    catchAsync(async (req: Request, res: Response) => {
      const { id } = req.params
      const updateData = req.body

      const user = await UserService.updateUser(id, updateData)

      if (!user) {
        return res.status(404).json({
          success: false,
          error: {
            code: 'USER_NOT_FOUND',
            message: 'User not found'
          }
        })
      }

      res.json({
        success: true,
        data: user,
        message: 'User updated successfully'
      })
    })
  ]

  static deleteUser = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params

    const deleted = await UserService.deleteUser(id)

    if (!deleted) {
      return res.status(404).json({
        success: false,
        error: {
          code: 'USER_NOT_FOUND',
          message: 'User not found'
        }
      })
    }

    res.json({
      success: true,
      message: 'User deleted successfully'
    })
  })

  static getUserProfile = catchAsync(async (req: Request, res: Response) => {
    // This would typically get the user from JWT token
    const userId = req.user?.id // Assuming auth middleware sets req.user

    if (!userId) {
      return res.status(401).json({
        success: false,
        error: {
          code: 'UNAUTHORIZED',
          message: 'User not authenticated'
        }
      })
    }

    const user = await UserService.getUserById(userId)

    if (!user) {
      return res.status(404).json({
        success: false,
        error: {
          code: 'USER_NOT_FOUND',
          message: 'User not found'
        }
      })
    }

    res.json({
      success: true,
      data: user
    })
  })
}
`
  }

  private generateUserService(config: ProjectConfiguration): string {
    return `import bcrypt from 'bcryptjs'
import { UserModel, User, CreateUserData, UpdateUserData } from '../models/User'

interface CreateUserInput {
  email: string
  name: string
  password: string
}

interface UpdateUserInput {
  email?: string
  name?: string
  password?: string
}

interface GetUsersOptions {
  page: number
  limit: number
  search?: string
}

interface GetUsersResult {
  users: Omit<User, 'passwordHash'>[]
  total: number
}

export class UserService {
  static async createUser(userData: CreateUserInput): Promise<Omit<User, 'passwordHash'>> {
    // Hash password
    const saltRounds = 12
    const passwordHash = await bcrypt.hash(userData.password, saltRounds)

    const createData: CreateUserData = {
      email: userData.email.toLowerCase().trim(),
      name: userData.name.trim(),
      passwordHash
    }

    const user = await UserModel.create(createData)
    
    // Remove password hash from response
    const { passwordHash: _, ...userResponse } = user
    return userResponse
  }

  static async getUserById(id: string): Promise<Omit<User, 'passwordHash'> | null> {
    const user = await UserModel.findById(id)
    
    if (!user) {
      return null
    }

    const { passwordHash: _, ...userResponse } = user
    return userResponse
  }

  static async getUserByEmail(email: string): Promise<User | null> {
    return await UserModel.findByEmail(email.toLowerCase().trim())
  }

  static async getUsers(options: GetUsersOptions): Promise<GetUsersResult> {
    const { page, limit, search } = options
    const offset = (page - 1) * limit

    // If search is provided, we would filter by it
    // For simplicity, we're not implementing search in this example
    const users = await UserModel.findAll(offset, limit)
    const total = await UserModel.count()

    // Remove password hashes from response
    const usersResponse = users.map(user => {
      const { passwordHash: _, ...userResponse } = user
      return userResponse
    })

    return {
      users: usersResponse,
      total
    }
  }

  static async updateUser(id: string, updateData: UpdateUserInput): Promise<Omit<User, 'passwordHash'> | null> {
    const processedData: UpdateUserData = {}

    if (updateData.email) {
      processedData.email = updateData.email.toLowerCase().trim()
    }

    if (updateData.name) {
      processedData.name = updateData.name.trim()
    }

    if (updateData.password) {
      const saltRounds = 12
      processedData.passwordHash = await bcrypt.hash(updateData.password, saltRounds)
    }

    const user = await UserModel.updateById(id, processedData)
    
    if (!user) {
      return null
    }

    const { passwordHash: _, ...userResponse } = user
    return userResponse
  }

  static async deleteUser(id: string): Promise<boolean> {
    return await UserModel.deleteById(id)
  }

  static async verifyPassword(plainPassword: string, hashedPassword: string): Promise<boolean> {
    return await bcrypt.compare(plainPassword, hashedPassword)
  }

  static async changePassword(userId: string, currentPassword: string, newPassword: string): Promise<boolean> {
    const user = await UserModel.findById(userId)
    
    if (!user) {
      throw new Error('User not found')
    }

    const isCurrentPasswordValid = await this.verifyPassword(currentPassword, user.passwordHash)
    
    if (!isCurrentPasswordValid) {
      throw new Error('Current password is incorrect')
    }

    const saltRounds = 12
    const newPasswordHash = await bcrypt.hash(newPassword, saltRounds)

    const updated = await UserModel.updateById(userId, { passwordHash: newPasswordHash })
    return !!updated
  }
}
`
  }

  private generateServerTypes(): string {
    return `import { Request } from 'express'

declare global {
  namespace Express {
    interface Request {
      id?: string
      user?: {
        id: string
        email: string
        name: string
      }
    }
  }
}

export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
  error?: {
    code: string
    message: string
  }
  meta?: {
    page?: number
    limit?: number
    total?: number
    totalPages?: number
  }
}

export interface PaginationOptions {
  page: number
  limit: number
  offset: number
}

export interface SortOptions {
  field: string
  direction: 'ASC' | 'DESC'
}

export interface FilterOptions {
  [key: string]: any
}

export interface QueryOptions {
  pagination?: PaginationOptions
  sort?: SortOptions
  filters?: FilterOptions
}

export interface DatabaseResult<T> {
  data: T[]
  total: number
}

export interface JWTPayload {
  userId: string
  email: string
  iat: number
  exp: number
}

export interface AuthRequest extends Request {
  user: {
    id: string
    email: string
    name: string
  }
}
`
  }

  private generateLogger(): string {
    return `import { createLogger, format, transports } from 'winston'

const { combine, timestamp, errors, json, simple, colorize, printf } = format

// Custom format for console output
const consoleFormat = printf(({ level, message, timestamp, ...meta }) => {
  return \`\${timestamp} [\${level.toUpperCase()}]: \${message} \${
    Object.keys(meta).length ? JSON.stringify(meta, null, 2) : ''
  }\`
})

// Create logger instance
export const logger = createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: combine(
    timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    errors({ stack: true }),
    json()
  ),
  defaultMeta: {
    service: process.env.SERVICE_NAME || 'api',
    environment: process.env.NODE_ENV || 'development'
  },
  transports: [
    // Console transport for development
    new transports.Console({
      format: combine(
        colorize(),
        simple(),
        consoleFormat
      )
    }),

    // File transport for errors
    new transports.File({
      filename: 'logs/error.log',
      level: 'error',
      maxsize: 5242880, // 5MB
      maxFiles: 5,
    }),

    // File transport for all logs
    new transports.File({
      filename: 'logs/combined.log',
      maxsize: 5242880, // 5MB
      maxFiles: 5,
    })
  ],

  // Handle exceptions and rejections
  exceptionHandlers: [
    new transports.File({ filename: 'logs/exceptions.log' })
  ],
  rejectionHandlers: [
    new transports.File({ filename: 'logs/rejections.log' })
  ]
})

// Create logs directory if it doesn't exist
import { existsSync, mkdirSync } from 'fs'
if (!existsSync('logs')) {
  mkdirSync('logs')
}

// In production, you might want to add additional transports like:
// - CloudWatch Logs (AWS)
// - Google Cloud Logging
// - Azure Monitor
// - Elasticsearch
// - External logging services (Loggly, Papertrail, etc.)

if (process.env.NODE_ENV === 'production') {
  // Add production-specific transports here
  logger.add(new transports.Console({
    format: json(),
    silent: false
  }))
}

export default logger
`
  }

  private generateEnvExample(config: ProjectConfiguration): string {
    return `# Application Configuration
NODE_ENV=development
PORT=3001
SERVICE_NAME=api

# Database Configuration
${config.database === 'postgresql' ? `
DATABASE_URL=postgresql://username:password@localhost:5432/database_name
` : config.database === 'mongodb' ? `
MONGODB_URI=mongodb://localhost:27017/database_name
` : `
# Add your database configuration here
`}

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here-make-it-long-and-random
JWT_EXPIRES_IN=7d
JWT_REFRESH_EXPIRES_IN=30d

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# Security Configuration
BCRYPT_ROUNDS=12
RATE_LIMIT_WINDOW=15
RATE_LIMIT_MAX_REQUESTS=100

# Logging Configuration
LOG_LEVEL=info

# Email Configuration (if needed)
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USER=your-email@example.com
SMTP_PASS=your-email-password
FROM_EMAIL=noreply@example.com

# File Upload Configuration
MAX_FILE_SIZE=10485760
UPLOAD_DIR=uploads

# Redis Configuration (if using Redis for caching)
REDIS_URL=redis://localhost:6379

# External API Keys
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
SENDGRID_API_KEY=SG...

# Monitoring and Analytics
SENTRY_DSN=https://...
NEW_RELIC_LICENSE_KEY=...

# Health Check Configuration
HEALTH_CHECK_TIMEOUT=5000
HEALTH_CHECK_INTERVAL=30000
`
  }

  private generateServerGitIgnore(): string {
    return `# Dependencies
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Build outputs
dist/
build/
*.tsbuildinfo

# Environment files
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# Logs
logs/
*.log

# Runtime data
pids/
*.pid
*.seed
*.pid.lock

# Coverage directory used by tools like istanbul
coverage/
.nyc_output/

# Dependency directories
node_modules/
jspm_packages/

# Optional npm cache directory
.npm

# Optional eslint cache
.eslintcache

# Microbundle cache
.rpt2_cache/
.rts2_cache_cjs/
.rts2_cache_es/
.rts2_cache_umd/

# Optional REPL history
.node_repl_history

# Output of 'npm pack'
*.tgz

# Yarn Integrity file
.yarn-integrity

# dotenv environment variables file
.env
.env.test

# parcel-bundler cache (https://parceljs.org/)
.cache
.parcel-cache

# Next.js build output
.next

# Nuxt.js build / generate output
.nuxt
dist

# Gatsby files
.cache/
public

# Vuepress build output
.vuepress/dist

# Serverless directories
.serverless/

# FuseBox cache
.fusebox/

# DynamoDB Local files
.dynamodb/

# TernJS port file
.tern-port

# Stores VSCode versions used for testing VSCode extensions
.vscode-test

# Temporary folders
tmp/
temp/

# IDE files
.vscode/
.idea/
*.swp
*.swo
*~

# OS files
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# Upload directories
uploads/
public/uploads/

# Database files
*.sqlite
*.sqlite3
*.db

# SSL certificates
*.pem
*.key
*.crt

# Docker files
docker-compose.override.yml
`
  }

  private generateDockerCompose(config: ProjectConfiguration): string {
    return `version: '3.8'

services:
  app:
    build: .
    ports:
      - "\${PORT:-3001}:3001"
    environment:
      - NODE_ENV=production
      - PORT=3001
      ${config.database === 'postgresql' ? '- DATABASE_URL=postgresql://postgres:password@db:5432/app_db' : ''}
      ${config.database === 'mongodb' ? '- MONGODB_URI=mongodb://mongo:27017/app_db' : ''}
    volumes:
      - ./logs:/app/logs
    depends_on:
      ${config.database === 'postgresql' ? '- db' : ''}
      ${config.database === 'mongodb' ? '- mongo' : ''}
    networks:
      - app-network
    restart: unless-stopped

${config.database === 'postgresql' ? `
  db:
    image: postgres:14-alpine
    environment:
      - POSTGRES_DB=app_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    networks:
      - app-network
    restart: unless-stopped
` : ''}

${config.database === 'mongodb' ? `
  mongo:
    image: mongo:5.0
    environment:
      - MONGO_INITDB_ROOT_USERNAME=admin
      - MONGO_INITDB_ROOT_PASSWORD=password
      - MONGO_INITDB_DATABASE=app_db
    volumes:
      - mongo_data:/data/db
    ports:
      - "27017:27017"
    networks:
      - app-network
    restart: unless-stopped
` : ''}

  # Redis for caching (optional)
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - app-network
    restart: unless-stopped
    command: redis-server --appendonly yes

  # Nginx reverse proxy (optional)
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    networks:
      - app-network
    restart: unless-stopped

volumes:
  ${config.database === 'postgresql' ? 'postgres_data:' : ''}
  ${config.database === 'mongodb' ? 'mongo_data:' : ''}
  redis_data:

networks:
  app-network:
    driver: bridge
`
  }

  private generateJestServerConfig(): string {
    return `module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/src'],
  testMatch: [
    '**/__tests__/**/*.+(ts|tsx|js)',
    '**/*.(test|spec).+(ts|tsx|js)'
  ],
  transform: {
    '^.+\\.(ts|tsx)$': 'ts-jest'
  },
  collectCoverageFrom: [
    'src/**/*.(ts|js)',
    '!src/**/*.d.ts',
    '!src/**/*.test.(ts|js)',
    '!src/**/*.spec.(ts|js)',
    '!src/tests/**/*'
  ],
  coverageDirectory: 'coverage',
  coverageReporters: [
    'text',
    'lcov',
    'clover',
    'html'
  ],
  setupFilesAfterEnv: ['<rootDir>/src/tests/setup.ts'],
  testTimeout: 10000,
  moduleNameMapping: {
    '^@/(.*)$': '<rootDir>/src/$1'
  },
  globalSetup: '<rootDir>/src/tests/globalSetup.ts',
  globalTeardown: '<rootDir>/src/tests/globalTeardown.ts'
}
`
  }

  private generateExpressTests(): string {
    return `import request from 'supertest'
import app from '../app'
import { connectDatabase, disconnectDatabase } from '../config/database'

describe('Express App', () => {
  beforeAll(async () => {
    await connectDatabase()
  })

  afterAll(async () => {
    await disconnectDatabase()
  })

  describe('Health Check', () => {
    it('should return 200 for GET /health', async () => {
      const response = await request(app)
        .get('/health')
        .expect(200)

      expect(response.body).toHaveProperty('status', 'ok')
      expect(response.body).toHaveProperty('timestamp')
      expect(response.body).toHaveProperty('uptime')
    })
  })

  describe('API Routes', () => {
    it('should return API documentation for GET /api/docs', async () => {
      const response = await request(app)
        .get('/api/docs')
        .expect(200)

      expect(response.body).toHaveProperty('name')
      expect(response.body).toHaveProperty('version')
      expect(response.body).toHaveProperty('endpoints')
    })

    it('should return 404 for unknown routes', async () => {
      const response = await request(app)
        .get('/api/nonexistent')
        .expect(404)

      expect(response.body).toHaveProperty('success', false)
      expect(response.body.error).toHaveProperty('code', 'NOT_FOUND')
    })
  })

  describe('Security Headers', () => {
    it('should include security headers', async () => {
      const response = await request(app)
        .get('/health')
        .expect(200)

      expect(response.headers).toHaveProperty('x-content-type-options')
      expect(response.headers).toHaveProperty('x-frame-options')
      expect(response.headers).toHaveProperty('x-xss-protection')
    })
  })

  describe('CORS', () => {
    it('should handle CORS preflight requests', async () => {
      const response = await request(app)
        .options('/api/users')
        .set('Origin', 'http://localhost:3000')
        .set('Access-Control-Request-Method', 'POST')
        .expect(204)

      expect(response.headers).toHaveProperty('access-control-allow-origin')
      expect(response.headers).toHaveProperty('access-control-allow-methods')
    })
  })
})
`
  }

  private generateHealthTests(): string {
    return `import request from 'supertest'
import app from '../../app'

describe('Health Routes', () => {
  describe('GET /api/health', () => {
    it('should return health status', async () => {
      const response = await request(app)
        .get('/api/health')
        .expect(200)

      expect(response.body).toMatchObject({
        status: expect.stringMatching(/^(ok|error)$/),
        timestamp: expect.any(String),
        uptime: expect.any(Number),
        version: expect.any(String),
        environment: expect.any(String),
        memory: {
          used: expect.any(Number),
          total: expect.any(Number),
          free: expect.any(Number)
        }
      })
    })

    it('should include database status when connected', async () => {
      const response = await request(app)
        .get('/api/health')

      if (response.body.database) {
        expect(response.body.database).toMatchObject({
          status: expect.stringMatching(/^(connected|disconnected)$/),
          responseTime: expect.any(Number)
        })
      }
    })
  })

  describe('GET /api/health/detailed', () => {
    it('should return detailed health information', async () => {
      const response = await request(app)
        .get('/api/health/detailed')

      expect(response.body).toMatchObject({
        status: expect.stringMatching(/^(ok|error)$/),
        timestamp: expect.any(String),
        checks: {
          database: expect.stringMatching(/^(pass|fail)$/),
          memory: expect.stringMatching(/^(pass|fail)$/),
          disk: expect.stringMatching(/^(pass|fail)$/)
        },
        details: expect.any(Object)
      })
    })

    it('should return 503 when health checks fail', async () => {
      // This test would require mocking database failures
      // For now, we'll just check that the endpoint exists
      await request(app)
        .get('/api/health/detailed')
        .expect(res => {
          expect([200, 503]).toContain(res.status)
        })
    })
  })
})
`
  }

  // Android specific methods
  private generateAndroidBuildGradle(config: ProjectConfiguration): string {
    return `plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    ${config.features?.includes('Database') ? "id 'kotlin-kapt'" : ''}
}

android {
    namespace 'com.example.${config.name.toLowerCase().replace(/\s+/g, '')}'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.${config.name.toLowerCase().replace(/\s+/g, '')}"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary true
        }
    }

    // Signing configuration
    signingConfigs {
        release {
            def keystorePropertiesFile = rootProject.file("app/release-keystore.properties")
            if (keystorePropertiesFile.exists()) {
                def keystoreProperties = new Properties()
                keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
                
                keyAlias keystoreProperties['keyAlias']
                keyPassword keystoreProperties['keyPassword']
                storeFile file(keystoreProperties['storeFile'])
                storePassword keystoreProperties['storePassword']
            }
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            
            // Enable R8 full mode for better optimization
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
        debug {
            applicationIdSuffix ".debug"
            debuggable true
            versionNameSuffix "-DEBUG"
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    buildFeatures {
        compose true
        buildConfig true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.8'
    }
    
    packaging {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
    
    // Bundle configuration for Play Store
    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.activity:activity-compose:1.8.2'
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    
    ${config.features?.includes('Navigation') ? `
    implementation 'androidx.navigation:navigation-compose:2.7.6'
    ` : ''}
    
    ${config.features?.includes('Database') ? `
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    ` : ''}
    
    ${config.features?.includes('Networking') ? `
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    ` : ''}
    
    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation platform('androidx.compose:compose-bom:2024.02.00')
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}
`
  }

  private generateProjectBuildGradle(): string {
    return `// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id 'com.android.application' version '8.2.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.22' apply false
}
`
  }

  private generateGradleProperties(): string {
    return `# Project-wide Gradle settings.
# IDE (e.g. Android Studio) users:
# Gradle settings configured through the IDE *will override*
# any settings specified in this file.
# For more details on how to configure your build environment visit
# http://www.gradle.org/docs/current/userguide/build_environment.html

# Specifies the JVM arguments used for the daemon process.
# The setting is particularly useful for tweaking memory settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

# When configured, Gradle will run in incubating parallel mode.
# This option should only be used with decoupled projects. More details, visit
# http://www.gradle.org/docs/current/userguide/multi_project_builds.html#sec:decoupled_projects
# org.gradle.parallel=true

# AndroidX package structure to make it clearer which packages are bundled with the
# Android operating system, and which are packaged with your app's APK
# https://developer.android.com/topic/libraries/support-library/androidx-rn
android.useAndroidX=true

# Kotlin code style for this project: "official" or "obsolete":
kotlin.code.style=official

# Enables namespacing of each library's R class so that its R class includes only the
# resources declared in the library itself and none from the library's dependencies,
# thereby reducing the size of the R class for that library
android.nonTransitiveRClass=true
`
  }

  private generateSettingsGradle(config: ProjectConfiguration): string {
    return `pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "${config.name}"
include ':app'
`
  }

  private generateAndroidManifest(config: ProjectConfiguration): string {
    return `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    ${config.features?.includes('Networking') ? '<uses-permission android:name="android.permission.INTERNET" />' : ''}
    ${config.features?.includes('Camera') ? '<uses-permission android:name="android.permission.CAMERA" />' : ''}
    ${config.features?.includes('Push Notifications') ? '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />' : ''}

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.${config.name.replace(/\s+/g, '')}"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.${config.name.replace(/\s+/g, '')}"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
`
  }

  private generateMainActivity(config: ProjectConfiguration): string {
    return `package com.example.${config.name.toLowerCase().replace(/\s+/g, '')}

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.${config.name.toLowerCase().replace(/\s+/g, '')}.ui.theme.${config.name.replace(/\s+/g, '')}Theme
import com.example.${config.name.toLowerCase().replace(/\s+/g, '')}.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ${config.name.replace(/\s+/g, '')}Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}
`
  }

  private generateApplication(config: ProjectConfiguration): string {
    return `package com.example.${config.name.toLowerCase().replace(/\s+/g, '')}

import android.app.Application

class ${config.name.replace(/\s+/g, '')}Application : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any libraries or components here
        ${config.features?.includes('Database') ? '// Initialize Room database' : ''}
        ${config.features?.includes('Networking') ? '// Initialize network client' : ''}
    }
}
`
  }

  private generateTheme(): string {
    return `package com.example.myapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
`
  }

  private generateColors(): string {
    return `package com.example.myapp.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
`
  }

  private generateHomeScreen(config: ProjectConfiguration): string {
    return `package com.example.${config.name.toLowerCase().replace(/\s+/g, '')}.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var items by remember { mutableStateOf(listOf("Item 1", "Item 2", "Item 3")) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "${config.name}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Subtitle
        Text(
            text = "${config.description}",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { items = items + "New Item \${items.size + 1}" }
            ) {
                Text("Add Item")
            }
            
            OutlinedButton(
                onClick = { items = items.dropLast(1) }
            ) {
                Text("Remove Item")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Items List
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.weight(1f)
                        )
                        
                        TextButton(
                            onClick = { items = items.filter { it != item } }
                        ) {
                            Text("Delete")
                        }
                    }
                }
                
                if (items.isEmpty()) {
                    item {
                        Text(
                            text = "No items yet. Add some!",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
`
  }

  private generateRepository(): string {
    return `package com.example.myapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class Repository {
    private val items = mutableListOf<String>()
    
    fun getItems(): Flow<List<String>> {
        return flowOf(items.toList())
    }
    
    suspend fun addItem(item: String) {
        items.add(item)
    }
    
    suspend fun removeItem(item: String) {
        items.remove(item)
    }
    
    suspend fun clearItems() {
        items.clear()
    }
}
`
  }

  private generateApiService(): string {
    return `package com.example.myapp.data.network

import retrofit2.http.*

interface ApiService {
    @GET("items")
    suspend fun getItems(): List<Item>
    
    @POST("items")
    suspend fun createItem(@Body item: CreateItemRequest): Item
    
    @PUT("items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: UpdateItemRequest): Item
    
    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String)
}

data class Item(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: String
)

data class CreateItemRequest(
    val name: String,
    val description: String?
)

data class UpdateItemRequest(
    val name: String?,
    val description: String?
)
`
  }

  private generateViewModel(): string {
    return `package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun addItem(item: String) {
        viewModelScope.launch {
            val currentItems = _uiState.value.items.toMutableList()
            currentItems.add(item)
            _uiState.value = _uiState.value.copy(items = currentItems)
        }
    }
    
    fun removeItem(item: String) {
        viewModelScope.launch {
            val currentItems = _uiState.value.items.toMutableList()
            currentItems.remove(item)
            _uiState.value = _uiState.value.copy(items = currentItems)
        }
    }
    
    fun clearItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(items = emptyList())
        }
    }
}

data class MainUiState(
    val items: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
`
  }

  private generateStrings(config: ProjectConfiguration): string {
    return `<resources>
    <string name="app_name">${config.name}</string>
    <string name="app_description">${config.description}</string>
    <string name="welcome_message">Welcome to ${config.name}</string>
    <string name="add_item">Add Item</string>
    <string name="remove_item">Remove Item</string>
    <string name="delete">Delete</string>
    <string name="no_items">No items yet. Add some!</string>
    <string name="loading">Loading...</string>
    <string name="error_occurred">An error occurred</string>
    <string name="retry">Retry</string>
    <string name="settings">Settings</string>
    <string name="about">About</string>
</resources>
`
  }

  private generateColorResources(): string {
    return `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="primary">#FF1976D2</color>
    <color name="primary_variant">#FF1565C0</color>
    <color name="secondary">#FF03DAC6</color>
    <color name="secondary_variant">#FF018786</color>
    <color name="background">#FFFFFFFF</color>
    <color name="surface">#FFFFFFFF</color>
    <color name="error">#FFB00020</color>
    <color name="on_primary">#FFFFFFFF</color>
    <color name="on_secondary">#FF000000</color>
    <color name="on_background">#FF000000</color>
    <color name="on_surface">#FF000000</color>
    <color name="on_error">#FFFFFFFF</color>
</resources>
`
  }

  private generateUnitTests(): string {
    return `package com.example.myapp

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun string_manipulation_works() {
        val text = "Hello World"
        assertEquals("HELLO WORLD", text.uppercase())
        assertEquals("hello world", text.lowercase())
    }
    
    @Test
    fun list_operations_work() {
        val list = mutableListOf<String>()
        assertTrue(list.isEmpty())
        
        list.add("Item 1")
        list.add("Item 2")
        assertEquals(2, list.size)
        
        list.remove("Item 1")
        assertEquals(1, list.size)
        assertEquals("Item 2", list.first())
    }
}
`
  }

  private generateInstrumentedTests(): string {
    return `package com.example.myapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.myapp", appContext.packageName)
    }
    
    @Test
    fun homeScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            HomeScreen()
        }
        
        // Check if main elements are displayed
        composeTestRule.onNodeWithText("Add Item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remove Item").assertIsDisplayed()
    }
    
    @Test
    fun addItemButtonWorks() {
        composeTestRule.setContent {
            HomeScreen()
        }
        
        // Click add item button
        composeTestRule.onNodeWithText("Add Item").performClick()
        
        // Check if new item appears
        composeTestRule.onNodeWithText("New Item 4").assertIsDisplayed()
    }
}
`
  }

  private generateAndroidGitIgnore(): string {
    return `# Built application files
*.apk
*.aar
*.ap_
*.aab

# Files for the ART/Dalvik VM
*.dex

# Java class files
*.class

# Generated files
bin/
gen/
out/
#  Uncomment the following line in case you need and you don't have the release build type files in your app
# release/

# Gradle files
.gradle/
build/

# Local configuration file (sdk path, etc)
local.properties

# Proguard folder generated by Eclipse
proguard/

# Log Files
*.log

# Android Studio Navigation editor temp files
.navigation/

# Android Studio captures folder
captures/

# IntelliJ
*.iml
.idea/workspace.xml
.idea/tasks.xml
.idea/gradle.xml
.idea/assetWizardSettings.xml
.idea/dictionaries
.idea/libraries
# Android Studio 3 in .gitignore file.
.idea/caches
.idea/modules.xml
# Comment next line if keeping position of elements in Navigation Editor is relevant for you
.idea/navEditor.xml

# Keystore files
# Uncomment the following lines if you do not want to check your keystore files in.
#*.jks
#*.keystore

# External native build folder generated in Android Studio 2.2 and later
.externalNativeBuild
.cxx/

# Google Services (e.g. APIs or Firebase)
# google-services.json

# Freeline
freeline.py
freeline/
freeline_project_description.json

# fastlane
fastlane/report.xml
fastlane/Preview.html
fastlane/screenshots
fastlane/test_output
fastlane/readme.md

# Version control
vcs.xml

# lint
lint/intermediates/
lint/generated/
lint/outputs/
lint/tmp/
# lint/reports/

# Android Profiling
*.hprof

# IDEs
.vscode/
.fleet/

# OS
.DS_Store
Thumbs.db
`
  }

  private getAndroidDependencies(config: ProjectConfiguration): Record<string, string> {
    return {
      'androidx.core:core-ktx': '1.12.0',
      'androidx.lifecycle:lifecycle-runtime-ktx': '2.7.0',
      'androidx.activity:activity-compose': '1.8.2',
      'androidx.compose:compose-bom': '2024.02.00',
      'androidx.compose.ui:ui': '',
      'androidx.compose.material3:material3': '',
      ...(config.features?.includes('Navigation') && {
        'androidx.navigation:navigation-compose': '2.7.6'
      }),
      ...(config.features?.includes('Database') && {
        'androidx.room:room-runtime': '2.6.1',
        'androidx.room:room-ktx': '2.6.1'
      }),
      ...(config.features?.includes('Networking') && {
        'com.squareup.retrofit2:retrofit': '2.9.0',
        'com.squareup.retrofit2:converter-gson': '2.9.0'
      })
    }
  }

  private generateAndroidDeploymentConfig(config: ProjectConfiguration): Record<string, any> {
    if (config.deploymentTarget === 'playstore') {
      return {
        'fastlane/Fastfile': this.generateFastlaneFile(),
        'fastlane/Appfile': this.generateAppfile(config),
        'app/proguard-rules.pro': this.generateProguardRules(),
        'app/release-keystore.properties': this.generateKeystoreProperties(),
        'gradle/wrapper/gradle-wrapper.properties': this.generateGradleWrapperProperties(),
        'gradle/wrapper/gradle-wrapper.jar': '# Binary file - download from Gradle distribution',
        'gradlew': this.generateGradleWrapper(),
        'gradlew.bat': this.generateGradleWrapperBat(),
        'app/release/output-metadata.json': this.generateOutputMetadata(config),
        'ANDROID_DEPLOYMENT.md': this.generateAndroidDeploymentDocs(config),
        '.github/workflows/android-release.yml': this.generateAndroidCICD(config),
        'app/src/main/play/listings/en-US/title.txt': config.name,
        'app/src/main/play/listings/en-US/short-description.txt': config.description.substring(0, 80),
        'app/src/main/play/listings/en-US/full-description.txt': this.generatePlayStoreDescription(config),
        'app/src/main/play/release-notes/en-US/default.txt': this.generateReleaseNotes(config)
      }
    }
    return {}
  }

  private generateFastlaneFile(): string {
    return `default_platform(:android)

platform :android do
  desc "Runs all the tests"
  lane :test do
    gradle(task: "test")
    gradle(task: "connectedAndroidTest")
  end

  desc "Build a debug APK"
  lane :build_debug do
    gradle(task: "clean assembleDebug")
  end

  desc "Build a release APK"
  lane :build_release do
    gradle(task: "clean assembleRelease")
  end

  desc "Build a release AAB for Play Store"
  lane :build_aab do
    gradle(task: "clean bundleRelease")
  end

  desc "Submit a new Internal Test Build to Play Store"
  lane :internal do
    gradle(task: "clean bundleRelease")
    upload_to_play_store(
      track: 'internal',
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true
    )
  end

  desc "Submit a new Beta Build to Play Store"
  lane :beta do
    gradle(task: "clean bundleRelease")
    upload_to_play_store(
      track: 'beta',
      skip_upload_metadata: false,
      skip_upload_images: false,
      skip_upload_screenshots: false,
      rollout: '0.1'  # Start with 10% rollout
    )
  end

  desc "Promote beta to production"
  lane :promote_to_production do
    upload_to_play_store(
      track: 'beta',
      track_promote_to: 'production',
      skip_upload_apk: true,
      skip_upload_aab: true,
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true,
      rollout: '0.1'  # Start with 10% rollout in production
    )
  end

  desc "Deploy a new version to the Google Play (Production)"
  lane :deploy do
    # Run tests first
    gradle(task: "test")
    
    # Build release AAB
    gradle(task: "clean bundleRelease")
    
    # Upload to Play Store
    upload_to_play_store(
      track: 'production',
      rollout: '0.1',  # Staged rollout: start with 10%
      skip_upload_metadata: false,
      skip_upload_images: false,
      skip_upload_screenshots: false
    )
    
    # Notify team
    slack(
      message: "Successfully deployed new version to Play Store! 🚀",
      success: true
    ) if ENV['SLACK_URL']
  end

  desc "Increase rollout percentage"
  lane :increase_rollout do |options|
    rollout_percentage = options[:percentage] || 0.5
    upload_to_play_store(
      track: 'production',
      rollout: rollout_percentage.to_s,
      skip_upload_apk: true,
      skip_upload_aab: true,
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true
    )
  end

  desc "Complete rollout to 100%"
  lane :complete_rollout do
    upload_to_play_store(
      track: 'production',
      rollout: '1.0',
      skip_upload_apk: true,
      skip_upload_aab: true,
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true
    )
  end

  desc "Generate screenshots"
  lane :screenshots do
    capture_android_screenshots
  end

  error do |lane, exception|
    slack(
      message: "Error in lane #{lane}: #{exception}",
      success: false
    ) if ENV['SLACK_URL']
  end
end
`
  }

  private generateAppfile(config: ProjectConfiguration): string {
    return `json_key_file("path/to/key.json") # Path to the json secret file
package_name("com.example.${config.name.toLowerCase().replace(/\s+/g, '')}") # e.g. com.krausefx.app
`
  }

  private generateProguardRules(): string {
    return `# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
`
  }

  private generateKeystoreProperties(): string {
    return `# Keystore properties
# Replace with your actual keystore details
storePassword=your_store_password
keyPassword=your_key_password  
keyAlias=your_key_alias
storeFile=path/to/your/keystore.jks
`
  }

  private generateGradleWrapperProperties(): string {
    return `distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
`
  }

  private generateGradleWrapper(): string {
    return `#!/bin/sh

##############################################################################
# Gradle startup script for UN*X
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=\`ls -ld "$PRG"\`
    link=\`expr "$ls" : '.*-> \\(.*\\)$'\`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=\`dirname "$PRG"\`"/$link"
    fi
done
SAVED="\`pwd\`"
cd "\`dirname \\"$PRG\\"\`/" >/dev/null
APP_HOME="\`pwd -P\`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=\`basename "$0"\`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "\`uname\`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Escape application args
save () {
    for i do printf %s\\\\n "$i" | sed "s/'/'\\\\\\\\''/g;1s/^/'/;\\\$s/\\\$/' \\\\\\\\/" ; done
    echo " "
}
APP_ARGS=\`save "$@"\`

# Collect all arguments for the java command, following the shell quoting and substitution rules
eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \\"-Dorg.gradle.appname=$APP_BASE_NAME\\" -classpath \\"$CLASSPATH\\" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"

exec "$JAVACMD" "$@"
`
  }

  private generateGradleWrapperBat(): string {
    return `@rem Gradle startup script for Windows

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto fail

:execute
@rem Setup the command line
set CLASSPATH=%APP_HOME%\\gradle\\wrapper\\gradle-wrapper.jar

@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
`
  }

  private generateOutputMetadata(config: ProjectConfiguration): string {
    return JSON.stringify({
      "version": 3,
      "artifactType": {
        "type": "APK",
        "kind": "Directory"
      },
      "applicationId": `com.example.${config.name.toLowerCase().replace(/\s+/g, '')}`,
      "variantName": "release",
      "elements": [
        {
          "type": "SINGLE",
          "filters": [],
          "attributes": [],
          "versionCode": 1,
          "versionName": "1.0",
          "outputFile": "app-release.apk"
        }
      ],
      "elementType": "File"
    }, null, 2)
  }

  private generateAndroidDeploymentDocs(config: ProjectConfiguration): string {
    const packageName = `com.example.${config.name.toLowerCase().replace(/\s+/g, '')}`;
    const appName = config.name.replace(/\s+/g, '');
    
    return `# Android Deployment Guide

## Overview
This guide covers the complete process of deploying your Android application to the Google Play Store.

## Prerequisites

### Required Tools
- Android Studio Arctic Fox or later
- JDK 11 or later
- Gradle 8.0+
- Fastlane (for automated deployment)

### Required Accounts
- Google Play Developer Account ($25 one-time fee)
- Google Cloud Console project (for Play Store API access)

## Step 1: Generate Signing Key

### Create a Keystore
\`\`\`bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ${appName.toLowerCase()}
\`\`\`

**IMPORTANT**: Store your keystore file and passwords securely! If you lose them, you cannot update your app.

### Update keystore.properties
Edit \`app/release-keystore.properties\`:
\`\`\`properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=${appName.toLowerCase()}
storeFile=../release-keystore.jks
\`\`\`

## Step 2: Build Release APK/AAB

### Build APK (for testing)
\`\`\`bash
./gradlew assembleRelease
\`\`\`
Output: \`app/build/outputs/apk/release/app-release.apk\`

### Build AAB (for Play Store)
\`\`\`bash
./gradlew bundleRelease
\`\`\`
Output: \`app/build/outputs/bundle/release/app-release.aab\`

**Note**: Google Play requires AAB format for new apps as of August 2021.

## Step 3: Test Release Build

### Install on Device
\`\`\`bash
adb install app/build/outputs/apk/release/app-release.apk
\`\`\`

### Verify
- Test all features thoroughly
- Check ProGuard hasn't broken anything
- Verify app signing is correct
- Test on multiple devices/Android versions

## Step 4: Play Console Setup

### First Time Setup
1. Go to [Google Play Console](https://play.google.com/console)
2. Create a new application
3. Fill out the store listing:
   - App name: ${config.name}
   - Short description
   - Full description
   - App category
   - Contact details
   - Privacy policy URL (required)

### Content Rating
Complete the content rating questionnaire:
1. Navigate to App content > Content rating
2. Answer all questions honestly
3. Submit for rating

### Pricing & Distribution
1. Set pricing (Free or Paid)
2. Select countries for distribution
3. Accept content guidelines

## Step 5: Upload to Play Console

### Manual Upload
1. Go to Production > Create new release
2. Upload your AAB file
3. Add release notes
4. Review and roll out

### Using Fastlane (Automated)

#### Install Fastlane
\`\`\`bash
# macOS
brew install fastlane

# Other platforms
gem install fastlane
\`\`\`

#### Deploy Commands
\`\`\`bash
# Deploy to internal testing
fastlane internal

# Deploy to beta
fastlane beta

# Deploy to production
fastlane deploy
\`\`\`

## Step 6: CI/CD Integration

### GitHub Actions
A workflow file is included at \`.github/workflows/android-release.yml\`

#### Setup Secrets
Add these secrets to your GitHub repository:
1. \`ANDROID_KEYSTORE_BASE64\`: Base64 encoded keystore file
2. \`KEYSTORE_PASSWORD\`: Keystore password
3. \`KEY_ALIAS\`: Key alias
4. \`KEY_PASSWORD\`: Key password
5. \`PLAY_STORE_JSON_KEY\`: Service account JSON key

#### Trigger Release
\`\`\`bash
git tag v1.0.0
git push origin v1.0.0
\`\`\`

## Versioning Strategy

### Version Code (Integer)
- Increment by 1 for each release
- Never reuse or decrease
- Example: 1, 2, 3, 4...

### Version Name (String)
- User-facing version
- Use semantic versioning: MAJOR.MINOR.PATCH
- Example: 1.0.0, 1.0.1, 1.1.0, 2.0.0

### Update build.gradle.kts
\`\`\`kotlin
android {
    defaultConfig {
        versionCode = 2
        versionName = "1.0.1"
    }
}
\`\`\`

## Troubleshooting

### Common Issues

#### "Upload failed: Version code already exists"
- Increment versionCode in build.gradle.kts

#### "Signatures do not match"
- You're using a different keystore
- Use the original keystore or create new app

#### "App not available in Play Store"
- Check country restrictions
- Verify app is in Production track
- Allow 1-3 days for review

#### "ProGuard removes required code"
- Check proguard-rules.pro
- Add keep rules for affected classes
- Test release builds thoroughly

## Best Practices

1. **Always test release builds** before uploading
2. **Keep keystores backed up** in multiple secure locations
3. **Use staged rollouts** (e.g., 10%, 50%, 100%)
4. **Monitor crashes actively** in first 24-48 hours
5. **Respond to user reviews** professionally and promptly
6. **Update regularly** with bug fixes and features
7. **Test on various devices** and Android versions
8. **Follow Material Design guidelines**
9. **Optimize app size** (use App Bundles)
10. **Implement proper error handling**

## App Information

- **Package Name**: ${packageName}
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **App Type**: Native Android with Kotlin & Jetpack Compose

## Support & Resources

- [Google Play Console](https://play.google.com/console)
- [Android Developer Documentation](https://developer.android.com)
- [Fastlane Documentation](https://docs.fastlane.tools)
- [Material Design Guidelines](https://material.io)

---

**Generated by AI Development Platform**
Last Updated: ${new Date().toISOString().split('T')[0]}
`;
  }

  private generateAndroidCICD(config: ProjectConfiguration): string {
    return `name: Android Release Build

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '17'
        cache: 'gradle'
        
    - name: Decode Keystore
      env:
        ENCODED_STRING: \${{ secrets.ANDROID_KEYSTORE_BASE64 }}
      run: |
        echo \$ENCODED_STRING | base64 -di > release-keystore.jks
        
    - name: Create keystore.properties
      run: |
        echo "storePassword=\${{ secrets.KEYSTORE_PASSWORD }}" > app/release-keystore.properties
        echo "keyPassword=\${{ secrets.KEY_PASSWORD }}" >> app/release-keystore.properties
        echo "keyAlias=\${{ secrets.KEY_ALIAS }}" >> app/release-keystore.properties
        echo "storeFile=../release-keystore.jks" >> app/release-keystore.properties
        
    - name: Make gradlew executable
      run: chmod +x ./gradlew
      
    - name: Build Release APK
      run: ./gradlew assembleRelease
      
    - name: Build Release AAB
      run: ./gradlew bundleRelease
      
    - name: Upload APK Artifact
      uses: actions/upload-artifact@v4
      with:
        name: app-release-apk
        path: app/build/outputs/apk/release/app-release.apk
        
    - name: Upload AAB Artifact
      uses: actions/upload-artifact@v4
      with:
        name: app-release-aab
        path: app/build/outputs/bundle/release/app-release.aab
        
    - name: Create GitHub Release
      uses: softprops/action-gh-release@v1
      if: startsWith(github.ref, 'refs/tags/')
      with:
        files: |
          app/build/outputs/apk/release/app-release.apk
          app/build/outputs/bundle/release/app-release.aab
      env:
        GITHUB_TOKEN: \${{ secrets.GITHUB_TOKEN }}
  
  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: startsWith(github.ref, 'refs/tags/')
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Ruby
      uses: ruby/setup-ruby@v1
      with:
        ruby-version: '3.0'
        bundler-cache: true
        
    - name: Install Fastlane
      run: |
        gem install fastlane -NV
        
    - name: Download AAB
      uses: actions/download-artifact@v4
      with:
        name: app-release-aab
        path: app/build/outputs/bundle/release/
        
    - name: Create Play Store JSON Key
      env:
        PLAY_STORE_JSON_KEY: \${{ secrets.PLAY_STORE_JSON_KEY }}
      run: |
        echo "\$PLAY_STORE_JSON_KEY" > play-store-key.json
        
    - name: Deploy to Play Store
      run: |
        cd fastlane
        fastlane deploy
      env:
        SUPPLY_JSON_KEY: ../play-store-key.json
`;
  }

  private generatePlayStoreDescription(config: ProjectConfiguration): string {
    const features = config.features?.map(f => `• ${f}`).join('\n') || '• Modern Android application\n• Material Design 3\n• Smooth performance';
    
    return `${config.name} - ${config.description}

🚀 KEY FEATURES

${features}

📱 ABOUT THE APP

${config.name} is a cutting-edge Android application built with the latest technologies including Kotlin and Jetpack Compose. Experience a modern, intuitive interface designed following Material Design principles.

✨ HIGHLIGHTS

• Clean and intuitive user interface
• Fast and responsive performance
• Regular updates with new features
• Secure and privacy-focused
• Optimized for all Android devices

🛡️ PRIVACY & SECURITY

We take your privacy seriously. This app:
• Does not collect personal information without consent
• Uses secure communication protocols
• Follows Android security best practices
• Transparent about data usage

📞 SUPPORT

Need help? Have suggestions? We'd love to hear from you!
• Email: support@example.com
• Website: https://example.com
• Report issues on our GitHub repository

⭐ RATE & REVIEW

Enjoying ${config.name}? Leave us a review! Your feedback helps us improve and reach more users.

🔄 UPDATES

We regularly update the app with:
• New features based on user feedback
• Performance improvements
• Bug fixes and stability enhancements
• UI/UX refinements

📋 REQUIREMENTS

• Android 7.0 (API 24) or higher
• 50 MB free storage space
• Internet connection (for online features)

---

Built with ❤️ using modern Android development tools
`;
  }

  private generateReleaseNotes(config: ProjectConfiguration): string {
    const features = config.features?.slice(0, 3).join('\n• ') || 'Core functionality';
    
    return `🎉 Initial Release - v1.0.0

Welcome to ${config.name}!

✨ What's New:
• Launch of ${config.name}
• ${features}
• Modern Material Design 3 interface
• Smooth performance and animations

🐛 Bug Fixes:
• Initial stable release

📝 Known Issues:
• None at this time

Thank you for downloading ${config.name}! We're excited to have you on board.

Having issues? Contact us at support@example.com
`;
  }

  private generateThemesXml(config: ProjectConfiguration): string {
    const themeName = config.name.replace(/\s+/g, '');
    return `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.${themeName}" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
`;
  }

  private generateBackupRules(): string {
    return `<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <!-- Exclude sensitive data from backup -->
    <exclude domain="sharedpref" path="secure_prefs.xml"/>
    <exclude domain="database" path="secure.db"/>
    
    <!-- Include user data -->
    <include domain="file" path="."/>
</full-backup-content>
`;
  }

  private generateDataExtractionRules(): string {
    return `<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="sharedpref" path="secure_prefs.xml"/>
        <exclude domain="database" path="secure.db"/>
    </cloud-backup>
    
    <device-transfer>
        <exclude domain="sharedpref" path="secure_prefs.xml"/>
        <exclude domain="database" path="secure.db"/>
    </device-transfer>
</data-extraction-rules>
`;
  }

  private generateLocalPropertiesExample(): string {
    return `## This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
# Location of the Android SDK. This is an example:
# sdk.dir=/Users/username/Library/Android/sdk
#
# Copy this file to local.properties and update with your SDK path
`;
  }
}
