/**
 * Compliance and regulation utilities
 * Handles various telecommunication regulations and compliance requirements
 */

const logger = require('./logger');

// Country-specific regulations
const REGULATIONS = {
  US: {
    // Telephone Consumer Protection Act (TCPA)
    tcpa: {
      requiresConsent: true,
      allowedHours: { start: 8, end: 21 }, // 8 AM to 9 PM local time
      doNotCallRegistry: true,
      maxCallsPerDay: 3
    },
    // CAN-SPAM Act for SMS/MMS
    canSpam: {
      requiresOptOut: true,
      requiresIdentification: true
    },
    // FCC regulations
    fcc: {
      callerIdRequired: true,
      recordingNotification: true
    }
  },
  EU: {
    // General Data Protection Regulation (GDPR)
    gdpr: {
      requiresConsent: true,
      dataRetentionDays: 30,
      rightToBeDeleted: true,
      dataProcessingLegal: true
    },
    // ePrivacy Directive
    ePrivacy: {
      cookieConsent: true,
      electronicCommConsent: true
    }
  },
  CA: {
    // Personal Information Protection and Electronic Documents Act (PIPEDA)
    pipeda: {
      requiresConsent: true,
      dataRetentionDays: 365,
      breachNotification: true
    },
    // Anti-Spam Legislation (CASL)
    casl: {
      requiresConsent: true,
      requiresUnsubscribe: true,
      requiresIdentification: true
    }
  },
  UK: {
    // Privacy and Electronic Communications Regulations (PECR)
    pecr: {
      requiresConsent: true,
      allowedHours: { start: 9, end: 21 },
      corporateExemption: true
    },
    // Data Protection Act 2018
    dpa: {
      requiresLawfulBasis: true,
      dataRetentionDays: 30,
      rightToBeDeleted: true
    }
  }
};

// Do Not Call registries (mock data - in production, integrate with actual DNC services)
const DO_NOT_CALL_REGISTRY = new Set([
  '+15551234567',
  '+15551234568',
  '+15551234569'
]);

// Time zone mappings for compliance with calling hour restrictions
const TIMEZONE_MAPPINGS = {
  '+1': 'America/New_York', // North America
  '+44': 'Europe/London', // UK
  '+33': 'Europe/Paris', // France
  '+49': 'Europe/Berlin', // Germany
  '+1604': 'America/Vancouver', // Canada Pacific
  '+1416': 'America/Toronto' // Canada Eastern
};

/**
 * Check if a call is compliant with regulations
 * @param {string} to - Destination number
 * @param {string} from - Source number
 * @param {Object} options - Call options
 * @returns {Object} - Compliance result
 */
async function getRegulationCompliance(to, from, options = {}) {
  try {
    const compliance = {
      allowed: true,
      reason: '',
      requirements: [],
      warnings: []
    };

    // Determine country/region from phone number
    const country = getCountryFromPhoneNumber(to);
    const regulations = REGULATIONS[country];

    if (!regulations) {
      compliance.warnings.push(`No specific regulations found for country: ${country}`);
      return compliance;
    }

    // Check Do Not Call registry
    if (isInDoNotCallRegistry(to)) {
      compliance.allowed = false;
      compliance.reason = 'Number is in Do Not Call registry';
      return compliance;
    }

    // Check calling hours restrictions
    const timeCheck = checkCallingHours(to, regulations);
    if (!timeCheck.allowed) {
      compliance.allowed = false;
      compliance.reason = timeCheck.reason;
      return compliance;
    }

    // Check consent requirements
    if (regulations.tcpa?.requiresConsent || regulations.gdpr?.requiresConsent) {
      compliance.requirements.push('Explicit consent required before calling');
    }

    // Check caller ID requirements
    if (regulations.fcc?.callerIdRequired) {
      compliance.requirements.push('Caller ID must be provided and accurate');
    }

    // Check recording notification requirements
    if (regulations.fcc?.recordingNotification && options.recording) {
      compliance.requirements.push('Must notify caller that call is being recorded');
    }

    // Check data retention requirements
    if (regulations.gdpr?.dataRetentionDays) {
      compliance.requirements.push(`Call data must be deleted after ${regulations.gdpr.dataRetentionDays} days`);
    }

    // GDPR-specific checks for EU
    if (country === 'EU') {
      compliance.requirements.push('Lawful basis for processing personal data required');
      compliance.requirements.push('Data subject rights must be respected');
    }

    // TCPA-specific checks for US
    if (country === 'US') {
      compliance.requirements.push('Must maintain internal Do Not Call list');
      compliance.requirements.push('Maximum 3 calls per day to same number');
    }

    logger.info(`Compliance check completed for ${to}: ${compliance.allowed ? 'ALLOWED' : 'BLOCKED'}`);
    return compliance;

  } catch (error) {
    logger.error('Failed to check regulation compliance:', error);
    return {
      allowed: false,
      reason: 'Compliance check failed',
      requirements: [],
      warnings: ['Unable to verify regulatory compliance']
    };
  }
}

