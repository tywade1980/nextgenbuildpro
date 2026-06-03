/**
 * IVR Service - Interactive Voice Response system
 * Handles customizable phone menus and call routing
 */

const { EventEmitter } = require('events');
const logger = require('../utils/logger');

class IVRService extends EventEmitter {
  constructor() {
    super();
    this.ivrSessions = new Map();
    this.ivrMenus = new Map();
    this.customMenus = new Map();
    this.dtmfListeners = new Map();

    // Initialize default IVR menus
    this.initializeDefaultMenus();
  }

  /**
   * Initialize default IVR menu configurations
   */
  initializeDefaultMenus() {
    // Standard transfer menu
    this.ivrMenus.set('standard_transfer', {
      id: 'standard_transfer',
      name: 'Standard Transfer Menu',
      prompt: 'Thank you for calling. Please select from the following options:',
      options: {
        '1': {
          text: 'Press 1 to schedule a meeting',
          action: 'schedule_meeting',
          destination: 'meeting_scheduler'
        },
        '2': {
          text: 'Press 2 for customer service to answer any questions',
          action: 'transfer',
          destination: 'customer_service'
        },
        '3': {
          text: 'Press 3 for vendor relations',
          action: 'transfer',
          destination: 'vendor_relations'
        },
        '0': {
          text: 'Press 0 to speak with an operator',
          action: 'transfer',
          destination: 'operator'
        },
        '*': {
          text: 'Press star to repeat this menu',
          action: 'repeat_menu'
        }
      },
      timeout: 10000,
      maxAttempts: 3,
      customizable: true
    });

    // Audio quality fallback menu
    this.ivrMenus.set('audio_fallback', {
      id: 'audio_fallback',
      name: 'Audio Quality Fallback',
      prompt: 'Audio quality issues detected. Please choose an option:',
      options: {
        '*#': {
          text: 'Press star hash (*#) to transfer to voicemail',
          action: 'transfer',
          destination: 'voicemail'
        },
        '#*': {
          text: 'Press pound hash (#*) for immediate human assistance',
          action: 'transfer',
          destination: 'human_agent'
        }
      },
      timeout: 10000,
      maxAttempts: 2,
      customizable: false
    });

    logger.info('Default IVR menus initialized');
  }

  /**
   * Start IVR session for a call
   * @param {string} callId - Call ID
   * @param {string} menuId - IVR menu to play
   * @param {Object} options - Additional options
   */
  async startIVRSession(callId, menuId, options = {}) {
    try {
      const menu = this.ivrMenus.get(menuId) || this.customMenus.get(menuId);
      if (!menu) {
        throw new Error(`IVR menu not found: ${menuId}`);
      }

      const session = {
        callId,
        menuId,
        menu,
        currentAttempt: 1,
        startTime: new Date(),
        lastInput: null,
        context: options.context || {}
      };

      this.ivrSessions.set(callId, session);

      // Play initial menu
      await this.playMenu(callId, menu);

      // Start DTMF listening
      this.startDTMFListener(callId, menu);

      this.emit('ivrSessionStarted', session);
      logger.info(`IVR session started for call ${callId} with menu ${menuId}`);

      return session;
    } catch (error) {
      logger.error('Failed to start IVR session:', error);
      throw error;
    }
  }

  /**
   * Play IVR menu to caller
   */
  async playMenu(callId, menu) {
    try {
      // Build full menu prompt
      let fullPrompt = menu.prompt;

      // Add options to prompt
      for (const option of Object.values(menu.options)) {
        fullPrompt += ` ${option.text}.`;
      }

      // Play the prompt
      this.emit('playAudio', {
        callId,
        text: fullPrompt,
        type: 'ivr_menu',
        menuId: menu.id
      });

      logger.debug(`Playing IVR menu for call ${callId}: ${menu.name}`);
    } catch (error) {
      logger.error('Failed to play IVR menu:', error);
    }
  }

  /**
   * Start DTMF listener for menu input
   */
  startDTMFListener(callId, menu) {
    const timeoutHandle = setTimeout(() => {
      this.handleTimeout(callId);
    }, menu.timeout);

    const dtmfBuffer = '';
    this.dtmfListeners.set(callId, {
      timeoutHandle,
      dtmfBuffer,
      menu,
      collecting: true
    });
  }

