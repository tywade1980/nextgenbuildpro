/**
 * AI Service - Handles AI-powered call processing and automation
 * Integrates with OpenAI and other AI services for intelligent call handling
 */

const { EventEmitter } = require('events');
const OpenAI = require('openai');
const logger = require('../utils/logger');
const { AIResponse } = require('../models/AIResponse');

class AIService extends EventEmitter {
  constructor() {
    super();
    this.openai = null;
    this.activeCallHandlers = new Map();
    this.conversationContexts = new Map();
    this.speechToText = null;
    this.textToSpeech = null;
  }

  /**
   * Initialize AI services
   */
  async initialize() {
    try {
      // Initialize OpenAI
      if (process.env.OPENAI_API_KEY) {
        this.openai = new OpenAI({
          apiKey: process.env.OPENAI_API_KEY
        });
        logger.info('OpenAI service initialized');
      }

      // Initialize speech services (placeholder for actual implementation)
      await this.initializeSpeechServices();

      logger.info('AI services initialized successfully');
    } catch (error) {
      logger.error('Failed to initialize AI services:', error);
      throw error;
    }
  }

  /**
   * Initialize call handling for a specific call
   * @param {string} callId - Call session ID
   * @param {Object} options - AI handling options
   */
  async initializeCallHandling(callId, options = {}) {
    try {
      const handlerConfig = {
        callId,
        mode: options.mode || 'assistant', // assistant, transcription, automation
        language: options.language || 'en-US',
        context: options.context || 'general',
        capabilities: options.capabilities || ['transcription', 'response', 'sentiment'],
        realTimeProcessing: options.realTimeProcessing || true
      };

      this.activeCallHandlers.set(callId, handlerConfig);
      this.conversationContexts.set(callId, {
        messages: [],
        sentiment: 'neutral',
        intent: null,
        entities: [],
        startTime: new Date()
      });

      this.emit('aiHandlerInitialized', { callId, config: handlerConfig });
      logger.info(`AI handler initialized for call: ${callId}`);

      return handlerConfig;
    } catch (error) {
      logger.error('Failed to initialize AI call handling:', error);
      throw error;
    }
  }

  /**
   * Handle incoming call with AI
   * @param {string} callId - Call session ID
   * @param {string} mode - AI handling mode (auto-answer, screening, assistant)
   */
  async handleIncomingCall(callId, mode = 'screening') {
    try {
      const handler = this.activeCallHandlers.get(callId);
      if (!handler) {
        throw new Error('AI handler not initialized for this call');
      }

      switch (mode) {
      case 'auto-answer':
        return await this.autoAnswerCall(callId);
      case 'screening':
        return await this.screenIncomingCall(callId);
      case 'assistant':
        return await this.assistantMode(callId);
      default:
        throw new Error(`Unsupported AI mode: ${mode}`);
      }
    } catch (error) {
      logger.error('Failed to handle incoming call with AI:', error);
      throw error;
    }
  }

  /**
   * Process audio stream for real-time transcription and AI response
   * @param {string} callId - Call session ID
   * @param {Buffer} audioBuffer - Audio data buffer
   */
  async processAudioStream(callId, audioBuffer) {
    try {
      const handler = this.activeCallHandlers.get(callId);
      if (!handler || !handler.realTimeProcessing) {
        return;
      }

      // Transcribe audio to text
      const transcription = await this.transcribeAudio(audioBuffer, handler.language);

      if (transcription && transcription.text) {
        // Update conversation context
        const context = this.conversationContexts.get(callId);
        context.messages.push({
          role: 'user',
          content: transcription.text,
          timestamp: new Date(),
          confidence: transcription.confidence
        });

        // Analyze sentiment and intent
        const analysis = await this.analyzeText(transcription.text);
        context.sentiment = analysis.sentiment;
        context.intent = analysis.intent;
        context.entities = analysis.entities;

        // Generate AI response for conversation modes
        if (['assistant', 'conversation', 'auto-answer', 'screening'].includes(handler.mode)) {
          const response = await this.generateResponse(callId, transcription.text);
          if (response) {
            await this.speakResponse(callId, response);

            // Log conversation flow
            logger.info(`Conversation flow in call ${callId}: User: "${transcription.text}" -> AI: "${response}"`);
          }
        }

        this.emit('audioProcessed', {
          callId,
          transcription,
          analysis,
          context,
          conversationMode: ['assistant', 'conversation', 'auto-answer', 'screening'].includes(handler.mode)
        });
      }
    } catch (error) {
      logger.error('Failed to process audio stream:', error);
    }
  }

