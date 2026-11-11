# ACE TeamUp — Database Setup Instructions

> [!IMPORTANT]
> Complete every step in order. Steps 1–5 are one-time setup; Step 6 verifies correctness.

---

## Step 1 — Verify MySQL 8+ is Installed

```bash
mysql --version
# Expected: mysql  Ver 8.x.x ...
```

If MySQL is not installed, download it from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/) and choose **MySQL 8.0+** or **MySQL 8.4 LTS**.

> [!NOTE]
> The schema uses `utf8mb4_unicode_ci` collation and InnoDB features (FK constraints, generated columns) that require MySQL 8+.

---

## Step 2 — Create the Application Database User

Connect as root (or any superuser):

```bash
mysql -u root -p
```

Then run the following SQL to create a **least-privilege** application user:

```sql
-- Create the database (idempotent)
CREATE DATABASE IF NOT EXISTS ace_teamup
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create the application user (replace 'StrongPassword@2026' with a real secret)
CREATE USER IF NOT EXISTS 'aceteamup_app'@'localhost'
  IDENTIFIED BY 'StrongPassword@2026';

-- Grant only what the application needs
GRANT SELECT, INSERT, UPDATE, DELETE
  ON ace_teamup.*
  TO 'aceteamup_app'@'localhost';

-- Grant additional DDL rights only during initial setup or migrations
-- (Remove after go-live if using spring.jpa.hibernate.ddl-auto=validate)
GRANT CREATE, ALTER, DROP, INDEX, REFERENCES
  ON ace_teamup.*
  TO 'aceteamup_app'@'localhost';

FLUSH PRIVILEGES;
```

> [!TIP]
> After the schema is stable and you switch to `ddl-auto=validate`, revoke the DDL grants:
> ```sql
> REVOKE CREATE, ALTER, DROP, INDEX, REFERENCES ON ace_teamup.* FROM 'aceteamup_app'@'localhost';
> FLUSH PRIVILEGES;
> ```

Update `application.properties`:

```properties
spring.datasource.username=aceteamup_app
spring.datasource.password=StrongPassword@2026
```

---

## Step 3 — Run `schema.sql` (Create Tables)

```bash
mysql -u root -p ace_teamup < database/schema.sql
```

Or from inside the MySQL shell:

```sql
SOURCE /path/to/database/schema.sql;
```

Expected output: `Query OK` for each `DROP TABLE IF EXISTS` and `CREATE TABLE` statement.  
The script is **idempotent** — safe to re-run; it drops and recreates all tables.

> [!WARNING]
> Re-running `schema.sql` **drops all data**. Only do this in development or on a fresh install.

---

## Step 4 — Run `indexes.sql` (Add Performance Indexes)

```bash
mysql -u root -p ace_teamup < database/indexes.sql
```

The file uses `CREATE INDEX IF NOT EXISTS`, so it is safe to run multiple times.

> [!NOTE]
> Some indexes augment those already created in `schema.sql`.  
> Full-text indexes (`ft_students_bio`, `ft_events_description`) are part of the schema
> and do not need to be re-created here.

---

## Step 5 — Run `sample_data.sql` (Seed Test Data)

```bash
mysql -u root -p ace_teamup < database/sample_data.sql
```

All `INSERT` statements use `INSERT IGNORE`, so running this more than once is safe.

The seed data includes:
| Entity | Count |
|---|---|
| Colleges | 3 |
| Students | 8 |
| Skills | 10 |
| Interests | 5 |
| Events | 2 (1 Hackathon, 1 Datathon) |
| Teams | 2 |
| Team Members | 6 |
| Invitations | 2 |
| Match Recommendations | 3 |
| Match Feedback | 2 |
| Availability Slots | 11 |

---

## Step 6 — Verify the Setup

Connect to the database and run these spot-check queries:

