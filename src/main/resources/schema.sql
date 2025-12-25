-- Incident Logs Table
CREATE TABLE IF NOT EXISTS incident_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    incident_id VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    status_message CLOB,
    severity VARCHAR(50),
    affected_components CLOB,
    incident_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    hash_code VARCHAR(255),
    is_notified BOOLEAN DEFAULT FALSE,
    UNIQUE (incident_id, provider)
);

-- Status Change Logs Table
CREATE TABLE IF NOT EXISTS status_change_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_id VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    previous_status VARCHAR(50),
    current_status VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL
);

-- Component Registry Table
CREATE TABLE IF NOT EXISTS component_registry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    component_id VARCHAR(255) NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL,
    component_name VARCHAR(255) NOT NULL,
    current_status VARCHAR(50),
    description CLOB,
    last_checked TIMESTAMP,
    UNIQUE (component_id, provider)
);

-- Create Indexes
CREATE INDEX idx_provider_timestamp ON incident_logs(provider, created_at);
CREATE INDEX idx_service_status ON incident_logs(service_name, status);
CREATE INDEX idx_service_change ON status_change_logs(service_id, changed_at);