  /**
   * Generate AI response based on conversation context
   * @param {string} callId - Call session ID
   * @param {string} userInput - User's input text
   */
  async generateResponse(callId, userInput) {
    try {
      const context = this.conversationContexts.get(callId);
      const handler = this.activeCallHandlers.get(callId);

      if (!context || !handler) {
        throw new Error('Call context not found');
      }

      // Build conversation history for AI with enhanced conversational context
      const messages = [
        {
          role: 'system',
          content: this.getConversationalSystemPrompt(handler.context, context)
        },
        ...context.messages.slice(-10) // Keep last 10 messages for context
      ];

      // Generate response using OpenAI with conversation-optimized parameters
      const completion = await this.openai.chat.completions.create({
        model: process.env.AI_MODEL || 'gpt-4',
        messages,
        max_tokens: 200, // Increased for more natural responses
        temperature: 0.8, // Higher for more natural conversation
        presence_penalty: 0.3, // Encourage diverse responses
        frequency_penalty: 0.2, // Reduce repetition
        top_p: 0.9 // Use nucleus sampling for better quality
      });

      const aiResponse = completion.choices[0].message.content;

      // Update conversation context
      context.messages.push({
        role: 'assistant',
        content: aiResponse,
        timestamp: new Date()
      });

      // Check if we should ask a follow-up question to keep conversation flowing
      const shouldFollowUp = await this.shouldAskFollowUp(context, aiResponse);

      let finalResponse = aiResponse;
      if (shouldFollowUp && !aiResponse.includes('?')) {
        const followUp = await this.generateFollowUpQuestion(context, aiResponse);
        if (followUp) {
          finalResponse = `${aiResponse} ${followUp}`;
          context.messages[context.messages.length - 1].content = finalResponse;
        }
      }

      // Save AI response
      const responseRecord = new AIResponse({
        callId,
        input: userInput,
        response: finalResponse,
        model: process.env.AI_MODEL,
        timestamp: new Date(),
        confidence: completion.choices[0].finish_reason === 'stop' ? 0.9 : 0.7
      });

      this.emit('aiResponseGenerated', responseRecord);
      logger.debug(`AI conversational response generated for call ${callId}: ${finalResponse}`);

      return finalResponse;
    } catch (error) {
      logger.error('Failed to generate AI response:', error);
      return null;
    }
  }

  /**
   * Auto-answer call with AI greeting and start conversation
   * @param {string} callId - Call session ID
   */
  async autoAnswerCall(callId) {
    try {
      const greeting = await this.generateGreeting(callId);
      await this.speakResponse(callId, greeting);

      // Initialize conversation mode for continuous interaction
      const handler = this.activeCallHandlers.get(callId);
      if (handler) {
        handler.mode = 'conversation'; // Switch to conversation mode
        handler.realTimeProcessing = true; // Enable real-time processing
      }

      logger.info(`Auto-answered call ${callId} with AI greeting and conversation mode enabled`);
      return { answered: true, greeting, conversationMode: true };
    } catch (error) {
      logger.error('Failed to auto-answer call:', error);
      throw error;
    }
  }

  /**
   * Screen incoming call using AI and start conversation if appropriate
   * @param {string} callId - Call session ID
   */
  async screenIncomingCall(callId) {
    try {
      const screeningMessage = 'Hello! I\'m your AI assistant. Please tell me your name and how I can help you today.';
      await this.speakResponse(callId, screeningMessage);

      // Enable conversation mode after screening
      const handler = this.activeCallHandlers.get(callId);
      if (handler) {
        handler.mode = 'conversation';
        handler.realTimeProcessing = true;
      }

      logger.info(`Screening call ${callId} with AI and enabling conversation mode`);
      return { screening: true, message: screeningMessage, conversationMode: true };
    } catch (error) {
      logger.error('Failed to screen call:', error);
      throw error;
    }
  }

