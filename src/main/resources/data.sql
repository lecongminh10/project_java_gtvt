-- Test Data Insertion Script
-- English Language Center Management System
-- Insert sample users and teachers for testing

-- Insert test users (passwords are already encoded by Spring Security)
INSERT INTO users (username, password, full_name, email, phone, role, status, employee_code, created_at, updated_at) 
VALUES 
  ('admin', '$2a$10$E8RWJzYHM.LXQ3kMyLvbiumKg7Ugs8q7K0GXqMONqUXJ5M8XJ8t9e', 'Admin User', 'admin@elc.local', '0123456789', 'ADMIN', 'ACTIVE', 'EMP001', NOW(), NOW()),
  ('teacher1', '$2a$10$QmM7R5F8W4q3h9x2k1b5K.8q4f5g9h2j1n0m9p8r7t6y5u4i3o2', 'John Doe', 'john@elc.local', '0987654321', 'TEACHER', 'ACTIVE', 'EMP002', NOW(), NOW()),
  ('teacher2', '$2a$10$QmM7R5F8W4q3h9x2k1b5K.8q4f5g9h2j1n0m9p8r7t6y5u4i3o2', 'Jane Smith', 'jane@elc.local', '0976543210', 'TEACHER', 'ACTIVE', 'EMP003', NOW(), NOW());

-- Insert test teachers
INSERT INTO teachers (code, name, dob, email, phone, status, user_id, salary, salary_currency, salary_pay_period, contract_start_date, contract_end_date, contract_type, performance_rating, last_performance_review_date, last_reviewer_notes, created_at, updated_at)
VALUES
  ('TCH001', 'John Doe', '1990-05-15', 'john@elc.local', '0987654321', 'ACTIVE', 2, 15000000.00, 'VND', 'MONTHLY', '2024-01-01', '2025-12-31', 'FULL_TIME', 4.5, NOW(), 'Excellent teacher, very engaged with students', NOW(), NOW()),
  ('TCH002', 'Jane Smith', '1992-08-20', 'jane@elc.local', '0976543210', 'ACTIVE', 3, 12000000.00, 'VND', 'MONTHLY', '2024-06-01', '2026-05-31', 'FULL_TIME', 4.0, NOW(), 'Good teaching skills, needs improvement in classroom management', NOW(), NOW());
