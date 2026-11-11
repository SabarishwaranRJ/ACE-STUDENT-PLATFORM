package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.ai.AiHealthResponse;
import com.aceteamup.dto.ai.FeedbackRequest;
import com.aceteamup.dto.ai.RecommendationResult;
import com.aceteamup.service.AiServiceClient;
import com.aceteamup.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AiServiceClient aiServiceClient;

    public RecommendationController(RecommendationService recommendationService,
                                    AiServiceClient aiServiceClient) {
        this.recommendationService = recommendationService;
        this.aiServiceClient = aiServiceClient;
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<RecommendationResult>>> getRecommendations(@AuthenticationPrincipal UserDetails userDetails,
                                                                                       @PathVariable Long eventId,
                                                                                       @RequestParam(defaultValue = "10") int topK) {
        List<RecommendationResult> recs = recommendationService.getRecommendations(userDetails.getUsername(), eventId, topK);
        return ResponseEntity.ok(ApiResponse.success(recs));
    }

    @GetMapping("/event/{eventId}/stored")
    public ResponseEntity<ApiResponse<List<RecommendationResult>>> getStoredRecommendations(@AuthenticationPrincipal UserDetails userDetails,
                                                                                             @PathVariable Long eventId) {
        List<RecommendationResult> recs = recommendationService.getStoredRecommendations(userDetails.getUsername(), eventId);
        return ResponseEntity.ok(ApiResponse.success(recs));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<Void>> saveFeedback(@AuthenticationPrincipal UserDetails userDetails,
                                                          @Valid @RequestBody FeedbackRequest req) {
        recommendationService.saveFeedback(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully", null));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<AiHealthResponse>> checkAiHealth() {
        AiHealthResponse health = aiServiceClient.checkHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
