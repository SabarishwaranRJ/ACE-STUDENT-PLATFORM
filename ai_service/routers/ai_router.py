import time
from fastapi import APIRouter, HTTPException
from models.schemas import (
    MatchStudentsRequest, MatchStudentsResponse, RecommendationResult,
    OptimizeTeamRequest, OptimizeTeamResponse,
    SkillGapRequest, SkillGapResponse,
    FeedbackRequest, HealthResponse
)

router = APIRouter(prefix="/api/ai", tags=["AI Matching"])

# References injected by main.py
embedding_service = None
similarity_service = None
knn_service = None
scoring_service = None
optimization_service = None
skill_gap_service = None

@router.get("/health", response_model=HealthResponse)
async def health():
    loaded = embedding_service.model_loaded if embedding_service else False
    dim = embedding_service.embedding_dim if embedding_service else 384
    return HealthResponse(
        status="UP",
        modelLoaded=loaded,
        version="1.0.0",
        embeddingDimension=dim
    )

@router.post("/match-students", response_model=MatchStudentsResponse)
async def match_students(req: MatchStudentsRequest):
    start = time.time()
    requester = req.requestingStudent
    candidates = req.candidateStudents
    required_skills = req.requiredSkills
    top_k = req.topK or 10

    if not candidates:
        return MatchStudentsResponse(
            recommendations=[],
            totalCandidatesEvaluated=0,
            processingTimeMs=round((time.time() - start) * 1000, 2)
        )

    # 1. Encode requester and candidates
    req_emb = embedding_service.encode_student(requester)
    candidate_embs = [(c.studentId, embedding_service.encode_student(c)) for c in candidates]

    # 2. KNN Candidate retrieval
    knn_results = knn_service.get_top_k_candidates(
        requester_embedding=req_emb,
        candidate_embeddings=candidate_embs,
        top_k=min(top_k * 3, len(candidates)),
        requester_id=requester.studentId
    )

    id_to_cand = {c.studentId: c for c in candidates}
    evaluated_ids = [res["studentId"] for res in knn_results]

    # 3. Weighted Compatibility Scoring (Prioritizing complementary skills)
    scored_results = []
    for cid in evaluated_ids:
        cand = id_to_cand.get(cid)
        if not cand:
            continue

        score_res = scoring_service.compute_match_score(requester, cand, required_skills)
        explanation = scoring_service.generate_explanation(requester, cand, required_skills, score_res["breakdown"])
        skill_analysis = scoring_service.identify_skill_matches(requester, cand, required_skills)

        scored_results.append(RecommendationResult(
            recommendedStudentId=cand.studentId,
            name=cand.name,
            compatibilityScore=score_res["finalScore"],
            explanation=explanation,
            skillMatches=skill_analysis["skillMatches"],
            complementarySkills=skill_analysis["complementarySkills"],
            missingSkills=skill_analysis["missingSkills"],
            scoreBreakdown=score_res["breakdown"]
        ))

    scored_results.sort(key=lambda x: x.compatibilityScore, reverse=True)
    final_recs = scored_results[:top_k]

    return MatchStudentsResponse(
        recommendations=final_recs,
        totalCandidatesEvaluated=len(candidates),
        processingTimeMs=round((time.time() - start) * 1000, 2)
    )

@router.post("/optimize-team", response_model=OptimizeTeamResponse)
async def optimize_team(req: OptimizeTeamRequest):
    return optimization_service.optimize_team(req)

@router.post("/skill-gap-analysis", response_model=SkillGapResponse)
async def skill_gap_analysis(req: SkillGapRequest):
    return skill_gap_service.analyze(req)

@router.post("/feedback")
async def submit_feedback(req: FeedbackRequest):
    return {
        "status": "feedback_recorded",
        "studentId": req.studentId,
        "recommendedStudentId": req.recommendedStudentId,
        "feedbackType": req.feedbackType
    }
