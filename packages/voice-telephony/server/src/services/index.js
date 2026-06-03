/**
 * Services Index - Central service initialization and management
 */

const SIPService = require('./sip');
const WebRTCService = require('./webrtc');
const AIService = require('./ai');
const DialerService = require('./dialer');
const CarrierService = require('./carrier');
const IVRService = require('./ivr');
const AgentService = require('./agent');
const logger = require('../utils/logger');

class ServiceManager {
  constructor() {
    this.services = new Map();
    this.initialized = false;
  }

  /**
   * Initialize all services
   */
  async initialize() {
    try {
      logger.info('Initializing NextGenTele services...');

      // Initialize Carrier service first (base infrastructure)
      const carrierService = new CarrierService();
      await carrierService.initialize();
      this.services.set('carrier', carrierService);
      logger.info('Carrier service initialized');

      // Initialize SIP service
      const sipService = new SIPService();
      await sipService.initialize();
      this.services.set('sip', sipService);
      logger.info('SIP service initialized');

      // Initialize AI service
      const aiService = new AIService();
      await aiService.initialize();
      this.services.set('ai', aiService);
      logger.info('AI service initialized');

      // Initialize Agent service
      const agentService = new AgentService();
      this.services.set('agent', agentService);
      logger.info('Agent service initialized');

      // Initialize IVR service
      const ivrService = new IVRService();
      this.services.set('ivr', ivrService);
      logger.info('IVR service initialized');

      // Initialize Dialer service (depends on other services)
      const dialerService = new DialerService();
      await dialerService.initialize(sipService, aiService, carrierService, ivrService, agentService);
      this.services.set('dialer', dialerService);
      logger.info('Dialer service initialized');

      // Initialize WebRTC service
      const webrtcService = new WebRTCService();
      await webrtcService.initialize();
      this.services.set('webrtc', webrtcService);
      logger.info('WebRTC service initialized');

      // Setup service inter-connections
      this.setupServiceConnections();

      this.initialized = true;
      logger.info('All services initialized successfully');

      return this.services;
    } catch (error) {
      logger.error('Failed to initialize services:', error);
      throw error;
    }
  }

  /**
   * Setup connections between services
   */
  setupServiceConnections() {
    const carrierService = this.services.get('carrier');
    const ivrService = this.services.get('ivr');
    const agentService = this.services.get('agent');
    const aiService = this.services.get('ai');

    // Carrier service events
    carrierService.on('fallbackTriggered', async (data) => {
      await ivrService.startIVRSession(data.callId, 'audio_fallback');
    });

    carrierService.on('transferToVoicemail', (data) => {
      // Handle voicemail transfer
      logger.info(`Transferring call ${data.callId} to voicemail`);
    });

    carrierService.on('transferToHuman', async (data) => {
      const agent = await agentService.findAvailableAgent(['customer_service']);
      if (agent) {
        await agentService.assignCallToAgent(data.callId, agent.id);
      }
    });

    // IVR service events
    ivrService.on('transferCall', async (data) => {
      if (data.destination === 'human_agent' || data.destination === 'operator') {
        const skills = this.getRequiredSkillsForDestination(data.destination);
        const agent = await agentService.findAvailableAgent(skills);
        if (agent) {
          await agentService.assignCallToAgent(data.callId, agent.id, data);
        }
      }
    });

    ivrService.on('startMeetingScheduling', (data) => {
      // Handle meeting scheduling
      logger.info(`Starting meeting scheduling for call ${data.callId}`);
    });

    // Agent service events
    agentService.on('callAssignedToAgent', async (session) => {
      // Enable guided mode for new agent sessions
      await agentService.enableGuidedMode(session.callId, {
        showSuggestions: true,
        realTimeAnalysis: true,
        sentimentMonitoring: true
      });
    });

    // AI service events for training
    aiService.on('audioProcessed', (data) => {
      const trainingSession = agentService.trainingModes.get(data.callId);
      if (trainingSession) {
        agentService.recordTrainingInteraction(data.callId, {
          type: 'customer_input',
          content: data.transcription.text,
          context: data.context,
          sentiment: data.analysis.sentiment,
          confidence: data.transcription.confidence
        });
      }
    });

    logger.info('Service connections established');
  }

  /**
   * Get required skills for destination
   */
  getRequiredSkillsForDestination(destination) {
    const skillMap = {
      'customer_service': ['customer_service'],
      'technical_support': ['technical_support'],
      'vendor_relations': ['vendor_relations'],
      'sales': ['sales'],
      'operator': ['customer_service', 'escalations'],
      'human_agent': ['customer_service']
    };

    return skillMap[destination] || ['customer_service'];
  }

  /**
   * Get service by name
   */
  getService(name) {
    return this.services.get(name);
  }

  /**
   * Get all services
   */
  getAllServices() {
    return this.services;
  }

  /**
   * Shutdown all services
   */
  async shutdown() {
    logger.info('Shutting down services...');

    for (const [name, service] of this.services) {
      try {
        if (service.shutdown) {
          await service.shutdown();
        }
        logger.info(`${name} service shut down`);
      } catch (error) {
        logger.error(`Failed to shutdown ${name} service:`, error);
      }
    }

    this.services.clear();
    this.initialized = false;
    logger.info('All services shut down');
  }
}

// Legacy compatibility
let aiServiceInstance = null;

async function initAIServices() {
  try {
    if (!aiServiceInstance) {
      aiServiceInstance = new AIService();
      await aiServiceInstance.initialize();
    }
    return aiServiceInstance;
  } catch (error) {
    logger.error('Failed to initialize AI services:', error);
    throw error;
  }
}

function getAIService() {
  return aiServiceInstance;
}

module.exports = {
  ServiceManager,
  initAIServices,
  getAIService,
  AIService
};
