-- Dummy data for documents (MySQL)
-- Assumes subjects, courses, and admin user exist.

-- Clear existing documents
TRUNCATE TABLE documents;

-- Subjects
INSERT INTO subjects (code, name, level, description)
VALUES
  ('SUBJ-A1', 'Tieng Anh A1', 'TIEU_HOC', 'Trinh do A1')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO subjects (code, name, level, description)
VALUES
  ('SUBJ-B1', 'Tieng Anh B1', 'THCS', 'Trinh do B1')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Courses
INSERT INTO courses (code, name, subject_id, sessions_count, fee, description, duration_weeks, status, created_at, updated_at)
SELECT 'COURSE-A1', 'Khoa A1 co ban', s.id, 24, 2500000, 'Khoa A1 co ban', 8, 'MOI', NOW(), NOW()
FROM subjects s WHERE s.code = 'SUBJ-A1'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO courses (code, name, subject_id, sessions_count, fee, description, duration_weeks, status, created_at, updated_at)
SELECT 'COURSE-B1', 'Khoa B1 giao tiep', s.id, 30, 3200000, 'Khoa B1 giao tiep', 10, 'MOI', NOW(), NOW()
FROM subjects s WHERE s.code = 'SUBJ-B1'
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Documents
INSERT INTO documents (name, type, subject_id, course_id, file_path, upload_by, created_at, updated_at)
SELECT 'Giao an A1 - Buoi 1', 'GIAO_AN', s.id, c.id, 'uploads/giao-an-a1-buoi-1.pdf', u.id, NOW(), NOW()
FROM subjects s
JOIN courses c ON c.code = 'COURSE-A1'
JOIN users u ON u.username = 'admin'
WHERE s.code = 'SUBJ-A1';

INSERT INTO documents (name, type, subject_id, course_id, file_path, upload_by, created_at, updated_at)
SELECT 'Tai lieu tham khao B1', 'TAI_LIEU_THAM_KHAO', s.id, c.id, 'uploads/tai-lieu-b1.pdf', u.id, NOW(), NOW()
FROM subjects s
JOIN courses c ON c.code = 'COURSE-B1'
JOIN users u ON u.username = 'admin'
WHERE s.code = 'SUBJ-B1';

-- Note: ensure files exist under uploads/ for view/download.
