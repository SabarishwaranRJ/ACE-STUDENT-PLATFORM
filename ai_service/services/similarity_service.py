import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from typing import List, Any

class SimilarityService:
    def cosine_sim(self, a: np.ndarray, b: np.ndarray) -> float:
        if np.linalg.norm(a) == 0 or np.linalg.norm(b) == 0:
            return 0.0
        sim = cosine_similarity(a.reshape(1, -1), b.reshape(1, -1))[0][0]
        return float(np.clip((sim + 1.0) / 2.0, 0.0, 1.0))

    def skill_compatibility(self, requester_skills: List[Any], candidate_skills: List[Any], required_skills: List[str]) -> float:
        req_set = {s.skillName.lower() for s in requester_skills}
        cand_set = {s.skillName.lower() for s in candidate_skills}
        required_set = {s.lower() for s in required_skills}

        if not cand_set:
            return 0.0

        # Complementary: skills candidate has that requester lacks
        complementary = cand_set - req_set
        complementary_ratio = len(complementary) / len(cand_set)

        # Coverage of required event skills
        required_coverage = 1.0
        if required_set:
            covered = len(cand_set & required_set)
            required_coverage = covered / len(required_set)

        # Overlap penalty: too much duplicate skill creates redundancy
        overlap = cand_set & req_set
        overlap_penalty = (len(overlap) / max(len(req_set | cand_set), 1)) * 0.25

        score = (0.55 * complementary_ratio + 0.45 * required_coverage) - overlap_penalty
        return float(np.clip(score, 0.05, 1.0))

    def experience_compatibility(self, requester_level: str, candidate_level: str) -> float:
        level_map = {"Beginner": 1, "Intermediate": 2, "Advanced": 3}
        r = level_map.get(requester_level or "Intermediate", 2)
        c = level_map.get(candidate_level or "Intermediate", 2)

        diff = abs(r - c)
        if diff == 1:
            return 1.0  # Adjacent levels create great peer-mentorship
        elif diff == 0:
            return 0.8  # Same level
        else:
            return 0.5  # Large gap

    def availability_compatibility(self, requester_slots: List[Any], candidate_slots: List[Any]) -> float:
        if not requester_slots or not candidate_slots:
            return 0.6  # Default neutral when not specified

        overlap_minutes = 0
        total_req_minutes = 0

        for r in requester_slots:
            r_start = self._time_to_minutes(r.startTime)
            r_end = self._time_to_minutes(r.endTime)
            total_req_minutes += max(0, r_end - r_start)

            for c in candidate_slots:
                if r.dayOfWeek.lower() == c.dayOfWeek.lower():
                    c_start = self._time_to_minutes(c.startTime)
                    c_end = self._time_to_minutes(c.endTime)
                    overlap = max(0, min(r_end, c_end) - max(r_start, c_start))
                    overlap_minutes += overlap

        if total_req_minutes == 0:
            return 0.6

        return float(np.clip(overlap_minutes / total_req_minutes, 0.0, 1.0))

    def _time_to_minutes(self, time_str: str) -> int:
        try:
            parts = time_str.split(":")
            return int(parts[0]) * 60 + int(parts[1])
        except Exception:
            return 0

    def interest_similarity(self, req_interests: List[str], cand_interests: List[str]) -> float:
        req_set = {i.lower() for i in req_interests}
        cand_set = {i.lower() for i in cand_interests}

        if not req_set and not cand_set:
            return 0.5

        intersection = len(req_set & cand_set)
        union = len(req_set | cand_set)
        return float(intersection / union) if union > 0 else 0.0

    def collaboration_score(self, req_profile: Any, cand_profile: Any, embedding_service: Any) -> float:
        req_emb = embedding_service.encode_student(req_profile)
        cand_emb = embedding_service.encode_student(cand_profile)
        return self.cosine_sim(req_emb, cand_emb)
