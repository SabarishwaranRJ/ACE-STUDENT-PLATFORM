# ACE TeamUp — AI-Powered Student Teammate Discovery & Formation Platform

ACE TeamUp is an intelligent matchmaking and team formation platform tailored for students participating in hackathons, datathons, research collaborations, and capstone projects. It pairs students based on semantic skill compatibility, complementary strengths, shared domain interests, schedule alignment, and experience level diversity, backed by Google OR-Tools Integer Linear Programming (ILP).

---

## Architecture Overview

```
                                      +------------------------------------+
                                      |         Client Applications        |
                                      | (Web / Mobile Frontend / Postman)  |
                                      +-----------------+------------------+
                                                        |
                                            REST / JWT (HTTP:8080)
                                                        |
                                                        v
                                      +------------------------------------+
                                      |     Spring Boot 3.x Backend        |
                                      | - Auth & Security (JWT, BCrypt)    |
                                      | - Student & Profile Services       |
                                      | - Team & Invitation Workflows      |
                                      | - Event & Requirement Management   |
                                      | - JPA / Hibernate Repository Layer |
                                      +---------+----------------+---------+
                                                |                |
                          JDBC (Port 3306)      |                | HTTP REST (Port 8000)
                                                v                v
                        +---------------------------+   +-------------------------------+
                        |    MySQL 8+ Database      |   |   Python FastAPI AI Engine    |
                        | - Normalized Relational   |   | - Sentence-BERT (all-MiniLM)  |
                        | - Foreign Key Constraints |   | - Cosine Similarity & KNN     |
                        | - Fulltext & Comp Indexes |   | - 5-Factor Weighted Scoring   |
                        | - Anti-duplicate Safeguards|  | - OR-Tools CP-SAT Team ILP    |
                        +---------------------------+   | - Skill Gap Analysis Engine   |
                                                        +-------------------------------+
```

---

## Key AI/ML Algorithms

1. **Sentence-BERT (`all-MiniLM-L6-v2`)**: Transforms student bios, skills, and event descriptions into 384-dimensional dense semantic vectors with LRU caching.
2. **Cosine Similarity**: Measures directional alignment between profile embeddings on a normalized [0, 1] scale.
3. **K-Nearest Neighbors (Scikit-Learn `NearestNeighbors`)**: Fast candidate retrieval filtered by event eligibility and duplicate membership rules.
4. **Weighted Compatibility Scoring**:
   $$\text{Score} = 0.35 \times S_{\text{skill}} + 0.25 \times S_{\text{interest}} + 0.15 \times S_{\text{experience}} + 0.15 \times S_{\text{availability}} + 0.10 \times S_{\text{collab}}$$
   - **Complementary Skill Bonus**: Actively rewards candidates who bring skills the team lacks rather than clone existing skills.
   - **Natural Language Explanations**: Dynamically constructed explanations justifying why each candidate is recommended.
5. **Integer Linear Programming (Google OR-Tools CP-SAT)**:
   - Formulates team formation as an exact constraint satisfaction and optimization problem.
   - Solves for team size bounds $[\text{min}, \text{max}]$, mandatory skill coverage, and pairwise synergy.
6. **Skill Gap Analysis**: Identifies missing and underrepresented technical skills between the event requirement and the current team roster.

---

## Quick-Start Setup

### Prerequisites
- Java 17+ (JDK 21 LTS supported)
- Maven 3.8+
- Python 3.11+
- MySQL 8.0+ (or Docker)

### 1. Database Setup
```bash
# Connect to MySQL and run:
mysql -u root -p < database/schema.sql
mysql -u root -p < database/indexes.sql
mysql -u root -p < database/sample_data.sql
```

### 2. Python AI Service
```bash
cd ai_service
python -m venv venv
venv\Scripts\activate  # On Linux: source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
Verify: `http://localhost:8000/api/ai/health`

### 3. Spring Boot Backend
```bash
cd backend
mvn clean spring-boot:run
```
Verify: `http://localhost:8080/api/events`

---

## API Documentation

### Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | Register new student profile | No |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT token | No |

