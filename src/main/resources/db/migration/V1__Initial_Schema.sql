-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    testit_token TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create admin user (password: admin)
INSERT INTO users (username, password, email, role)
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'admin@example.com', 'ADMIN');

-- Global settings table
CREATE TABLE global_settings (
    id SERIAL PRIMARY KEY,
    key VARCHAR(50) NOT NULL UNIQUE,
    value TEXT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert default global settings
INSERT INTO global_settings (key, value, description)
VALUES ('GLOBAL_TESTIT_TOKEN', '', 'Global TestIT API token used for scheduled tasks');

INSERT INTO global_settings (key, value, description)
VALUES ('API_SCHEDULE_CRON', '0 0 1 * * ?', 'Cron expression for API data collection schedule');

INSERT INTO global_settings (key, value, description)
VALUES ('API_BASE_URL', 'https://team-0tcj.testit.software/api/v2', 'Base URL for TestIT API');

-- Projects table
CREATE TABLE projects (
    id SERIAL PRIMARY KEY,
    testit_id UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User-Project relationship table
CREATE TABLE user_projects (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    project_id INTEGER NOT NULL REFERENCES projects(id),
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, project_id)
);

-- Test case statistics table
CREATE TABLE test_case_statistics (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id),
    testit_user_id UUID NOT NULL,
    testit_username VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    created_count INTEGER NOT NULL DEFAULT 0,
    modified_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, testit_user_id, date)
);

-- Test run statistics table
CREATE TABLE test_run_statistics (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id),
    testit_user_id UUID NOT NULL,
    testit_username VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    passed_count INTEGER NOT NULL DEFAULT 0,
    failed_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, testit_user_id, date)
);

-- Create indexes
CREATE INDEX idx_test_case_statistics_project_date ON test_case_statistics(project_id, date);
CREATE INDEX idx_test_run_statistics_project_date ON test_run_statistics(project_id, date);
