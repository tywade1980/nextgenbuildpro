/**
 * Dialer Service - Core dialing functionality
 * Handles making and receiving calls with various protocols
 */

const { EventEmitter } = require('events');
const logger = require('../utils/logger');
const { CallSession } = require('../models/CallSession');
const { validatePhoneNumber } = require('../utils/validation');
const { getRegulationCompliance } = require('../utils/compliance');

class DialerService extends EventEmitter {
  constructor() {
    super();
    this.activeCalls = new Map();
    this.callQueue = [];
    this.sipStack = null;
    this.webrtcPeers = new Map();
    this.aiHandler = null;
  }

  /**
   * Initialize dialer with required services
   */
  async initialize(sipStack, aiHandler, carrierService, ivrService, agentService) {
    this.sipStack = sipStack;
    this.aiHandler = aiHandler;
    this.carrierService = carrierService;
    this.ivrService = ivrService;
    this.agentService = agentService;

    // Setup event listeners for carrier integration
    if (this.carrierService) {
      this.carrierService.on('audioQualityIssue', (data) => {
        this.handleAudioQualityIssue(data);
      });
    }

    logger.info('Dialer service initialized with carrier integration');
  }

  /**
   * Make an outbound call
   * @param {Object} callParams - Call parameters
   * @param {string} callParams.to - Destination number
   * @param {string} callParams.from - Source number
   * @param {string} callParams.protocol - Call protocol (SIP, WebRTC, PSTN)
   * @param {Object} callParams.options - Additional options
   */
  async makeCall(callParams) {
    try {
      const { to, from, protocol = 'SIP', options = {} } = callParams;

      // Validate phone numbers
      if (!validatePhoneNumber(to)) {
        throw new Error('Invalid destination number');
      }

      // Check regulation compliance
      const compliance = await getRegulationCompliance(to, from);
      if (!compliance.allowed) {
        throw new Error(`Call blocked: ${compliance.reason}`);
      }

      // Create call session
      const callSession = new CallSession({
        id: this.generateCallId(),
        from,
        to,
        protocol,
        direction: 'outbound',
        startTime: new Date(),
        status: 'initiating'
      });

      this.activeCalls.set(callSession.id, callSession);

      // Route call based on protocol
      let callResult;
      switch (protocol.toLowerCase()) {
      case 'sip':
        callResult = await this.makeSIPCall(callSession, options);
        break;
      case 'webrtc':
        callResult = await this.makeWebRTCCall(callSession, options);
        break;
      case 'pstn':
        callResult = await this.makePSTNCall(callSession, options);
        break;
      default:
        throw new Error(`Unsupported protocol: ${protocol}`);
      }

      // Update call status
      callSession.status = 'ringing';
      callSession.sessionData = callResult;

      // Initialize AI handling if enabled
      if (options.aiEnabled) {
        await this.aiHandler.initializeCallHandling(callSession.id);
      }

      this.emit('callInitiated', callSession);
      logger.info(`Call initiated: ${callSession.id} from ${from} to ${to}`);

      return callSession;

    } catch (error) {
      logger.error('Failed to make call:', error);
      throw error;
    }
  }

  /**
   * Answer an incoming call
   * @param {string} callId - Call session ID
   * @param {Object} options - Answer options
   */
  async answerCall(callId, options = {}) {
    try {
      const callSession = this.activeCalls.get(callId);
      if (!callSession) {
        throw new Error('Call session not found');
      }

      callSession.status = 'connected';
      callSession.answerTime = new Date();

      // Initialize AI handling for incoming call
      if (options.aiEnabled) {
        await this.aiHandler.handleIncomingCall(callId, options.aiMode);
      }

      this.emit('callAnswered', callSession);
      logger.info(`Call answered: ${callId}`);

      return callSession;

    } catch (error) {
      logger.error('Failed to answer call:', error);
      throw error;
    }
  }

  /**
   * End a call
   * @param {string} callId - Call session ID
   * @param {string} reason - Reason for ending call
   */
  async endCall(callId, reason = 'user_hangup') {
    try {
      const callSession = this.activeCalls.get(callId);
      if (!callSession) {
        throw new Error('Call session not found');
      }

      // End the actual call session based on protocol
      switch (callSession.protocol.toLowerCase()) {
      case 'sip':
        await this.endSIPCall(callSession);
        break;
      case 'webrtc':
        await this.endWebRTCCall(callSession);
        break;
      case 'pstn':
        await this.endPSTNCall(callSession);
        break;
      }

      // Update call session
      callSession.status = 'ended';
      callSession.endTime = new Date();
      callSession.endReason = reason;
      callSession.duration = callSession.endTime - callSession.startTime;

      // Clean up AI handling
      if (this.aiHandler) {
        await this.aiHandler.cleanup(callId);
      }

      // Remove from active calls
      this.activeCalls.delete(callId);

      this.emit('callEnded', callSession);
      logger.info(`Call ended: ${callId}, duration: ${callSession.duration}ms`);

      return callSession;

    } catch (error) {
      logger.error('Failed to end call:', error);
      throw error;
    }
  }

