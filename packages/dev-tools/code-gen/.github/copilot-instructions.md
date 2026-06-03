# AI Development Platform - GitHub Copilot Instructions

**CRITICAL**: Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Overview

The AI Development Platform is a comprehensive React 19 + TypeScript application built with Vite that provides AI-powered full-stack development tools. It features 20+ integrated tabs including project management, AI assistance, live preview, visual builders, production code generation, microservices architecture, testing suites, and deployment pipelines.

## Working Effectively

### Bootstrap, build, and test the repository:
1. **Install dependencies**:
   ```bash
   npm install
   ```
   - Takes 45 seconds on fresh clone (measured)
   - Shows 0 vulnerabilities (secure dependencies)
   - **NEVER CANCEL** - Set timeout to 120+ seconds

2. **Build for production**:
   ```bash
   npm run build
   ```
   - Takes 15.451 seconds (measured)
   - **NEVER CANCEL** - Set timeout to 60+ seconds
   - Outputs to `dist/` directory
   - Bundle size: ~1.26MB JS, ~400KB CSS (acceptable for feature-rich app)
   - Shows expected icon proxy warnings (cosmetic only)

3. **Start development server**:
   ```bash
   npm run dev
   ```
   - Starts in 465ms (very fast)
   - Runs on http://localhost:5000/
   - Hot reload enabled
   - **NEVER CANCEL** - Server starts immediately

4. **Preview production build**:
   ```bash
   npm run preview
   ```
   - Starts production preview server on http://localhost:4173/
   - Use after `npm run build`
   - Starts instantly

5. **Dependency optimization (if needed)**:
   ```bash
   npm run optimize
   ```
   - Takes 565ms (very fast)
   - Pre-optimizes Vite dependencies
   - **DEPRECATED** but functional - use only if CSS errors occur

6. **Code quality**:
   ```bash
   npm run lint
   ```
   - **CURRENTLY BROKEN** - ESLint config needs migration to eslint.config.js format
   - Shows error: "ESLint couldn't find an eslint.config.(js|mjs|cjs) file"
   - DO NOT rely on lint command until this is fixed

7. **Type checking**:
   ```bash
   npx tsc --noEmit
   ```
   - **SHOWS 586 TypeScript ERRORS** (measured)
   - Primarily missing Phosphor icon exports and type mismatches
   - **APPLICATION STILL WORKS** - Vite ignores TS errors in development
   - Focus on functionality over strict type compliance

## Validation

### CRITICAL: Manual Testing Required
After making any changes, **ALWAYS** run through these complete user scenarios:

1. **Basic Application Flow**:
   ```bash
   npm run dev
   ```
   - Navigate to http://localhost:5000/
   - Verify main UI loads with "AI Development Platform" header
   - Check all 20+ tabs are visible and clickable
   - Verify "No projects yet" message displays initially

2. **Project Creation Workflow**:
   - Click "New Project" button
   - Fill in project details (name: "Test Project", description: "This is a test project")
   - Select project type (React App, Express Server, etc.)
   - Verify all wizard steps work (Steps 1-3)
   - Expected: KV storage 403 errors (normal in local development)

3. **Tab Navigation Validation**:
   - Click each available tab: Projects, Production, Templates, Schema, etc.
   - Verify tab content loads correctly
   - Disabled tabs (AI Assistant, Preview, etc.) should show as disabled
   - Production tab shows comprehensive configuration interface
   - Templates tab shows editor interface

4. **Build and Production Validation**:
   ```bash
   npm run build && npm run preview
   ```
   - Verify build completes without critical errors
   - Test production build on http://localhost:4173/
   - Ensure all features work in production mode

### Validation steps:
- Build succeeds without TypeScript compilation errors
- Development server starts without critical errors
- Main UI renders correctly with all tabs visible
- Project creation dialog works through all steps
- Tab navigation functions properly
- Application handles missing dependencies gracefully

## Common Tasks

