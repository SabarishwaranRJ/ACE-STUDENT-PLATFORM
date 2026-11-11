-- =============================================================================
-- ACE TeamUp — Sample / Seed Data
-- Run AFTER schema.sql and indexes.sql
-- =============================================================================

USE ace_teamup;

-- -----------------------------------------------------------------------------
-- 1. Colleges (3)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO colleges (college_id, college_name, location) VALUES
(1, 'Kongu Engineering College',      'Erode, Tamil Nadu'),
(2, 'PSG College of Technology',      'Coimbatore, Tamil Nadu'),
(3, 'Sri Venkateswara College of Eng','Chennai, Tamil Nadu');

-- -----------------------------------------------------------------------------
-- 2. Skills (10)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO skills (skill_id, skill_name, skill_category) VALUES
(1,  'Python',        'Programming'),
(2,  'React',         'Frontend'),
(3,  'Machine Learning', 'AI/ML'),
(4,  'SQL',           'Database'),
(5,  'Java',          'Programming'),
(6,  'Node.js',       'Backend'),
(7,  'Data Analysis', 'Data Science'),
(8,  'Leadership',    'Soft Skills'),
(9,  'Communication', 'Soft Skills'),
(10, 'UI/UX Design',  'Design');

-- -----------------------------------------------------------------------------
-- 3. Interests (5)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO interests (interest_id, interest_name) VALUES
(1, 'Artificial Intelligence'),
(2, 'Web Development'),
(3, 'Data Science'),
(4, 'Cybersecurity'),
(5, 'IoT & Embedded Systems');

-- -----------------------------------------------------------------------------
-- 4. Students (8)
-- password_hash values are bcrypt hashes of 'Password@123'
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO students
    (student_id, name, email, password_hash, college_id, department,
     academic_year, bio, preferred_role, experience_level, availability,
     profile_visibility)
VALUES
(1, 'Arjun Krishnamoorthy', 'arjun.k@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    1, 'Computer Science Engineering', '3rd',
    'Passionate ML engineer with hands-on project experience in NLP and computer vision.',
    'ML Engineer', 'Intermediate', 'Full-time', 'Public'),

(2, 'Priya Subramaniam', 'priya.s@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    1, 'Information Technology', '3rd',
    'Full-stack developer who loves building beautiful and scalable web apps.',
    'Full Stack Developer', 'Advanced', 'Weekends', 'Public'),

(3, 'Rahul Verma', 'rahul.v@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    2, 'Electronics and Communication', '2nd',
    'Embedded systems enthusiast exploring IoT and edge computing solutions.',
    'Hardware Engineer', 'Beginner', 'Part-time', 'College-only'),

(4, 'Sneha Patel', 'sneha.p@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    2, 'Data Science', '4th',
    'Data storyteller with strong SQL and visualization skills. Looking for hackathon teams.',
    'Data Analyst', 'Advanced', 'Full-time', 'Public'),

(5, 'Karthik Rajan', 'karthik.r@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    3, 'Computer Science Engineering', '1st',
    'Freshman exploring Java development and competitive programming.',
    'Backend Developer', 'Beginner', 'Weekends', 'Public'),

(6, 'Divya Nair', 'divya.n@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    3, 'Computer Science Engineering', '4th',
    'Team leader with strong communication skills and experience managing 5+ hackathon teams.',
    'Team Lead', 'Advanced', 'Full-time', 'Public'),

(7, 'Vignesh Murugan', 'vignesh.m@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    1, 'Artificial Intelligence', '2nd',
    'AI/ML enthusiast focusing on deep learning and generative models.',
    'AI Researcher', 'Intermediate', 'Part-time', 'Public'),

(8, 'Anjali Sharma', 'anjali.s@ace.dev',
    '$2a$12$Xe7KkQtFVGk0CvR9eG1NJeVHsH5DGHOkxEeTPF9UGZy6Fj6p6qGQa',
    2, 'Information Technology', 'Alumni',
    'Industry professional and mentor; alumni returning to judge and guide hackathon teams.',
    'Mentor / Judge', 'Advanced', 'Weekends', 'Public');

-- -----------------------------------------------------------------------------
-- 5. student_skills
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO student_skills (student_id, skill_id, proficiency_level, years_of_experience) VALUES
-- Arjun: Python, ML, SQL
(1, 1, 'Advanced',      2.5),
(1, 3, 'Intermediate',  1.5),
(1, 4, 'Intermediate',  1.0),
-- Priya: React, Node.js, Python
(2, 2, 'Expert',        3.0),
(2, 6, 'Advanced',      2.0),
(2, 1, 'Intermediate',  1.5),
-- Rahul: Java, Communication
(3, 5, 'Beginner',      0.5),
(3, 9, 'Intermediate',  1.0),
-- Sneha: SQL, Data Analysis, Python
(4, 4, 'Expert',        3.5),
(4, 7, 'Advanced',      2.5),
(4, 1, 'Advanced',      2.0),
-- Karthik: Java, Python
(5, 5, 'Beginner',      0.3),
(5, 1, 'Beginner',      0.3),
-- Divya: Leadership, Communication, React
(6, 8, 'Expert',        4.0),
(6, 9, 'Advanced',      3.5),
(6, 2, 'Intermediate',  1.5),
-- Vignesh: Python, ML, Data Analysis
(7, 1, 'Advanced',      2.0),
(7, 3, 'Advanced',      2.0),
(7, 7, 'Intermediate',  1.0),
-- Anjali: Leadership, Communication, UI/UX
(8, 8, 'Expert',        5.0),
(8, 9, 'Expert',        5.0),
(8, 10,'Advanced',      3.0);

