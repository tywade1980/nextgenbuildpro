/**
 * SIP Service - Handles SIP protocol for traditional telephony integration
 * Enables connectivity with PSTN, PBX systems, and SIP providers
 */

const { EventEmitter } = require('events');
const JsSIP = require('jssip');
const logger = require('../utils/logger');

class SIPService extends EventEmitter {
  constructor() {
    super();
    this.sipStack = null;
    this.registeredAccounts = new Map();
    this.activeSessions = new Map();
    this.sipDomain = process.env.SIP_DOMAIN;
    this.sipUsername = process.env.SIP_USERNAME;
    this.sipPassword = process.env.SIP_PASSWORD;
    this.localPort = 5060;
    this.publicIP = null;
  }

  /**
   * Initialize SIP service
   */
  async initialize() {
    try {
      await this.initSIPStack();
      logger.info('SIP handlers setup complete');
    } catch (error) {
      logger.error('Failed to initialize SIP service:', error);
      throw error;
    }
  }

  /**
   * Initialize SIP stack
   */
  async initSIPStack() {
    try {
      // JsSIP configuration
      const socket = new JsSIP.WebSocketInterface('wss://sip.example.com:443');
      const configuration = {
        sockets: [socket],
        uri: `sip:${this.sipUsername}@${this.sipDomain}`,
        password: this.sipPassword,
        register: true,
        session_timers: false
      };

      // Create JsSIP UA (User Agent)
      this.sipStack = new JsSIP.UA(configuration);

      // Setup event handlers
      this.setupSIPHandlers();

      // Start the SIP stack
      this.sipStack.start();

      logger.info('SIP stack initialized with JsSIP');
      this.emit('sipInitialized');
    } catch (error) {
      logger.error('Failed to initialize SIP stack:', error);
      throw error;
    }
  }

  /**
   * Setup SIP message handlers
   */
  setupSIPHandlers() {
    if (!this.sipStack) return;

    // Handle registration events
    this.sipStack.on('registered', () => {
      logger.info('SIP registration successful');
      this.emit('registered', { status: 'registered' });
    });

    this.sipStack.on('unregistered', () => {
      logger.info('SIP unregistered');
      this.emit('unregistered');
    });

    this.sipStack.on('registrationFailed', (e) => {
      logger.error('SIP registration failed:', e);
      this.emit('registrationFailed', e);
    });

    // Handle incoming calls
    this.sipStack.on('newRTCSession', (e) => {
      const session = e.session;
      if (session.direction === 'incoming') {
        this.handleIncomingCall(session);
      }
    });

    logger.info('SIP handlers setup complete');
  }

  /**
   * Setup session event handlers
   * @param {Object} session - JsSIP session
   * @param {Object} sessionData - Session data
   */
  setupSessionHandlers(session, sessionData) {
    session.on('progress', () => {
      sessionData.status = 'ringing';
      this.emit('callProgress', sessionData);
    });

    session.on('accepted', () => {
      sessionData.status = 'connected';
      sessionData.answerTime = new Date();
      this.emit('callAnswered', sessionData);
    });

    session.on('ended', () => {
      sessionData.status = 'ended';
      sessionData.endTime = new Date();
      this.activeSessions.delete(sessionData.callId);
      this.emit('callEnded', sessionData);
    });

    session.on('failed', (e) => {
      sessionData.status = 'failed';
      sessionData.failureReason = e.cause;
      this.activeSessions.delete(sessionData.callId);
      this.emit('callFailed', sessionData);
    });
  }

  /**
   * Handle registration response
   * @param {Object} response - SIP response
   * @param {Object} registrationData - Registration data
   */
  handleRegistrationResponse(response, registrationData) {
    try {
      if (response.status === 200) {
        registrationData.status = 'registered';
        registrationData.registeredAt = new Date();

        logger.info(`SIP registration successful: ${registrationData.username}@${registrationData.domain}`);
        this.emit('registered', registrationData);
      } else if (response.status === 401 || response.status === 407) {
        // Handle authentication challenge
        this.handleAuthenticationChallenge(response, registrationData);
      } else {
        registrationData.status = 'failed';
        logger.error(`SIP registration failed: ${response.status} ${response.reason}`);
        this.emit('registrationFailed', { registrationData, response });
      }
    } catch (error) {
      logger.error('Failed to handle registration response:', error);
    }
  }