  /**
   * Process DTMF input
   * @param {string} callId - Call ID
   * @param {string} digit - DTMF digit received
   */
  async processDTMFInput(callId, digit) {
    try {
      const listener = this.dtmfListeners.get(callId);
      const session = this.ivrSessions.get(callId);

      if (!listener || !session || !listener.collecting) {
        return;
      }

      // Add digit to buffer
      listener.dtmfBuffer += digit;

      // Check for multi-digit sequences first
      const multiDigitMatches = this.checkMultiDigitSequences(listener.dtmfBuffer, session.menu);

      if (multiDigitMatches.length > 0) {
        const match = multiDigitMatches[0];
        await this.executeMenuAction(callId, match.key, match.option);
        return;
      }

      // Check for single digit matches
      const option = session.menu.options[digit];
      if (option) {
        await this.executeMenuAction(callId, digit, option);
        return;
      }

      // If buffer gets too long without match, reset
      if (listener.dtmfBuffer.length > 5) {
        listener.dtmfBuffer = digit; // Start fresh with latest digit
      }

    } catch (error) {
      logger.error('Failed to process DTMF input:', error);
    }
  }

  /**
   * Check for multi-digit DTMF sequences
   */
  checkMultiDigitSequences(buffer, menu) {
    const matches = [];

    for (const [key, option] of Object.entries(menu.options)) {
      if (key.length > 1 && buffer.endsWith(key)) {
        matches.push({ key, option });
      }
    }

    return matches;
  }

  /**
   * Execute menu action based on selection
   */
  async executeMenuAction(callId, selectedKey, option) {
    try {
      const session = this.ivrSessions.get(callId);
      const listener = this.dtmfListeners.get(callId);

      // Clear timeout
      if (listener && listener.timeoutHandle) {
        clearTimeout(listener.timeoutHandle);
        listener.collecting = false;
      }

      session.lastInput = selectedKey;

      logger.info(`IVR action selected for call ${callId}: ${selectedKey} -> ${option.action}`);

      switch (option.action) {
      case 'transfer':
        await this.handleTransfer(callId, option.destination, option);
        break;

      case 'schedule_meeting':
        await this.handleMeetingScheduling(callId, option);
        break;

      case 'repeat_menu':
        await this.repeatMenu(callId);
        break;

      case 'custom_action':
        await this.handleCustomAction(callId, option);
        break;

      default:
        logger.warn(`Unknown IVR action: ${option.action}`);
        await this.playErrorMessage(callId);
      }

      this.emit('ivrActionExecuted', {
        callId,
        selectedKey,
        action: option.action,
        destination: option.destination
      });

    } catch (error) {
      logger.error('Failed to execute menu action:', error);
      await this.playErrorMessage(callId);
    }
  }

  /**
   * Handle call transfer
   */
  async handleTransfer(callId, destination, option) {
    try {
      const transferPrompt = option.transferPrompt || `Transferring to ${destination}. Please hold.`;

      this.emit('playAudio', {
        callId,
        text: transferPrompt,
        type: 'transfer_prompt'
      });

      // Execute transfer
      this.emit('transferCall', {
        callId,
        destination,
        type: 'ivr_transfer',
        reason: 'user_selection'
      });

      this.endIVRSession(callId);

      logger.info(`Call ${callId} transferred to ${destination} via IVR`);
    } catch (error) {
      logger.error('Failed to handle transfer:', error);
    }
  }

  /**
   * Handle meeting scheduling
   */
  async handleMeetingScheduling(callId, _option) {
    try {
      const schedulingPrompt = 'Connecting you to our meeting scheduling system. Please hold while we gather your information.';

      this.emit('playAudio', {
        callId,
        text: schedulingPrompt,
        type: 'scheduling_prompt'
      });

      // Start meeting scheduling flow
      this.emit('startMeetingScheduling', { callId });

      this.endIVRSession(callId);

      logger.info(`Meeting scheduling initiated for call ${callId}`);
    } catch (error) {
      logger.error('Failed to handle meeting scheduling:', error);
    }
  }