  /**
   * Assistant mode for ongoing conversational support
   * @param {string} callId - Call session ID
   */
  async assistantMode(callId) {
    try {
      const assistantMessage = 'Hello! I\'m your AI assistant. I\'m here to have a conversation and help with whatever you need. What\'s on your mind today?';
      await this.speakResponse(callId, assistantMessage);

      // Set up for continuous conversation
      const handler = this.activeCallHandlers.get(callId);
      if (handler) {
        handler.mode = 'conversation';
        handler.realTimeProcessing = true;
        handler.capabilities.push('continuous_conversation');
      }

      logger.info(`AI conversational assistant activated for call ${callId}`);
      return { assistant: true, message: assistantMessage, conversationMode: true };
    } catch (error) {
      logger.error('Failed to activate assistant mode:', error);
      throw error;
    }
  }

  /**
   * Transcribe audio to text
   * @param {Buffer} audioBuffer - Audio data
   * @param {string} language - Language code
   */
  async transcribeAudio(audioBuffer, language = 'en-US') {
    try {
      // Placeholder for actual speech-to-text implementation
      // This would integrate with services like OpenAI Whisper, Google Speech-to-Text, etc.

      // For now, return a mock transcription
      return {
        text: 'Mock transcription of audio',
        confidence: 0.95,
        language,
        timestamp: new Date()
      };
    } catch (error) {
      logger.error('Failed to transcribe audio:', error);
      return null;
    }
  }

  /**
   * Convert text to speech and play in call
   * @param {string} callId - Call session ID
   * @param {string} text - Text to speak
   */
  async speakResponse(callId, text) {
    try {
      // Placeholder for text-to-speech implementation
      // This would integrate with services like OpenAI TTS, Google Text-to-Speech, etc.

      logger.debug(`Speaking response in call ${callId}: ${text}`);

      this.emit('aiSpeaking', { callId, text });
      return true;
    } catch (error) {
      logger.error('Failed to speak response:', error);
      return false;
    }
  }

  /**
   * Analyze text for sentiment and intent
   * @param {string} text - Text to analyze
   */
  async analyzeText(text) {
    try {
      // Use OpenAI for text analysis
      const completion = await this.openai.chat.completions.create({
        model: 'gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: 'Analyze the following text and return JSON with sentiment (positive/negative/neutral), intent, and entities. Be concise.'
          },
          {
            role: 'user',
            content: text
          }
        ],
        max_tokens: 100,
        temperature: 0.1
      });

