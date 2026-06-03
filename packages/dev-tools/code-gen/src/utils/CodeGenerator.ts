/**
 * Code Generator Utility
 * 
 * Generates project structures and files based on templates and configurations
 */

import { FrameworkTemplate } from '@/templates/framework-templates'

export interface GeneratedProjectStructure {
  files: Record<string, string>
  dependencies: string[]
}

export class CodeGenerator {
  /**
   * Generate project from template
   */
  static generateFromTemplate(
    template: FrameworkTemplate,
    projectName: string,
    description: string
  ): GeneratedProjectStructure {
    const files: Record<string, string> = {}
    
    // Copy template files
    Object.entries(template.files).forEach(([path, content]) => {
      files[path] = content
        .replace(/{{PROJECT_NAME}}/g, projectName)
        .replace(/{{DESCRIPTION}}/g, description)
    })
    
    // Generate package.json
    files['package.json'] = JSON.stringify({
      name: projectName.toLowerCase().replace(/\s+/g, '-'),
      version: '1.0.0',
      description,
      scripts: template.scripts,
      dependencies: template.dependencies,
      devDependencies: template.devDependencies
    }, null, 2)
    
    // Generate README
    files['README.md'] = this.generateREADME(projectName, description, template)
    
    // Generate gitignore
    files['.gitignore'] = this.generateGitIgnore(template.framework)
    
    const dependencies = [
      ...Object.keys(template.dependencies),
      ...Object.keys(template.devDependencies)
    ]
    
    return { files, dependencies }
  }
  
  /**
   * Generate blank project structure
   */
  static generateBlankProject(
    projectName: string,
    projectType: string,
    description: string
  ): GeneratedProjectStructure {
    const files: Record<string, string> = {}
    
    switch (projectType) {
      case 'react':
        return this.generateBlankReactProject(projectName, description)
      case 'node':
        return this.generateBlankNodeProject(projectName, description)
      case 'android':
        return this.generateBlankAndroidProject(projectName, description)
      default:
        files['README.md'] = `# ${projectName}\n\n${description}`
        return { files, dependencies: [] }
    }
  }
  
  private static generateBlankReactProject(name: string, description: string): GeneratedProjectStructure {
    const files: Record<string, string> = {
      'package.json': JSON.stringify({
        name: name.toLowerCase().replace(/\s+/g, '-'),
        version: '1.0.0',
        description,
        type: 'module',
        scripts: {
          dev: 'vite',
          build: 'tsc && vite build',
          preview: 'vite preview'
        },
        dependencies: {
          react: '^18.2.0',
          'react-dom': '^18.2.0'
        },
        devDependencies: {
          '@types/react': '^18.0.28',
          '@types/react-dom': '^18.0.11',
          '@vitejs/plugin-react': '^3.1.0',
          typescript: '^4.9.5',
          vite: '^4.1.0'
        }
      }, null, 2),
      'src/App.tsx': `import React from 'react'

function App() {
  return (
    <div className="App">
      <h1>${name}</h1>
      <p>${description}</p>
    </div>
  )
}

export default App`,
      'src/main.tsx': `import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)`,
      'src/index.css': `body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.App {
  text-align: center;
  padding: 2rem;
}`,
      'index.html': `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${name}</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>`,
      'vite.config.ts': `import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})`,
      'tsconfig.json': JSON.stringify({
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
          noFallthroughCasesInSwitch: true
        },
        include: ["src"],
        references: [{ path: "./tsconfig.node.json" }]
      }, null, 2),
      'README.md': this.generateREADME(name, description, null),
      '.gitignore': this.generateGitIgnore('react')
    }
    
    return {
      files,
      dependencies: ['react', 'react-dom', '@types/react', '@types/react-dom', '@vitejs/plugin-react', 'typescript', 'vite']
    }
  }
  
  private static generateBlankNodeProject(name: string, description: string): GeneratedProjectStructure {
    const files: Record<string, string> = {
      'package.json': JSON.stringify({
        name: name.toLowerCase().replace(/\s+/g, '-'),
        version: '1.0.0',
        description,
        main: 'dist/index.js',
        scripts: {
          dev: 'nodemon src/index.ts',
          build: 'tsc',
          start: 'node dist/index.js'
        },
        dependencies: {
          express: '^4.18.2'
        },
        devDependencies: {
          '@types/express': '^4.17.17',
          '@types/node': '^18.14.6',
          nodemon: '^2.0.20',
          'ts-node': '^10.9.1',
          typescript: '^4.9.5'
        }
      }, null, 2),
      'src/index.ts': `import express from 'express'

const app = express()
const PORT = process.env.PORT || 3000

app.use(express.json())

app.get('/', (req, res) => {
  res.json({ message: 'Hello from ${name}!' })
})

app.listen(PORT, () => {
  console.log(\`Server running on port \${PORT}\`)
})`,
      'tsconfig.json': JSON.stringify({
        compilerOptions: {
          target: "ES2020",
          module: "commonjs",
          outDir: "./dist",
          rootDir: "./src",
          strict: true,
          esModuleInterop: true,
          skipLibCheck: true,
          forceConsistentCasingInFileNames: true,
          resolveJsonModule: true
        },
        include: ["src/**/*"],
        exclude: ["node_modules", "dist"]
      }, null, 2),
      'README.md': this.generateREADME(name, description, null),
      '.gitignore': this.generateGitIgnore('node')
    }
    
    return {
      files,
      dependencies: ['express', '@types/express', '@types/node', 'nodemon', 'ts-node', 'typescript']
    }
  }
  
  private static generateBlankAndroidProject(name: string, description: string): GeneratedProjectStructure {
    const files: Record<string, string> = {
      'app/build.gradle.kts': `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.${name.toLowerCase().replace(/\s+/g, '')}"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.${name.toLowerCase().replace(/\s+/g, '')}"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.material3:material3:1.0.0")
}`,
      'app/src/main/AndroidManifest.xml': `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.App"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.App">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>`,
      'README.md': this.generateREADME(name, description, null),
      '.gitignore': this.generateGitIgnore('android')
    }
    
    return {
      files,
      dependencies: []
    }
  }
  
  private static generateREADME(name: string, description: string, template: FrameworkTemplate | null): string {
    return `# ${name}

${description}

## Getting Started

${template ? `This project was generated using the ${template.name} template.` : ''}

### Installation

\`\`\`bash
npm install
\`\`\`

### Development

\`\`\`bash
npm run dev
\`\`\`

### Building

\`\`\`bash
npm run build
\`\`\`

## Features

${template ? template.features.map(f => `- ${f}`).join('\n') : '- Basic project structure'}

## License

MIT`
  }
  
  private static generateGitIgnore(framework: string): string {
    const common = `# Dependencies
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Environment
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# Editor
.vscode/
.idea/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db`

    switch (framework) {
      case 'react':
      case 'nextjs':
        return `${common}

# Build outputs
dist/
build/
.next/
out/

# Vite
.vite/`

      case 'node':
      case 'express':
        return `${common}

# Build outputs
dist/
build/

# Logs
logs/
*.log`

      case 'android':
        return `# Android
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties

# Gradle
.gradle/
build/

# Android Studio
.idea/
*.iws
*.iml
*.ipr`

      default:
        return common
    }
  }
}