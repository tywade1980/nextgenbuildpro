/**
 * Carrier API Routes - Carrier service management endpoints
 */

const express = require('express');
const router = express.Router();
const logger = require('../utils/logger');

// This will be injected by the main app
let carrierService = null;

// Initialize service reference
function initializeService(services) {
  carrierService = services.get('carrier');
}

/**
 * Get carrier status
 * GET /api/carrier/status
 */
router.get('/status', async (req, res) => {
  try {
    if (!carrierService) {
      return res.status(503).json({
        success: false,
        error: 'Carrier service not available'
      });
    }

    const status = carrierService.getCarrierStatus();

    res.status(200).json({
      success: true,
      status
    });

  } catch (error) {
    logger.error('Failed to get carrier status:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get carrier status',
      message: error.message
    });
  }
});

/**
 * Trigger manual failover
 * POST /api/carrier/failover
 */
router.post('/failover', async (req, res) => {
  try {
    if (!carrierService) {
      return res.status(503).json({
        success: false,
        error: 'Carrier service not available'
      });
    }

    await carrierService.failoverToBackup();

    res.status(200).json({
      success: true,
      message: 'Failover completed successfully'
    });

  } catch (error) {
    logger.error('Failed to execute failover:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to execute failover',
      message: error.message
    });
  }
});

/**
 * Trigger smart fallback for call
 * POST /api/carrier/fallback/:callId
 */
router.post('/fallback/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { qualityData } = req.body;

    if (!carrierService) {
      return res.status(503).json({
        success: false,
        error: 'Carrier service not available'
      });
    }

    await carrierService.triggerSmartFallback(callId, qualityData || {});

    res.status(200).json({
      success: true,
      message: 'Smart fallback triggered'
    });

  } catch (error) {
    logger.error('Failed to trigger smart fallback:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to trigger smart fallback',
      message: error.message
    });
  }
});

/**
 * Process DTMF input for fallback
 * POST /api/carrier/dtmf/:callId
 */
router.post('/dtmf/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { sequence } = req.body;

    if (!carrierService) {
      return res.status(503).json({
        success: false,
        error: 'Carrier service not available'
      });
    }

    carrierService.emit('dtmfReceived', { callId, sequence });

    res.status(200).json({
      success: true,
      message: 'DTMF processed'
    });

  } catch (error) {
    logger.error('Failed to process DTMF:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to process DTMF',
      message: error.message
    });
  }
});

module.exports = { router, initializeService };
