package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.ai.OptimizeTeamRequest;
import com.aceteamup.dto.ai.OptimizeTeamResponse;
import com.aceteamup.dto.ai.SkillGapRequest;
import com.aceteamup.dto.ai.SkillGapResponse;
import com.aceteamup.service.TeamOptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/optimize")
public class TeamOptimizationController {

    private final TeamOptimizationService teamOptimizationService;

    public TeamOptimizationController(TeamOptimizationService teamOptimizationService) {
        this.teamOptimizationService = teamOptimizationService;
    }

    @PostMapping("/team")
    public ResponseEntity<ApiResponse<OptimizeTeamResponse>> optimizeTeam(@RequestBody OptimizeTeamRequest req) {
        OptimizeTeamResponse res = teamOptimizationService.optimizeTeam(req);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/skill-gap")
    public ResponseEntity<ApiResponse<SkillGapResponse>> getSkillGap(@RequestBody SkillGapRequest req) {
        SkillGapResponse res = teamOptimizationService.getSkillGap(req.getTeamId(), req.getEventId());
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}
