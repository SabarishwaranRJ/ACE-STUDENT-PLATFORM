from typing import Any
from models.schemas import SkillGapRequest, SkillGapResponse

class SkillGapService:
    def __init__(self, scoring_service: Any, embedding_service: Any):
        self.scoring = scoring_service
        self.emb = embedding_service

    def analyze(self, request: SkillGapRequest) -> SkillGapResponse:
        team = request.team
        required = [s.strip() for s in request.requiredEventSkills]
        required_lower = [s.lower() for s in required]
        present = [s.strip() for s in team.existingSkills]
        present_lower = [s.lower() for s in present]

        missing = [s for s in required if s.lower() not in present_lower]

        # Underrepresented skills in candidates
        underrepresented = []
        for s in present:
            count = sum(
                1 for c in request.candidateStudents
                if any(cs.skillName.lower() == s.lower() for cs in c.skills)
            )
            if count < 2:
                underrepresented.append(s)

        # Match candidates who fill missing skills
        gap_set = {s.lower() for s in missing}
        candidate_matches = []
        for candidate in request.candidateStudents:
            fills = [
                cs.skillName for cs in candidate.skills
                if cs.skillName.lower() in gap_set
            ]
            if fills:
                candidate_matches.append({
                    "studentId": candidate.studentId,
                    "name": candidate.name,
                    "fillsSkills": fills,
                    "role": candidate.preferredRole,
                    "experienceLevel": candidate.experienceLevel
                })

        candidate_matches.sort(key=lambda x: len(x["fillsSkills"]), reverse=True)

        coverage = len([s for s in required_lower if s in present_lower]) / max(len(required), 1)

        recommended_profiles = []
        if missing:
            recommended_profiles.append(f"Recruit a teammate proficient in {', '.join(missing)} to complete event stack.")
        if underrepresented:
            recommended_profiles.append(f"Strengthen depth in: {', '.join(underrepresented[:3])}.")

        return SkillGapResponse(
            presentSkills=present,
            missingSkills=missing,
            underrepresentedSkills=underrepresented,
            coveragePercent=round(coverage * 100.0, 2),
            recommendedProfiles=recommended_profiles,
            candidateMatches=candidate_matches[:10]
        )