### Repository structure:
```
.
├── README.md                    # Basic project info (minimal Spark template)
├── PRD.md                       # Product Requirements Document
├── SYSTEM_TEST_SUMMARY.md       # Comprehensive test documentation
├── package.json                 # Dependencies and scripts
├── vite.config.ts              # Vite configuration with Spark plugins
├── tsconfig.json               # TypeScript configuration
├── tailwind.config.js          # TailwindCSS v4 configuration
├── index.html                  # Main HTML entry point
├── src/
│   ├── main.tsx               # React application entry
│   ├── App.tsx                # Main application component (760+ lines)
│   ├── main.css               # Global CSS with theme variables
│   ├── components/            # React components (40+ components)
│   │   ├── ui/               # Radix UI component library
│   │   ├── ProjectManager.tsx # Project creation and management
│   │   ├── AIAssistant.tsx   # AI conversation interface
│   │   ├── ProductionCodeGenerator.tsx # Production code generation
│   │   ├── TemplateEditor.tsx # Template creation and editing
│   │   └── ...               # 35+ other feature components
│   ├── services/              # Business logic services
│   ├── utils/                 # Utility functions and generators
│   ├── templates/             # Framework and project templates
│   └── hooks/                 # Custom React hooks
├── .github/
│   ├── dependabot.yml         # Dependency updates
│   └── copilot-instructions.md # This file
└── dist/                      # Build output (generated)
```

### Key components and features:
- **ProjectManager**: Create and manage development projects with wizard interface
- **AIAssistant**: Natural language development support with persistent memory
- **ProductionCodeGenerator**: Generate production-ready code with comprehensive configuration
- **TemplateEditor**: Custom template creation and management
- **SchemaCodeGenerator**: Database-driven development
- **MicroservicesArchitect**: Distributed architecture design
- **TestingSuite**: Automated testing layers
- **DeploymentPipeline**: Production deployment workflows
- **AndroidPreview**: Mobile application emulation
- **VisualBuilder**: Drag-and-drop UI creation
- **LivePreview**: Real-time application preview

### Dependencies highlights:
- **Framework**: React 19.1.1 + TypeScript 5.7.2
- **Build**: Vite 6.3.5 + TailwindCSS 4.1.11
- **UI**: Radix UI components + Phosphor Icons + Lucide React
- **GitHub Integration**: @github/spark for platform features
- **State**: useKV hooks for persistent storage (shows 403 errors locally)
- **Forms**: React Hook Form + Zod validation
- **Animation**: Framer Motion
- **Data**: D3.js for visualizations

### Package.json scripts explained:
- `dev`: Start development server with Vite
- `build`: TypeScript compilation + Vite production build (with --noCheck flag)
- `lint`: ESLint (currently broken - missing eslint.config.js)
- `optimize`: Pre-optimize Vite dependencies (deprecated)
- `preview`: Preview production build locally
- `kill`: Kill process using port 5000

### Build and performance:
- Fresh npm install: 45 seconds (measured)
- Clean build time: 15.451 seconds (measured)
- Development server startup: 465ms (measured)
- Optimize command: 565ms (measured)
- Hot reload: <1 second for most changes
- Bundle size: 1,284.75 kB JS (compressed: 333.13 kB), 403.17 kB CSS (compressed: 73.87 kB)

## Known Issues and Workarounds

1. **ESLint Configuration**: 
   - `npm run lint` fails - needs migration from .eslintrc to eslint.config.js format
   - Use TypeScript compiler for syntax checking instead
   - Error: "ESLint couldn't find an eslint.config.(js|mjs|cjs) file"

2. **TypeScript Errors**:
   - 586 TypeScript errors in 50+ files (measured)
   - **APPLICATION STILL FUNCTIONS** - Vite ignores TS errors in development
   - Primary causes: Missing Phosphor icon exports, type compatibility issues
   - Focus on functionality over strict type compliance

3. **Icon Proxy Warnings**:
   - Build shows many "[icon-proxy] Proxying non-existent icon" warnings
   - Icons like "Cog", "AlertTriangle", "Zap", etc. fallback to "Question" icon
   - These are cosmetic and do not affect functionality
   - Normal behavior from the Spark icon proxy system

