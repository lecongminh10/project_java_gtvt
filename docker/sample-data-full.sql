SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

DROP TABLE IF EXISTS class_students;
DROP TABLE IF EXISTS class_schedule;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS teacher_subjects;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS teachers;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id bigint NOT NULL AUTO_INCREMENT,
  created_at datetime(6) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  employee_code varchar(255) DEFAULT NULL,
  full_name varchar(255) DEFAULT NULL,
  password varchar(255) NOT NULL,
  phone varchar(255) DEFAULT NULL,
  role enum('ADMIN','TEACHER') DEFAULT NULL,
  status enum('ACTIVE','INACTIVE') DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  username varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_r43af9ap4edm43mmtq01oddj6 (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE password_reset_tokens (
  id bigint NOT NULL AUTO_INCREMENT,
  token varchar(255) NOT NULL,
  created_at datetime(6) NOT NULL,
  expires_at datetime(6) NOT NULL,
  used_at datetime(6) DEFAULT NULL,
  user_id bigint NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_password_reset_token (token),
  KEY FK_password_reset_user (user_id),
  CONSTRAINT FK_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE subjects (
  id bigint NOT NULL AUTO_INCREMENT,
  code varchar(255) NOT NULL,
  description varchar(255) DEFAULT NULL,
  level enum('TIEU_HOC','THCS','THPT') DEFAULT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_rg7x1lyii7kdyycw98d45vep5 (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE courses (
  id bigint NOT NULL AUTO_INCREMENT,
  code varchar(255) NOT NULL,
  created_at datetime(6) DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  deleted bit(1) NOT NULL DEFAULT b'0',
  duration_weeks int DEFAULT NULL,
  fee decimal(38,2) DEFAULT NULL,
  name varchar(255) NOT NULL,
  sessions_count int DEFAULT NULL,
  status enum('MOI','DANG_HOC','KET_THUC') DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  subject_id bigint NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_61og8rbqdd2y28rx2et5fdnxd (code),
  KEY FK5tckdihu5akp5nkxiacx1gfhi (subject_id),
  CONSTRAINT FK5tckdihu5akp5nkxiacx1gfhi FOREIGN KEY (subject_id) REFERENCES subjects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE teachers (
  id bigint NOT NULL AUTO_INCREMENT,
  code varchar(255) NOT NULL,
  created_at datetime(6) DEFAULT NULL,
  salary decimal(10,2) DEFAULT NULL,
  salary_currency varchar(255) DEFAULT NULL,
  salary_pay_period varchar(255) DEFAULT NULL,
  contract_start_date date DEFAULT NULL,
  contract_end_date date DEFAULT NULL,
  contract_type varchar(255) DEFAULT NULL,
  performance_rating double DEFAULT NULL,
  last_performance_review_date datetime(6) DEFAULT NULL,
  last_reviewer_notes text DEFAULT NULL,
  dob date DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  name varchar(255) NOT NULL,
  phone varchar(255) DEFAULT NULL,
  status enum('ACTIVE','INACTIVE') DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  user_id bigint DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_lmg2qx967u1knxjscfxomathn (code),
  UNIQUE KEY UK_cd1k6xwg9jqtiwx9ybnxpmoh9 (user_id),
  CONSTRAINT FKb8dct7w2j1vl1r2bpstw5isc0 FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE students (
  id bigint NOT NULL AUTO_INCREMENT,
  code varchar(255) NOT NULL,
  created_at datetime(6) DEFAULT NULL,
  dob date DEFAULT NULL,
  name varchar(255) NOT NULL,
  parent_address varchar(255) DEFAULT NULL,
  parent_email varchar(255) DEFAULT NULL,
  parent_name varchar(255) DEFAULT NULL,
  parent_phone varchar(255) DEFAULT NULL,
  status enum('ACTIVE','INACTIVE') DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_eqa1d4jiyg5m5rnuja7ifgw73 (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE classes (
  id bigint NOT NULL AUTO_INCREMENT,
  auto_created bit(1) NOT NULL,
  code varchar(255) NOT NULL,
  created_at datetime(6) DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  end_date date DEFAULT NULL,
  name varchar(255) NOT NULL,
  room varchar(255) DEFAULT NULL,
  source_class_id bigint DEFAULT NULL,
  start_date date DEFAULT NULL,
  status enum('MOI','DANG_HOC','KET_THUC') DEFAULT NULL,
  total_sessions int DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  course_id bigint NOT NULL,
  teacher_id bigint DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_ivcaxrbwnp0dosg2gj4i3sxpq (code),
  KEY FK9v6ijeybapa0ontdtd4o4rycs (course_id),
  KEY FK8td8h5k21lq8jax2h6oobm9l0 (teacher_id),
  CONSTRAINT FK8td8h5k21lq8jax2h6oobm9l0 FOREIGN KEY (teacher_id) REFERENCES teachers (id),
  CONSTRAINT FK9v6ijeybapa0ontdtd4o4rycs FOREIGN KEY (course_id) REFERENCES courses (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE teacher_subjects (
  teacher_id bigint NOT NULL,
  subject_id bigint NOT NULL,
  PRIMARY KEY (teacher_id,subject_id),
  KEY FKdweqkwxroox2u7pbmksehx04i (subject_id),
  CONSTRAINT FK6dcl3ihufp4v0j1fuxlw4ksoj FOREIGN KEY (teacher_id) REFERENCES teachers (id),
  CONSTRAINT FKdweqkwxroox2u7pbmksehx04i FOREIGN KEY (subject_id) REFERENCES subjects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE documents (
  id bigint NOT NULL AUTO_INCREMENT,
  created_at datetime(6) DEFAULT NULL,
  file_path varchar(255) DEFAULT NULL,
  name varchar(255) NOT NULL,
  type enum('GIAO_AN','TAI_LIEU_THAM_KHAO','KHAC') DEFAULT NULL,
  updated_at datetime(6) DEFAULT NULL,
  course_id bigint DEFAULT NULL,
  subject_id bigint DEFAULT NULL,
  upload_by bigint DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FKsuenl009odnidqiyaao22gw0s (course_id),
  KEY FKpby9dar20817f18ipuxmqf4x4 (subject_id),
  KEY FKm90sbe3jeej3t1xfwv0of62wf (upload_by),
  CONSTRAINT FKm90sbe3jeej3t1xfwv0of62wf FOREIGN KEY (upload_by) REFERENCES users (id),
  CONSTRAINT FKpby9dar20817f18ipuxmqf4x4 FOREIGN KEY (subject_id) REFERENCES subjects (id),
  CONSTRAINT FKsuenl009odnidqiyaao22gw0s FOREIGN KEY (course_id) REFERENCES courses (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE class_schedule (
  id bigint NOT NULL AUTO_INCREMENT,
  day_of_week enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') DEFAULT NULL,
  room varchar(255) DEFAULT NULL,
  shift enum('CA_1','CA_2','CA_3','CA_4','CA_5') DEFAULT NULL,
  class_id bigint NOT NULL,
  PRIMARY KEY (id),
  KEY FK9fplehvxxgfj35vacj8b0qabh (class_id),
  CONSTRAINT FK9fplehvxxgfj35vacj8b0qabh FOREIGN KEY (class_id) REFERENCES classes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE class_students (
  joined_date date DEFAULT NULL,
  leave_date date DEFAULT NULL,
  role_in_class varchar(255) NOT NULL,
  student_id bigint NOT NULL,
  class_id bigint NOT NULL,
  PRIMARY KEY (class_id,student_id),
  KEY FK8x4jwkaf3emayhwuqidfr5w0c (student_id),
  CONSTRAINT FK8x4jwkaf3emayhwuqidfr5w0c FOREIGN KEY (student_id) REFERENCES students (id),
  CONSTRAINT FKjuh9br5vimkw71ko8qyswp3ci FOREIGN KEY (class_id) REFERENCES classes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO users (id, created_at, email, employee_code, full_name, password, phone, role, status, updated_at, username) VALUES
(1, NOW(), 'admin@gtvt.edu.vn', 'EMP-001', 'Admin Master', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000001', 'ADMIN', 'ACTIVE', NOW(), 'admin'),
(2, NOW(), 'teacher1@gtvt.edu.vn', 'EMP-002', 'Teacher One', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000002', 'TEACHER', 'ACTIVE', NOW(), 'teacher1'),
(3, NOW(), 'teacher2@gtvt.edu.vn', 'EMP-003', 'Teacher Two', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000003', 'TEACHER', 'ACTIVE', NOW(), 'teacher2'),
(4, NOW(), 'teacher3@gtvt.edu.vn', 'EMP-004', 'Teacher Three', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000004', 'TEACHER', 'ACTIVE', NOW(), 'teacher3'),
(5, NOW(), 'teacher4@gtvt.edu.vn', 'EMP-005', 'Teacher Four', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000005', 'TEACHER', 'ACTIVE', NOW(), 'teacher4'),
(6, NOW(), 'teacher5@gtvt.edu.vn', 'EMP-006', 'Teacher Five', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000006', 'TEACHER', 'ACTIVE', NOW(), 'teacher5'),
(7, NOW(), 'staff1@gtvt.edu.vn', 'EMP-007', 'Staff One', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000007', 'ADMIN', 'ACTIVE', NOW(), 'staff1'),
(8, NOW(), 'staff2@gtvt.edu.vn', 'EMP-008', 'Staff Two', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000008', 'ADMIN', 'ACTIVE', NOW(), 'staff2'),
(9, NOW(), 'inactive1@gtvt.edu.vn', 'EMP-009', 'Inactive User', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000009', 'TEACHER', 'INACTIVE', NOW(), 'inactive1'),
(10, NOW(), 'inactive2@gtvt.edu.vn', 'EMP-010', 'Inactive Admin', '$2b$12$cKRk7PLiYghAMpUHz5ZfYOAgQgP4hZHLDu7C2nXj9DNaFXHfyizPC', '0900000010', 'ADMIN', 'INACTIVE', NOW(), 'inactive2');

INSERT INTO subjects (id, code, name, level, description) VALUES
(1, 'MATH01', 'Mathematics', 'THCS', 'Core math subject'),
(2, 'PHY01', 'Physics', 'THPT', 'Basic physics'),
(3, 'ENG01', 'English', 'TIEU_HOC', 'English foundation'),
(4, 'CHEM01', 'Chemistry', 'THPT', 'Chemistry basics'),
(5, 'BIO01', 'Biology', 'THCS', 'Biology fundamentals'),
(6, 'LIT01', 'Literature', 'THPT', 'Vietnamese literature'),
(7, 'HIS01', 'History', 'THCS', 'Vietnam history'),
(8, 'GEO01', 'Geography', 'THCS', 'Geography basics'),
(9, 'ICT01', 'Informatics', 'THPT', 'Coding basics'),
(10, 'ART01', 'Arts', 'TIEU_HOC', 'Creative arts');

INSERT INTO courses (id, code, name, subject_id, sessions_count, duration_weeks, fee, status, description, created_at, updated_at) VALUES
(1, 'KH-001', 'Math Starter', 1, 24, 12, 1500000.00, 'MOI', 'Intro math course', NOW(), NOW()),
(2, 'KH-002', 'Physics Core', 2, 30, 15, 2200000.00, 'DANG_HOC', 'Physics basics', NOW(), NOW()),
(3, 'KH-003', 'English Kids', 3, 20, 10, 1200000.00, 'KET_THUC', 'Kids English course', NOW(), NOW()),
(4, 'KH-004', 'Chemistry Lab', 4, 28, 14, 2100000.00, 'DANG_HOC', 'Chemistry experiments', NOW(), NOW()),
(5, 'KH-005', 'Biology Intro', 5, 22, 11, 1300000.00, 'MOI', 'Biology basics', NOW(), NOW()),
(6, 'KH-006', 'Literature Pro', 6, 26, 13, 1700000.00, 'DANG_HOC', 'Literature analysis', NOW(), NOW()),
(7, 'KH-007', 'History Map', 7, 18, 9, 1100000.00, 'MOI', 'History overview', NOW(), NOW()),
(8, 'KH-008', 'Geography 360', 8, 16, 8, 1000000.00, 'KET_THUC', 'Geography topics', NOW(), NOW()),
(9, 'KH-009', 'ICT Basics', 9, 32, 16, 2400000.00, 'DANG_HOC', 'Programming starter', NOW(), NOW()),
(10, 'KH-010', 'Arts & Craft', 10, 12, 6, 900000.00, 'MOI', 'Creative crafts', NOW(), NOW());

INSERT INTO teachers (id, code, created_at, dob, email, name, phone, status, updated_at, user_id) VALUES
(1, 'GV-001', NOW(), '1985-01-15', 'teacher1@gtvt.edu.vn', 'Teacher One', '0900000002', 'ACTIVE', NOW(), 2),
(2, 'GV-002', NOW(), '1987-02-20', 'teacher2@gtvt.edu.vn', 'Teacher Two', '0900000003', 'ACTIVE', NOW(), 3),
(3, 'GV-003', NOW(), '1989-03-10', 'teacher3@gtvt.edu.vn', 'Teacher Three', '0900000004', 'ACTIVE', NOW(), 4),
(4, 'GV-004', NOW(), '1990-04-05', 'teacher4@gtvt.edu.vn', 'Teacher Four', '0900000005', 'ACTIVE', NOW(), 5),
(5, 'GV-005', NOW(), '1992-05-12', 'teacher5@gtvt.edu.vn', 'Teacher Five', '0900000006', 'ACTIVE', NOW(), 6),
(6, 'GV-006', NOW(), '1988-06-11', 'teacher6@gtvt.edu.vn', 'Teacher Six', '0900000011', 'INACTIVE', NOW(), 9),
(7, 'GV-007', NOW(), '1986-07-07', 'teacher7@gtvt.edu.vn', 'Teacher Seven', '0900000012', 'ACTIVE', NOW(), 7),
(8, 'GV-008', NOW(), '1991-08-19', 'teacher8@gtvt.edu.vn', 'Teacher Eight', '0900000013', 'ACTIVE', NOW(), 8),
(9, 'GV-009', NOW(), '1984-09-03', 'teacher9@gtvt.edu.vn', 'Teacher Nine', '0900000014', 'INACTIVE', NOW(), 10),
(10, 'GV-010', NOW(), '1983-10-22', 'teacher10@gtvt.edu.vn', 'Teacher Ten', '0900000015', 'ACTIVE', NOW(), NULL);

INSERT INTO students (id, code, created_at, dob, name, parent_address, parent_email, parent_name, parent_phone, status, updated_at) VALUES
(1, 'HS-001', NOW(), '2012-01-05', 'Nguyen Van A', 'Ha Noi', 'ph1@example.com', 'Parent A', '0910000001', 'ACTIVE', NOW()),
(2, 'HS-002', NOW(), '2011-02-10', 'Tran Thi B', 'Ha Noi', 'ph2@example.com', 'Parent B', '0910000002', 'ACTIVE', NOW()),
(3, 'HS-003', NOW(), '2012-03-15', 'Le Van C', 'Hai Phong', 'ph3@example.com', 'Parent C', '0910000003', 'ACTIVE', NOW()),
(4, 'HS-004', NOW(), '2011-04-20', 'Pham Thi D', 'Da Nang', 'ph4@example.com', 'Parent D', '0910000004', 'INACTIVE', NOW()),
(5, 'HS-005', NOW(), '2012-05-25', 'Hoang Van E', 'Hue', 'ph5@example.com', 'Parent E', '0910000005', 'ACTIVE', NOW()),
(6, 'HS-006', NOW(), '2011-06-30', 'Vo Thi F', 'Can Tho', 'ph6@example.com', 'Parent F', '0910000006', 'ACTIVE', NOW()),
(7, 'HS-007', NOW(), '2012-07-08', 'Bui Van G', 'Ha Noi', 'ph7@example.com', 'Parent G', '0910000007', 'ACTIVE', NOW()),
(8, 'HS-008', NOW(), '2011-08-12', 'Dang Thi H', 'Hai Duong', 'ph8@example.com', 'Parent H', '0910000008', 'ACTIVE', NOW()),
(9, 'HS-009', NOW(), '2012-09-18', 'Do Van I', 'Nam Dinh', 'ph9@example.com', 'Parent I', '0910000009', 'INACTIVE', NOW()),
(10, 'HS-010', NOW(), '2011-10-22', 'Mai Thi K', 'Bac Ninh', 'ph10@example.com', 'Parent K', '0910000010', 'ACTIVE', NOW());

INSERT INTO classes (id, auto_created, code, created_at, description, end_date, name, room, source_class_id, start_date, status, total_sessions, updated_at, course_id, teacher_id) VALUES
(1, b'0', 'CLS-001', NOW(), 'Math starter class', '2026-06-30', 'Math A1', 'A101', NULL, '2026-04-01', 'DANG_HOC', 24, NOW(), 1, 1),
(2, b'0', 'CLS-002', NOW(), 'Physics class', '2026-07-15', 'Physics B1', 'B201', NULL, '2026-04-10', 'DANG_HOC', 30, NOW(), 2, 2),
(3, b'1', 'CLS-003', NOW(), 'English kids class', '2026-05-30', 'English K1', 'C301', NULL, '2026-03-20', 'KET_THUC', 20, NOW(), 3, 3),
(4, b'0', 'CLS-004', NOW(), 'Chemistry class', '2026-08-01', 'Chem Lab', 'D101', NULL, '2026-04-15', 'DANG_HOC', 28, NOW(), 4, 4),
(5, b'0', 'CLS-005', NOW(), 'Biology class', '2026-06-10', 'Bio A1', 'E101', NULL, '2026-04-05', 'MOI', 22, NOW(), 5, 5),
(6, b'0', 'CLS-006', NOW(), 'Literature class', '2026-09-01', 'Lit A1', 'F201', NULL, '2026-04-20', 'DANG_HOC', 26, NOW(), 6, 7),
(7, b'1', 'CLS-007', NOW(), 'History class', '2026-05-25', 'History H1', 'G101', NULL, '2026-03-25', 'KET_THUC', 18, NOW(), 7, 8),
(8, b'0', 'CLS-008', NOW(), 'Geography class', '2026-06-05', 'Geo G1', 'H101', NULL, '2026-04-08', 'MOI', 16, NOW(), 8, 1),
(9, b'0', 'CLS-009', NOW(), 'ICT class', '2026-09-15', 'ICT I1', 'I201', NULL, '2026-04-12', 'DANG_HOC', 32, NOW(), 9, 2),
(10, b'0', 'CLS-010', NOW(), 'Arts class', '2026-05-20', 'Arts A1', 'J101', NULL, '2026-03-28', 'KET_THUC', 12, NOW(), 10, 3);

INSERT INTO teacher_subjects (teacher_id, subject_id) VALUES
(1, 1),
(1, 8),
(2, 2),
(2, 9),
(3, 3),
(3, 6),
(4, 4),
(5, 5),
(7, 7),
(8, 10);

INSERT INTO documents (id, created_at, file_path, name, type, updated_at, course_id, subject_id, upload_by) VALUES
(1, NOW(), '/docs/math-starter.pdf', 'Math Starter Doc', 'GIAO_AN', NOW(), 1, 1, 1),
(2, NOW(), '/docs/physics-core.pdf', 'Physics Core Doc', 'TAI_LIEU_THAM_KHAO', NOW(), 2, 2, 2),
(3, NOW(), '/docs/english-kids.pdf', 'English Kids Doc', 'GIAO_AN', NOW(), 3, 3, 3),
(4, NOW(), '/docs/chemistry-lab.pdf', 'Chemistry Lab Doc', 'TAI_LIEU_THAM_KHAO', NOW(), 4, 4, 4),
(5, NOW(), '/docs/biology-intro.pdf', 'Biology Intro Doc', 'GIAO_AN', NOW(), 5, 5, 5),
(6, NOW(), '/docs/literature-pro.pdf', 'Literature Pro Doc', 'KHAC', NOW(), 6, 6, 7),
(7, NOW(), '/docs/history-map.pdf', 'History Map Doc', 'TAI_LIEU_THAM_KHAO', NOW(), 7, 7, 8),
(8, NOW(), '/docs/geography-360.pdf', 'Geography 360 Doc', 'GIAO_AN', NOW(), 8, 8, 1),
(9, NOW(), '/docs/ict-basics.pdf', 'ICT Basics Doc', 'TAI_LIEU_THAM_KHAO', NOW(), 9, 9, 2),
(10, NOW(), '/docs/arts-craft.pdf', 'Arts & Craft Doc', 'KHAC', NOW(), 10, 10, 3);

INSERT INTO class_schedule (id, day_of_week, room, shift, class_id) VALUES
(1, 'MONDAY', 'A101', 'CA_1', 1),
(2, 'WEDNESDAY', 'A101', 'CA_2', 1),
(3, 'TUESDAY', 'B201', 'CA_2', 2),
(4, 'THURSDAY', 'B201', 'CA_3', 2),
(5, 'FRIDAY', 'C301', 'CA_1', 3),
(6, 'SATURDAY', 'D101', 'CA_3', 4),
(7, 'MONDAY', 'E101', 'CA_2', 5),
(8, 'TUESDAY', 'F201', 'CA_4', 6),
(9, 'WEDNESDAY', 'G101', 'CA_5', 7),
(10, 'SUNDAY', 'H101', 'CA_1', 8);

INSERT INTO class_students (joined_date, leave_date, role_in_class, student_id, class_id) VALUES
('2026-04-01', NULL, 'HOC_VIEN', 1, 1),
('2026-04-01', NULL, 'HOC_VIEN', 2, 1),
('2026-04-05', NULL, 'HOC_VIEN', 3, 2),
('2026-04-05', NULL, 'HOC_VIEN', 4, 2),
('2026-03-20', '2026-05-30', 'HOC_VIEN', 5, 3),
('2026-04-10', NULL, 'HOC_VIEN', 6, 4),
('2026-04-12', NULL, 'HOC_VIEN', 7, 5),
('2026-04-15', NULL, 'HOC_VIEN', 8, 6),
('2026-03-25', '2026-05-25', 'HOC_VIEN', 9, 7),
('2026-04-08', NULL, 'HOC_VIEN', 10, 8);

SET foreign_key_checks = 1;
