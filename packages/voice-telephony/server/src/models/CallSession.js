/**
 * Call Session Model
 * Represents a call session with all its properties and methods
 */

class CallSession {
  constructor(data = {}) {
    this.id = data.id || null;
    this.from = data.from || null;
    this.to = data.to || null;
    this.protocol = data.protocol || 'SIP';
    this.direction = data.direction || 'outbound'; // outbound, inbound
    this.status = data.status || 'initiating'; // initiating, ringing, connected, hold, ended, failed
    this.startTime = data.startTime || null;
    this.answerTime = data.answerTime || null;
    this.endTime = data.endTime || null;
    this.duration = data.duration || null;
    this.endReason = data.endReason || null;
    this.transferredTo = data.transferredTo || null;
    this.transferTime = data.transferTime || null;
    this.holdTime = data.holdTime || null;
    this.resumeTime = data.resumeTime || null;
    this.sessionData = data.sessionData || {}; // Protocol-specific session data
    this.metadata = data.metadata || {}; // Additional metadata
    this.recording = data.recording || false;
    this.recordingPath = data.recordingPath || null;
    this.aiEnabled = data.aiEnabled || false;
    this.aiData = data.aiData || {};
    this.quality = data.quality || null; // Call quality metrics
    this.cost = data.cost || null;
    this.createdAt = data.createdAt || new Date();
    this.updatedAt = data.updatedAt || new Date();
  }

  /**
   * Update call status
   * @param {string} status - New status
   * @param {Object} data - Additional data to update
   */
  updateStatus(status, data = {}) {
    this.status = status;
    this.updatedAt = new Date();

    // Update specific timestamps based on status
    switch (status) {
    case 'connected':
      if (!this.answerTime) {
        this.answerTime = new Date();
      }
      break;
    case 'ended':
      if (!this.endTime) {
        this.endTime = new Date();
      }
      if (this.answerTime) {
        this.duration = this.endTime - this.answerTime;
      }
      break;
    case 'hold':
      this.holdTime = new Date();
      break;
    case 'resumed': // Resuming from hold
      if (this.holdTime) {
        this.resumeTime = new Date();
      }
      break;
    }

    // Update additional data
    Object.assign(this, data);
  }

  /**
   * Get call duration in milliseconds
   * @returns {number} - Duration in milliseconds
   */
  getDuration() {
    if (this.duration) {
      return this.duration;
    }

    if (this.answerTime) {
      const endTime = this.endTime || new Date();
      return endTime - this.answerTime;
    }

    return 0;
  }

  /**
   * Get call duration in human readable format
   * @returns {string} - Formatted duration
   */
  getFormattedDuration() {
    const duration = this.getDuration();
    const seconds = Math.floor(duration / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);

    if (hours > 0) {
      return `${hours}:${(minutes % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`;
    } else {
      return `${minutes}:${(seconds % 60).toString().padStart(2, '0')}`;
    }
  }

  /**
   * Check if call is active
   * @returns {boolean} - True if call is active
   */
  isActive() {
    return ['ringing', 'connected', 'hold'].includes(this.status);
  }

  /**
   * Check if call was answered
   * @returns {boolean} - True if call was answered
   */
  wasAnswered() {
    return this.answerTime !== null;
  }

  /**
   * Get call summary
   * @returns {Object} - Call summary
   */
  getSummary() {
    return {
      id: this.id,
      from: this.from,
      to: this.to,
      direction: this.direction,
      status: this.status,
      duration: this.getFormattedDuration(),
      answered: this.wasAnswered(),
      startTime: this.startTime,
      endTime: this.endTime,
      protocol: this.protocol
    };
  }

  /**
   * Convert to JSON for storage
   * @returns {Object} - JSON representation
   */
  toJSON() {
    return {
      id: this.id,
      from: this.from,
      to: this.to,
      protocol: this.protocol,
      direction: this.direction,
      status: this.status,
      startTime: this.startTime,
      answerTime: this.answerTime,
      endTime: this.endTime,
      duration: this.duration,
      endReason: this.endReason,
      transferredTo: this.transferredTo,
      transferTime: this.transferTime,
      holdTime: this.holdTime,
      resumeTime: this.resumeTime,
      sessionData: this.sessionData,
      metadata: this.metadata,
      recording: this.recording,
      recordingPath: this.recordingPath,
      aiEnabled: this.aiEnabled,
      aiData: this.aiData,
      quality: this.quality,
      cost: this.cost,
      createdAt: this.createdAt,
      updatedAt: this.updatedAt
    };
  }

  /**
   * Create from JSON data
   * @param {Object} json - JSON data
   * @returns {CallSession} - CallSession instance
   */
  static fromJSON(json) {
    return new CallSession(json);
  }

  /**
   * Validate call session data
   * @returns {Object} - Validation result
   */
  validate() {
    const errors = [];

    if (!this.from) {
      errors.push('From number is required');
    }

    if (!this.to) {
      errors.push('To number is required');
    }

    if (!this.protocol) {
      errors.push('Protocol is required');
    }

    if (!['inbound', 'outbound'].includes(this.direction)) {
      errors.push('Direction must be inbound or outbound');
    }

    const validStatuses = ['initiating', 'ringing', 'connected', 'hold', 'ended', 'failed'];
    if (!validStatuses.includes(this.status)) {
      errors.push('Invalid status');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }
}

module.exports = { CallSession };
