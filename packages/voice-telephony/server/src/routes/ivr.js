/**
 * IVR API Routes - Interactive Voice Response management endpoints
 */

const express = require('express');
const router = express.Router();
const logger = require('../utils/logger');

// This will be injected by the main app
let ivrService = null;

// Initialize service reference
function initializeService(services) {
  ivrService = services.get('ivr');
}

/**
 * Start IVR session
 * POST /api/ivr/start/:callId
 */
router.post('/start/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { menuId, options } = req.body;

    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    const session = await ivrService.startIVRSession(callId, menuId || 'standard_transfer', options);

    res.status(200).json({
      success: true,
      session
    });

  } catch (error) {
    logger.error('Failed to start IVR session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to start IVR session',
      message: error.message
    });
  }
});

/**
 * Process DTMF input
 * POST /api/ivr/dtmf/:callId
 */
router.post('/dtmf/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { digit } = req.body;

    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    await ivrService.processDTMFInput(callId, digit);

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

/**
 * Get all available menus
 * GET /api/ivr/menus
 */
router.get('/menus', async (req, res) => {
  try {
    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    const menus = ivrService.getAllMenus();

    res.status(200).json({
      success: true,
      menus
    });

  } catch (error) {
    logger.error('Failed to get menus:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get menus',
      message: error.message
    });
  }
});

/**
 * Create custom menu
 * POST /api/ivr/menus
 */
router.post('/menus', async (req, res) => {
  try {
    const menuConfig = req.body;

    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    const menu = ivrService.createCustomMenu(menuConfig);

    res.status(201).json({
      success: true,
      menu
    });

  } catch (error) {
    logger.error('Failed to create custom menu:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to create custom menu',
      message: error.message
    });
  }
});

/**
 * Update existing menu
 * PUT /api/ivr/menus/:menuId
 */
router.put('/menus/:menuId', async (req, res) => {
  try {
    const { menuId } = req.params;
    const updates = req.body;

    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    const menu = ivrService.updateMenu(menuId, updates);

    res.status(200).json({
      success: true,
      menu
    });

  } catch (error) {
    logger.error('Failed to update menu:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to update menu',
      message: error.message
    });
  }
});

/**
 * Get IVR session status
 * GET /api/ivr/session/:callId
 */
router.get('/session/:callId', async (req, res) => {
  try {
    const { callId } = req.params;

    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    const session = ivrService.getSessionStatus(callId);

    res.status(200).json({
      success: true,
      session
    });

  } catch (error) {
    logger.error('Failed to get session status:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get session status',
      message: error.message
    });
  }
});

/**
 * Process DTMF input for Android app
 * POST /api/ivr/process/:callId
 */
router.post('/process/:callId', async (req, res) => {
  try {
    const { callId } = req.params;
    const { digit } = req.body;

    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    if (!digit) {
      return res.status(400).json({
        success: false,
        error: 'DTMF digit required'
      });
    }

    // Process the DTMF input
    const result = await ivrService.processDTMFInput(callId, digit);

    // Get the session to determine the response
    const session = ivrService.getSessionStatus(callId);

    let response = {
      action: 'continue',
      destination: null,
      message: 'Invalid selection. Please try again.',
      shouldTransfer: false,
      nextMenu: null
    };

    if (result && session) {
      const menu = session.menu;
      const option = menu.options[digit];

      if (option) {
        response = {
          action: option.action,
          destination: option.destination,
          message: option.text,
          shouldTransfer: option.action === 'transfer',
          nextMenu: option.action === 'sub_menu' ? option.destination : null
        };
      }
    }

    res.status(200).json({
      success: true,
      response
    });

  } catch (error) {
    logger.error('Failed to process DTMF input:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to process DTMF input',
      message: error.message
    });
  }
});

/**
 * End IVR session
 * DELETE /api/ivr/session/:callId
 */
router.delete('/session/:callId', async (req, res) => {
  try {
    const { callId } = req.params;

    if (!ivrService) {
      return res.status(503).json({
        success: false,
        error: 'IVR service not available'
      });
    }

    ivrService.endIVRSession(callId);

    res.status(200).json({
      success: true,
      message: 'IVR session ended'
    });

  } catch (error) {
    logger.error('Failed to end IVR session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to end IVR session',
      message: error.message
    });
  }
});

module.exports = { router, initializeService };
