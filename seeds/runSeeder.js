#!/usr/bin/env node

/**
 * Node.js runner for the TypeScript seeding script
 * Compiles and runs the seedCatalogue.ts file
 */

const { exec } = require('child_process');
const path = require('path');

console.log('🌱 Running construction catalogue seeder...');

// Compile and run the TypeScript seeding script
const command = 'npx ts-node seeds/seedCatalogue.ts';

exec(command, (error, stdout, stderr) => {
  if (error) {
    console.error('❌ Seeding failed:', error);
    process.exit(1);
  }
  
  if (stderr) {
    console.error('⚠️  Warnings:', stderr);
  }
  
  console.log(stdout);
  console.log('✅ Seeding completed successfully!');
});