4. **KV Storage Errors**:
   - Features require GitHub Spark KV storage
   - Shows 403 Forbidden errors in local development
   - This is expected behavior and doesn't break functionality
   - Errors appear when creating projects or using AI features

5. **Bundle Size Warnings**:
   - Large bundle (~1.26MB) due to comprehensive feature set
   - Vite suggests code splitting and manual chunks
   - Consider optimization for production deployment

6. **CSS Variables**:
   - If "--spacing function requires --spacing theme variable" errors occur
   - Restart dev server after running `npm run optimize`

## Architecture Notes

### Application structure:
- Single-page React application with tab-based navigation
- 23 feature tabs: Projects, AI Assistant, Preview, Android, Visual Builder, Production, Templates, Construction, Schema, Microservices, Lifecycle, Navigation, Endpoints, Structure, Code, Agentic, Rewrite, Backend, API Docs, Test, Deploy, IVR System, System Check, Status
- Some tabs disabled until projects are created
- Persistent state management using GitHub Spark useKV hooks
- Error boundaries for component isolation
- Complex state management across 200+ features

### Development patterns:
- Component-based architecture with functional components
- Custom hooks for state management and effects
- Service layer for business logic separation
- Template system for project scaffolding
- Plugin architecture for extensibility
- AI integration throughout the platform

### Core Technologies:
- **React 19** - Latest React version with modern features
- **TypeScript 5.7.2** - Type safety (with known errors that don't affect functionality)
- **Vite 6** - Build tool and dev server with Spark plugins
- **Tailwind CSS 4** - Modern styling with custom theme system
- **@github/spark** - GitHub Spark framework integration for KV storage and AI features

## Making Changes

### Before Making Changes:
- Always run `npm install` first when working with a fresh clone
- Run `npm run dev` to ensure current state works
- Understand the tab-based architecture in `src/App.tsx`
- Check existing components in `src/components/` before creating new ones

### During Development:
- **DO NOT** rely on TypeScript compiler for error checking - 586 errors but app works
- Test changes immediately with hot reload in dev server
- Switch between tabs to ensure no breaking changes
- Focus on functionality over type-perfect code
- KV storage 403 errors are expected in local development

### After Making Changes:
- **ALWAYS** run complete build: `npm run build`
- **ALWAYS** test production build: `npm run preview`
- **ALWAYS** manually test the complete user workflow described above
- Take screenshots of UI changes to verify visual impact
- Test tab navigation and project creation workflow

### File Organization:
- Components go in `src/components/`
- Services and business logic in `src/services/`
- Utilities in `src/utils/`
- Templates in `src/templates/`
- Follow existing TypeScript patterns and naming conventions

## Troubleshooting

### Common Issues:
1. **Dev server won't start**: Check port 5000 is available, run `npm run kill` if needed
2. **Build fails**: Focus on runtime errors, not TypeScript errors (586 expected)
3. **Icons missing**: Known issue, icons fallback to "Question" automatically
4. **Lint errors**: ESLint is broken, ignore lint command entirely
5. **KV storage errors**: 403 errors are expected in local development
6. **CSS errors**: Restart dev server, run `npm run optimize` if needed

### Getting Help:
- Check existing documentation: `/README.md`, `/PRD.md`, `/SYSTEM_TEST_SUMMARY.md`
- Review component implementations in `src/components/`
- Use TypeScript compiler for syntax checking: `npx tsc --noEmit`
- Focus on functional testing over type compliance

## Critical Reminders

- **NEVER CANCEL** any npm commands - all operations complete within 60 seconds
- **ALWAYS** test core user flows after making changes
- **ALWAYS** run manual validation scenarios described above
- The application is complex with 200+ features - focus changes on specific components
- KV storage errors in local development are normal and don't break functionality
- Icon proxy warnings are normal behavior and don't indicate problems
- TypeScript errors (586) are expected - application works despite them
- Focus on functionality and user experience over strict type compliance
