import numpy as np
from services.similarity_service import SimilarityService
from models.schemas import SkillProfile, AvailabilitySlot

sim = SimilarityService()

def test_cosine_similarity_identical_vectors():
    v = np.array([1.0, 2.0, 3.0], dtype=np.float32)
    v = v / np.linalg.norm(v)
    assert np.isclose(sim.cosine_sim(v, v), 1.0, atol=1e-4)

def test_cosine_similarity_orthogonal_vectors():
    v1 = np.array([1.0, 0.0], dtype=np.float32)
    v2 = np.array([0.0, 1.0], dtype=np.float32)
    # Orthogonal cosine = 0.0, mapped to [0, 1] scale = (0 + 1) / 2 = 0.5
    assert np.isclose(sim.cosine_sim(v1, v2), 0.5, atol=1e-4)

def test_complementary_skill_compatibility():
    req_skills = [SkillProfile(skillName="Java", proficiencyLevel="Advanced")]
    cand_skills_comp = [SkillProfile(skillName="React", proficiencyLevel="Intermediate")]
    cand_skills_dupe = [SkillProfile(skillName="Java", proficiencyLevel="Beginner")]

    score_comp = sim.skill_compatibility(req_skills, cand_skills_comp, ["Java", "React"])
    score_dupe = sim.skill_compatibility(req_skills, cand_skills_dupe, ["Java", "React"])

    assert score_comp > score_dupe, "Complementary skills should score higher than redundant duplicate skills"

def test_experience_compatibility():
    assert sim.experience_compatibility("Beginner", "Intermediate") == 1.0
    assert sim.experience_compatibility("Intermediate", "Intermediate") == 0.8
    assert sim.experience_compatibility("Beginner", "Advanced") == 0.5

def test_availability_overlap():
    slots_a = [AvailabilitySlot(dayOfWeek="Monday", startTime="09:00", endTime="12:00")]
    slots_b = [AvailabilitySlot(dayOfWeek="Monday", startTime="10:00", endTime="13:00")]
    score = sim.availability_compatibility(slots_a, slots_b)
    # Overlap is 10:00 to 12:00 (120 min), total req is 180 min -> 120/180 = 0.667
    assert np.isclose(score, 2.0 / 3.0, atol=1e-2)

def test_interest_jaccard():
    score = sim.interest_similarity(["AI", "Web"], ["Web", "Mobile"])
    assert np.isclose(score, 1.0 / 3.0, atol=1e-3)
