/**
 * NextGenTele - Next-generation telecommunication system
 * Main entry point for the application
 */

require('dotenv').config();
const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const helmet = require('helmet');
const path = require('path');

const logger = require('./utils/logger');
const { initDatabase } = require('./database/init');
const { ServiceManager } = require('./services');

// Import route handlers
const authRoutes = require('./routes/auth');
const dialerRoutes = require('./routes/dialer');
const callRoutes = require('./routes/calls');
const aiRoutes = require('./routes/ai');
const carrierRoutes = require('./routes/carrier');
const ivrRoutes = require('./routes/ivr');
const agentRoutes = require('./routes/agent');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: process.env.NODE_ENV === 'production' ? false : '*',
    methods: ['GET', 'POST']
  }
});

const PORT = process.env.PORT || 3000;

// Security middleware
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Static files
app.use(express.static(path.join(__dirname, '../public')));

// Global services instance
let services = null;

// Health check endpoint
app.get('/health', (req, res) => {
  const health = {
    status: 'healthy',
    timestamp: new Date().toISOString(),
    services: services ? Array.from(services.keys()) : [],
    uptime: process.uptime()
  };

  res.status(200).json(health);
});

// System status endpoint with enhanced carrier and agent information
app.get('/api/status', (req, res) => {
  if (!services) {
    return res.status(503).json({
      status: 'initializing',
      message: 'Services are still initializing'
    });
  }

  const carrierService = services.get('carrier');
  const agentService = services.get('agent');
  const ivrService = services.get('ivr');

  const status = {
    system: 'operational',
    timestamp: new Date().toISOString(),
    services: {
      carrier: carrierService ? carrierService.getCarrierStatus() : null,
      agents: agentService ? {
        total: agentService.getAllAgentsStatus().length,
        available: agentService.getAllAgentsStatus().filter(a => a.status === 'available').length,
        busy: agentService.getAllAgentsStatus().filter(a => a.status === 'busy').length
      } : null,
      ivr: ivrService ? {
        availableMenus: ivrService.getAllMenus().length,
        activeSessions: ivrService.ivrSessions.size
      } : null,
      activeServices: Array.from(services.keys())
    }
  };

  res.status(200).json(status);
});

// Initialize services
async function initializeApp() {
  try {
    logger.info('Initializing NextGenTele application...');

    // Initialize database
    await initDatabase();
    logger.info('Database initialized');

    // Initialize services using ServiceManager
    const serviceManager = new ServiceManager();
    services = await serviceManager.initialize();
    logger.info('All services initialized');

    // Initialize routes with service references
    carrierRoutes.initializeService(services);
    ivrRoutes.initializeService(services);
    agentRoutes.initializeService(services);

    // Setup WebRTC signaling
    const webrtcService = services.get('webrtc');
    if (webrtcService) {
      webrtcService.setupSignalingServer(io);
      logger.info('WebRTC signaling initialized');
    }

    // Setup routes
    const { router: dialerRouter, initializeRoutes: initDialerRoutes } = dialerRoutes;
    const { router: aiRouter, initializeRoutes: initAIRoutes } = aiRoutes;
    const { router: authRouter } = authRoutes;
    const callRouter = callRoutes;

    // Initialize route dependencies
    const dialerService = services.get('dialer');
    const aiService = services.get('ai');

    initDialerRoutes(dialerService, aiService);
    initAIRoutes(aiService);

    // Mount API routes
    app.use('/api/dialer', dialerRouter);
    app.use('/api/ai', aiRouter);
    app.use('/api/auth', authRouter);
    app.use('/api/calls', callRouter);
    app.use('/api/carrier', carrierRoutes.router);
    app.use('/api/ivr', ivrRoutes.router);
    app.use('/api/agents', agentRoutes.router);

    logger.info('API routes initialized');

    // Serve main application
    app.get('/', (req, res) => {
      res.sendFile(path.join(__dirname, '../public/index.html'));
    });

    // Error handling middleware
    app.use((err, req, res, _next) => {
      logger.error('Unhandled error:', err);
      res.status(500).json({
        success: false,
        error: 'Internal server error',
        message: process.env.NODE_ENV === 'development' ? err.message : 'Something went wrong'
      });
    });

    // 404 handler
    app.use((req, res) => {
      res.status(404).json({
        success: false,
        error: 'Not found',
        message: 'The requested resource was not found'
      });
    });

    // Start server
    server.listen(PORT, () => {
      logger.info(`NextGenTele server running on port ${PORT}`);
      logger.info(`Environment: ${process.env.NODE_ENV}`);
      logger.info('All services initialized successfully');
    });

  } catch (error) {
    logger.error('Failed to initialize application:', error);
    process.exit(1);
  }
}

// Socket.IO connection handling with enhanced features
io.on('connection', (socket) => {
  logger.info(`Client connected: ${socket.id}`);

  // Handle DTMF input for IVR
  socket.on('dtmf', (data) => {
    const ivrService = services?.get('ivr');
    if (ivrService && data.callId && data.digit) {
      ivrService.processDTMFInput(data.callId, data.digit);
    }
  });

  // Handle agent responses for training
  socket.on('agent_response', (data) => {
    const agentService = services?.get('agent');
    if (agentService && data.callId && data.response) {
      agentService.recordTrainingInteraction(data.callId, {
        type: 'agent_response',
        content: data.response,
        context: data.context,
        timestamp: new Date()
      });
    }
  });

  // Handle carrier fallback requests
  socket.on('carrier_fallback', (data) => {
    const carrierService = services?.get('carrier');
    if (carrierService && data.callId) {
      carrierService.triggerSmartFallback(data.callId, data.qualityData || {});
    }
  });

  // Handle guided mode requests
  socket.on('enable_guided_mode', async (data) => {
    const agentService = services?.get('agent');
    if (agentService && data.callId) {
      try {
        await agentService.enableGuidedMode(data.callId, data.options);
        socket.emit('guided_mode_enabled', { callId: data.callId });
      } catch (error) {
        socket.emit('error', { message: 'Failed to enable guided mode' });
      }
    }
  });

  socket.on('disconnect', () => {
    logger.info(`Client disconnected: ${socket.id}`);
  });
});

// Error handling
process.on('uncaughtException', (error) => {
  logger.error('Uncaught Exception:', error);
  process.exit(1);
});

process.on('unhandledRejection', (reason, promise) => {
  logger.error('Unhandled Rejection at:', promise, 'reason:', reason);
  process.exit(1);
});

// Graceful shutdown
process.on('SIGTERM', async () => {
  logger.info('SIGTERM received, shutting down gracefully');

  if (services) {
    const serviceManager = new ServiceManager();
    serviceManager.services = services;
    await serviceManager.shutdown();
  }

  server.close(() => {
    logger.info('Process terminated');
    process.exit(0);
  });
});

process.on('SIGINT', async () => {
  logger.info('SIGINT received, shutting down gracefully');

  if (services) {
    const serviceManager = new ServiceManager();
    serviceManager.services = services;
    await serviceManager.shutdown();
  }

  server.close(() => {
    logger.info('Process terminated');
    process.exit(0);
  });
});

// Initialize the application
initializeApp();

module.exports = app;