```sql
USE ace_teamup;

-- 1. Table count — should be 14
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'ace_teamup';

-- 2. All 8 students
SELECT student_id, name, email, academic_year, experience_level
FROM students;

-- 3. Skills per student
SELECT s.name, sk.skill_name, ss.proficiency_level
FROM student_skills ss
JOIN students s  ON s.student_id = ss.student_id
JOIN skills sk   ON sk.skill_id  = ss.skill_id
ORDER BY s.name, ss.proficiency_level;

-- 4. Open teams with their events
SELECT t.team_name, e.event_name, t.team_status
FROM teams t
JOIN events e ON e.event_id = t.event_id
WHERE t.team_status = 'Open';

-- 5. Top match recommendations by score
SELECT
    s.name   AS for_student,
    r.name   AS recommended,
    ev.event_name,
    mr.compatibility_score
FROM match_recommendations mr
JOIN students s  ON s.student_id  = mr.student_id
JOIN students r  ON r.student_id  = mr.recommended_student_id
JOIN events ev   ON ev.event_id   = mr.event_id
ORDER BY mr.compatibility_score DESC;

-- 6. Pending invitations
SELECT
    sender.name    AS sender,
    receiver.name  AS receiver,
    t.team_name,
    i.invitation_status
FROM invitations i
JOIN students sender   ON sender.student_id   = i.sender_id
JOIN students receiver ON receiver.student_id = i.receiver_id
JOIN teams t           ON t.team_id           = i.team_id
WHERE i.invitation_status = 'Pending';

-- 7. Full-text search demo
SELECT student_id, name, bio
FROM students
WHERE MATCH(bio) AGAINST('machine learning' IN BOOLEAN MODE);
```

---

## Step 7 — Spring Boot Configuration Notes

### `application.properties` key settings

| Property | Value | Reason |
|---|---|---|
| `ddl-auto` | `validate` | Hibernate validates schema against entities on startup without touching data |
| `open-in-view` | `false` | Prevents lazy-load outside transaction (best practice) |
| `show-sql` | `false` | Avoid log spam in production; set `DEBUG` on `org.hibernate.SQL` instead |
| `MySQL8Dialect` | ✓ | Required for MySQL 8-specific features (window functions, JSON, etc.) |

### Running Locally

```bash
cd backend
./mvnw spring-boot:run
```

The app connects to `localhost:3306/ace_teamup`. Ensure MySQL is running before starting:

```bash
# macOS/Linux
sudo systemctl start mysql

# Windows (PowerShell, as Administrator)
Start-Service -Name MySQL80
```

---

## Step 8 — AI/ML Integration Notes (Python Matching Service)

The `match_recommendations` table acts as a **cache layer** between the Spring Boot API and an external Python ML service.

### Recommended Architecture

```
Spring Boot API  ──(REST/gRPC)──►  Python ML Service
        │                                  │
        │  reads cached scores             │  writes scores
        ▼                                  ▼
  match_recommendations table  ◄────────────
```

### Cache Strategy

| Scenario | Action |
|---|---|
| Student opens event page | Spring API queries `match_recommendations` by `(student_id, event_id)` |
| Cache miss (no rows) or stale (> 24 h) | API triggers async call to Python service |
| Python service responds | Upserts rows via `INSERT ... ON DUPLICATE KEY UPDATE` |
| Student submits feedback | Spring API writes to `match_feedback`; Python service polls/subscribes to retrain |

### Python Service — Suggested Stack

- **Framework**: FastAPI
- **ML libs**: scikit-learn, sentence-transformers (profile embedding), pandas
- **DB access**: SQLAlchemy + PyMySQL
- **Job scheduler**: APScheduler or Celery + Redis for periodic retraining

### Upsert Pattern (Python / SQLAlchemy raw SQL)

```sql
INSERT INTO match_recommendations
    (student_id, recommended_student_id, event_id, compatibility_score, explanation)
VALUES (%s, %s, %s, %s, %s)
ON DUPLICATE KEY UPDATE
    compatibility_score = VALUES(compatibility_score),
    explanation         = VALUES(explanation),
    created_at          = CURRENT_TIMESTAMP;
```

> [!TIP]
> Index `idx_mr_student_event_score (student_id, event_id, compatibility_score)` makes
> the Spring API's `ORDER BY compatibility_score DESC` query a covering-index scan —
> no row lookups needed.

---

*Generated for ACE TeamUp Hackathon Project — KIT HackFest 2026*

