/**
 * WebRTC Service - Handles WebRTC signaling and peer connections
 * Enables browser-to-browser and browser-to-server voice/video calls
 */

const { EventEmitter } = require('events');
const logger = require('../utils/logger');

class WebRTCService extends EventEmitter {
  constructor() {
    super();
    this.peers = new Map();
    this.signalingServer = null;
    this.stunServers = [];
    this.turnServers = [];
  }

  /**
   * Initialize WebRTC service
   */
  async initialize() {
    try {
      // Parse STUN servers from environment
      if (process.env.STUN_SERVERS) {
        this.stunServers = process.env.STUN_SERVERS.split(',').map(server => ({
          urls: server.trim()
        }));
      }

      // Parse TURN servers from environment
      if (process.env.TURN_SERVERS) {
        this.turnServers = process.env.TURN_SERVERS.split(',').map(server => ({
          urls: server.trim(),
          username: process.env.TURN_USERNAME,
          credential: process.env.TURN_PASSWORD
        }));
      }

      logger.info('WebRTC service initialized');
      logger.info(`STUN servers: ${this.stunServers.length}`);
      logger.info(`TURN servers: ${this.turnServers.length}`);
    } catch (error) {
      logger.error('Failed to initialize WebRTC service:', error);
      throw error;
    }
  }

  /**
   * Setup WebRTC signaling server
   * @param {SocketIO} io - Socket.IO server instance
   */
  setupSignalingServer(io) {
    this.signalingServer = io;

    io.on('connection', (socket) => {
      logger.info(`WebRTC client connected: ${socket.id}`);

      // Handle peer registration
      socket.on('register', (data) => {
        this.handlePeerRegistration(socket, data);
      });

      // Handle call offer
      socket.on('offer', (data) => {
        this.handleOffer(socket, data);
      });

      // Handle call answer
      socket.on('answer', (data) => {
        this.handleAnswer(socket, data);
      });

      // Handle ICE candidates
      socket.on('ice-candidate', (data) => {
        this.handleIceCandidate(socket, data);
      });

      // Handle call end
      socket.on('end-call', (data) => {
        this.handleEndCall(socket, data);
      });

      // Handle disconnection
      socket.on('disconnect', () => {
        this.handleDisconnection(socket);
      });
    });

    logger.info('WebRTC signaling server setup complete');
  }

  /**
   * Handle peer registration
   * @param {Socket} socket - Socket.IO socket
   * @param {Object} data - Registration data
   */
  handlePeerRegistration(socket, data) {
    try {
      const { peerId, capabilities } = data;

      this.peers.set(peerId, {
        socketId: socket.id,
        socket,
        capabilities: capabilities || { audio: true, video: false },
        status: 'available',
        registeredAt: new Date()
      });

      socket.peerId = peerId;
      socket.emit('registered', { peerId, status: 'success' });

      logger.info(`Peer registered: ${peerId} (${socket.id})`);
      this.emit('peerRegistered', { peerId, socket });
    } catch (error) {
      logger.error('Failed to register peer:', error);
      socket.emit('error', { message: 'Registration failed' });
    }
  }

  /**
   * Handle call offer
   * @param {Socket} socket - Socket.IO socket
   * @param {Object} data - Offer data
   */
  handleOffer(socket, data) {
    try {
      const { targetPeer, offer, callId } = data;
      const targetPeerInfo = this.peers.get(targetPeer);

      if (!targetPeerInfo) {
        socket.emit('call-failed', {
          callId,
          reason: 'Target peer not found'
        });
        return;
      }

      if (targetPeerInfo.status !== 'available') {
        socket.emit('call-failed', {
          callId,
          reason: 'Target peer is busy'
        });
        return;
      }

      // Update peer status
      this.peers.get(socket.peerId).status = 'calling';
      targetPeerInfo.status = 'receiving_call';

      // Forward offer to target peer
      targetPeerInfo.socket.emit('incoming-call', {
        callId,
        callerPeer: socket.peerId,
        offer,
        callerCapabilities: this.peers.get(socket.peerId).capabilities
      });

      logger.info(`Call offer forwarded: ${socket.peerId} -> ${targetPeer}`);
      this.emit('callOffer', { callerId: socket.peerId, targetPeer, callId });
    } catch (error) {
      logger.error('Failed to handle offer:', error);
      socket.emit('error', { message: 'Failed to process call offer' });
    }
  }

  /**
   * Handle call answer
   * @param {Socket} socket - Socket.IO socket
   * @param {Object} data - Answer data
   */
  handleAnswer(socket, data) {
    try {
      const { callerPeer, answer, callId, accepted } = data;
      const callerPeerInfo = this.peers.get(callerPeer);

      if (!callerPeerInfo) {
        socket.emit('error', { message: 'Caller peer not found' });
        return;
      }

      if (accepted) {
        // Update peer status
        this.peers.get(socket.peerId).status = 'in_call';
        callerPeerInfo.status = 'in_call';

        // Forward answer to caller
        callerPeerInfo.socket.emit('call-answered', {
          callId,
          targetPeer: socket.peerId,
          answer
        });

        logger.info(`Call answered: ${socket.peerId} answered ${callerPeer}`);
        this.emit('callAnswered', { callerId: callerPeer, targetPeer: socket.peerId, callId });
      } else {
        // Call rejected
        this.peers.get(socket.peerId).status = 'available';
        callerPeerInfo.status = 'available';

        callerPeerInfo.socket.emit('call-rejected', {
          callId,
          targetPeer: socket.peerId
        });

        logger.info(`Call rejected: ${socket.peerId} rejected ${callerPeer}`);
        this.emit('callRejected', { callerId: callerPeer, targetPeer: socket.peerId, callId });
      }
    } catch (error) {
      logger.error('Failed to handle answer:', error);
      socket.emit('error', { message: 'Failed to process call answer' });
    }
  }