      const analysis = JSON.parse(completion.choices[0].message.content);
      return {
        sentiment: analysis.sentiment || 'neutral',
        intent: analysis.intent || 'unknown',
        entities: analysis.entities || [],
        confidence: 0.8
      };
    } catch (error) {
      logger.error('Failed to analyze text:', error);
      return {
        sentiment: 'neutral',
        intent: 'unknown',
        entities: [],
        confidence: 0.0
      };
    }
  }

  /**
   * Generate appropriate greeting based on context and time
   * @param {string} callId - Call session ID
   */
  async generateGreeting(callId) {
    const handler = this.activeCallHandlers.get(callId);
    const timeOfDay = new Date().getHours();

    let timeGreeting = 'Hello';
    if (timeOfDay < 12) {
      timeGreeting = 'Good morning';
    } else if (timeOfDay < 18) {
      timeGreeting = 'Good afternoon';
    } else {
      timeGreeting = 'Good evening';
    }

    // Generate context-aware greeting using AI
    try {
      const completion = await this.openai.chat.completions.create({
        model: 'gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: 'Generate a warm, friendly phone greeting (1-2 sentences) that sounds natural and inviting for conversation. Include the time-based greeting provided.'
          },
          {
            role: 'user',
            content: `Create a conversational greeting starting with "${timeGreeting}" for a ${handler?.context || 'general'} context call.`
          }
        ],
        max_tokens: 50,
        temperature: 0.7
      });

      return completion.choices[0].message.content.trim();
    } catch (error) {
      logger.error('Failed to generate AI greeting:', error);
      // Fallback to basic greeting
      return `${timeGreeting}! Thanks for calling. I'm your AI assistant and I'm here to chat and help with whatever you need. What's going on today?`;
    }
  }

  /**
   * Get system prompt based on context
   * @param {string} context - Context type
   */
  getSystemPrompt(context) {
    const prompts = {
      general: 'You are a helpful AI assistant taking phone calls. Be polite, professional, and concise in your responses.',
      customer_service: 'You are a customer service AI assistant. Help resolve customer issues efficiently and professionally.',
      sales: 'You are a sales AI assistant. Help customers understand products and services while being helpful and not pushy.',
      support: 'You are a technical support AI assistant. Help users troubleshoot issues with clear, step-by-step guidance.'
    };

    return prompts[context] || prompts.general;
  }

  /**
   * Get enhanced conversational system prompt based on context
   * @param {string} context - Context type
   * @param {Object} conversationContext - Current conversation state
   */
  getConversationalSystemPrompt(context, conversationContext) {
    const basePrompt = this.getSystemPrompt(context);
    const conversationalEnhancement = `

You are engaging in a natural phone conversation. Follow these guidelines:
- Speak naturally and conversationally, as if talking to a friend
- Ask follow-up questions to keep the conversation flowing
- Show genuine interest in what the caller is saying
- Use empathetic responses when appropriate
- Keep responses concise but engaging (1-3 sentences typically)
- If the caller seems to be ending the conversation, gracefully acknowledge it
- Remember previous parts of this conversation and reference them naturally
- Use the caller's name if they've provided it
- Be helpful and try to understand what the caller really needs

Current conversation sentiment: ${conversationContext.sentiment}
Conversation duration: ${conversationContext.messages.length} exchanges
Key topics discussed: ${this.extractTopics(conversationContext.messages)}`;

    return basePrompt + conversationalEnhancement;
  }

  /**
   * Extract key topics from conversation messages
   * @param {Array} messages - Conversation messages
   */
  extractTopics(messages) {
    const userMessages = messages.filter(msg => msg.role === 'user').slice(-3);
    return userMessages.map(msg => msg.content.substring(0, 50)).join(', ') || 'No topics yet';
  }

  /**
   * Determine if AI should ask a follow-up question
   * @param {Object} context - Conversation context
   * @param {string} response - AI's current response
   */
  async shouldAskFollowUp(context, response) {
    try {
      // Don't ask follow-up if response already has a question
      if (response.includes('?')) {
        return false;
      }

      // Don't ask follow-up if conversation is getting too long
      if (context.messages.length > 20) {
        return false;
      }

      // Don't ask follow-up if last user message seemed like ending conversation
      const lastUserMessage = context.messages.filter(msg => msg.role === 'user').slice(-1)[0];
      if (lastUserMessage && this.isEndingConversation(lastUserMessage.content)) {
        return false;
      }

      // Ask follow-up if conversation sentiment is positive or neutral
      return ['positive', 'neutral'].includes(context.sentiment);
    } catch (error) {
      logger.error('Error determining follow-up:', error);
      return false;
    }
  }

  /**
   * Generate a natural follow-up question
   * @param {Object} context - Conversation context
   * @param {string} currentResponse - Current AI response
   */
  async generateFollowUpQuestion(context, currentResponse) {
    try {
      const completion = await this.openai.chat.completions.create({
        model: 'gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: 'Generate a brief, natural follow-up question (max 15 words) to keep the conversation flowing. The question should relate to what was just discussed and show genuine interest.'
          },
          {
            role: 'user',
            content: `Based on this conversation context and my response "${currentResponse}", suggest a brief follow-up question.`
          }
        ],
        max_tokens: 30,
        temperature: 0.8
      });

      return completion.choices[0].message.content.trim();
    } catch (error) {
      logger.error('Failed to generate follow-up question:', error);
      return null;
    }
  }

  /**
   * Check if user message indicates ending conversation
   * @param {string} message - User message
   */
  isEndingConversation(message) {
    const endingPhrases = [
      'bye', 'goodbye', 'talk to you later', 'gotta go', 'have to go',
      'thanks for your help', 'that\'s all', 'nothing else', 'hang up'
    ];
    const lowerMessage = message.toLowerCase();
    return endingPhrases.some(phrase => lowerMessage.includes(phrase));
  }

  /**
   * Initialize speech-to-text and text-to-speech services
   */
  async initializeSpeechServices() {
    // Placeholder for actual speech service initialization
    // This would set up connections to speech processing services
    logger.info('Speech services initialized (placeholder)');
  }

  /**
   * Start continuous conversation mode
   * @param {string} callId - Call session ID
   * @param {Object} options - Conversation options
   */
  async startConversation(callId, options = {}) {
    try {
      const handler = this.activeCallHandlers.get(callId);
      if (!handler) {
        throw new Error('AI handler not found for this call');
      }

      // Configure for conversation
      handler.mode = 'conversation';
      handler.realTimeProcessing = true;
      handler.conversationStarted = true;
      handler.conversationOptions = {
        personality: options.personality || 'friendly',
        topics: options.topics || ['general'],
        responseStyle: options.responseStyle || 'natural',
        maxTurns: options.maxTurns || 50
      };

      // Send conversation starter
      const starter = await this.generateConversationStarter(callId, options);
      await this.speakResponse(callId, starter);

      this.emit('conversationStarted', { callId, options: handler.conversationOptions });
      logger.info(`Continuous conversation started for call ${callId}`);

      return { started: true, message: starter };
    } catch (error) {
      logger.error('Failed to start conversation:', error);
      throw error;
    }
  }

  /**
   * Generate conversation starter based on context
   * @param {string} callId - Call session ID
   * @param {Object} options - Options for conversation
   */
  async generateConversationStarter(callId, options) {
    try {
      const completion = await this.openai.chat.completions.create({
        model: 'gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: `You are starting a friendly phone conversation. Generate an engaging conversation starter that's ${options.personality || 'friendly'} and invites the caller to share something about themselves or their day.`
          },
          {
            role: 'user',
            content: 'Generate a natural conversation starter for a phone call.'
          }
        ],
        max_tokens: 60,
        temperature: 0.8
      });

      return completion.choices[0].message.content.trim();
    } catch (error) {
      logger.error('Failed to generate conversation starter:', error);
      return 'I\'m really glad you called! I\'d love to chat with you. How has your day been going so far?';
    }
  }

  /**
   * Cleanup AI handler for a call
   * @param {string} callId - Call session ID
   */
  async cleanup(callId) {
    try {
      this.activeCallHandlers.delete(callId);
      this.conversationContexts.delete(callId);

      this.emit('aiHandlerCleanup', { callId });
      logger.info(`AI handler cleaned up for call: ${callId}`);
    } catch (error) {
      logger.error('Failed to cleanup AI handler:', error);
    }
  }

  /**
   * Get conversation summary for a call
   * @param {string} callId - Call session ID
   */
  async getConversationSummary(callId) {
    try {
      const context = this.conversationContexts.get(callId);
      if (!context || context.messages.length === 0) {
        return null;
      }

      const conversationText = context.messages
        .map(msg => `${msg.role}: ${msg.content}`)
        .join('\n');

      const completion = await this.openai.chat.completions.create({
        model: 'gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: 'Summarize this phone conversation in 2-3 sentences. Focus on key points and outcomes.'
          },
          {
            role: 'user',
            content: conversationText
          }
        ],
        max_tokens: 150,
        temperature: 0.3
      });

      return completion.choices[0].message.content;
    } catch (error) {
      logger.error('Failed to generate conversation summary:', error);
      return null;
    }
  }
}

module.exports = AIService;
