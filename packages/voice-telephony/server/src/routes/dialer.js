/**
 * Dialer API Routes
 * Handles all dialer-related API endpoints
 */

const express = require('express');
const router = express.Router();
const logger = require('../utils/logger');
const { validateCallParams } = require('../utils/validation');
const { getRegulationCompliance } = require('../utils/compliance');

// This would be injected in a real application
let dialerService = null;

/**
 * Initialize route dependencies
 */
function initializeRoutes(dialer, _ai) {
  dialerService = dialer;
}

/**
 * Make an outbound call
 * POST /api/dialer/call
 */
router.post('/call', async (req, res) => {
  try {
    const { to, from, protocol, options } = req.body;

    // Validate input parameters
    const validation = validateCallParams({ to, from, protocol });
    if (!validation.isValid) {
      return res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: validation.errors
      });
    }

    // Check regulation compliance
    const compliance = await getRegulationCompliance(to, from);
    if (!compliance.allowed) {
      return res.status(403).json({
        success: false,
        error: 'Call not allowed',
        reason: compliance.reason,
        requirements: compliance.requirements
      });
    }

    // Make the call
    const callSession = await dialerService.makeCall({
      to,
      from,
      protocol: protocol || 'SIP',
      options: options || {}
    });

    res.status(200).json({
      success: true,
      callSession: callSession.getSummary(),
      compliance
    });

  } catch (error) {
    logger.error('Failed to make call:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to make call',
      message: error.message
    });
  }
});

/**
 * Answer an incoming call
 * POST /api/dialer/answer/:callId
 */
router.post('/answer/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { options } = req.body;

    const callSession = await dialerService.answerCall(callId, options || {});

    res.status(200).json({
      success: true,
      callSession: callSession.getSummary()
    });

  } catch (error) {
    logger.error('Failed to answer call:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to answer call',
      message: error.message
    });
  }
});

/**
 * End a call
 * POST /api/dialer/end/:callId
 */
router.post('/end/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { reason } = req.body;

    const callSession = await dialerService.endCall(callId, reason);

    res.status(200).json({
      success: true,
      callSession: callSession.getSummary()
    });

  } catch (error) {
    logger.error('Failed to end call:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to end call',
      message: error.message
    });
  }
});

/**
 * Transfer a call
 * POST /api/dialer/transfer/:callId
 */
router.post('/transfer/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { destination, options } = req.body;

    if (!destination) {
      return res.status(400).json({
        success: false,
        error: 'Transfer destination is required'
      });
    }

    const result = await dialerService.transferCall(callId, destination, options || {});

    res.status(200).json({
      success: true,
      transfer: result
    });

  } catch (error) {
    logger.error('Failed to transfer call:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to transfer call',
      message: error.message
    });
  }
});

/**
 * Hold a call
 * POST /api/dialer/hold/:callId
 */
router.post('/hold/:callId', async (req, res) => {
  try {
    const { callId } = req.params;

    const callSession = await dialerService.holdCall(callId);

    res.status(200).json({
      success: true,
      callSession: callSession.getSummary()
    });

  } catch (error) {
    logger.error('Failed to hold call:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to hold call',
      message: error.message
    });
  }
});

/**
 * Resume a call from hold
 * POST /api/dialer/resume/:callId
 */
router.post('/resume/:callId', async (req, res) => {
  try {
    const { callId } = req.params;

    const callSession = await dialerService.resumeCall(callId);

    res.status(200).json({
      success: true,
      callSession: callSession.getSummary()
    });

  } catch (error) {
    logger.error('Failed to resume call:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to resume call',
      message: error.message
    });
  }
});

/**
 * Get active calls
 * GET /api/dialer/calls/active
 */
router.get('/calls/active', async (req, res) => {
  try {
    const activeCalls = dialerService.getActiveCalls();

    res.status(200).json({
      success: true,
      calls: activeCalls.map(call => call.getSummary()),
      count: activeCalls.length
    });

  } catch (error) {
    logger.error('Failed to get active calls:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get active calls',
      message: error.message
    });
  }
});

/**
 * Get call details
 * GET /api/dialer/call/:callId
 */
router.get('/call/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const activeCalls = dialerService.getActiveCalls();
    const call = activeCalls.find(c => c.id === callId);

    if (!call) {
      return res.status(404).json({
        success: false,
        error: 'Call not found'
      });
    }

    res.status(200).json({
      success: true,
      call: call.toJSON()
    });

  } catch (error) {
    logger.error('Failed to get call details:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get call details',
      message: error.message
    });
  }
});

/**
 * Check compliance for a number
 * GET /api/dialer/compliance/:number
 */
router.get('/compliance/:number', async (req, res) => {
  try {
    const { number } = req.params;
    const { from } = req.query;

    const compliance = await getRegulationCompliance(number, from);

    res.status(200).json({
      success: true,
      compliance
    });

  } catch (error) {
    logger.error('Failed to check compliance:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to check compliance',
      message: error.message
    });
  }
});

module.exports = { router, initializeRoutes };
