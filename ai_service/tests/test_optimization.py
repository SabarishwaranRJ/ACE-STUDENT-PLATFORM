from config import settings
from services.embedding_service import EmbeddingService
from services.similarity_service import SimilarityService
from services.scoring_service import ScoringService
from services.optimization_service import TeamOptimizationService
from models.schemas import StudentProfile, SkillProfile, OptimizeTeamRequest

def test_ilp_optimization_feasibility():
    emb = EmbeddingService()
    sim = SimilarityService()
    scoring = ScoringService(settings, sim, emb)
    opt = TeamOptimizationService(settings, scoring, emb)

    candidates = [
        StudentProfile(studentId=1, name="Alice", skills=[SkillProfile(skillName="Python"), SkillProfile(skillName="ML")]),
        StudentProfile(studentId=2, name="Bob", skills=[SkillProfile(skillName="React"), SkillProfile(skillName="CSS")]),
        StudentProfile(studentId=3, name="Charlie", skills=[SkillProfile(skillName="Java"), SkillProfile(skillName="Docker")]),
        StudentProfile(studentId=4, name="Diana", skills=[SkillProfile(skillName="Python"), SkillProfile(skillName="SQL")])
    ]

    req = OptimizeTeamRequest(
        candidateStudents=candidates,
        requiredSkills=["Python", "React", "Docker"],
        minTeamSize=2,
        maxTeamSize=3
    )

    res = opt.optimize_team(req)
    assert res.feasible
    assert 2 <= len(res.selectedStudentIds) <= 3
    assert len(set(res.selectedStudentIds)) == len(res.selectedStudentIds), "No duplicate members allowed"

def test_ilp_infeasible_when_too_few_candidates():
    emb = EmbeddingService()
    sim = SimilarityService()
    scoring = ScoringService(settings, sim, emb)
    opt = TeamOptimizationService(settings, scoring, emb)

    candidates = [
        StudentProfile(studentId=1, name="Alice", skills=[SkillProfile(skillName="Python")])
    ]

    req = OptimizeTeamRequest(
        candidateStudents=candidates,
        requiredSkills=["Python"],
        minTeamSize=3,
        maxTeamSize=4
    )

    res = opt.optimize_team(req)
    assert not res.feasible
    assert "Insufficient" in res.explanation
