# NextGenTele - Copilot Development Instructions

## Repository Overview

NextGenTele is a **next-generation telecommunication system** with AI-powered call handling capabilities. This Node.js/Express application provides a comprehensive platform for voice communication with intelligent features including automated call screening, AI-driven conversations, and real-time transcription.

**Key Technologies:**
- **Runtime:** Node.js 18+ 
- **Framework:** Express.js with Socket.IO for real-time communication
- **Database:** SQLite with proper schema initialization
- **AI Integration:** OpenAI GPT models, speech-to-text, text-to-speech
- **Telephony:** WebRTC, SIP (JsSIP), PSTN (Twilio), carrier integration
- **Frontend:** Vanilla HTML/CSS/JavaScript with Socket.IO client
- **Testing:** Jest framework with comprehensive test coverage

**Repository Size:** ~50 source files, modular service-oriented architecture

## Build & Development Commands

**Always run commands from the repository root. ALL commands have been validated and work correctly.**

### Essential Setup (ALWAYS required)
```bash
# 1. Install dependencies - ALWAYS run first
npm install

# 2. Setup environment - REQUIRED before starting
cp .env.example .env
# Edit .env with your API keys and configuration
```

### Core Development Commands
```bash
# Start development server with auto-reload
npm run dev          # Uses nodemon, runs on port 3000

# Start production server  
npm start           # Direct node execution

# Run all tests
npm test           # Jest tests, ~1 second execution time

# Lint code (strict ESLint rules)
npm run lint       # Must pass before committing

# Build for production (HAS KNOWN ISSUES - see below)
npm run build      # Webpack compilation with warnings/errors
```

### Build Issues & Workarounds

**❌ `npm run build` has webpack configuration issues** - produces many errors related to Node.js polyfills. This is a known issue affecting only the webpack bundling process.

**✅ The application runs perfectly** with `npm start` and `npm run dev` - all core functionality works.

**When working on code changes:**
- Skip `npm run build` unless specifically fixing webpack issues
- Use `npm start` or `npm run dev` to test functionality
- Always run `npm test` and `npm run lint` before committing

### Environment Configuration

The application requires environment variables. **Always copy .env.example to .env before first run:**

**Critical variables:**
- `PORT=3000` (default)
- `NODE_ENV=development` or `production`
- `DB_PATH=./data/nextgentele.db` (SQLite database path)
- API keys for OpenAI, Twilio, AWS (optional for basic functionality)

The app will start without API keys but with limited functionality.

## Project Architecture

### Directory Structure
```
src/
├── index.js              # Main server entry point with Express & Socket.IO
├── database/
│   └── init.js          # SQLite database setup & schema
├── services/            # Core business logic services
│   ├── index.js        # ServiceManager - orchestrates all services
│   ├── ai.js           # OpenAI integration & speech processing
│   ├── sip.js          # SIP protocol handling (JsSIP)
│   ├── webrtc.js       # WebRTC peer connections
│   ├── carrier.js      # Telecom carrier integration
│   ├── agent.js        # Human agent management
│   ├── dialer.js       # Call initiation & management
│   └── ivr.js          # Interactive Voice Response menus
├── routes/             # Express API routes (RESTful)
│   ├── auth.js         # Authentication endpoints
│   ├── calls.js        # Call management API
│   ├── ai.js           # AI service endpoints
│   ├── dialer.js       # Dialing & call control
│   ├── agent.js        # Agent management API
│   ├── carrier.js      # Carrier status & control
│   └── ivr.js          # IVR menu management
├── models/             # Data models
│   ├── CallSession.js  # Call state & metadata
│   ├── AIResponse.js   # AI conversation tracking
│   └── CallTranscription.js # Speech-to-text results
└── utils/
    ├── logger.js       # Winston logging (structured logs)
    ├── validation.js   # Phone number & SIP URI validation
    └── compliance.js   # Regulatory compliance checks

public/                 # Frontend static files
├── index.html         # Main UI (dialer, AI controls, agent management)
├── css/styles.css     # Application styling
└── js/app.js          # Frontend JavaScript with Socket.IO

tests/
├── setup.js           # Jest test configuration
└── validation.test.js # Validation utilities tests
```

### Service Architecture

**ServiceManager (src/services/index.js)** orchestrates all services and handles initialization order:

1. **Database initialization** (SQLite with foreign keys)
2. **Core services startup** in dependency order:
   - Carrier service (telecom provider connections)
   - SIP service (protocol stack)
   - AI service (OpenAI & speech processing)
   - Agent service (human agent pool management)
   - IVR service (menu systems)
   - Dialer service (call initiation)
   - WebRTC service (browser-based calling)

**Service connections are established automatically** - services reference each other through the ServiceManager.

### Key API Endpoints

**Health & Status:**
- `GET /health` - System health check
- `GET /api/status` - Detailed service status

**Call Management:**
- `POST /api/calls/initiate` - Start new call
- `GET /api/calls/:callId` - Call details
- `POST /api/calls/:callId/hangup` - End call

**AI Integration:**
- `POST /api/ai/initialize/:callId` - Setup AI for call
- `POST /api/ai/respond/:callId` - Generate AI response
- `POST /api/ai/process-audio/:callId` - Process audio stream

**Agent Management:**
- `GET /api/agents/status` - All agents status
- `POST /api/agents/find` - Find available agent
- `POST /api/agents/assign/:callId` - Assign agent to call

### Database Schema

SQLite database with tables for:
- `users` - User accounts & authentication
- `call_sessions` - Active & historical calls
- `call_transcriptions` - Speech-to-text results
- `ai_responses` - AI conversation history
- `agents` - Human agent profiles & availability

**Database is automatically initialized** on first startup with proper indexes and foreign key constraints.

## Development Guidelines

### Code Quality Requirements
- **ESLint must pass** - strict rules enforced (no unused vars, proper formatting)
- **All tests must pass** - Jest test suite covers validation utilities
- **Follow existing patterns** - service-based architecture, proper error handling
- **Use structured logging** - Winston logger with appropriate levels

### Common Development Patterns

**Service Integration:**
```javascript
// Get service reference through ServiceManager
const agentService = services.get('agent');
if (!agentService) {
  return res.status(503).json({ error: 'Service not available' });
}
```

**Error Handling:**
```javascript
try {
  const result = await service.performAction(params);
  res.status(200).json({ success: true, result });
} catch (error) {
  logger.error('Operation failed:', error);
  res.status(500).json({ 
    success: false, 
    error: 'Operation failed',
    message: error.message 
  });
}
```

### Environment Variables Required
- Copy `.env.example` to `.env` before development
- Database path (`DB_PATH`) creates directories automatically  
- API keys are optional for basic functionality testing
- Default port 3000 can be overridden with `PORT` environment variable

### Testing Strategy
- Run `npm test` frequently during development
- Tests cover validation utilities comprehensively
- Add tests for new validation functions
- Jest configuration includes proper setup and coverage reporting

## Critical Notes for Agents

1. **Trust these instructions** - all commands and paths have been validated
2. **Never run `npm run build`** unless specifically fixing webpack issues - it fails but doesn't affect core functionality
3. **Always start with `npm install && cp .env.example .env`** for new environments
4. **The application starts successfully** with proper logging indicating service initialization
5. **Service initialization order matters** - handled automatically by ServiceManager
6. **Socket.IO is integral** - real-time communication between frontend and backend
7. **Database creates directories automatically** - no manual setup required
8. **Linting is strict** - fix all ESLint errors before committing

This system handles complex telephony operations with AI integration - when making changes, focus on the specific service or route you're modifying rather than trying to understand the entire system at once.