/**
 * Get country from phone number
 * @param {string} phoneNumber - Phone number
 * @returns {string} - Country code
 */
function getCountryFromPhoneNumber(phoneNumber) {
  const cleaned = phoneNumber.replace(/\D/g, '');

  // Basic country detection based on country codes
  if (cleaned.startsWith('1')) {
    return 'US'; // US/Canada
  } else if (cleaned.startsWith('44')) {
    return 'UK';
  } else if (cleaned.startsWith('33') || cleaned.startsWith('49') ||
             cleaned.startsWith('39') || cleaned.startsWith('34')) {
    return 'EU';
  } else {
    return 'UNKNOWN';
  }
}

/**
 * Check if number is in Do Not Call registry
 * @param {string} phoneNumber - Phone number to check
 * @returns {boolean} - True if in DNC registry
 */
function isInDoNotCallRegistry(phoneNumber) {
  // In production, this would check against actual DNC registries
  return DO_NOT_CALL_REGISTRY.has(phoneNumber);
}

/**
 * Check calling hours compliance
 * @param {string} phoneNumber - Destination phone number
 * @param {Object} regulations - Applicable regulations
 * @returns {Object} - Time compliance result
 */
function checkCallingHours(phoneNumber, regulations) {
  try {
    if (!regulations.tcpa?.allowedHours && !regulations.pecr?.allowedHours) {
      return { allowed: true };
    }

    const allowedHours = regulations.tcpa?.allowedHours || regulations.pecr?.allowedHours;
    const timezone = getTimezoneForPhoneNumber(phoneNumber);

    // Get current time in the destination timezone
    const now = new Date();
    const destinationTime = new Date(now.toLocaleString('en-US', { timeZone: timezone }));
    const hour = destinationTime.getHours();

    if (hour < allowedHours.start || hour >= allowedHours.end) {
      return {
        allowed: false,
        reason: `Call outside allowed hours (${allowedHours.start}:00 - ${allowedHours.end}:00 local time)`
      };
    }

    return { allowed: true };
  } catch (error) {
    logger.error('Failed to check calling hours:', error);
    return { allowed: true }; // Default to allowed if check fails
  }
}

/**
 * Get timezone for phone number
 * @param {string} phoneNumber - Phone number
 * @returns {string} - Timezone identifier
 */
function getTimezoneForPhoneNumber(phoneNumber) {
  const cleaned = phoneNumber.replace(/\D/g, '');

  // Check for specific area codes/prefixes
  for (const [prefix, timezone] of Object.entries(TIMEZONE_MAPPINGS)) {
    if (cleaned.startsWith(prefix.replace(/\D/g, ''))) {
      return timezone;
    }
  }

  // Default timezones by country
  if (cleaned.startsWith('1')) {
    return 'America/New_York'; // Default US Eastern
  } else if (cleaned.startsWith('44')) {
    return 'Europe/London';
  } else if (cleaned.startsWith('33')) {
    return 'Europe/Paris';
  } else if (cleaned.startsWith('49')) {
    return 'Europe/Berlin';
  }

  return 'UTC'; // Default fallback
}

