-- Test point results table
CREATE TABLE test_point_results (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id),
    test_plan_id UUID NOT NULL,
    test_point_id UUID NOT NULL UNIQUE,
    testit_user_id UUID NOT NULL,
    testit_username VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_test_point_results_project_date ON test_point_results(project_id, date);
CREATE INDEX idx_test_point_results_user_date ON test_point_results(testit_user_id, date);
CREATE INDEX idx_test_point_results_test_plan ON test_point_results(test_plan_id);
