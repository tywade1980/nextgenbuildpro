-- Initialize NextGen APK Database
-- This script sets up the database schema with pgvector extension

-- Create the database if it doesn't exist
CREATE DATABASE nextgen_db;

-- Connect to the database
\c nextgen_db;

-- Enable pgvector extension for vector operations
CREATE EXTENSION IF NOT EXISTS vector;

-- Create user if it doesn't exist
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'nextgen_user') THEN
      CREATE ROLE nextgen_user LOGIN PASSWORD 'nextgen_password';
   END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE nextgen_db TO nextgen_user;
GRANT ALL ON SCHEMA public TO nextgen_user;

-- Create initial tables (these will be managed by Flyway in production)

-- Voice Commands table
CREATE TABLE IF NOT EXISTS voice_commands (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    text TEXT NOT NULL,
    confidence REAL NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    language VARCHAR(10) DEFAULT 'en-US',
    processed BOOLEAN DEFAULT FALSE,
    response TEXT,
    processing_time_ms BIGINT
);

-- Service Status table
CREATE TABLE IF NOT EXISTS service_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_update TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    details TEXT,
    uptime BIGINT
);

-- Integration Connections table
CREATE TABLE IF NOT EXISTS integration_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_package VARCHAR(200) UNIQUE NOT NULL,
    connection_type VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_communication TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- MCP Messages table
CREATE TABLE IF NOT EXISTS mcp_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL,
    source VARCHAR(100) NOT NULL,
    target VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed BOOLEAN DEFAULT FALSE
);

-- Vector Entries table with pgvector
CREATE TABLE IF NOT EXISTS vector_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    embedding vector(1536),  -- OpenAI embedding dimension
    metadata JSONB,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    document_id VARCHAR(100),
    document_type VARCHAR(50)
);

-- Database Operations Log table
CREATE TABLE IF NOT EXISTS database_operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation VARCHAR(20) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    data JSONB NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    success BOOLEAN DEFAULT FALSE,
    error_message TEXT,
    execution_time_ms BIGINT
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_voice_commands_timestamp ON voice_commands(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_voice_commands_processed ON voice_commands(processed);
CREATE INDEX IF NOT EXISTS idx_service_status_name ON service_status(service_name);
CREATE INDEX IF NOT EXISTS idx_integration_connections_package ON integration_connections(target_package);
CREATE INDEX IF NOT EXISTS idx_mcp_messages_timestamp ON mcp_messages(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_mcp_messages_type ON mcp_messages(type);
CREATE INDEX IF NOT EXISTS idx_vector_entries_type ON vector_entries(document_type);
CREATE INDEX IF NOT EXISTS idx_database_operations_timestamp ON database_operations(timestamp DESC);

-- Create HNSW index for vector similarity search
CREATE INDEX IF NOT EXISTS idx_vector_entries_embedding ON vector_entries 
USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

-- Insert initial service status records
INSERT INTO service_status (service_name, status, details) VALUES
    ('voice-engine', 'STARTING', 'Voice processing engine'),
    ('database', 'ONLINE', 'PostgreSQL with pgvector'),
    ('backend-core', 'STARTING', 'Spring Boot backend services'),
    ('mcp-server', 'STARTING', 'Model Context Protocol server'),
    ('integration-hub', 'STARTING', 'Cross-application integration hub')
ON CONFLICT (service_name) DO NOTHING;

-- Create function to update last_update timestamp
CREATE OR REPLACE FUNCTION update_last_update_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_update = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for service_status table
DROP TRIGGER IF EXISTS update_service_status_last_update ON service_status;
CREATE TRIGGER update_service_status_last_update
    BEFORE UPDATE ON service_status
    FOR EACH ROW
    EXECUTE FUNCTION update_last_update_column();

COMMIT;