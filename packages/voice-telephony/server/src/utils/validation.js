/**
 * Validation utilities
 */

/**
 * Validate phone number format
 * @param {string} phoneNumber - Phone number to validate
 * @returns {boolean} - True if valid
 */
function validatePhoneNumber(phoneNumber) {
  if (!phoneNumber || typeof phoneNumber !== 'string') {
    return false;
  }

  // Remove all non-digit characters
  const cleaned = phoneNumber.replace(/\D/g, '');

  // Check various phone number formats
  const patterns = [
    /^\d{10}$/, // US 10-digit
    /^1\d{10}$/, // US with country code
    /^\d{11}$/, // International 11-digit
    /^\d{12}$/, // International 12-digit
    /^\d{13}$/, // International 13-digit
    /^\d{14}$/, // International 14-digit
    /^\d{15}$/ // International 15-digit (max ITU-T E.164)
  ];

  return patterns.some(pattern => pattern.test(cleaned));
}

/**
 * Validate SIP URI format
 * @param {string} sipUri - SIP URI to validate
 * @returns {boolean} - True if valid
 */
function validateSIPUri(sipUri) {
  if (!sipUri || typeof sipUri !== 'string') {
    return false;
  }

  const sipUriPattern = /^sip:([a-zA-Z0-9_\-.]+)@([a-zA-Z0-9_\-.]+)(:(\d+))?$/;
  return sipUriPattern.test(sipUri);
}

/**
 * Validate email address
 * @param {string} email - Email to validate
 * @returns {boolean} - True if valid
 */
function validateEmail(email) {
  if (!email || typeof email !== 'string') {
    return false;
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailPattern.test(email);
}

/**
 * Validate IP address
 * @param {string} ip - IP address to validate
 * @returns {boolean} - True if valid
 */
function validateIPAddress(ip) {
  if (!ip || typeof ip !== 'string') {
    return false;
  }

  const ipv4Pattern = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
  const ipv6Pattern = /^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$/;

  return ipv4Pattern.test(ip) || ipv6Pattern.test(ip);
}

/**
 * Validate call duration
 * @param {number} duration - Duration in milliseconds
 * @returns {boolean} - True if valid
 */
function validateCallDuration(duration) {
  return typeof duration === 'number' && duration >= 0 && duration <= (24 * 60 * 60 * 1000); // Max 24 hours
}

/**
 * Sanitize phone number
 * @param {string} phoneNumber - Phone number to sanitize
 * @returns {string} - Sanitized phone number
 */
function sanitizePhoneNumber(phoneNumber) {
  if (!phoneNumber || typeof phoneNumber !== 'string') {
    return '';
  }

  // Remove all non-digit characters except +
  let cleaned = phoneNumber.replace(/[^\d+]/g, '');

  // If starts with +, keep it, otherwise remove any + in middle
  if (cleaned.startsWith('+')) {
    cleaned = '+' + cleaned.substring(1).replace(/\+/g, '');
  } else {
    cleaned = cleaned.replace(/\+/g, '');
  }

  return cleaned;
}

/**
 * Format phone number for display
 * @param {string} phoneNumber - Phone number to format
 * @returns {string} - Formatted phone number
 */
function formatPhoneNumber(phoneNumber) {
  const cleaned = sanitizePhoneNumber(phoneNumber);

  if (!cleaned) {
    return '';
  }

  // Format US numbers
  if (cleaned.length === 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }

  // Format US numbers with country code
  if (cleaned.length === 11 && cleaned.startsWith('1')) {
    return `+1 (${cleaned.slice(1, 4)}) ${cleaned.slice(4, 7)}-${cleaned.slice(7)}`;
  }

  // Format international numbers
  if (cleaned.startsWith('+')) {
    return cleaned;
  }

  return `+${cleaned}`;
}

/**
 * Validate call parameters
 * @param {Object} params - Call parameters
 * @returns {Object} - Validation result
 */
function validateCallParams(params) {
  const errors = [];

  if (!params.to) {
    errors.push('Destination number is required');
  } else if (!validatePhoneNumber(params.to)) {
    errors.push('Invalid destination number format');
  }

  if (!params.from) {
    errors.push('Source number is required');
  } else if (!validatePhoneNumber(params.from)) {
    errors.push('Invalid source number format');
  }

  if (params.protocol) {
    const validProtocols = ['SIP', 'WebRTC', 'PSTN'];
    if (!validProtocols.includes(params.protocol.toUpperCase())) {
      errors.push('Invalid protocol. Must be SIP, WebRTC, or PSTN');
    }
  }

  return {
    isValid: errors.length === 0,
    errors
  };
}

/**
 * Validate user input for security
 * @param {string} input - User input to validate
 * @returns {boolean} - True if safe
 */
function validateUserInput(input) {
  if (!input || typeof input !== 'string') {
    return false;
  }

  // Check for common injection patterns
  const dangerousPatterns = [
    /<script/i,
    /javascript:/i,
    /on\w+\s*=/i,
    /eval\s*\(/i,
    /expression\s*\(/i,
    /vbscript:/i,
    /data:/i
  ];

  return !dangerousPatterns.some(pattern => pattern.test(input));
}

module.exports = {
  validatePhoneNumber,
  validateSIPUri,
  validateEmail,
  validateIPAddress,
  validateCallDuration,
  sanitizePhoneNumber,
  formatPhoneNumber,
  validateCallParams,
  validateUserInput
};
