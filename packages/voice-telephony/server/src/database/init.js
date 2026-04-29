/**
 * Database initialization and setup
 * Sets up SQLite database with required tables
 */

const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const fs = require('fs');
const logger = require('../utils/logger');

let db = null;

/**
 * Initialize database connection and create tables
 */
async function initDatabase() {
  try {
    // Ensure data directory exists
    const dataDir = path.dirname(process.env.DB_PATH || './data/nextgentele.db');
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true });
    }

    // Create database connection
    const dbPath = process.env.DB_PATH || './data/nextgentele.db';
    db = new sqlite3.Database(dbPath);

    // Enable foreign keys
    await runQuery('PRAGMA foreign_keys = ON');

    // Create tables
    await createTables();

    logger.info(`Database initialized: ${dbPath}`);
    return db;
  } catch (error) {
    logger.error('Failed to initialize database:', error);
    throw error;
  }
}

/**
 * Create all required tables
 */
async function createTables() {
  // Users table
  await runQuery(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT UNIQUE NOT NULL,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      phone_number TEXT,
      role TEXT DEFAULT 'user',
      status TEXT DEFAULT 'active',
      settings TEXT, -- JSON string
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Call sessions table
  await runQuery(`
    CREATE TABLE IF NOT EXISTS call_sessions (
      id TEXT PRIMARY KEY,
      user_id INTEGER,
      from_number TEXT NOT NULL,
      to_number TEXT NOT NULL,
      protocol TEXT NOT NULL,
      direction TEXT NOT NULL,
      status TEXT NOT NULL,
      start_time DATETIME,
      answer_time DATETIME,
      end_time DATETIME,
      duration INTEGER,
      end_reason TEXT,
      transferred_to TEXT,
      transfer_time DATETIME,
      hold_time DATETIME,
      resume_time DATETIME,
      session_data TEXT, -- JSON string
      metadata TEXT, -- JSON string
      recording BOOLEAN DEFAULT FALSE,
      recording_path TEXT,
      ai_enabled BOOLEAN DEFAULT FALSE,
      ai_data TEXT, -- JSON string
      quality REAL,
      cost REAL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users (id)
    )
  `);

  // Call transcriptions table
  await runQuery(`
    CREATE TABLE IF NOT EXISTS call_transcriptions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      call_id TEXT NOT NULL,
      speaker TEXT NOT NULL,
      text TEXT NOT NULL,
      confidence REAL DEFAULT 0,
      language TEXT DEFAULT 'en-US',
      timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
      start_time INTEGER,
      end_time INTEGER,
      audio_offset INTEGER DEFAULT 0,
      sentiment TEXT DEFAULT 'neutral',
      intent TEXT,
      entities TEXT, -- JSON string
      metadata TEXT, -- JSON string
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (call_id) REFERENCES call_sessions (id)
    )
  `);

  // AI responses table
  await runQuery(`
    CREATE TABLE IF NOT EXISTS ai_responses (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      call_id TEXT NOT NULL,
      input TEXT NOT NULL,
      response TEXT NOT NULL,
      model TEXT DEFAULT 'gpt-4',
      confidence REAL DEFAULT 0,
      timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
      processing_time INTEGER DEFAULT 0,
      intent TEXT,
      context TEXT, -- JSON string
      feedback TEXT, -- JSON string
      metadata TEXT, -- JSON string
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (call_id) REFERENCES call_sessions (id)
    )
  `);

  // Call recordings table
  await runQuery(`
    CREATE TABLE IF NOT EXISTS call_recordings (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      call_id TEXT NOT NULL,
      file_path TEXT NOT NULL,
      file_size INTEGER,
      duration INTEGER,
      format TEXT DEFAULT 'wav',
      sample_rate INTEGER DEFAULT 8000,
      channels INTEGER DEFAULT 1,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (call_id) REFERENCES call_sessions (id)
    )
  `);

  // User consents table (for compliance)
  await runQuery(`
    CREATE TABLE IF NOT EXISTS user_consents (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER,
      phone_number TEXT NOT NULL,
      consent_type TEXT NOT NULL, -- call, sms, recording, data_processing
      granted BOOLEAN DEFAULT FALSE,
      consent_date DATETIME,
      expiry_date DATETIME,
      source TEXT, -- web, phone, email, etc.
      metadata TEXT, -- JSON string
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users (id)
    )
  `);

  // Do not call registry
  await runQuery(`
    CREATE TABLE IF NOT EXISTS do_not_call_registry (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      phone_number TEXT UNIQUE NOT NULL,
      reason TEXT,
      added_date DATETIME DEFAULT CURRENT_TIMESTAMP,
      expires_date DATETIME,
      source TEXT,
      active BOOLEAN DEFAULT TRUE
    )
  `);

  // Call analytics table
  await runQuery(`
    CREATE TABLE IF NOT EXISTS call_analytics (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      call_id TEXT NOT NULL,
      metric_name TEXT NOT NULL,
      metric_value REAL,
      metric_unit TEXT,
      timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
      metadata TEXT, -- JSON string
      FOREIGN KEY (call_id) REFERENCES call_sessions (id)
    )
  `);

  // SIP accounts table
  await runQuery(`
    CREATE TABLE IF NOT EXISTS sip_accounts (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER,
      username TEXT NOT NULL,
      domain TEXT NOT NULL,
      password_hash TEXT NOT NULL,
      display_name TEXT,
      status TEXT DEFAULT 'inactive',
      registration_expires DATETIME,
      last_registration DATETIME,
      settings TEXT, -- JSON string
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users (id),
      UNIQUE(username, domain)
    )
  `);

  // Create indexes for better performance
  await createIndexes();

  logger.info('Database tables created successfully');
}

/**
 * Create database indexes
 */
async function createIndexes() {
  const indexes = [
    'CREATE INDEX IF NOT EXISTS idx_call_sessions_user_id ON call_sessions (user_id)',
    'CREATE INDEX IF NOT EXISTS idx_call_sessions_status ON call_sessions (status)',
    'CREATE INDEX IF NOT EXISTS idx_call_sessions_start_time ON call_sessions (start_time)',
    'CREATE INDEX IF NOT EXISTS idx_call_sessions_from_number ON call_sessions (from_number)',
    'CREATE INDEX IF NOT EXISTS idx_call_sessions_to_number ON call_sessions (to_number)',
    'CREATE INDEX IF NOT EXISTS idx_call_transcriptions_call_id ON call_transcriptions (call_id)',
    'CREATE INDEX IF NOT EXISTS idx_call_transcriptions_timestamp ON call_transcriptions (timestamp)',
    'CREATE INDEX IF NOT EXISTS idx_ai_responses_call_id ON ai_responses (call_id)',
    'CREATE INDEX IF NOT EXISTS idx_user_consents_phone_number ON user_consents (phone_number)',
    'CREATE INDEX IF NOT EXISTS idx_user_consents_consent_type ON user_consents (consent_type)',
    'CREATE INDEX IF NOT EXISTS idx_dnc_phone_number ON do_not_call_registry (phone_number)',
    'CREATE INDEX IF NOT EXISTS idx_call_analytics_call_id ON call_analytics (call_id)',
    'CREATE INDEX IF NOT EXISTS idx_sip_accounts_username_domain ON sip_accounts (username, domain)'
  ];

  for (const indexSql of indexes) {
    await runQuery(indexSql);
  }

  logger.info('Database indexes created successfully');
}

/**
 * Run a SQL query
 * @param {string} sql - SQL query
 * @param {Array} params - Query parameters
 * @returns {Promise} - Query result
 */
function runQuery(sql, params = []) {
  return new Promise((resolve, reject) => {
    if (!db) {
      reject(new Error('Database not initialized'));
      return;
    }

    db.run(sql, params, function(err) {
      if (err) {
        logger.error('Database query error:', err);
        reject(err);
      } else {
        resolve({ lastID: this.lastID, changes: this.changes });
      }
    });
  });
}

/**
 * Get single row from database
 * @param {string} sql - SQL query
 * @param {Array} params - Query parameters
 * @returns {Promise} - Single row result
 */
function getRow(sql, params = []) {
  return new Promise((resolve, reject) => {
    if (!db) {
      reject(new Error('Database not initialized'));
      return;
    }

    db.get(sql, params, (err, row) => {
      if (err) {
        logger.error('Database query error:', err);
        reject(err);
      } else {
        resolve(row);
      }
    });
  });
}

/**
 * Get all rows from database
 * @param {string} sql - SQL query
 * @param {Array} params - Query parameters
 * @returns {Promise} - All rows result
 */
function getAllRows(sql, params = []) {
  return new Promise((resolve, reject) => {
    if (!db) {
      reject(new Error('Database not initialized'));
      return;
    }

    db.all(sql, params, (err, rows) => {
      if (err) {
        logger.error('Database query error:', err);
        reject(err);
      } else {
        resolve(rows);
      }
    });
  });
}

/**
 * Close database connection
 */
function closeDatabase() {
  return new Promise((resolve, reject) => {
    if (db) {
      db.close((err) => {
        if (err) {
          logger.error('Error closing database:', err);
          reject(err);
        } else {
          logger.info('Database connection closed');
          resolve();
        }
      });
    } else {
      resolve();
    }
  });
}

/**
 * Get database instance
 * @returns {sqlite3.Database} - Database instance
 */
function getDatabase() {
  return db;
}

module.exports = {
  initDatabase,
  runQuery,
  getRow,
  getAllRows,
  closeDatabase,
  getDatabase
};