  /**
   * Repeat current menu
   */
  async repeatMenu(callId) {
    try {
      const session = this.ivrSessions.get(callId);
      if (session) {
        await this.playMenu(callId, session.menu);
        this.startDTMFListener(callId, session.menu);
      }
    } catch (error) {
      logger.error('Failed to repeat menu:', error);
    }
  }

  /**
   * Handle timeout when no input received
   */
  async handleTimeout(callId) {
    try {
      const session = this.ivrSessions.get(callId);

      if (!session) return;

      session.currentAttempt++;

      if (session.currentAttempt <= session.menu.maxAttempts) {
        // Retry with timeout message
        const timeoutPrompt = 'We did not receive your selection. Please try again.';

        this.emit('playAudio', {
          callId,
          text: timeoutPrompt,
          type: 'timeout_prompt'
        });

        setTimeout(() => {
          this.playMenu(callId, session.menu);
          this.startDTMFListener(callId, session.menu);
        }, 2000);

      } else {
        // Max attempts reached, transfer to operator
        await this.handleTransfer(callId, 'operator', {
          transferPrompt: 'Transferring to operator for assistance.'
        });
      }

    } catch (error) {
      logger.error('Failed to handle timeout:', error);
    }
  }

  /**
   * Play error message
   */
  async playErrorMessage(callId) {
    const errorPrompt = 'We are experiencing technical difficulties. Transferring to operator for assistance.';

    this.emit('playAudio', {
      callId,
      text: errorPrompt,
      type: 'error_prompt'
    });

    // Transfer to operator after error
    setTimeout(() => {
      this.handleTransfer(callId, 'operator', {});
    }, 3000);
  }

  /**
   * Create custom IVR menu
   */
  createCustomMenu(menuConfig) {
    try {
      const menu = {
        id: menuConfig.id || `custom_${Date.now()}`,
        name: menuConfig.name || 'Custom Menu',
        prompt: menuConfig.prompt || 'Please select from the following options:',
        options: menuConfig.options || {},
        timeout: menuConfig.timeout || 10000,
        maxAttempts: menuConfig.maxAttempts || 3,
        customizable: true,
        createdAt: new Date()
      };

      this.customMenus.set(menu.id, menu);

      this.emit('customMenuCreated', menu);
      logger.info(`Custom IVR menu created: ${menu.id}`);

      return menu;
    } catch (error) {
      logger.error('Failed to create custom menu:', error);
      throw error;
    }
  }

  /**
   * Update existing menu
   */
  updateMenu(menuId, updates) {
    try {
      const menu = this.ivrMenus.get(menuId) || this.customMenus.get(menuId);

      if (!menu) {
        throw new Error(`Menu not found: ${menuId}`);
      }

      if (!menu.customizable) {
        throw new Error(`Menu is not customizable: ${menuId}`);
      }

      // Apply updates
      Object.assign(menu, updates);
      menu.updatedAt = new Date();

      this.emit('menuUpdated', menu);
      logger.info(`IVR menu updated: ${menuId}`);

      return menu;
    } catch (error) {
      logger.error('Failed to update menu:', error);
      throw error;
    }
  }

  /**
   * Get all available menus
   */
  getAllMenus() {
    const allMenus = new Map([...this.ivrMenus, ...this.customMenus]);
    return Array.from(allMenus.values());
  }

  /**
   * End IVR session
   */
  endIVRSession(callId) {
    const session = this.ivrSessions.get(callId);
    const listener = this.dtmfListeners.get(callId);

    if (listener && listener.timeoutHandle) {
      clearTimeout(listener.timeoutHandle);
    }

    this.ivrSessions.delete(callId);
    this.dtmfListeners.delete(callId);

    if (session) {
      session.endTime = new Date();
      this.emit('ivrSessionEnded', session);
      logger.info(`IVR session ended for call ${callId}`);
    }
  }

  /**
   * Get IVR session status
   */
  getSessionStatus(callId) {
    return this.ivrSessions.get(callId);
  }
}

module.exports = IVRService;