-- -----------------------------------------------------------------------------
-- 6. student_interests
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO student_interests (student_id, interest_id) VALUES
(1, 1), (1, 3),          -- Arjun: AI, Data Science
(2, 2), (2, 1),          -- Priya: Web Dev, AI
(3, 5), (3, 4),          -- Rahul: IoT, Cybersecurity
(4, 3), (4, 1),          -- Sneha: Data Science, AI
(5, 2), (5, 4),          -- Karthik: Web Dev, Cybersecurity
(6, 1), (6, 2),          -- Divya: AI, Web Dev
(7, 1), (7, 3),          -- Vignesh: AI, Data Science
(8, 1), (8, 2), (8, 3);  -- Anjali: AI, Web Dev, Data Science

-- -----------------------------------------------------------------------------
-- 7. Events (2)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO events
    (event_id, event_name, description, event_date, registration_deadline,
     required_team_size, event_type, eligibility)
VALUES
(1, 'KIT HackFest 2026',
    'A 36-hour inter-college hackathon focused on AI and social impact solutions. '
    'Teams will prototype, pitch, and compete for prizes and internship opportunities.',
    '2026-12-15', '2026-11-30', 4, 'Hackathon',
    'Open to all undergraduate students'),

(2, 'DataStorm 2026',
    'A data science competition where teams analyze real-world datasets to uncover insights. '
    'Participants use Python, SQL, and ML to build predictive models and dashboards.',
    '2026-12-20', '2026-12-05', 3, 'Datathon',
    'Open to 2nd year and above');

-- -----------------------------------------------------------------------------
-- 8. Teams (2)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO teams (team_id, team_name, event_id, leader_id, description, team_status) VALUES
(1, 'Neural Nexus',  1, 1,
    'Building an AI-powered mental health support tool for college students.',
    'Open'),
(2, 'DataDynamos',   2, 4,
    'Analyzing urban mobility data to predict traffic patterns and reduce congestion.',
    'Open');

-- -----------------------------------------------------------------------------
-- 9. team_members (at least 4; event_id denormalized)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO team_members
    (team_id, student_id, event_id, assigned_role, membership_status)
VALUES
(1, 1, 1, 'ML Engineer',       'Active'),   -- Arjun in Neural Nexus
(1, 2, 1, 'Full Stack Dev',    'Active'),   -- Priya in Neural Nexus
(1, 6, 1, 'Team Lead',         'Active'),   -- Divya in Neural Nexus
(1, 7, 1, 'AI Researcher',     'Pending'),  -- Vignesh invited, pending
(2, 4, 2, 'Data Analyst',      'Active'),   -- Sneha in DataDynamos
(2, 1, 2, 'ML Engineer',       'Active');   -- Arjun also in DataDynamos (different event, OK)

-- Wait — Arjun (student_id=1) joins both team 1 (event 1) and team 2 (event 2).
-- That's fine: UNIQUE(student_id, event_id) only blocks same student in same event.

-- -----------------------------------------------------------------------------
-- 10. team_requirements (2)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO team_requirements (team_id, required_skill_id, minimum_proficiency) VALUES
(1, 3, 'Advanced'),     -- Neural Nexus needs Advanced ML
(2, 7, 'Intermediate'); -- DataDynamos needs Intermediate Data Analysis

-- -----------------------------------------------------------------------------
-- 11. Invitations (2)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO invitations
    (invitation_id, team_id, sender_id, receiver_id, invitation_status)
VALUES
(1, 1, 1, 7, 'Pending'),    -- Arjun invites Vignesh to Neural Nexus
(2, 2, 4, 3, 'Declined');   -- Sneha invited Rahul to DataDynamos; Rahul declined

-- -----------------------------------------------------------------------------
-- 12. match_recommendations (3)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO match_recommendations
    (student_id, recommended_student_id, event_id, compatibility_score, explanation)
VALUES
(2, 7, 1, 0.9150,
    'Vignesh complements Priya''s full-stack skills with strong ML expertise, '
    'directly addressing Neural Nexus''s AI requirement.'),
(1, 4, 2, 0.8730,
    'Sneha''s expert-level SQL and data analysis skills pair well with Arjun''s '
    'Python background for the DataStorm competition.'),
(6, 5, 1, 0.6420,
    'Karthik is an emerging developer whose Java background could be useful; '
    'compatibility is moderate given his beginner level.');

-- -----------------------------------------------------------------------------
-- 13. match_feedback (2)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO match_feedback
    (student_id, recommended_student_id, event_id, feedback_type)
VALUES
(2, 7, 1, 'Like'),     -- Priya liked the Vignesh recommendation
(1, 4, 2, 'Like');     -- Arjun liked the Sneha recommendation

-- -----------------------------------------------------------------------------
-- 14. student_availability (sample for 3 students)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO student_availability
    (student_id, day_of_week, start_time, end_time)
VALUES
-- Arjun: Mon–Fri evenings + Saturday mornings
(1, 'Monday',    '18:00:00', '22:00:00'),
(1, 'Wednesday', '18:00:00', '22:00:00'),
(1, 'Saturday',  '09:00:00', '13:00:00'),
-- Priya: Weekends
(2, 'Saturday',  '10:00:00', '18:00:00'),
(2, 'Sunday',    '10:00:00', '16:00:00'),
-- Divya: Every day
(6, 'Monday',    '09:00:00', '21:00:00'),
(6, 'Tuesday',   '09:00:00', '21:00:00'),
(6, 'Wednesday', '09:00:00', '21:00:00'),
(6, 'Thursday',  '09:00:00', '21:00:00'),
(6, 'Friday',    '09:00:00', '21:00:00'),
(6, 'Saturday',  '10:00:00', '18:00:00');

-- =============================================================================
-- End of sample_data.sql
-- =============================================================================

