# Construction AI - Next.js Business Management Solution

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Bootstrap and Dependencies
- Install dependencies: `npm install` -- takes ~31 seconds. NEVER CANCEL. Set timeout to 60+ minutes.
- Dependencies are current and working. npm audit shows 4 moderate vulnerabilities but these don't affect functionality.
- Node.js v20.19.5 and npm 10.8.2 are working versions.

### Database Setup
- Database: SQLite with Drizzle ORM (local.db file)
- Initialize database schema: `npm run db:push` -- takes ~2 seconds
- View database tables: `npm run db:studio` -- starts Drizzle Studio on https://local.drizzle.studio
- Seed database with sample data: `curl -X POST http://localhost:3000/api/init` (requires dev server running)
- Database tables: ai_models, building_codes, call_logs, clients, labor_rates, market_data, projects, tasks

### Build and Lint
- Lint code: `npm run lint` -- takes ~2 seconds. Linting passes cleanly with no errors or warnings.
- Build application: `npm run build` -- takes ~18 seconds with Turbopack. NEVER CANCEL. Set timeout to 60+ minutes.
- Linting passes without issues. Build succeeds every time.

### Development Server
- Start development: `npm run dev` -- starts in ~1 second using Turbopack
- Access application: http://localhost:3000
- Production server: `npm run start` (requires build first)

### CRITICAL Timeout Values
- **npm install**: Set 60+ minute timeout. NEVER CANCEL.
- **npm run build**: Set 60+ minute timeout. NEVER CANCEL. Actual time: ~18 seconds.
- **npm run lint**: Set 30+ minute timeout. NEVER CANCEL. Actual time: ~2 seconds.
- **npm run dev**: Starts in ~1 second, no timeout needed for startup.

## Validation Scenarios

### ALWAYS test these scenarios after making changes:
1. **Application loads successfully**: Navigate to http://localhost:3000 and verify "Welcome to Construction AI" appears
2. **Database connectivity**: Check that project stats (Active Projects: 12, Pending Calls: 3, etc.) display correctly
3. **Page navigation**: Test all three main pages work:
   - Dashboard (http://localhost:3000/dashboard) - shows project management interface
   - Call Screen (http://localhost:3000/call-screen) - shows call screening interface
   - AI Models (http://localhost:3000/ai-models) - shows AI model management
4. **Database initialization**: If database is empty, run `curl -X POST http://localhost:3000/api/init` to seed with sample data
5. **Build validation**: Always run `npm run build` before finalizing changes to ensure production build works

## Application Architecture

### Core Features
- **Project Management**: Construction project tracking, client management, cost estimation
- **Smart Call Screening**: AI-powered call routing and intent classification
- **AI Model Management**: OpenAI integration and local model management
- **Database**: Comprehensive schema for construction business (projects, clients, labor rates, building codes, etc.)

### Key Files and Directories
- `src/app/` - Next.js App Router pages and API routes
- `src/lib/schema.ts` - Drizzle database schema definitions
- `src/lib/db.ts` - Database connection and configuration
- `src/lib/ai-models.ts` - AI model management utilities
- `drizzle.config.ts` - Drizzle ORM configuration (created during setup)
- `local.db` - SQLite database file
- `package.json` - Dependencies and scripts

### Environment Requirements
- No .env files required for basic functionality
- OpenAI API key would be needed for full AI features (not required for development)
- All dependencies install and work without additional setup

## Common Tasks

### Adding New Features
- Use existing patterns in `src/app/` for new pages
- Follow the component structure in existing pages
- Database changes require schema updates in `src/lib/schema.ts` followed by `npm run db:push`

### Database Operations
- View data: `npm run db:studio` opens visual database browser
- Schema changes: Edit `src/lib/schema.ts` then run `npm run db:push`
- Sample data: API endpoint `/api/init` provides comprehensive sample construction data

### Debugging
- Check browser console for React/Next.js errors
- Server logs appear in terminal running `npm run dev`
- Database issues: Use `sqlite3 local.db` for direct database access

### Code Quality
- Always run `npm run lint` before committing changes
- Current warnings are acceptable and don't break functionality
- Build must succeed: `npm run build` before finalizing changes

## Technologies Used
- **Frontend**: Next.js 15, React 19, TypeScript, Tailwind CSS
- **Database**: SQLite with Drizzle ORM
- **AI**: OpenAI API integration, AI model management
- **Icons**: Lucide React, Radix UI icons
- **Styling**: Tailwind CSS with custom components
- **Build**: Turbopack for fast development and builds

## Troubleshooting

### Build Issues
- If build fails, check TypeScript errors in output
- Ensure all required files exist and imports are correct
- Database connection issues: verify `local.db` exists and `drizzle.config.ts` is present

### Runtime Issues
- Database empty: Run database initialization endpoint
- Pages not loading: Check for JavaScript errors in browser console
- AI features not working: Check if sample data includes AI model entries

## Repository Structure Overview
```
├── src/
│   ├── app/                    # Next.js pages and API routes
│   │   ├── ai-models/         # AI model management page
│   │   ├── call-screen/       # Call screening interface
│   │   ├── dashboard/         # Project management dashboard
│   │   └── api/               # API endpoints
│   ├── lib/                   # Utilities and configurations
│   └── data/                  # Sample data for initialization
├── public/                    # Static assets
├── drizzle.config.ts         # Database configuration
├── local.db                  # SQLite database
└── package.json              # Dependencies and scripts
```

The application is a fully functional construction business management solution with AI integration, designed for ease of development and rapid feature addition.