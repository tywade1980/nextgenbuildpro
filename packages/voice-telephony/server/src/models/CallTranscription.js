/**
 * Call Transcription Model
 * Represents transcription data for calls
 */

class CallTranscription {
  constructor(data = {}) {
    this.id = data.id || null;
    this.callId = data.callId || null;
    this.speaker = data.speaker || 'unknown'; // user, agent, ai
    this.text = data.text || '';
    this.confidence = data.confidence || 0;
    this.language = data.language || 'en-US';
    this.timestamp = data.timestamp || new Date();
    this.startTime = data.startTime || null; // Time offset from call start
    this.endTime = data.endTime || null; // Time offset from call start
    this.audioOffset = data.audioOffset || 0; // Offset in audio file
    this.sentiment = data.sentiment || 'neutral';
    this.intent = data.intent || null;
    this.entities = data.entities || [];
    this.metadata = data.metadata || {};
    this.createdAt = data.createdAt || new Date();
  }

  /**
   * Update transcription sentiment
   * @param {string} sentiment - Sentiment value
   * @param {number} confidence - Confidence score
   */
  updateSentiment(sentiment, confidence = 0) {
    this.sentiment = sentiment;
    this.metadata.sentimentConfidence = confidence;
  }

  /**
   * Add detected entities
   * @param {Array} entities - Array of entities
   */
  addEntities(entities) {
    this.entities = [...this.entities, ...entities];
  }

  /**
   * Set intent
   * @param {string} intent - Detected intent
   * @param {number} confidence - Confidence score
   */
  setIntent(intent, confidence = 0) {
    this.intent = intent;
    this.metadata.intentConfidence = confidence;
  }

  /**
   * Get duration of this transcription segment
   * @returns {number} - Duration in milliseconds
   */
  getDuration() {
    if (this.startTime && this.endTime) {
      return this.endTime - this.startTime;
    }
    return 0;
  }

  /**
   * Convert to JSON for storage
   * @returns {Object} - JSON representation
   */
  toJSON() {
    return {
      id: this.id,
      callId: this.callId,
      speaker: this.speaker,
      text: this.text,
      confidence: this.confidence,
      language: this.language,
      timestamp: this.timestamp,
      startTime: this.startTime,
      endTime: this.endTime,
      audioOffset: this.audioOffset,
      sentiment: this.sentiment,
      intent: this.intent,
      entities: this.entities,
      metadata: this.metadata,
      createdAt: this.createdAt
    };
  }

  /**
   * Create from JSON data
   * @param {Object} json - JSON data
   * @returns {CallTranscription} - CallTranscription instance
   */
  static fromJSON(json) {
    return new CallTranscription(json);
  }

  /**
   * Validate transcription data
   * @returns {Object} - Validation result
   */
  validate() {
    const errors = [];

    if (!this.callId) {
      errors.push('Call ID is required');
    }

    if (!this.text || this.text.trim().length === 0) {
      errors.push('Transcription text is required');
    }

    if (this.confidence < 0 || this.confidence > 1) {
      errors.push('Confidence must be between 0 and 1');
    }

    const validSpeakers = ['user', 'agent', 'ai', 'unknown'];
    if (!validSpeakers.includes(this.speaker)) {
      errors.push('Invalid speaker type');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }
}

module.exports = { CallTranscription };
