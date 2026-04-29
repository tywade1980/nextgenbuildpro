/**
 * Agent Service - Human agent management and guided interactions
 * Handles live agent availability, handoffs, and training scenarios
 */

const { EventEmitter } = require('events');
const logger = require('../utils/logger');

class AgentService extends EventEmitter {
  constructor() {
    super();
    this.agents = new Map();
    this.activeAgentSessions = new Map();
    this.agentQueues = new Map();
    this.agentSkills = new Map();
    this.trainingModes = new Map();

    // Initialize default agent data
    this.initializeDefaultAgents();
  }

  /**
   * Initialize default agents for demonstration
   */
  initializeDefaultAgents() {
    const defaultAgents = [
      {
        id: 'agent_001',
        name: 'Sarah Johnson',
        email: 'sarah.johnson@company.com',
        skills: ['customer_service', 'billing', 'technical_support'],
        status: 'available',
        maxConcurrentCalls: 3,
        currentCalls: 0,
        department: 'customer_service',
        experience: 'senior',
        languages: ['en', 'es'],
        trainingMode: false
      },
      {
        id: 'agent_002',
        name: 'Mike Rodriguez',
        email: 'mike.rodriguez@company.com',
        skills: ['technical_support', 'vendor_relations', 'escalations'],
        status: 'available',
        maxConcurrentCalls: 2,
        currentCalls: 0,
        department: 'technical_support',
        experience: 'expert',
        languages: ['en'],
        trainingMode: false
      },
      {
        id: 'agent_003',
        name: 'Emily Chen',
        email: 'emily.chen@company.com',
        skills: ['sales', 'meeting_scheduling', 'customer_service'],
        status: 'available',
        maxConcurrentCalls: 4,
        currentCalls: 0,
        department: 'sales',
        experience: 'intermediate',
        languages: ['en', 'zh'],
        trainingMode: false
      }
    ];

    defaultAgents.forEach(agent => {
      this.agents.set(agent.id, {
        ...agent,
        loginTime: new Date(),
        lastActivity: new Date(),
        callHistory: [],
        performanceMetrics: {
          callsHandled: 0,
          averageCallTime: 0,
          customerSatisfaction: 4.5,
          resolutionRate: 0.92
        }
      });
    });

    logger.info(`Initialized ${defaultAgents.length} default agents`);
  }

  /**
   * Find available agent with required skills
   * @param {Array} requiredSkills - Skills needed for the call
   * @param {string} priority - Priority level (high, normal, low)
   */
  async findAvailableAgent(requiredSkills = [], _priority = 'normal') {
    try {
      const availableAgents = Array.from(this.agents.values())
        .filter(agent => agent.status === 'available')
        .filter(agent => agent.currentCalls < agent.maxConcurrentCalls)
        .filter(agent => {
          // Check if agent has at least one required skill
          if (requiredSkills.length === 0) return true;
          return requiredSkills.some(skill => agent.skills.includes(skill));
        })
        .sort((a, b) => {
          // Sort by skill match, then by availability, then by experience
          const aSkillMatch = requiredSkills.filter(skill => a.skills.includes(skill)).length;
          const bSkillMatch = requiredSkills.filter(skill => b.skills.includes(skill)).length;

          if (aSkillMatch !== bSkillMatch) {
            return bSkillMatch - aSkillMatch; // More skill matches first
          }

          const aAvailability = (a.maxConcurrentCalls - a.currentCalls) / a.maxConcurrentCalls;
          const bAvailability = (b.maxConcurrentCalls - b.currentCalls) / b.maxConcurrentCalls;

          if (aAvailability !== bAvailability) {
            return bAvailability - aAvailability; // More available first
          }

          // Sort by experience level
          const experienceOrder = { 'expert': 3, 'senior': 2, 'intermediate': 1, 'junior': 0 };
          return experienceOrder[b.experience] - experienceOrder[a.experience];
        });

      return availableAgents.length > 0 ? availableAgents[0] : null;
    } catch (error) {
      logger.error('Failed to find available agent:', error);
      return null;
    }
  }

