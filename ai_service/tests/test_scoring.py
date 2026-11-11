from config import settings
from services.embedding_service import EmbeddingService
from services.similarity_service import SimilarityService
from services.scoring_service import ScoringService
from models.schemas import StudentProfile, SkillProfile, AvailabilitySlot

def test_weighted_scoring_formula():
    emb = EmbeddingService()
    sim = SimilarityService()
    scoring = ScoringService(settings, sim, emb)

    requester = StudentProfile(
        studentId=1,
        name="Alice",
        skills=[SkillProfile(skillName="Java"), SkillProfile(skillName="Spring Boot")],
        interests=["FinTech"],
        experienceLevel="Intermediate",
        availabilitySlots=[AvailabilitySlot(dayOfWeek="Monday", startTime="10:00", endTime="16:00")]
    )

    candidate = StudentProfile(
        studentId=2,
        name="Bob",
        skills=[SkillProfile(skillName="React"), SkillProfile(skillName="TypeScript")],
        interests=["FinTech", "Web"],
        experienceLevel="Intermediate",
        availabilitySlots=[AvailabilitySlot(dayOfWeek="Monday", startTime="12:00", endTime="18:00")]
    )

    res = scoring.compute_match_score(requester, candidate, required_skills=["Java", "React"])

    assert "finalScore" in res
    assert 0 <= res["finalScore"] <= 100
    assert "breakdown" in res
    assert "skill" in res["breakdown"]
    assert "interest" in res["breakdown"]

    explanation = scoring.generate_explanation(requester, candidate, ["Java", "React"], res["breakdown"])
    assert "Bob" in explanation
    assert "complementary" in explanation.lower() or "skills" in explanation.lower()