  /**
   * Transfer a call to another destination
   * @param {string} callId - Call session ID
   * @param {string} destination - Transfer destination
   * @param {Object} options - Transfer options
   */
  async transferCall(callId, destination, options = {}) {
    try {
      const callSession = this.activeCalls.get(callId);
      if (!callSession) {
        throw new Error('Call session not found');
      }

      // Validate destination
      if (!validatePhoneNumber(destination)) {
        throw new Error('Invalid transfer destination');
      }

      // Perform transfer based on protocol
      let transferResult;
      switch (callSession.protocol.toLowerCase()) {
      case 'sip':
        transferResult = await this.transferSIPCall(callSession, destination, options);
        break;
      case 'webrtc':
        transferResult = await this.transferWebRTCCall(callSession, destination, options);
        break;
      case 'pstn':
        transferResult = await this.transferPSTNCall(callSession, destination, options);
        break;
      default:
        throw new Error(`Transfer not supported for protocol: ${callSession.protocol}`);
      }

      // Update call session
      callSession.transferredTo = destination;
      callSession.transferTime = new Date();

      this.emit('callTransferred', { callSession, destination });
      logger.info(`Call transferred: ${callId} to ${destination}`);

      return transferResult;

    } catch (error) {
      logger.error('Failed to transfer call:', error);
      throw error;
    }
  }

  /**
   * Put a call on hold
   * @param {string} callId - Call session ID
   */
  async holdCall(callId) {
    try {
      const callSession = this.activeCalls.get(callId);
      if (!callSession) {
        throw new Error('Call session not found');
      }

      callSession.status = 'hold';
      callSession.holdTime = new Date();

      this.emit('callHold', callSession);
      logger.info(`Call on hold: ${callId}`);

      return callSession;

    } catch (error) {
      logger.error('Failed to hold call:', error);
      throw error;
    }
  }

  /**
   * Resume a call from hold
   * @param {string} callId - Call session ID
   */
  async resumeCall(callId) {
    try {
      const callSession = this.activeCalls.get(callId);
      if (!callSession) {
        throw new Error('Call session not found');
      }

      callSession.status = 'connected';
      callSession.resumeTime = new Date();

      this.emit('callResume', callSession);
      logger.info(`Call resumed: ${callId}`);

      return callSession;

    } catch (error) {
      logger.error('Failed to resume call:', error);
      throw error;
    }
  }

  /**
   * Get active calls
   */
  getActiveCalls() {
    return Array.from(this.activeCalls.values());
  }

  /**
   * Generate unique call ID
   */
  generateCallId() {
    return `call_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  // Protocol-specific implementation methods
  async makeSIPCall(callSession, options) {
    // SIP call implementation
    return this.sipStack.invite(callSession.to, options);
  }

  async makeWebRTCCall(callSession, options) {
    // WebRTC call implementation
    const peer = this.webrtcPeers.get(callSession.to);
    if (!peer) {
      throw new Error('WebRTC peer not found');
    }
    return peer.call(options);
  }

  async makePSTNCall(callSession, options) {
    // PSTN call implementation via Twilio
    const twilio = require('twilio')(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN);
    return twilio.calls.create({
      to: callSession.to,
      from: callSession.from,
      ...options
    });
  }

  async endSIPCall(callSession) {
    return this.sipStack.bye(callSession.sessionData);
  }

  async endWebRTCCall(callSession) {
    const peer = this.webrtcPeers.get(callSession.to);
    if (peer) {
      peer.destroy();
      this.webrtcPeers.delete(callSession.to);
    }
  }

  async endPSTNCall(callSession) {
    const twilio = require('twilio')(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN);
    return twilio.calls(callSession.sessionData.sid).update({ status: 'completed' });
  }

  async transferSIPCall(callSession, destination, options) {
    return this.sipStack.refer(callSession.sessionData, destination, options);
  }

  async transferWebRTCCall(_callSession, _destination, _options) {
    // WebRTC transfer implementation
    throw new Error('WebRTC transfer not yet implemented');
  }

  async transferPSTNCall(callSession, destination, _options) {
    const twilio = require('twilio')(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN);
    return twilio.calls(callSession.sessionData.sid).update({
      method: 'POST',
      url: `${process.env.BASE_URL}/api/calls/transfer/${destination}`
    });
  }
}

module.exports = DialerService;
