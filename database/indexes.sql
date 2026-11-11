-- =============================================================================
-- ACE TeamUp — Performance Indexes
-- Run AFTER schema.sql (indexes augment but do not replace schema-level ones)
-- =============================================================================

USE ace_teamup;

-- -----------------------------------------------------------------------------
-- students
-- -----------------------------------------------------------------------------

-- Covering index: profile search by college, department, year, visibility
-- Useful for "show me all 3rd-year CS students at college X who are Public"
CREATE INDEX IF NOT EXISTS idx_students_college_dept_year_vis
    ON students (college_id, department, academic_year, profile_visibility);

-- Covering index: search by role + experience for teammate matching
-- Useful for "find Intermediate-level Backend Developers"
CREATE INDEX IF NOT EXISTS idx_students_role_experience
    ON students (preferred_role, experience_level);

-- Full-text search on bio is already defined in schema.sql:
--   FULLTEXT INDEX ft_students_bio (bio)
-- Use query: SELECT * FROM students WHERE MATCH(bio) AGAINST('machine learning' IN BOOLEAN MODE);

-- -----------------------------------------------------------------------------
-- student_skills
-- -----------------------------------------------------------------------------

-- Composite: find all students who have a specific skill at a given proficiency
-- Useful for AI matching engine: "who has skill X at level >= Advanced?"
CREATE INDEX IF NOT EXISTS idx_ss_skill_proficiency
    ON student_skills (skill_id, proficiency_level);

-- Covering index: list a student's skills ordered by proficiency
CREATE INDEX IF NOT EXISTS idx_ss_student_proficiency
    ON student_skills (student_id, proficiency_level, skill_id);

-- -----------------------------------------------------------------------------
-- teams
-- -----------------------------------------------------------------------------

-- Composite covering: find Open teams for a given event
-- Query: SELECT * FROM teams WHERE event_id = ? AND team_status = 'Open'
CREATE INDEX IF NOT EXISTS idx_teams_event_open
    ON teams (event_id, team_status, team_id, team_name);

-- Leader-centric lookup: all teams led by a student
CREATE INDEX IF NOT EXISTS idx_teams_leader_status
    ON teams (leader_id, team_status);

-- -----------------------------------------------------------------------------
-- team_members
-- -----------------------------------------------------------------------------

-- Composite: list active members of a team (most common dashboard query)
-- Query: SELECT * FROM team_members WHERE team_id = ? AND membership_status = 'Active'
CREATE INDEX IF NOT EXISTS idx_tm_team_active_members
    ON team_members (team_id, membership_status, student_id);

-- Student event membership check: is this student already in a team for event X?
-- Covered by UNIQUE KEY uq_tm_student_event (student_id, event_id) in schema.sql

-- -----------------------------------------------------------------------------
-- team_requirements
-- -----------------------------------------------------------------------------

-- Find teams that need a specific skill
-- Query: SELECT team_id FROM team_requirements WHERE required_skill_id = ? AND minimum_proficiency = ?
CREATE INDEX IF NOT EXISTS idx_tr_skill_proficiency
    ON team_requirements (required_skill_id, minimum_proficiency);

-- -----------------------------------------------------------------------------
-- invitations
-- -----------------------------------------------------------------------------

-- Covering: inbox view — all pending invitations for a receiver
-- Query: SELECT * FROM invitations WHERE receiver_id = ? AND invitation_status = 'Pending'
CREATE INDEX IF NOT EXISTS idx_inv_receiver_pending
    ON invitations (receiver_id, invitation_status, team_id, created_at);

-- Outbox view — invitations sent by a student
CREATE INDEX IF NOT EXISTS idx_inv_sender_status_created
    ON invitations (sender_id, invitation_status, created_at);

-- -----------------------------------------------------------------------------
-- match_recommendations
-- -----------------------------------------------------------------------------

-- Primary sort index: recommendations for a student+event ordered by score DESC
-- Covered by idx_mr_student_event_score (student_id, event_id, compatibility_score)
-- in schema.sql — no additional index needed.

-- Find all events for which a student has recommendations (dashboard feed)
CREATE INDEX IF NOT EXISTS idx_mr_student_created
    ON match_recommendations (student_id, created_at DESC);

-- -----------------------------------------------------------------------------
-- match_feedback
-- -----------------------------------------------------------------------------

-- Covering: retrieve feedback stats per event for model retraining
-- Query: SELECT feedback_type, COUNT(*) FROM match_feedback WHERE event_id = ? GROUP BY feedback_type
CREATE INDEX IF NOT EXISTS idx_mf_event_feedback_type
    ON match_feedback (event_id, feedback_type);

-- Look up whether a student has already given feedback on a specific recommendation
-- Covered by UNIQUE KEY uq_mf_student_recommended_event in schema.sql

-- -----------------------------------------------------------------------------
-- student_availability
-- -----------------------------------------------------------------------------

-- Find available students on a given day
-- Query: SELECT student_id FROM student_availability WHERE day_of_week = ? AND start_time <= ? AND end_time >= ?
CREATE INDEX IF NOT EXISTS idx_sa_day_time
    ON student_availability (day_of_week, start_time, end_time);

-- -----------------------------------------------------------------------------
-- events
-- -----------------------------------------------------------------------------

-- Active upcoming events: sort by registration deadline
-- Covered by idx_events_registration_deadline in schema.sql

-- Combined: type + deadline for filtered event listings
CREATE INDEX IF NOT EXISTS idx_events_type_deadline
    ON events (event_type, registration_deadline);

-- Full-text search on description is defined in schema.sql:
--   FULLTEXT INDEX ft_events_description (description)
-- Use query: SELECT * FROM events WHERE MATCH(description) AGAINST('AI healthcare' IN BOOLEAN MODE);

-- -----------------------------------------------------------------------------
-- colleges
-- -----------------------------------------------------------------------------

-- Location-based lookup
CREATE INDEX IF NOT EXISTS idx_colleges_location
    ON colleges (location);

-- =============================================================================
-- End of indexes.sql
-- =============================================================================

