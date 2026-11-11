from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health():
    res = client.get("/api/ai/health")
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "UP"

def test_match_students_endpoint():
    payload = {
        "requestingStudent": {
            "studentId": 1,
            "name": "Alice",
            "skills": [{"skillName": "Java", "proficiencyLevel": "Advanced"}],
            "interests": ["Backend"],
            "experienceLevel": "Intermediate"
        },
        "candidateStudents": [
            {
                "studentId": 2,
                "name": "Bob",
                "skills": [{"skillName": "React", "proficiencyLevel": "Intermediate"}],
                "interests": ["Frontend"],
                "experienceLevel": "Intermediate"
            }
        ],
        "requiredSkills": ["Java", "React"],
        "topK": 5
    }
    res = client.post("/api/ai/match-students", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert len(data["recommendations"]) == 1
    assert data["recommendations"][0]["recommendedStudentId"] == 2

def test_skill_gap_analysis_endpoint():
    payload = {
        "team": {
            "teamId": 1,
            "eventId": 10,
            "existingMemberIds": [1],
            "existingSkills": ["Java", "Spring Boot"],
            "requiredSkills": ["Java", "React", "AWS"]
        },
        "requiredEventSkills": ["Java", "React", "AWS"],
        "candidateStudents": [
            {
                "studentId": 2,
                "name": "Bob",
                "skills": [{"skillName": "React", "proficiencyLevel": "Advanced"}, {"skillName": "AWS", "proficiencyLevel": "Intermediate"}]
            }
        ]
    }
    res = client.post("/api/ai/skill-gap-analysis", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert "React" in data["missingSkills"]
    assert len(data["candidateMatches"]) > 0

def test_feedback_endpoint():
    payload = {
        "studentId": 1,
        "recommendedStudentId": 2,
        "eventId": 10,
        "feedbackType": "Like"
    }
    res = client.post("/api/ai/feedback", json=payload)
    assert res.status_code == 200
    assert res.json()["status"] == "feedback_recorded"
