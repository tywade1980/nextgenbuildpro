/**
 * Tests for validation utilities
 */

const {
  validatePhoneNumber,
  validateSIPUri,
  validateEmail,
  validateCallParams,
  sanitizePhoneNumber,
  formatPhoneNumber
} = require('../src/utils/validation');

describe('Validation Utilities', () => {
  describe('validatePhoneNumber', () => {
    test('should validate US 10-digit numbers', () => {
      expect(validatePhoneNumber('5551234567')).toBe(true);
      expect(validatePhoneNumber('(555) 123-4567')).toBe(true);
      expect(validatePhoneNumber('555-123-4567')).toBe(true);
    });

    test('should validate international numbers', () => {
      expect(validatePhoneNumber('+15551234567')).toBe(true);
      expect(validatePhoneNumber('+442012345678')).toBe(true);
      expect(validatePhoneNumber('+33123456789')).toBe(true);
    });

    test('should reject invalid numbers', () => {
      expect(validatePhoneNumber('')).toBe(false);
      expect(validatePhoneNumber('123')).toBe(false);
      expect(validatePhoneNumber('abc')).toBe(false);
      expect(validatePhoneNumber(null)).toBe(false);
    });
  });

  describe('validateSIPUri', () => {
    test('should validate SIP URIs', () => {
      expect(validateSIPUri('sip:user@example.com')).toBe(true);
      expect(validateSIPUri('sip:user@192.168.1.1')).toBe(true);
      expect(validateSIPUri('sip:user@example.com:5060')).toBe(true);
    });

    test('should reject invalid SIP URIs', () => {
      expect(validateSIPUri('user@example.com')).toBe(false);
      expect(validateSIPUri('sip:user')).toBe(false);
      expect(validateSIPUri('')).toBe(false);
    });
  });

  describe('validateEmail', () => {
    test('should validate email addresses', () => {
      expect(validateEmail('user@example.com')).toBe(true);
      expect(validateEmail('test.email+tag@example.co.uk')).toBe(true);
    });

    test('should reject invalid emails', () => {
      expect(validateEmail('invalid')).toBe(false);
      expect(validateEmail('user@')).toBe(false);
      expect(validateEmail('@example.com')).toBe(false);
    });
  });

  describe('sanitizePhoneNumber', () => {
    test('should remove non-digit characters', () => {
      expect(sanitizePhoneNumber('(555) 123-4567')).toBe('5551234567');
      expect(sanitizePhoneNumber('+1-555-123-4567')).toBe('+15551234567');
    });

    test('should handle edge cases', () => {
      expect(sanitizePhoneNumber('')).toBe('');
      expect(sanitizePhoneNumber(null)).toBe('');
    });
  });

  describe('formatPhoneNumber', () => {
    test('should format US numbers', () => {
      expect(formatPhoneNumber('5551234567')).toBe('(555) 123-4567');
      expect(formatPhoneNumber('15551234567')).toBe('+1 (555) 123-4567');
    });

    test('should handle international numbers', () => {
      expect(formatPhoneNumber('+442012345678')).toBe('+442012345678');
    });
  });

  describe('validateCallParams', () => {
    test('should validate complete call parameters', () => {
      const params = {
        to: '+15551234567',
        from: '+15557654321',
        protocol: 'SIP'
      };
      const result = validateCallParams(params);
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    test('should detect missing parameters', () => {
      const params = {
        to: '+15551234567'
        // Missing 'from'
      };
      const result = validateCallParams(params);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Source number is required');
    });

    test('should detect invalid protocols', () => {
      const params = {
        to: '+15551234567',
        from: '+15557654321',
        protocol: 'INVALID'
      };
      const result = validateCallParams(params);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Invalid protocol. Must be SIP, WebRTC, or PSTN');
    });
  });
});