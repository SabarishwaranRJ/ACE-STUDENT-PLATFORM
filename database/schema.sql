-- =============================================================================
-- ACE TeamUp — AI-Powered Student Teammate Discovery Platform
-- Database Schema (MySQL 8+)
-- Engine: InnoDB | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ace_teamup
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ace_teamup;

-- -----------------------------------------------------------------------------
-- DROP TABLES (child tables first to respect FK constraints)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS student_availability;
DROP TABLE IF EXISTS match_feedback;
DROP TABLE IF EXISTS match_recommendations;
DROP TABLE IF EXISTS invitations;
DROP TABLE IF EXISTS team_requirements;
DROP TABLE IF EXISTS team_members;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS student_interests;
DROP TABLE IF EXISTS interests;
DROP TABLE IF EXISTS student_skills;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS colleges;

-- =============================================================================
-- 1. colleges
-- Purpose: Stores participating academic institutions.
-- =============================================================================
CREATE TABLE colleges (
    college_id   INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    college_name VARCHAR(255)   NOT NULL,
    location     VARCHAR(255)   DEFAULT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (college_id),
    INDEX idx_colleges_name (college_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Participating academic institutions';

-- =============================================================================
-- 2. students
-- Purpose: Core user table; stores profile, credentials and preferences.
-- =============================================================================
CREATE TABLE students (
    student_id          INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255)   NOT NULL,
    email               VARCHAR(320)   NOT NULL,
    password_hash       VARCHAR(255)   NOT NULL,
    college_id          INT UNSIGNED   DEFAULT NULL,
    department          VARCHAR(255)   DEFAULT NULL,
    academic_year       ENUM('1st','2nd','3rd','4th','Alumni') DEFAULT NULL,
    bio                 TEXT           DEFAULT NULL,
    preferred_role      VARCHAR(100)   DEFAULT NULL,
    experience_level    ENUM('Beginner','Intermediate','Advanced') DEFAULT NULL,
    availability        ENUM('Full-time','Part-time','Weekends') DEFAULT NULL,
    profile_visibility  ENUM('Public','Private','College-only') NOT NULL DEFAULT 'Public',
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id),
    UNIQUE KEY uq_students_email (email),
    INDEX idx_students_college_id (college_id),
    INDEX idx_students_department (department),
    INDEX idx_students_experience_level (experience_level),
    INDEX idx_students_academic_year (academic_year),
    FULLTEXT INDEX ft_students_bio (bio),
    CONSTRAINT fk_students_college
        FOREIGN KEY (college_id) REFERENCES colleges (college_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Core user profiles, credentials, and preferences';

-- =============================================================================
-- 3. skills
-- Purpose: Master list of technical and soft skills.
-- =============================================================================
CREATE TABLE skills (
    skill_id       INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    skill_name     VARCHAR(150)  NOT NULL,
    skill_category VARCHAR(100)  DEFAULT NULL,
    PRIMARY KEY (skill_id),
    UNIQUE KEY uq_skills_name (skill_name),
    INDEX idx_skills_category (skill_category)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Master catalogue of technical and soft skills';

-- =============================================================================
-- 4. student_skills
-- Purpose: Many-to-many mapping of students to skills with proficiency data.
-- =============================================================================
CREATE TABLE student_skills (
    student_id          INT UNSIGNED    NOT NULL,
    skill_id            INT UNSIGNED    NOT NULL,
    proficiency_level   ENUM('Beginner','Intermediate','Advanced','Expert') NOT NULL DEFAULT 'Beginner',
    years_of_experience DECIMAL(4,1)    DEFAULT NULL,
    PRIMARY KEY (student_id, skill_id),
    INDEX idx_student_skills_skill_id (skill_id),
    CONSTRAINT fk_ss_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_ss_skill
        FOREIGN KEY (skill_id) REFERENCES skills (skill_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Student-skill associations with proficiency and experience';

-- =============================================================================
-- 5. interests
-- Purpose: Master list of hackathon / project interest domains.
-- =============================================================================
CREATE TABLE interests (
    interest_id   INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    interest_name VARCHAR(150)  NOT NULL,
    PRIMARY KEY (interest_id),
    UNIQUE KEY uq_interests_name (interest_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Master catalogue of interest/domain areas';

-- =============================================================================
-- 6. student_interests
-- Purpose: Many-to-many mapping of students to their interests.
-- =============================================================================
CREATE TABLE student_interests (
    student_id  INT UNSIGNED NOT NULL,
    interest_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (student_id, interest_id),
    INDEX idx_student_interests_interest_id (interest_id),
    CONSTRAINT fk_si_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_si_interest
        FOREIGN KEY (interest_id) REFERENCES interests (interest_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Student-interest domain associations';

-- =============================================================================
-- 7. events
-- Purpose: Hackathons, datathons, case studies, and other events.
-- =============================================================================
CREATE TABLE events (
    event_id              INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    event_name            VARCHAR(255)   NOT NULL,
    description           TEXT           DEFAULT NULL,
    event_date            DATE           DEFAULT NULL,
    registration_deadline DATE           DEFAULT NULL,
    required_team_size    TINYINT UNSIGNED DEFAULT NULL,
    event_type            ENUM('Hackathon','Datathon','Case Study','Project','Other') NOT NULL DEFAULT 'Other',
    eligibility           VARCHAR(255)   DEFAULT NULL,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id),
    INDEX idx_events_event_type (event_type),
    INDEX idx_events_registration_deadline (registration_deadline),
    INDEX idx_events_event_date (event_date),
    FULLTEXT INDEX ft_events_description (description)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Events: hackathons, datathons, case studies, projects';

-- =============================================================================
-- 8. teams
-- Purpose: Teams formed for specific events.
-- =============================================================================
CREATE TABLE teams (
    team_id     INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    team_name   VARCHAR(255)   NOT NULL,
    event_id    INT UNSIGNED   NOT NULL,
    leader_id   INT UNSIGNED   NOT NULL,
    description TEXT           DEFAULT NULL,
    team_status ENUM('Open','Full','Closed','Disbanded') NOT NULL DEFAULT 'Open',
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id),
    INDEX idx_teams_event_id (event_id),
    INDEX idx_teams_leader_id (leader_id),
    INDEX idx_teams_event_status (event_id, team_status),
    CONSTRAINT fk_teams_event
        FOREIGN KEY (event_id) REFERENCES events (event_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_teams_leader
        FOREIGN KEY (leader_id) REFERENCES students (student_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Teams formed for specific events, with status and leader';

-- =============================================================================
-- 9. team_members
-- Purpose: Membership of students in teams.
--          event_id is denormalized from teams to enforce the unique constraint
--          that a student cannot join two different teams for the same event.
-- =============================================================================
CREATE TABLE team_members (
    team_id           INT UNSIGNED   NOT NULL,
    student_id        INT UNSIGNED   NOT NULL,
    -- Denormalized: mirrors teams.event_id to enforce one-team-per-event constraint
    event_id          INT UNSIGNED   NOT NULL,
    assigned_role     VARCHAR(100)   DEFAULT NULL,
    membership_status ENUM('Active','Pending','Left','Removed') NOT NULL DEFAULT 'Active',
    joined_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, student_id),
    -- Prevents a student from joining two teams in the same event
    UNIQUE KEY uq_tm_student_event (student_id, event_id),
    INDEX idx_tm_student_id (student_id),
    INDEX idx_tm_event_id (event_id),
    INDEX idx_tm_team_status (team_id, membership_status),
    CONSTRAINT fk_tm_team
        FOREIGN KEY (team_id) REFERENCES teams (team_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_tm_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_tm_event
        FOREIGN KEY (event_id) REFERENCES events (event_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Team membership; event_id denormalized to enforce one-team-per-event uniqueness';

-- =============================================================================
-- 10. team_requirements
-- Purpose: Skill requirements posted by a team for open positions.
-- =============================================================================
CREATE TABLE team_requirements (
    requirement_id      INT UNSIGNED NOT NULL AUTO_INCREMENT,
    team_id             INT UNSIGNED NOT NULL,
    required_skill_id   INT UNSIGNED NOT NULL,
    minimum_proficiency ENUM('Beginner','Intermediate','Advanced','Expert') NOT NULL DEFAULT 'Beginner',
    PRIMARY KEY (requirement_id),
    UNIQUE KEY uq_tr_team_skill (team_id, required_skill_id),
    INDEX idx_tr_skill_id (required_skill_id),
    CONSTRAINT fk_tr_team
        FOREIGN KEY (team_id) REFERENCES teams (team_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_tr_skill
        FOREIGN KEY (required_skill_id) REFERENCES skills (skill_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Skills required by a team, with minimum proficiency level';

-- =============================================================================
-- 11. invitations
-- Purpose: Team invitation workflow between students.
--          NOTE: MySQL does not support partial unique indexes.
--          Business rule: application logic must WITHDRAW any existing Pending
--          invitation before sending a new one to the same receiver for the
--          same team. The unique constraint prevents duplicate rows outright.
-- =============================================================================
CREATE TABLE invitations (
    invitation_id     INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    team_id           INT UNSIGNED   NOT NULL,
    sender_id         INT UNSIGNED   NOT NULL,
    receiver_id       INT UNSIGNED   NOT NULL,
    invitation_status ENUM('Pending','Accepted','Declined','Withdrawn') NOT NULL DEFAULT 'Pending',
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (invitation_id),
    -- One active invitation per team-receiver pair; withdraw old ones before resending
    UNIQUE KEY uq_inv_team_receiver (team_id, receiver_id),
    INDEX idx_inv_sender_id (sender_id),
    INDEX idx_inv_receiver_status (receiver_id, invitation_status),
    INDEX idx_inv_team_id (team_id),
    CONSTRAINT fk_inv_team
        FOREIGN KEY (team_id) REFERENCES teams (team_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_inv_sender
        FOREIGN KEY (sender_id) REFERENCES students (student_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_inv_receiver
        FOREIGN KEY (receiver_id) REFERENCES students (student_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Team invitations; withdraw existing before resending to same receiver';

-- =============================================================================
-- 12. match_recommendations
-- Purpose: AI-generated teammate compatibility scores and explanations.
-- =============================================================================
CREATE TABLE match_recommendations (
    match_id               INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_id             INT UNSIGNED    NOT NULL,
    recommended_student_id INT UNSIGNED    NOT NULL,
    event_id               INT UNSIGNED    NOT NULL,
    compatibility_score    DECIMAL(5,4)    NOT NULL DEFAULT 0.0000,
    explanation            TEXT            DEFAULT NULL,
    created_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (match_id),
    UNIQUE KEY uq_mr_student_recommended_event (student_id, recommended_student_id, event_id),
    INDEX idx_mr_student_event_score (student_id, event_id, compatibility_score),
    INDEX idx_mr_recommended_student_id (recommended_student_id),
    INDEX idx_mr_event_id (event_id),
    CONSTRAINT fk_mr_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_mr_recommended
        FOREIGN KEY (recommended_student_id) REFERENCES students (student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_mr_event
        FOREIGN KEY (event_id) REFERENCES events (event_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI-generated compatibility recommendations per student-event pair';

-- =============================================================================
-- 13. match_feedback
-- Purpose: User feedback (Like/Dislike/Skip) on AI recommendations.
-- =============================================================================
CREATE TABLE match_feedback (
    feedback_id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id             INT UNSIGNED NOT NULL,
    recommended_student_id INT UNSIGNED NOT NULL,
    event_id               INT UNSIGNED NOT NULL,
    feedback_type          ENUM('Like','Dislike','Skip') NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (feedback_id),
    UNIQUE KEY uq_mf_student_recommended_event (student_id, recommended_student_id, event_id),
    INDEX idx_mf_student_event (student_id, event_id),
    INDEX idx_mf_recommended_student_id (recommended_student_id),
    CONSTRAINT fk_mf_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_mf_recommended
        FOREIGN KEY (recommended_student_id) REFERENCES students (student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_mf_event
        FOREIGN KEY (event_id) REFERENCES events (event_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='User feedback on AI match recommendations for model retraining';

-- =============================================================================
-- 14. student_availability
-- Purpose: Weekly time-slot availability for collaborative scheduling.
-- =============================================================================
CREATE TABLE student_availability (
    availability_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id      INT UNSIGNED NOT NULL,
    day_of_week     ENUM('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday') NOT NULL,
    start_time      TIME         NOT NULL,
    end_time        TIME         NOT NULL,
    PRIMARY KEY (availability_id),
    UNIQUE KEY uq_sa_student_day_start (student_id, day_of_week, start_time),
    INDEX idx_sa_student_id (student_id),
    CONSTRAINT fk_sa_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Weekly time-slot availability per student for collaborative scheduling';

-- =============================================================================
-- End of schema.sql
-- =============================================================================

