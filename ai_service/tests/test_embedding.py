import numpy as np
from services.embedding_service import EmbeddingService
from models.schemas import StudentProfile, SkillProfile

def test_embedding_service_encode_text():
    svc = EmbeddingService(cache_size=100)
    emb = svc.encode_text("Machine learning with Python and PyTorch")
    assert isinstance(emb, np.ndarray)
    assert emb.shape[0] == 384
    assert np.isclose(np.linalg.norm(emb), 1.0, atol=1e-3)

def test_embedding_cache_consistency():
    svc = EmbeddingService(cache_size=100)
    text = "Fullstack React and Java Spring Boot developer"
    emb1 = svc.encode_text(text)
    emb2 = svc.encode_text(text)
    assert np.allclose(emb1, emb2)

def test_encode_student_profile():
    svc = EmbeddingService(cache_size=100)
    student = StudentProfile(
        studentId=1,
        name="Alice",
        bio="Experienced backend engineer with distributed systems focus",
        skills=[SkillProfile(skillName="Java", proficiencyLevel="Expert"), SkillProfile(skillName="Kafka", proficiencyLevel="Advanced")],
        interests=["Cloud Computing", "AI Systems"]
    )
    emb = svc.encode_student(student)
    assert emb.shape[0] == 384
    assert np.isclose(np.linalg.norm(emb), 1.0, atol=1e-3)

def test_encode_skills():
    svc = EmbeddingService(cache_size=100)
    emb = svc.encode_skills(["Python", "Machine Learning", "Scikit-Learn"])
    assert emb.shape[0] == 384