  /**
   * Handle authentication challenge
   * @param {Object} response - SIP response with authentication challenge
   * @param {Object} registrationData - Registration data
   */
  handleAuthenticationChallenge(response, registrationData) {
    try {
      const wwwAuth = response.headers['www-authenticate'] || response.headers['proxy-authenticate'];
      if (!wwwAuth) {
        logger.error('No authentication header in challenge response');
        return;
      }

      // Parse authentication parameters
      const authParams = this.parseAuthHeader(wwwAuth);

      // Generate authentication response
      const authResponse = this.generateAuthResponse(
        registrationData.username,
        registrationData.password,
        'REGISTER',
        `sip:${registrationData.domain}`,
        authParams
      );

      // Resend REGISTER with authentication
      const registerRequest = {
        method: 'REGISTER',
        uri: `sip:${registrationData.domain}`,
        headers: {
          to: { uri: `sip:${registrationData.username}@${registrationData.domain}` },
          from: { uri: `sip:${registrationData.username}@${registrationData.domain}`, tag: this.generateTag() },
          'call-id': this.generateCallId(),
          cseq: { method: 'REGISTER', seq: 2 },
          contact: [{ uri: `sip:${registrationData.username}@${this.getLocalIP()}:${this.localPort}` }],
          expires: 3600,
          authorization: authResponse
        }
      };

      this.sipStack.send(registerRequest, (response) => {
        this.handleRegistrationResponse(response, registrationData);
      });

      logger.info('Sent authenticated REGISTER request');
    } catch (error) {
      logger.error('Failed to handle authentication challenge:', error);
    }
  }

  /**
   * Make outbound SIP call
   * @param {string} to - Destination URI
   * @param {Object} options - Call options
   */
  async invite(to, options = {}) {
    try {
      if (!this.sipStack || !this.sipStack.isRegistered()) {
        throw new Error('SIP stack not registered');
      }

      const callId = this.generateCallId();
      const sessionData = {
        callId,
        to,
        from: options.from || `${this.sipUsername}@${this.sipDomain}`,
        status: 'inviting',
        startTime: new Date()
      };

      // JsSIP call options
      const callOptions = {
        mediaConstraints: { audio: true, video: false },
        ...options
      };

      // Make the call
      const session = this.sipStack.call(to, callOptions);

      sessionData.session = session;
      this.activeSessions.set(callId, sessionData);

      // Setup session event handlers
      this.setupSessionHandlers(session, sessionData);

      logger.info(`SIP INVITE sent: ${sessionData.from} -> ${to}`);

      return sessionData;
    } catch (error) {
      logger.error('Failed to send SIP INVITE:', error);
      throw error;
    }
  }

  /**
   * Handle incoming call
   * @param {Object} session - JsSIP session
   */
  handleIncomingCall(session) {
    try {
      const callId = this.generateCallId();
      const from = session.remote_identity.uri.toString();
      const to = session.local_identity.uri.toString();

      const sessionData = {
        callId,
        from,
        to,
        status: 'ringing',
        direction: 'inbound',
        startTime: new Date(),
        session
      };

      this.activeSessions.set(callId, sessionData);
      this.setupSessionHandlers(session, sessionData);

      this.emit('incomingCall', sessionData);
      logger.info(`Incoming SIP call: ${from} -> ${to}`);
    } catch (error) {
      logger.error('Failed to handle incoming call:', error);
    }
  }

  /**
   * Answer incoming call
   * @param {string} callId - Call ID
   * @param {Object} options - Answer options
   */
  async answer(callId, options = {}) {
    try {
      const sessionData = this.activeSessions.get(callId);
      if (!sessionData || !sessionData.session) {
        throw new Error('Call session not found');
      }

      // Answer the call using JsSIP
      sessionData.session.answer({
        mediaConstraints: { audio: true, video: false },
        ...options
      });

      sessionData.status = 'connected';
      sessionData.answerTime = new Date();

      this.emit('callAnswered', sessionData);
      logger.info(`SIP call answered: ${callId}`);

      return sessionData;
    } catch (error) {
      logger.error('Failed to answer SIP call:', error);
      throw error;
    }
  }

  /**
   * End SIP call
   * @param {Object} sessionData - Session data
   */
  async bye(sessionData) {
    try {
      if (sessionData.session) {
        sessionData.session.terminate();
      }

      sessionData.status = 'ended';
      sessionData.endTime = new Date();

      this.activeSessions.delete(sessionData.callId);

      this.emit('callEnded', sessionData);
      logger.info(`SIP call ended: ${sessionData.callId}`);
    } catch (error) {
      logger.error('Failed to end SIP call:', error);
      throw error;
    }
  }

  /**
   * Transfer SIP call (placeholder implementation)
   * @param {Object} sessionData - Session data
   * @param {string} destination - Transfer destination
   * @param {Object} options - Transfer options
   */
  async refer(sessionData, destination, _options = {}) {
    try {
      // JsSIP doesn't have built-in refer, would need to implement manually
      logger.warn('SIP transfer not yet implemented with JsSIP');

      sessionData.transferredTo = destination;
      sessionData.transferTime = new Date();

      this.emit('callTransferred', { sessionData, destination });
      logger.info(`SIP call transfer requested: ${sessionData.callId} -> ${destination}`);

      return { success: false, message: 'Transfer not implemented with JsSIP' };
    } catch (error) {
      logger.error('Failed to transfer SIP call:', error);
      throw error;
    }
  }

