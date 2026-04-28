# Repository Cleanup Plan

## Overview
After successfully transferring the NextGenBuildPro content to the correct `ngbp` repository, this repository should be reset to its intended purpose as a basic Spark template.

## Current Repository State
This repository currently contains a full AI development platform (127 files) that belongs in the `ngbp` repository.

## Target Repository State
This should be a minimal Spark template with basic scaffolding for new projects.

## Files to Remove After Transfer

### Remove Entire Directories
```bash
# Remove AI platform specific components
rm -rf src/components/AIAssistant.tsx
rm -rf src/components/AgenticCodeEditor.tsx
rm -rf src/components/AndroidEmulator.tsx
rm -rf src/components/AndroidPreview.tsx
rm -rf src/components/AndroidTemplateSelector.tsx
rm -rf src/components/BackendIntegration.tsx
rm -rf src/components/CompleteSystemValidator.tsx
rm -rf src/components/ComponentImportValidator.tsx
rm -rf src/components/ComprehensiveErrorDetector.tsx
rm -rf src/components/ComprehensiveSystemTestSummary.tsx
rm -rf src/components/CriticalComponentValidator.tsx
rm -rf src/components/DeploymentPipeline.tsx
rm -rf src/components/EndpointValidator.tsx
rm -rf src/components/ExpressTemplateSelector.tsx
rm -rf src/components/FileRewriteManager.tsx
rm -rf src/components/FileStructureGenerator.tsx
rm -rf src/components/FinalSystemValidation.tsx
rm -rf src/components/FullSystemTest.tsx
rm -rf src/components/IVRSystem.tsx
rm -rf src/components/LifecycleAnalysis.tsx
rm -rf src/components/LivePreview.tsx
rm -rf src/components/MasterSystemHealthDashboard.tsx
rm -rf src/components/MicroservicesArchitect.tsx
rm -rf src/components/NavigationFlowAnalyzer.tsx
rm -rf src/components/NavigationFlowTracker.tsx
rm -rf src/components/PlatformTester.tsx
rm -rf src/components/ProductionCodeGenerator.tsx
rm -rf src/components/ProjectManager.tsx
rm -rf src/components/SchemaCodeGenerator.tsx
rm -rf src/components/SchemaCodeGeneratorSimple.tsx
rm -rf src/components/ServiceMeshPolicies.tsx
rm -rf src/components/SystemStatusDashboard.tsx
rm -rf src/components/SystemValidator.tsx
rm -rf src/components/TemplateEditor.tsx
rm -rf src/components/TemplateSelector.tsx
rm -rf src/components/TestingSuite.tsx
rm -rf src/components/UltimatePlatformTestRunner.tsx
rm -rf src/components/VisualBuilder.tsx
rm -rf src/components/VisualBuilderWorkspace.tsx
rm -rf src/components/VisualComponentBuilder.tsx

# Remove all services
rm -rf src/services/

# Remove all utilities except basic ones
rm -rf src/utils/

# Remove templates
rm -rf src/templates/

# Remove documentation directories
rm -rf src/docs/

# Remove custom styles
rm -rf src/styles/
```

### Remove Individual Files
```bash
# Remove AI platform specific documentation
rm PRD.md
rm NAVIGATION_LIFECYCLE_COMPLETION.md
rm SYSTEM_TEST_SUMMARY.md
rm SECURITY.md
rm src/prd.md
rm validate-platform.js

# Remove AI platform specific hooks
rm src/hooks/useRealTimePreview.ts

# Remove AI platform specific type definitions
rm src/vite-end.d.ts
```

### Files to Keep (Basic Spark Template)
- `package.json` (updated with basic dependencies)
- `tsconfig.json`
- `vite.config.ts`
- `tailwind.config.js`
- `components.json`
- `theme.json`
- `index.html`
- `LICENSE`
- `.gitignore`
- `src/main.tsx` (simplified)
- `src/App.tsx` (basic template)
- `src/index.css` (basic styles)
- `src/components/ui/` (keep UI components for template use)
- `src/lib/utils.ts`
- `src/hooks/use-mobile.ts`

### Update Remaining Files

#### package.json Updates
```json
{
    "name": "spark-template",
    "version": "1.0.0",
    "description": "A minimal Spark development template",
    "dependencies": {
        "@github/spark": "^0.39.0",
        "react": "^19.1.1",
        "react-dom": "^19.0.0",
        "@radix-ui/react-slot": "^1.1.2",
        "class-variance-authority": "^0.7.1",
        "clsx": "^2.1.1",
        "tailwind-merge": "^3.3.1"
    }
}
```

#### src/App.tsx (Simplified)
```tsx
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
```

#### README.md (New)
```markdown
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
- `public/` - Static assets

## License

MIT License - see LICENSE file for details
```

## Cleanup Script
Create a cleanup script that can be run after successful transfer:

```bash
#!/bin/bash
# cleanup_repository.sh - Run after successful transfer to ngbp

echo "Cleaning up repository to basic Spark template..."

# Remove AI platform components
rm -rf src/components/AIAssistant.tsx
rm -rf src/components/Agentic*
rm -rf src/components/Android*
rm -rf src/components/Backend*
rm -rf src/components/Complete*
rm -rf src/components/Component*
rm -rf src/components/Comprehensive*
rm -rf src/components/Critical*
rm -rf src/components/Deployment*
rm -rf src/components/Endpoint*
rm -rf src/components/Express*
rm -rf src/components/File*
rm -rf src/components/Final*
rm -rf src/components/Full*
rm -rf src/components/IVR*
rm -rf src/components/Lifecycle*
rm -rf src/components/Live*
rm -rf src/components/Master*
rm -rf src/components/Microservices*
rm -rf src/components/Navigation*
rm -rf src/components/Platform*
rm -rf src/components/Production*
rm -rf src/components/Project*
rm -rf src/components/Schema*
rm -rf src/components/Service*
rm -rf src/components/System*
rm -rf src/components/Template*
rm -rf src/components/Testing*
rm -rf src/components/Ultimate*
rm -rf src/components/Visual*

# Remove services, utils, templates, docs, styles
rm -rf src/services/
rm -rf src/utils/
rm -rf src/templates/
rm -rf src/docs/
rm -rf src/styles/

# Remove documentation
rm -f PRD.md NAVIGATION_LIFECYCLE_COMPLETION.md SYSTEM_TEST_SUMMARY.md SECURITY.md
rm -f src/prd.md validate-platform.js src/vite-end.d.ts

# Remove specific hooks
rm -f src/hooks/useRealTimePreview.ts

echo "Repository cleaned up to basic Spark template"
echo "Remember to:"
echo "1. Update package.json with basic dependencies"
echo "2. Simplify src/App.tsx"
echo "3. Create a new README.md"
echo "4. Test the basic template functionality"
```

## Verification Steps
After cleanup:
1. Run `npm install` to ensure dependencies are correct
2. Run `npm run dev` to verify the basic template works
3. Verify only essential files remain
4. Update documentation to reflect template purpose
5. Test that the template can be used as a starting point for new projects