### Students (`/api/students`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/students/me` | Current user profile | Bearer JWT |
| `PUT` | `/api/students/me` | Update current profile | Bearer JWT |
| `GET` | `/api/students/{id}` | Public student profile by ID | Bearer JWT |
| `GET` | `/api/students` | List all visible students | Bearer JWT |
| `GET` | `/api/students/me/skills` | List user skills | Bearer JWT |
| `POST` | `/api/students/me/skills` | Add/update skill | Bearer JWT |
| `DELETE` | `/api/students/me/skills/{id}` | Remove skill | Bearer JWT |
| `GET` | `/api/students/me/interests` | List user interests | Bearer JWT |
| `POST` | `/api/students/me/interests/{id}` | Add interest | Bearer JWT |
| `DELETE` | `/api/students/me/interests/{id}` | Remove interest | Bearer JWT |
| `GET` | `/api/students/me/availability` | List availability slots | Bearer JWT |
| `POST` | `/api/students/me/availability` | Add availability slot | Bearer JWT |
| `DELETE` | `/api/students/me/availability/{id}`| Remove slot | Bearer JWT |

### Events (`/api/events`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/events` | List all events | No |
| `GET` | `/api/events/open` | List active registration events | No |
| `GET` | `/api/events/{id}` | Get event details | No |
| `POST` | `/api/events` | Create new event | Bearer JWT |
| `PUT` | `/api/events/{id}` | Update event | Bearer JWT |
| `DELETE` | `/api/events/{id}` | Delete event | Bearer JWT |

### Teams (`/api/teams`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/teams` | Create team for event (creator becomes leader) | Bearer JWT |
| `GET` | `/api/teams/{id}` | Full team details, roster & requirements | Bearer JWT |
| `GET` | `/api/teams/event/{eventId}` | List teams competing in event | Bearer JWT |
| `GET` | `/api/teams/my` | Current user's teams | Bearer JWT |
| `PUT` | `/api/teams/{id}` | Update team (leader only) | Bearer JWT |
| `POST` | `/api/teams/join` | Apply to join team | Bearer JWT |
| `POST` | `/api/teams/{id}/members/{sid}/approve` | Leader approves member | Bearer JWT |
| `DELETE` | `/api/teams/{id}/members/{sid}` | Leader removes member | Bearer JWT |
| `POST` | `/api/teams/{id}/leave` | Member leaves team | Bearer JWT |
| `POST` | `/api/teams/{id}/requirements` | Add required skill for team | Bearer JWT |
| `GET` | `/api/teams/{id}/requirements` | List team requirements | Bearer JWT |

### Invitations (`/api/invitations`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/invitations` | Invite student to team | Bearer JWT |
| `GET` | `/api/invitations/inbox` | List received invitations | Bearer JWT |
| `GET` | `/api/invitations/sent` | List sent invitations | Bearer JWT |
| `PUT` | `/api/invitations/{id}/respond` | Accept or decline invitation | Bearer JWT |
| `PUT` | `/api/invitations/{id}/withdraw`| Withdraw pending invitation | Bearer JWT |

### AI Recommendations & Matching (`/api/recommendations` & `/api/optimize`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/recommendations/event/{eventId}` | Dynamic AI-powered teammate recommendations | Bearer JWT |
| `GET` | `/api/recommendations/event/{eventId}/stored` | Historical recommendations from database | Bearer JWT |
| `POST` | `/api/recommendations/feedback` | Record recommendation feedback (Like/Dislike/Skip) | Bearer JWT |
| `POST` | `/api/optimize/team` | OR-Tools team optimization solver | Bearer JWT |
| `POST` | `/api/optimize/skill-gap` | Skill gap analysis for open team | Bearer JWT |

---

## Security & Ethical Principles
- **No Sensitive Attributes**: Demographics such as caste, religion, gender, or race are completely omitted from the schema, embeddings, and matching algorithms.
- **Fairness**: Candidates are evaluated strictly on verified skills, project domain interests, availability overlap, and complementary capabilities. No college prestige bias is applied.
- **Password Protection**: Passwords are saved solely as BCrypt hashes with salt.
- **Human in the Loop**: The platform provides suggestions and optimization scores, but invitation acceptance and final roster decisions remain strictly under human control.