  /**
   * Handle incoming BYE
   * @param {Object} request - SIP BYE request
   */
  handleBye(request) {
    try {
      const callId = request.headers['call-id'];
      const sessionData = this.activeSessions.get(callId);

      if (sessionData) {
        sessionData.status = 'ended';
        sessionData.endTime = new Date();
        sessionData.endReason = 'remote_hangup';

        this.activeSessions.delete(callId);
        this.emit('callEnded', sessionData);
      }

      // Send 200 OK response to BYE
      this.sipStack.send({
        method: request.method,
        uri: request.uri,
        headers: request.headers,
        status: 200,
        reason: 'OK'
      });

      logger.info(`SIP BYE received for call: ${callId}`);
    } catch (error) {
      logger.error('Failed to handle BYE:', error);
    }
  }

  /**
   * Generate SDP content
   * @param {Object} options - SDP options
   */
  generateSDP(options = {}) {
    const sessionId = Date.now();
    const sessionVersion = sessionId;
    const localIP = this.getLocalIP();

    const sdp = [
      'v=0',
      `o=- ${sessionId} ${sessionVersion} IN IP4 ${localIP}`,
      's=NextGenTele SIP Session',
      'c=IN IP4 ' + localIP,
      't=0 0'
    ];

    // Audio media description
    if (options.audio !== false) {
      sdp.push(
        'm=audio 5004 RTP/AVP 0 8 101',
        'a=rtpmap:0 PCMU/8000',
        'a=rtpmap:8 PCMA/8000',
        'a=rtpmap:101 telephone-event/8000',
        'a=sendrecv'
      );
    }

    // Video media description
    if (options.video) {
      sdp.push(
        'm=video 5006 RTP/AVP 96',
        'a=rtpmap:96 H264/90000',
        'a=sendrecv'
      );
    }

    return sdp.join('\r\n') + '\r\n';
  }

  /**
   * Get local IP address
   */
  getLocalIP() {
    if (this.publicIP) {
      return this.publicIP;
    }

    const { networkInterfaces } = require('os');
    const nets = networkInterfaces();

    for (const name of Object.keys(nets)) {
      for (const net of nets[name]) {
        if (net.family === 'IPv4' && !net.internal) {
          return net.address;
        }
      }
    }

    return '127.0.0.1';
  }

  /**
   * Generate random tag
   */
  generateTag() {
    return Math.random().toString(36).substr(2, 10);
  }

  /**
   * Generate call ID
   */
  generateCallId() {
    return `call_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Parse authentication header
   * @param {string} authHeader - Authentication header value
   */
  parseAuthHeader(authHeader) {
    const params = {};
    const regex = /(\w+)="?([^",]+)"?/g;
    let match;

    while ((match = regex.exec(authHeader)) !== null) {
      params[match[1]] = match[2];
    }

    return params;
  }

  /**
   * Generate authentication response
   */
  generateAuthResponse(username, password, method, uri, authParams) {
    const crypto = require('crypto');

    const realm = authParams.realm;
    const nonce = authParams.nonce;
    const opaque = authParams.opaque;

    const ha1 = crypto.createHash('md5').update(`${username}:${realm}:${password}`).digest('hex');
    const ha2 = crypto.createHash('md5').update(`${method}:${uri}`).digest('hex');
    const response = crypto.createHash('md5').update(`${ha1}:${nonce}:${ha2}`).digest('hex');

    let authResponse = `Digest username="${username}", realm="${realm}", nonce="${nonce}", uri="${uri}", response="${response}"`;

    if (opaque) {
      authResponse += `, opaque="${opaque}"`;
    }

    return authResponse;
  }

  // Placeholder handlers for other SIP methods
  handleCancel(_request) {
    logger.info('SIP CANCEL received');
  }

  handleAck(_request) {
    logger.debug('SIP ACK received');
  }

  handleRefer(_request) {
    logger.info('SIP REFER received');
  }

  handleResponse(response) {
    logger.debug(`SIP response received: ${response.status} ${response.reason}`);
  }

  handleInviteResponse(response, sessionData) {
    logger.debug(`SIP INVITE response: ${response.status} ${response.reason}`);

    if (response.status === 200) {
      sessionData.status = 'connected';
      this.emit('callConnected', sessionData);
    } else if (response.status >= 400) {
      sessionData.status = 'failed';
      sessionData.failureReason = `${response.status} ${response.reason}`;
      this.activeSessions.delete(sessionData.callId);
      this.emit('callFailed', sessionData);
    }
  }
}

/**
 * Initialize SIP stack
 */
module.exports = SIPService;