  /**
   * Assign call to agent
   * @param {string} callId - Call ID
   * @param {string} agentId - Agent ID
   * @param {Object} callContext - Call context information
   */
  async assignCallToAgent(callId, agentId, callContext = {}) {
    try {
      const agent = this.agents.get(agentId);
      if (!agent) {
        throw new Error(`Agent not found: ${agentId}`);
      }

      if (agent.status !== 'available') {
        throw new Error(`Agent ${agentId} is not available`);
      }

      if (agent.currentCalls >= agent.maxConcurrentCalls) {
        throw new Error(`Agent ${agentId} is at maximum capacity`);
      }

      // Create agent session
      const session = {
        callId,
        agentId,
        agent,
        startTime: new Date(),
        callContext,
        status: 'connected',
        notes: [],
        guideMode: false,
        trainingMode: agent.trainingMode
      };

      this.activeAgentSessions.set(callId, session);

      // Update agent status
      agent.currentCalls++;
      agent.status = agent.currentCalls >= agent.maxConcurrentCalls ? 'busy' : 'available';
      agent.lastActivity = new Date();

      this.emit('callAssignedToAgent', session);
      logger.info(`Call ${callId} assigned to agent ${agentId} (${agent.name})`);

      return session;
    } catch (error) {
      logger.error('Failed to assign call to agent:', error);
      throw error;
    }
  }

  /**
   * Enable guided interaction mode
   * @param {string} callId - Call ID
   * @param {Object} guideOptions - Guide configuration
   */
  async enableGuidedMode(callId, guideOptions = {}) {
    try {
      const session = this.activeAgentSessions.get(callId);
      if (!session) {
        throw new Error(`No active agent session for call ${callId}`);
      }

      session.guideMode = true;
      session.guideOptions = {
        showSuggestions: guideOptions.showSuggestions !== false,
        realTimeAnalysis: guideOptions.realTimeAnalysis !== false,
        scriptGuidance: guideOptions.scriptGuidance || false,
        sentimentMonitoring: guideOptions.sentimentMonitoring !== false,
        knowledgeBaseAccess: guideOptions.knowledgeBaseAccess !== false,
        escalationAlerts: guideOptions.escalationAlerts !== false
      };

      this.emit('guidedModeEnabled', { callId, session });
      logger.info(`Guided mode enabled for call ${callId}`);

      // Start providing real-time guidance
      this.startGuidanceEngine(callId);

      return session;
    } catch (error) {
      logger.error('Failed to enable guided mode:', error);
      throw error;
    }
  }

  /**
   * Start guidance engine for real-time agent assistance
   */
  startGuidanceEngine(callId) {
    const session = this.activeAgentSessions.get(callId);
    if (!session || !session.guideMode) return;

    // Simulate real-time guidance
    const guidanceInterval = setInterval(() => {
      this.generateGuidance(callId);
    }, 10000); // Generate guidance every 10 seconds

    session.guidanceInterval = guidanceInterval;
  }

  /**
   * Generate real-time guidance for agent
   */
  async generateGuidance(callId) {
    try {
      const session = this.activeAgentSessions.get(callId);
      if (!session || !session.guideMode) return;

      // Simulate guidance generation based on call context
      const guidance = {
        callId,
        timestamp: new Date(),
        type: 'suggestion',
        suggestions: [
          {
            category: 'response',
            text: 'Consider acknowledging the customer\'s concern and asking for more details',
            confidence: 0.85
          },
          {
            category: 'knowledge',
            text: 'Similar issue resolved with solution KB-2024-001',
            confidence: 0.92,
            link: '/knowledge-base/KB-2024-001'
          }
        ],
        sentiment: {
          customer: 'frustrated',
          agent: 'professional',
          recommendation: 'Use empathetic language and offer specific help'
        },
        metrics: {
          callDuration: '00:03:45',
          talkTime: '65%',
          silenceDetected: false
        }
      };

      this.emit('guidanceGenerated', guidance);
      logger.debug(`Guidance generated for call ${callId}`);

    } catch (error) {
      logger.error('Failed to generate guidance:', error);
    }
  }