  /**
   * Handle ICE candidate exchange
   * @param {Socket} socket - Socket.IO socket
   * @param {Object} data - ICE candidate data
   */
  handleIceCandidate(socket, data) {
    try {
      const { targetPeer, candidate, callId } = data;
      const targetPeerInfo = this.peers.get(targetPeer);

      if (targetPeerInfo) {
        targetPeerInfo.socket.emit('ice-candidate', {
          callId,
          candidate,
          fromPeer: socket.peerId
        });

        logger.debug(`ICE candidate forwarded: ${socket.peerId} -> ${targetPeer}`);
      }
    } catch (error) {
      logger.error('Failed to handle ICE candidate:', error);
    }
  }

  /**
   * Handle call end
   * @param {Socket} socket - Socket.IO socket
   * @param {Object} data - End call data
   */
  handleEndCall(socket, data) {
    try {
      const { targetPeer, callId } = data;
      const targetPeerInfo = this.peers.get(targetPeer);

      if (targetPeerInfo) {
        targetPeerInfo.socket.emit('call-ended', {
          callId,
          endedBy: socket.peerId
        });

        // Update peer status
        targetPeerInfo.status = 'available';
      }

      if (this.peers.has(socket.peerId)) {
        this.peers.get(socket.peerId).status = 'available';
      }

      logger.info(`Call ended: ${callId} by ${socket.peerId}`);
      this.emit('callEnded', { callId, endedBy: socket.peerId, targetPeer });
    } catch (error) {
      logger.error('Failed to handle call end:', error);
    }
  }

  /**
   * Handle peer disconnection
   * @param {Socket} socket - Socket.IO socket
   */
  handleDisconnection(socket) {
    try {
      if (socket.peerId) {
        const peerInfo = this.peers.get(socket.peerId);

        if (peerInfo && peerInfo.status === 'in_call') {
          // Notify other party about disconnection
          this.notifyCallParticipants(socket.peerId, 'peer-disconnected');
        }

        this.peers.delete(socket.peerId);
        logger.info(`Peer disconnected: ${socket.peerId} (${socket.id})`);
        this.emit('peerDisconnected', { peerId: socket.peerId });
      }
    } catch (error) {
      logger.error('Failed to handle disconnection:', error);
    }
  }

  /**
   * Get WebRTC configuration for clients
   */
  getWebRTCConfig() {
    return {
      iceServers: [
        ...this.stunServers,
        ...this.turnServers
      ],
      iceCandidatePoolSize: 10,
      iceTransportPolicy: 'all',
      bundlePolicy: 'balanced',
      rtcpMuxPolicy: 'require'
    };
  }

  /**
   * Get available peers
   */
  getAvailablePeers() {
    const availablePeers = [];

    for (const [peerId, peerInfo] of this.peers) {
      if (peerInfo.status === 'available') {
        availablePeers.push({
          peerId,
          capabilities: peerInfo.capabilities,
          registeredAt: peerInfo.registeredAt
        });
      }
    }

    return availablePeers;
  }

  /**
   * Initiate a call to a peer
   * @param {string} callerPeer - Caller peer ID
   * @param {string} targetPeer - Target peer ID
   * @param {Object} options - Call options
   */
  async initiateCall(callerPeer, targetPeer, options = {}) {
    try {
      const callerInfo = this.peers.get(callerPeer);
      const targetInfo = this.peers.get(targetPeer);

      if (!callerInfo || !targetInfo) {
        throw new Error('One or both peers not found');
      }

      if (targetInfo.status !== 'available') {
        throw new Error('Target peer is not available');
      }

      const callId = this.generateCallId();

      // Notify caller to start the call
      callerInfo.socket.emit('start-call', {
        callId,
        targetPeer,
        webrtcConfig: this.getWebRTCConfig(),
        options
      });

      return { callId, status: 'initiated' };
    } catch (error) {
      logger.error('Failed to initiate call:', error);
      throw error;
    }
  }

  /**
   * Notify call participants about events
   * @param {string} peerId - Peer ID
   * @param {string} event - Event type
   * @param {Object} data - Event data
   */
  notifyCallParticipants(peerId, event, data = {}) {
    try {
      // Find peers in call with this peer
      for (const [otherPeerId, peerInfo] of this.peers) {
        if (otherPeerId !== peerId && peerInfo.status === 'in_call') {
          peerInfo.socket.emit(event, { ...data, peerId });
        }
      }
    } catch (error) {
      logger.error('Failed to notify call participants:', error);
    }
  }

  /**
   * Generate unique call ID
   */
  generateCallId() {
    return `webrtc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Get peer statistics
   */
  getPeerStats() {
    const stats = {
      totalPeers: this.peers.size,
      availablePeers: 0,
      busyPeers: 0,
      inCallPeers: 0
    };

    for (const peerInfo of this.peers.values()) {
      switch (peerInfo.status) {
      case 'available':
        stats.availablePeers++;
        break;
      case 'in_call':
        stats.inCallPeers++;
        break;
      default:
        stats.busyPeers++;
      }
    }

    return stats;
  }
}

/**
 * Setup WebRTC signaling with Socket.IO
 * @param {SocketIO} io - Socket.IO server instance
 */
module.exports = WebRTCService;
