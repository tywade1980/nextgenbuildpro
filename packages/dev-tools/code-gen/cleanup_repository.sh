#!/bin/bash

# Cleanup script to reset repository to basic Spark template
# Run this AFTER successfully transferring files to the ngbp repository

echo "=== Repository Cleanup Script ==="
echo "This will reset the repository to a basic Spark template"
echo "ONLY run this after confirming successful transfer to ngbp repository"
echo ""

read -p "Are you sure you want to proceed with cleanup? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "Cleanup cancelled"
    exit 0
fi

echo "Starting cleanup..."

# Remove AI platform specific components
echo "Removing AI platform components..."
rm -f src/components/AIAssistant.tsx
rm -f src/components/APIDocumentation.tsx
rm -f src/components/AgenticCodeEditor.tsx
rm -f src/components/AndroidEmulator.tsx
rm -f src/components/AndroidPreview.tsx
rm -f src/components/AndroidTemplateSelector.tsx
rm -f src/components/BackendIntegration.tsx
rm -f src/components/CompleteSystemValidator.tsx
rm -f src/components/ComponentImportValidator.tsx
rm -f src/components/ComprehensiveErrorDetector.tsx
rm -f src/components/ComprehensiveSystemTestSummary.tsx
rm -f src/components/CriticalComponentValidator.tsx
rm -f src/components/DeploymentPipeline.tsx
rm -f src/components/EndpointValidator.tsx
rm -f src/components/ExpressTemplateSelector.tsx
rm -f src/components/FileRewriteManager.tsx
rm -f src/components/FileStructureGenerator.tsx
rm -f src/components/FinalSystemValidation.tsx
rm -f src/components/FullSystemTest.tsx
rm -f src/components/IVRSystem.tsx
rm -f src/components/LifecycleAnalysis.tsx
rm -f src/components/LivePreview.tsx
rm -f src/components/MasterSystemHealthDashboard.tsx
rm -f src/components/MicroservicesArchitect.tsx
rm -f src/components/NavigationFlowAnalyzer.tsx
rm -f src/components/NavigationFlowTracker.tsx
rm -f src/components/PlatformTester.tsx
rm -f src/components/ProductionCodeGenerator.tsx
rm -f src/components/ProjectManager.tsx
rm -f src/components/SchemaCodeGenerator.tsx
rm -f src/components/SchemaCodeGeneratorSimple.tsx
rm -f src/components/ServiceMeshPolicies.tsx
rm -f src/components/SystemStatusDashboard.tsx
rm -f src/components/SystemValidator.tsx
rm -f src/components/TemplateEditor.tsx
rm -f src/components/TemplateSelector.tsx
rm -f src/components/TestingSuite.tsx
rm -f src/components/UltimatePlatformTestRunner.tsx
rm -f src/components/VisualBuilder.tsx
rm -f src/components/VisualBuilderWorkspace.tsx
rm -f src/components/VisualComponentBuilder.tsx

# Remove entire service directories
echo "Removing services and utilities..."
rm -rf src/services/
rm -rf src/utils/
rm -rf src/templates/
rm -rf src/docs/
rm -rf src/styles/

# Remove AI platform documentation
echo "Removing AI platform documentation..."
rm -f PRD.md
rm -f NAVIGATION_LIFECYCLE_COMPLETION.md
rm -f SYSTEM_TEST_SUMMARY.md
rm -f SECURITY.md
rm -f src/prd.md
rm -f validate-platform.js
rm -f src/vite-end.d.ts

# Remove specific hooks
rm -f src/hooks/useRealTimePreview.ts

echo "Creating basic template files..."

# Create simplified App.tsx
cat > src/App.tsx << 'EOF'
import React from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

function App() {
  return (
    <div className="min-h-screen bg-background p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold text-center mb-8">
          Welcome to Your Spark Template
        </h1>
        <Card>
          <CardHeader>
            <CardTitle>Getting Started</CardTitle>
            <CardDescription>
              This is a minimal Spark template ready for development
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="mb-4">
              Start building your application by editing the components in the src directory.
            </p>
            <Button>Get Started</Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

export default App
EOF

# Create simplified main.tsx
cat > src/main.tsx << 'EOF'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
EOF

# Create basic README.md
cat > README.md << 'EOF'
# Spark Template

A minimal development template built with React, TypeScript, and Tailwind CSS using GitHub Spark.

## Features

- React 19 with TypeScript
- Tailwind CSS for styling
- Vite for fast development and building
- Pre-configured UI components from Radix UI
- Modern development tooling

## Getting Started

1. Clone this repository
2. Install dependencies: `npm install`
3. Start development server: `npm run dev`
4. Start building your application!

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Project Structure

- `src/` - Source code
  - `components/` - React components
    - `ui/` - Reusable UI components
  - `lib/` - Utility functions
  - `hooks/` - Custom React hooks

## License

MIT License - see LICENSE file for details
EOF

# Update package.json to basic template
echo "Updating package.json..."
cat > package.json << 'EOF'
{
    "name": "spark-template",
    "private": true,
    "version": "1.0.0",
    "description": "A minimal Spark development template",
    "type": "module",
    "scripts": {
        "dev": "vite",
        "build": "tsc -b --noCheck && vite build",
        "lint": "eslint .",
        "preview": "vite preview"
    },
    "dependencies": {
        "@github/spark": "^0.39.0",
        "@radix-ui/react-slot": "^1.1.2",
        "class-variance-authority": "^0.7.1",
        "clsx": "^2.1.1",
        "lucide-react": "^0.484.0",
        "react": "^19.1.1",
        "react-dom": "^19.0.0",
        "tailwind-merge": "^3.3.1"
    },
    "devDependencies": {
        "@eslint/js": "^9.21.0",
        "@tailwindcss/postcss": "^4.1.8",
        "@types/react": "^19.1.13",
        "@types/react-dom": "^19.0.4",
        "@vitejs/plugin-react": "^4.3.4",
        "eslint": "^9.28.0",
        "eslint-plugin-react-hooks": "^5.2.0",
        "eslint-plugin-react-refresh": "^0.4.19",
        "globals": "^16.0.0",
        "tailwindcss": "^4.1.11",
        "typescript": "~5.7.2",
        "typescript-eslint": "^8.38.0",
        "vite": "^6.3.5"
    }
}
EOF

echo ""
echo "=== Cleanup Complete ==="
echo "Repository has been reset to a basic Spark template"
echo ""
echo "Remaining structure:"
find . -type f -name "*.tsx" -o -name "*.ts" -o -name "*.json" -o -name "*.md" -o -name "*.js" -o -name "*.css" -o -name "*.html" | grep -v node_modules | sort

echo ""
echo "Next steps:"
echo "1. Run 'npm install' to install basic dependencies"
echo "2. Run 'npm run dev' to test the template"
echo "3. Verify everything works as expected"
echo "4. Commit the cleanup changes"
echo "5. Push the cleaned up template to the repository"
EOF