  /**
   * Start training mode for AI learning
   * @param {string} callId - Call ID
   * @param {Object} trainingConfig - Training configuration
   */
  async startTrainingMode(callId, trainingConfig = {}) {
    try {
      const session = this.activeAgentSessions.get(callId);
      if (!session) {
        throw new Error(`No active agent session for call ${callId}`);
      }

      const trainingSession = {
        callId,
        agentId: session.agentId,
        startTime: new Date(),
        config: {
          recordInteractions: trainingConfig.recordInteractions !== false,
          analyzeResponses: trainingConfig.analyzeResponses !== false,
          captureDecisionPoints: trainingConfig.captureDecisionPoints !== false,
          trackOutcomes: trainingConfig.trackOutcomes !== false,
          learningObjectives: trainingConfig.learningObjectives || [
            'customer_service_best_practices',
            'problem_resolution_techniques',
            'communication_patterns'
          ]
        },
        interactions: [],
        learningPoints: []
      };

      this.trainingModes.set(callId, trainingSession);
      session.trainingMode = true;

      this.emit('trainingModeStarted', trainingSession);
      logger.info(`Training mode started for call ${callId} with agent ${session.agentId}`);

      return trainingSession;
    } catch (error) {
      logger.error('Failed to start training mode:', error);
      throw error;
    }
  }

  /**
   * Record training interaction
   * @param {string} callId - Call ID
   * @param {Object} interaction - Interaction data
   */
  async recordTrainingInteraction(callId, interaction) {
    try {
      const trainingSession = this.trainingModes.get(callId);
      if (!trainingSession) return;

      const recordedInteraction = {
        timestamp: new Date(),
        type: interaction.type, // 'customer_input', 'agent_response', 'system_action'
        content: interaction.content,
        context: interaction.context,
        metadata: {
          sentiment: interaction.sentiment,
          confidence: interaction.confidence,
          tags: interaction.tags || []
        }
      };

      trainingSession.interactions.push(recordedInteraction);

      // Analyze interaction for learning points
      if (trainingSession.config.analyzeResponses) {
        const learningPoint = await this.analyzeInteractionForLearning(recordedInteraction);
        if (learningPoint) {
          trainingSession.learningPoints.push(learningPoint);
        }
      }

      this.emit('trainingInteractionRecorded', {
        callId,
        interaction: recordedInteraction
      });

    } catch (error) {
      logger.error('Failed to record training interaction:', error);
    }
  }

  /**
   * Analyze interaction for learning opportunities
   */
  async analyzeInteractionForLearning(interaction) {
    try {
      // Simulate learning analysis
      // In production, this would use ML/AI to identify learning patterns

      const learningPatterns = [
        {
          pattern: 'empathy_expression',
          triggers: ['sorry', 'understand', 'frustrating'],
          learningPoint: 'Agent effectively used empathetic language'
        },
        {
          pattern: 'solution_offering',
          triggers: ['can help', 'let me', 'here\'s what'],
          learningPoint: 'Agent proactively offered solutions'
        },
        {
          pattern: 'clarification_seeking',
          triggers: ['can you tell me', 'could you clarify', 'help me understand'],
          learningPoint: 'Agent sought clarification before proceeding'
        }
      ];

      for (const pattern of learningPatterns) {
        if (pattern.triggers.some(trigger =>
          interaction.content.toLowerCase().includes(trigger))) {
          return {
            timestamp: new Date(),
            pattern: pattern.pattern,
            description: pattern.learningPoint,
            example: interaction.content,
            quality: 'positive'
          };
        }
      }

      return null;
    } catch (error) {
      logger.error('Failed to analyze interaction for learning:', error);
      return null;
    }
  }

