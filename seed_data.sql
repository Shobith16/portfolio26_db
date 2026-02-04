-- Insert Skills
INSERT INTO skills (name, category, percentage, icon, "order", is_hidden) VALUES
('Java, Dart, xHarbour', 'Languages', 90, 'fas fa-terminal', 1, false),
('HTML, CSS, JavaScript, EJS', 'Frontend', 85, 'fas fa-paint-brush', 2, false),
('Flutter', 'Mobile Development', 95, 'fas fa-mobile-alt', 3, false),
('.NET Core API, Firebase, REST APIs', 'Backend', 80, 'fas fa-server', 4, false),
('MSSQL', 'Database', 75, 'fas fa-database', 5, false),
('Git, GitHub, Postman, VS Code, Android Studio', 'Tools & Platforms', 85, 'fas fa-tools', 6, false);

-- Insert Experiences
INSERT INTO experiences (company, role, description, start_date, end_date, is_current, "order") VALUES
('Trikaiser', 'Software Developer', 'Full-time | Udupi, Karnataka, India\n\nFlutter Application – Offline App:\n- Engineered an offline-first app enabling gatekeepers to log container data in low connectivity areas, reducing downtime by 80%.\n- Implemented Hive for local storage, ensuring 95% data recovery during sync.\n\nClipper Application – Legacy System Migration:\n- Migrated 10K+ records from legacy DB to MSSQL.\n- Refactored 5K+ lines of Clipper code.\n\nFlutter Application – Social Mobile Application:\n- Developed scalable UI with Flutter.\n- Built 10+ backend REST APIs in C#.', '2024-08-01 00:00:00', NULL, true, 1),
('Next24tech', 'Intern', 'Developed secure online voting system using MERN stack, integrated JWT for authentication.', '2024-03-01 00:00:00', '2024-05-01 00:00:00', false, 2),
('Accolade Tech Solutions', 'Intern', 'Created AI/ML tutorial modules and built KNN-based classifiers with Flask and Python.', '2024-01-01 00:00:00', '2024-02-01 00:00:00', false, 3);

-- Insert Projects (excluding tech_stack for now to avoid errors)
INSERT INTO projects (title, description, image_url, live_link, github_link, category, "order", is_hidden, created_at, updated_at) VALUES
('AI Music Transcription', 'Python + Flask app using CNN and HDemucs for converting music into musical notes. Boosted transcription accuracy by 5%.', '', '', 'https://github.com/Shobith16/MusicalNotesGeneration', 'AI', 1, false, NOW(), NOW()),
('Foodie Hub MITE', 'College food ordering platform using Node.js, Firebase, and Google APIs. Reduced order processing time by 40%.', '', 'https://www.messeatz.com/', '', 'Web', 2, false, NOW(), NOW());

-- Attempt to insert tech stack (if table exists, might fail if name is different, that's acceptable for now)
-- INSERT INTO project_tech_stack (project_id, tech_stack) VALUES (1, 'Python'), (1, 'Flask'), (2, 'Node.js'), (2, 'Firebase');
