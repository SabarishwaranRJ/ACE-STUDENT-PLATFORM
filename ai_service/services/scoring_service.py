from typing import Any, List, Dict
import numpy as np

class ScoringService:
    def __init__(self, settings: Any, similarity_service: Any, embedding_service: Any):
        self.weights = {
            "skill": settings.weight_skill,
            "interest": settings.weight_interest,
            "experience": settings.weight_experience,
            "availability": settings.weight_availability,
            "collaboration": settings.weight_collaboration,
        }
        self.sim = similarity_service
        self.emb = embedding_service

    def compute_match_score(self, requester: Any, candidate: Any, required_skills: List[str]) -> Dict[str, Any]:
        skill_score = self.sim.skill_compatibility(
            requester.skills, candidate.skills, required_skills
        )
        interest_score = self.sim.interest_similarity(
            requester.interests, candidate.interests
        )
        exp_score = self.sim.experience_compatibility(
            requester.experienceLevel or "Intermediate",
            candidate.experienceLevel or "Intermediate"
        )
        avail_score = self.sim.availability_compatibility(
            requester.availabilitySlots, candidate.availabilitySlots
        )
        collab_score = self.sim.collaboration_score(requester, candidate, self.emb)

        raw = (
            self.weights["skill"] * skill_score +
            self.weights["interest"] * interest_score +
            self.weights["experience"] * exp_score +
            self.weights["availability"] * avail_score +
            self.weights["collaboration"] * collab_score
        )

        final_score = round(float(np.clip(raw * 100.0, 1.0, 100.0)), 2)

        return {
            "finalScore": final_score,
            "breakdown": {
                "skill": round(skill_score * 100.0, 2),
                "interest": round(interest_score * 100.0, 2),
                "experience": round(exp_score * 100.0, 2),
                "availability": round(avail_score * 100.0, 2),
                "collaboration": round(collab_score * 100.0, 2),
            }
        }

    def generate_explanation(self, requester: Any, candidate: Any, required_skills: List[str], score_breakdown: Dict[str, float]) -> str:
        req_skills = {s.skillName.lower() for s in requester.skills}
        cand_skills = {s.skillName.lower() for s in candidate.skills}
        req_set = {s.lower() for s in required_skills}

        complementary = [s for s in candidate.skills if s.skillName.lower() not in req_skills]
        shared_interests = [i for i in candidate.interests if i.lower() in {x.lower() for x in requester.interests}]
        covers_required = [s for s in cand_skills if s in req_set]

        reasons = []
        if complementary:
            names = ", ".join(s.skillName for s in complementary[:3])
            reasons.append(f"brings complementary skills ({names}) that fill your team gaps")

        if covers_required:
            names = ", ".join(s.capitalize() for s in covers_required[:2])
            reasons.append(f"fulfills required skills: {names}")

        if shared_interests:
            reasons.append(f"shares common interests in {', '.join(shared_interests[:2])}")

        if score_breakdown.get("availability", 0) >= 70:
            reasons.append("has strong schedule alignment")

        if not reasons:
            reasons.append("has a balanced technical profile well-suited for collaboration")

        return f"Recommended because {candidate.name} " + " and ".join(reasons[:3]) + "."

    def identify_skill_matches(self, requester: Any, candidate: Any, required_skills: List[str]) -> Dict[str, List[str]]:
        req_skills = {s.skillName.lower() for s in requester.skills}
        cand_skills = {s.skillName.lower() for s in candidate.skills}

        return {
            "skillMatches": [s.skillName for s in candidate.skills if s.skillName.lower() in req_skills],
            "complementarySkills": [s.skillName for s in candidate.skills if s.skillName.lower() not in req_skills],
            "missingSkills": [s for s in required_skills if s.lower() not in cand_skills],
        }
