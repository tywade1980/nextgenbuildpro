/**
 * AI Response Model
 * Represents AI-generated responses during calls
 */

class AIResponse {
  constructor(data = {}) {
    this.id = data.id || null;
    this.callId = data.callId || null;
    this.input = data.input || '';
    this.response = data.response || '';
    this.model = data.model || 'gpt-4';
    this.confidence = data.confidence || 0;
    this.timestamp = data.timestamp || new Date();
    this.processingTime = data.processingTime || 0; // Time to generate response
    this.intent = data.intent || null;
    this.context = data.context || {};
    this.feedback = data.feedback || null; // User feedback on response quality
    this.metadata = data.metadata || {};
    this.createdAt = data.createdAt || new Date();
  }

  /**
   * Set user feedback on the response
   * @param {string} feedback - Feedback (positive, negative, neutral)
   * @param {string} comments - Additional comments
   */
  setFeedback(feedback, comments = '') {
    this.feedback = {
      rating: feedback,
      comments,
      timestamp: new Date()
    };
  }

  /**
   * Update processing metrics
   * @param {number} processingTime - Time taken to generate response
   * @param {number} confidence - Confidence in the response
   */
  updateMetrics(processingTime, confidence) {
    this.processingTime = processingTime;
    this.confidence = confidence;
  }

  /**
   * Add context information
   * @param {Object} context - Context data
   */
  addContext(context) {
    this.context = { ...this.context, ...context };
  }

  /**
   * Get response quality score
   * @returns {number} - Quality score between 0 and 1
   */
  getQualityScore() {
    let score = this.confidence;

    // Adjust based on feedback
    if (this.feedback) {
      switch (this.feedback.rating) {
      case 'positive':
        score = Math.min(1, score * 1.2);
        break;
      case 'negative':
        score = Math.max(0, score * 0.5);
        break;
      }
    }

    // Adjust based on processing time (faster is generally better for real-time)
    if (this.processingTime > 0) {
      if (this.processingTime < 1000) { // Less than 1 second
        score = Math.min(1, score * 1.1);
      } else if (this.processingTime > 5000) { // More than 5 seconds
        score = Math.max(0, score * 0.8);
      }
    }

    return Math.round(score * 100) / 100;
  }

  /**
   * Convert to JSON for storage
   * @returns {Object} - JSON representation
   */
  toJSON() {
    return {
      id: this.id,
      callId: this.callId,
      input: this.input,
      response: this.response,
      model: this.model,
      confidence: this.confidence,
      timestamp: this.timestamp,
      processingTime: this.processingTime,
      intent: this.intent,
      context: this.context,
      feedback: this.feedback,
      metadata: this.metadata,
      createdAt: this.createdAt
    };
  }

  /**
   * Create from JSON data
   * @param {Object} json - JSON data
   * @returns {AIResponse} - AIResponse instance
   */
  static fromJSON(json) {
    return new AIResponse(json);
  }

  /**
   * Validate AI response data
   * @returns {Object} - Validation result
   */
  validate() {
    const errors = [];

    if (!this.callId) {
      errors.push('Call ID is required');
    }

    if (!this.response || this.response.trim().length === 0) {
      errors.push('Response text is required');
    }

    if (this.confidence < 0 || this.confidence > 1) {
      errors.push('Confidence must be between 0 and 1');
    }

    if (this.processingTime < 0) {
      errors.push('Processing time cannot be negative');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }
}

module.exports = { AIResponse };
