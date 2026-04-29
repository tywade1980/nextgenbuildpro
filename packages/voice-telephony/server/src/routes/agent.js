/**
 * Agent API Routes - Human agent management and guided interaction endpoints
 */

const express = require('express');
const router = express.Router();
const logger = require('../utils/logger');

// This will be injected by the main app
let agentService = null;

// Initialize service reference
function initializeService(services) {
  agentService = services.get('agent');
}

/**
 * Get all agents status
 * GET /api/agents/status
 */
router.get('/status', async (req, res) => {
  try {
    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const agents = agentService.getAllAgentsStatus();

    res.status(200).json({
      success: true,
      agents
    });

  } catch (error) {
    logger.error('Failed to get agents status:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get agents status',
      message: error.message
    });
  }
});

/**
 * Find available agent
 * POST /api/agents/find
 */
router.post('/find', async (req, res) => {
  try {
    const { skills, priority } = req.body;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const agent = await agentService.findAvailableAgent(skills || [], priority || 'normal');

    res.status(200).json({
      success: true,
      agent
    });

  } catch (error) {
    logger.error('Failed to find available agent:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to find available agent',
      message: error.message
    });
  }
});

/**
 * Assign call to agent
 * POST /api/agents/assign/:callId
 */
router.post('/assign/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { agentId, callContext } = req.body;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const session = await agentService.assignCallToAgent(callId, agentId, callContext);

    res.status(200).json({
      success: true,
      session
    });

  } catch (error) {
    logger.error('Failed to assign call to agent:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to assign call to agent',
      message: error.message
    });
  }
});

/**
 * Enable guided mode
 * POST /api/agents/guided/:callId
 */
router.post('/guided/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { guideOptions } = req.body;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const session = await agentService.enableGuidedMode(callId, guideOptions);

    res.status(200).json({
      success: true,
      session
    });

  } catch (error) {
    logger.error('Failed to enable guided mode:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to enable guided mode',
      message: error.message
    });
  }
});

/**
 * Start training mode
 * POST /api/agents/training/:callId
 */
router.post('/training/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { trainingConfig } = req.body;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const trainingSession = await agentService.startTrainingMode(callId, trainingConfig);

    res.status(200).json({
      success: true,
      trainingSession
    });

  } catch (error) {
    logger.error('Failed to start training mode:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to start training mode',
      message: error.message
    });
  }
});

/**
 * Record training interaction
 * POST /api/agents/training/:callId/interaction
 */
router.post('/training/:callId/interaction', async (req, res) => {
  try {
    const { callId } = req.params;
    const { interaction } = req.body;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    await agentService.recordTrainingInteraction(callId, interaction);

    res.status(200).json({
      success: true,
      message: 'Training interaction recorded'
    });

  } catch (error) {
    logger.error('Failed to record training interaction:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to record training interaction',
      message: error.message
    });
  }
});

/**
 * End training session
 * DELETE /api/agents/training/:callId
 */
router.delete('/training/:callId', async (req, res) => {
  try {
    const { callId } = req.params;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const summary = await agentService.endTrainingSession(callId);

    res.status(200).json({
      success: true,
      summary
    });

  } catch (error) {
    logger.error('Failed to end training session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to end training session',
      message: error.message
    });
  }
});

/**
 * Get agent session status
 * GET /api/agents/session/:callId
 */
router.get('/session/:callId', async (req, res) => {
  try {
    const { callId } = req.params;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const session = agentService.activeAgentSessions.get(callId);

    res.status(200).json({
      success: true,
      session
    });

  } catch (error) {
    logger.error('Failed to get agent session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get agent session',
      message: error.message
    });
  }
});

/**
 * End agent session
 * DELETE /api/agents/session/:callId
 */
router.delete('/session/:callId', async (req, res) => {
  try {
    const { callId } = req.params;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const session = await agentService.endAgentSession(callId);

    res.status(200).json({
      success: true,
      session
    });

  } catch (error) {
    logger.error('Failed to end agent session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to end agent session',
      message: error.message
    });
  }
});

/**
 * Get specific agent status
 * GET /api/agents/:agentId
 */
router.get('/:agentId', async (req, res) => {
  try {
    const { agentId } = req.params;

    if (!agentService) {
      return res.status(503).json({
        success: false,
        error: 'Agent service not available'
      });
    }

    const agent = agentService.getAgentStatus(agentId);

    if (!agent) {
      return res.status(404).json({
        success: false,
        error: 'Agent not found'
      });
    }

    res.status(200).json({
      success: true,
      agent
    });

  } catch (error) {
    logger.error('Failed to get agent status:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get agent status',
      message: error.message
    });
  }
});

module.exports = { router, initializeService };
