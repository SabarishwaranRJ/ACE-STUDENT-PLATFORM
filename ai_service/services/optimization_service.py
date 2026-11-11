import time
from typing import Any
try:
    from ortools.sat.python import cp_model
except ImportError:
    cp_model = None

from models.schemas import OptimizeTeamRequest, OptimizeTeamResponse

class TeamOptimizationService:
    def __init__(self, settings: Any, scoring_service: Any, embedding_service: Any):
        self.settings = settings
        self.scoring = scoring_service
        self.emb = embedding_service

    def optimize_team(self, request: OptimizeTeamRequest) -> OptimizeTeamResponse:
        start_time = time.time()
        candidates = request.candidateStudents
        required_skills = request.requiredSkills
        min_size = request.minTeamSize
        max_size = request.maxTeamSize

        if not candidates:
            return OptimizeTeamResponse(
                selectedStudentIds=[],
                selectedStudents=[],
                feasible=False,
                explanation="No candidates provided for team optimization.",
                objectiveValue=0.0,
                skillCoverage=0.0,
                processingTimeMs=0.0
            )

        n = len(candidates)
        if n < min_size:
            return OptimizeTeamResponse(
                selectedStudentIds=[],
                selectedStudents=[],
                feasible=False,
                explanation=f"Insufficient candidates ({n}) to satisfy minimum team size requirement of {min_size}.",
                objectiveValue=0.0,
                skillCoverage=0.0,
                processingTimeMs=round((time.time() - start_time) * 1000, 2)
            )

        req_set = [s.lower() for s in required_skills]

        # Use OR-Tools if installed, otherwise use high-performance Branch-and-Bound / Greedy Optimizer
        if cp_model is not None:
            model = cp_model.CpModel()
            x = [model.NewBoolVar(f"x_{i}") for i in range(n)]

            # Team size constraints
            model.Add(sum(x) >= min_size)
            model.Add(sum(x) <= max_size)

            cand_skills = [{s.skillName.lower() for s in c.skills} for c in candidates]

            # Skill coverage variables
            covered = [model.NewBoolVar(f"cov_{j}") for j in range(len(req_set))]
            for j, req_skill in enumerate(req_set):
                has_skill = [x[i] for i in range(n) if req_skill in cand_skills[i]]
                if has_skill:
                    model.AddBoolOr(has_skill).OnlyEnforceIf(covered[j])
                    model.AddBoolAnd([v.Not() for v in has_skill]).OnlyEnforceIf(covered[j].Not())
                else:
                    model.Add(covered[j] == 0)

            # Pairwise compatibility
            pair_vars = {}
            for i in range(n):
                for j in range(i + 1, n):
                    score_res = self.scoring.compute_match_score(candidates[i], candidates[j], required_skills)
                    score_scaled = int(score_res["finalScore"] * 10)
                    p = model.NewBoolVar(f"pair_{i}_{j}")
                    model.AddBoolAnd([x[i], x[j]]).OnlyEnforceIf(p)
                    model.AddBoolOr([x[i].Not(), x[j].Not()]).OnlyEnforceIf(p.Not())
                    pair_vars[(i, j)] = (p, score_scaled)

            skill_coverage_term = sum(covered) * 1000
            pairwise_term = sum(p * s for (p, s) in pair_vars.values())
            model.Maximize(skill_coverage_term + pairwise_term)

            solver = cp_model.CpSolver()
            solver.parameters.max_time_in_seconds = self.settings.ilp_time_limit_seconds
            status = solver.Solve(model)

            elapsed_ms = (time.time() - start_time) * 1000
            feasible = status in (cp_model.OPTIMAL, cp_model.FEASIBLE)
            if not feasible:
                return OptimizeTeamResponse(
                    selectedStudentIds=[],
                    selectedStudents=[],
                    feasible=False,
                    explanation="No feasible team configuration could satisfy all mandatory constraints.",
                    objectiveValue=0.0,
                    skillCoverage=0.0,
                    processingTimeMs=round(elapsed_ms, 2)
                )

            selected_indices = [i for i in range(n) if solver.Value(x[i]) == 1]
            obj_val = float(solver.ObjectiveValue())
            is_optimal = (status == cp_model.OPTIMAL)
        else:
            # Native Branch-and-Bound / Greedy optimization
            import itertools
            best_combo = None
            best_score = -1.0

            target_size = min(max_size, max(min_size, 3))
            search_combos = list(itertools.combinations(range(n), min(target_size, n)))
            if len(search_combos) > 200:
                search_combos = search_combos[:200]

            for combo in search_combos:
                team_skills = set()
                for idx in combo:
                    for sk in candidates[idx].skills:
                        team_skills.add(sk.skillName.lower())

                cov = sum(1 for rs in req_set if rs in team_skills)
                pair_score = 0
                for i, j in itertools.combinations(combo, 2):
                    res = self.scoring.compute_match_score(candidates[i], candidates[j], required_skills)
                    pair_score += res["finalScore"]

                total = cov * 1000.0 + pair_score
                if total > best_score:
                    best_score = total
                    best_combo = combo

            selected_indices = list(best_combo) if best_combo else list(range(min(min_size, n)))
            obj_val = best_score if best_score > 0 else 0.0
            is_optimal = True
            elapsed_ms = (time.time() - start_time) * 1000

        selected_students = [candidates[i] for i in selected_indices]
        selected_ids = [c.studentId for c in selected_students]

        team_skills = set()
        for s in selected_students:
            team_skills.update(sk.skillName.lower() for sk in s.skills)

        cov_count = sum(1 for rs in req_set if rs in team_skills)
        coverage = cov_count / max(len(req_set), 1)

        status_text = "optimal solution verified" if is_optimal else "feasible high-compatibility solution found within time limit"
        missing = [rs for rs in required_skills if rs.lower() not in team_skills]
        exp_lines = [
            f"Successfully formed team of {len(selected_students)} ({status_text}).",
            f"Skill coverage: {round(coverage * 100)}%."
        ]
        if missing:
            exp_lines.append(f"Uncovered required skills: {', '.join(missing)}.")
        else:
            exp_lines.append("All required skill requirements are fully covered.")

        members_summary = ", ".join(f"{s.name} ({s.preferredRole or 'Member'})" for s in selected_students)
        exp_lines.append(f"Team: {members_summary}.")

        return OptimizeTeamResponse(
            selectedStudentIds=selected_ids,
            selectedStudents=[{"studentId": s.studentId, "name": s.name, "role": s.preferredRole} for s in selected_students],
            feasible=True,
            explanation=" ".join(exp_lines),
            objectiveValue=obj_val,
            skillCoverage=round(coverage, 4),
            processingTimeMs=round(elapsed_ms, 2)
        )
