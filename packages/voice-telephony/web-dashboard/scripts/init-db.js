#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

// SQLite database creation script
const dbPath = path.join(__dirname, '../prisma/dev.db');

// Ensure the prisma directory exists
const prismaDir = path.dirname(dbPath);
if (!fs.existsSync(prismaDir)) {
  fs.mkdirSync(prismaDir, { recursive: true });
}

// Create empty database file if it doesn't exist
if (!fs.existsSync(dbPath)) {
  fs.writeFileSync(dbPath, '');
  console.log('✅ Created SQLite database file');
} else {
  console.log('✅ Database file already exists');
}

console.log('Database initialization complete!');