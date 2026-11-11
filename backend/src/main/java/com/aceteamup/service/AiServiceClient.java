package com.aceteamup.service;

import com.aceteamup.dto.ai.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://127.0.0.1:8000}")
    private String aiServiceUrl;

    public AiServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public MatchResponse matchStudents(Map<String, Object> payload) {
        String url = aiServiceUrl + "/api/ai/match-students";
        try {
            ResponseEntity<MatchResponse> response = restTemplate.postForEntity(url, payload, MatchResponse.class);
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("AI service /match-students unavailable: {}. Falling back to default response.", ex.getMessage());
            return MatchResponse.builder()
                    .recommendations(Collections.emptyList())
                    .totalCandidatesEvaluated(0)
                    .processingTimeMs(0.0)
                    .build();
        }
    }

    public OptimizeTeamResponse optimizeTeam(Map<String, Object> payload) {
        String url = aiServiceUrl + "/api/ai/optimize-team";
        try {
            ResponseEntity<OptimizeTeamResponse> response = restTemplate.postForEntity(url, payload, OptimizeTeamResponse.class);
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("AI service /optimize-team unavailable: {}. Falling back.", ex.getMessage());
            return OptimizeTeamResponse.builder()
                    .selectedStudentIds(Collections.emptyList())
                    .selectedStudents(Collections.emptyList())
                    .feasible(false)
                    .explanation("AI service currently unavailable for optimization: " + ex.getMessage())
                    .objectiveValue(0.0)
                    .skillCoverage(0.0)
                    .processingTimeMs(0.0)
                    .build();
        }
    }

    public SkillGapResponse getSkillGap(Map<String, Object> payload) {
        String url = aiServiceUrl + "/api/ai/skill-gap-analysis";
        try {
            ResponseEntity<SkillGapResponse> response = restTemplate.postForEntity(url, payload, SkillGapResponse.class);
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("AI service /skill-gap-analysis unavailable: {}. Falling back.", ex.getMessage());
            return SkillGapResponse.builder()
                    .presentSkills(Collections.emptyList())
                    .missingSkills(Collections.emptyList())
                    .underrepresentedSkills(Collections.emptyList())
                    .coveragePercent(0.0)
                    .recommendedProfiles(Collections.emptyList())
                    .candidateMatches(Collections.emptyList())
                    .build();
        }
    }

    public void sendFeedback(FeedbackRequest req) {
        String url = aiServiceUrl + "/api/ai/feedback";
        try {
            restTemplate.postForEntity(url, req, Map.class);
        } catch (RestClientException ex) {
            log.warn("Failed to notify AI service of feedback: {}", ex.getMessage());
        }
    }

    public AiHealthResponse checkHealth() {
        String url = aiServiceUrl + "/api/ai/health";
        try {
            ResponseEntity<AiHealthResponse> response = restTemplate.getForEntity(url, AiHealthResponse.class);
            return response.getBody();
        } catch (RestClientException ex) {
            return AiHealthResponse.builder()
                    .status("UNAVAILABLE: " + ex.getMessage())
                    .modelLoaded(false)
                    .version("1.0.0")
                    .embeddingDimension(0)
                    .build();
        }
    }
}