  /**
   * End agent session
   */
  async endAgentSession(callId) {
    try {
      const session = this.activeAgentSessions.get(callId);
      if (!session) return;

      // Clear guidance interval
      if (session.guidanceInterval) {
        clearInterval(session.guidanceInterval);
      }

      // Update agent status
      const agent = this.agents.get(session.agentId);
      if (agent) {
        agent.currentCalls--;
        agent.status = 'available';
        agent.performanceMetrics.callsHandled++;

        // Update call history
        agent.callHistory.push({
          callId,
          duration: new Date() - session.startTime,
          endTime: new Date(),
          outcome: 'completed'
        });
      }

      // End training session if active
      const trainingSession = this.trainingModes.get(callId);
      if (trainingSession) {
        await this.endTrainingSession(callId);
      }

      session.endTime = new Date();
      session.status = 'ended';

      this.activeAgentSessions.delete(callId);

      this.emit('agentSessionEnded', session);
      logger.info(`Agent session ended for call ${callId}`);

      return session;
    } catch (error) {
      logger.error('Failed to end agent session:', error);
    }
  }

  /**
   * End training session and generate learning summary
   */
  async endTrainingSession(callId) {
    try {
      const trainingSession = this.trainingModes.get(callId);
      if (!trainingSession) return;

      trainingSession.endTime = new Date();
      trainingSession.duration = trainingSession.endTime - trainingSession.startTime;

      // Generate training summary
      const summary = {
        callId,
        agentId: trainingSession.agentId,
        duration: trainingSession.duration,
        totalInteractions: trainingSession.interactions.length,
        learningPoints: trainingSession.learningPoints,
        recommendations: this.generateTrainingRecommendations(trainingSession),
        aiLearnings: this.extractAILearnings(trainingSession)
      };

      this.trainingModes.delete(callId);

      this.emit('trainingSessionEnded', { trainingSession, summary });
      logger.info(`Training session ended for call ${callId}, generated ${summary.learningPoints.length} learning points`);

      return summary;
    } catch (error) {
      logger.error('Failed to end training session:', error);
    }
  }

  /**
   * Generate training recommendations based on session
   */
  generateTrainingRecommendations(trainingSession) {
    const recommendations = [];

    // Analyze learning points for patterns
    const positivePatterns = trainingSession.learningPoints.filter(lp => lp.quality === 'positive');
    const improvementAreas = trainingSession.learningPoints.filter(lp => lp.quality === 'needs_improvement');

    if (positivePatterns.length > 0) {
      recommendations.push({
        type: 'reinforcement',
        description: 'Continue using effective communication patterns',
        examples: positivePatterns.slice(0, 3)
      });
    }

    if (improvementAreas.length > 0) {
      recommendations.push({
        type: 'improvement',
        description: 'Focus on areas for development',
        examples: improvementAreas.slice(0, 3)
      });
    }

    return recommendations;
  }

  /**
   * Extract AI learnings from training session
   */
  extractAILearnings(trainingSession) {
    const aiLearnings = {
      communicationPatterns: [],
      problemSolvingApproaches: [],
      customerHandlingTechniques: [],
      escalationTriggers: []
    };

    // Process interactions to extract patterns for AI training
    trainingSession.interactions.forEach(interaction => {
      if (interaction.type === 'agent_response') {
        // Extract communication patterns
        aiLearnings.communicationPatterns.push({
          context: interaction.context,
          response: interaction.content,
          effectiveness: interaction.metadata.confidence
        });
      }
    });

    return aiLearnings;
  }

  /**
   * Get agent status and metrics
   */
  getAgentStatus(agentId) {
    const agent = this.agents.get(agentId);
    if (!agent) return null;

    return {
      ...agent,
      activeCalls: Array.from(this.activeAgentSessions.values())
        .filter(session => session.agentId === agentId)
        .map(session => ({ callId: session.callId, duration: new Date() - session.startTime }))
    };
  }

  /**
   * Get all agents status
   */
  getAllAgentsStatus() {
    return Array.from(this.agents.values()).map(agent => this.getAgentStatus(agent.id));
  }
}

module.exports = AgentService;