/**
 * Check if recording is allowed
 * @param {string} country - Country code
 * @param {boolean} twoPartyConsent - Whether both parties consent
 * @returns {Object} - Recording compliance result
 */
function checkRecordingCompliance(country, twoPartyConsent = false) {
  const compliance = {
    allowed: true,
    requirements: []
  };

  switch (country) {
  case 'US':
    // US has mixed one-party and two-party consent states
    compliance.requirements.push('Check state-specific recording laws');
    if (!twoPartyConsent) {
      compliance.requirements.push('Notify all parties that call is being recorded');
    }
    break;
  case 'EU':
    compliance.requirements.push('All parties must consent to recording');
    compliance.requirements.push('Recording purpose must be clearly stated');
    if (!twoPartyConsent) {
      compliance.allowed = false;
    }
    break;
  case 'UK':
    compliance.requirements.push('Recording for business purposes requires notification');
    compliance.requirements.push('Data must be stored securely');
    break;
  case 'CA':
    compliance.requirements.push('One-party consent required');
    compliance.requirements.push('Recording purpose must be legitimate');
    break;
  }

  return compliance;
}

/**
 * Get data retention requirements
 * @param {string} country - Country code
 * @returns {Object} - Data retention requirements
 */
function getDataRetentionRequirements(country) {
  const regulations = REGULATIONS[country];

  if (!regulations) {
    return {
      retentionDays: 30, // Default
      requirements: ['Implement reasonable data retention policy']
    };
  }

  const requirements = [];
  let retentionDays = 30;

  if (regulations.gdpr) {
    retentionDays = regulations.gdpr.dataRetentionDays;
    requirements.push('Data must be deleted when no longer necessary');
    requirements.push('Data subject has right to deletion');
  }

  if (regulations.pipeda) {
    retentionDays = Math.max(retentionDays, regulations.pipeda.dataRetentionDays);
    requirements.push('Personal information must be disposed of securely');
  }

  return { retentionDays, requirements };
}

/**
 * Generate compliance report for a call
 * @param {Object} callData - Call data
 * @returns {Object} - Compliance report
 */
function generateComplianceReport(callData) {
  const country = getCountryFromPhoneNumber(callData.to);
  const report = {
    callId: callData.callId,
    timestamp: new Date(),
    country,
    complianceChecks: []
  };

  // Check basic compliance
  const basicCompliance = getRegulationCompliance(callData.to, callData.from);
  report.complianceChecks.push({
    type: 'basic_compliance',
    result: basicCompliance
  });

  // Check recording compliance if applicable
  if (callData.recording) {
    const recordingCompliance = checkRecordingCompliance(country, callData.recordingConsent);
    report.complianceChecks.push({
      type: 'recording_compliance',
      result: recordingCompliance
    });
  }

  // Check data retention requirements
  const dataRetention = getDataRetentionRequirements(country);
  report.complianceChecks.push({
    type: 'data_retention',
    result: dataRetention
  });

  return report;
}

/**
 * Check if user has provided necessary consents
 * @param {string} userId - User ID
 * @param {string} phoneNumber - Phone number
 * @returns {Object} - Consent status
 */
async function checkUserConsent(_userId, _phoneNumber) {
  // In production, this would check against a consent database
  return {
    hasConsent: false, // Default to false for safety
    consentType: null,
    consentDate: null,
    expiryDate: null,
    canCall: false,
    canSMS: false,
    canRecord: false
  };
}

module.exports = {
  getRegulationCompliance,
  getCountryFromPhoneNumber,
  isInDoNotCallRegistry,
  checkCallingHours,
  checkRecordingCompliance,
  getDataRetentionRequirements,
  generateComplianceReport,
  checkUserConsent,
  REGULATIONS
};
