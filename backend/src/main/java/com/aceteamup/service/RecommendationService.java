package com.aceteamup.service;

import com.aceteamup.dto.ai.FeedbackRequest;
import com.aceteamup.dto.ai.MatchResponse;
import com.aceteamup.dto.ai.RecommendationResult;
import com.aceteamup.entity.*;
import com.aceteamup.entity.MatchFeedback.FeedbackType;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final StudentInterestRepository studentInterestRepository;
    private final StudentAvailabilityRepository studentAvailabilityRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MatchRecommendationRepository matchRecommendationRepository;
    private final MatchFeedbackRepository matchFeedbackRepository;
    private final AiServiceClient aiServiceClient;

    public RecommendationService(StudentRepository studentRepository,
                                  EventRepository eventRepository,
                                  StudentSkillRepository studentSkillRepository,
                                  StudentInterestRepository studentInterestRepository,
                                  StudentAvailabilityRepository studentAvailabilityRepository,
                                  TeamMemberRepository teamMemberRepository,
                                  MatchRecommendationRepository matchRecommendationRepository,
                                  MatchFeedbackRepository matchFeedbackRepository,
                                  AiServiceClient aiServiceClient) {
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.studentInterestRepository = studentInterestRepository;
        this.studentAvailabilityRepository = studentAvailabilityRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.matchRecommendationRepository = matchRecommendationRepository;
        this.matchFeedbackRepository = matchFeedbackRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional
    public List<RecommendationResult> getRecommendations(String email, Long eventId, int topK) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Build requesting student profile payload
        Map<String, Object> reqProfile = buildStudentProfilePayload(student);

        // Find candidate students: eligible, not in a team for this event, public visibility, not the student themselves
        List<Student> allStudents = studentRepository.findAll();
        List<Map<String, Object>> candidateProfiles = new ArrayList<>();

        for (Student cand : allStudents) {
            if (cand.getStudentId().equals(student.getStudentId())) continue;
            if (cand.getProfileVisibility() == Student.ProfileVisibility.Private) continue;

            // Check if candidate already has a team for this event
            if (teamMemberRepository.findByStudentStudentIdAndEventId(cand.getStudentId(), eventId).isPresent()) {
                continue;
            }

            candidateProfiles.add(buildStudentProfilePayload(cand));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("requestingStudent", reqProfile);
        payload.put("candidateStudents", candidateProfiles);
        payload.put("eventDescription", event.getDescription());
        payload.put("requiredSkills", Collections.emptyList());
        payload.put("topK", topK);

        MatchResponse aiResponse = aiServiceClient.matchStudents(payload);
        List<RecommendationResult> results = aiResponse.getRecommendations();

        // Fallback scoring if AI service returns empty recommendations
        if (results == null || results.isEmpty()) {
            results = computeFallbackRecommendations(student, allStudents, eventId, topK);
        }

        // Persist to match_recommendations table
        for (RecommendationResult rec : results) {
            Optional<Student> recStudentOpt = studentRepository.findById(rec.getRecommendedStudentId());
            if (recStudentOpt.isPresent()) {
                Student recStudent = recStudentOpt.get();
                MatchRecommendation matchRec = matchRecommendationRepository
                        .findAll().stream()
                        .filter(m -> m.getStudent().getStudentId().equals(student.getStudentId())
                                && m.getRecommendedStudent().getStudentId().equals(recStudent.getStudentId())
                                && m.getEvent().getEventId().equals(eventId))
                        .findFirst()
                        .orElse(MatchRecommendation.builder()
                                .student(student)
                                .recommendedStudent(recStudent)
                                .event(event)
                                .build());

                matchRec.setCompatibilityScore(BigDecimal.valueOf(rec.getCompatibilityScore() / 100.0));
                matchRec.setExplanation(rec.getExplanation());
                matchRecommendationRepository.save(matchRec);
            }
        }

        return results;
    }

    public List<RecommendationResult> getStoredRecommendations(String email, Long eventId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return matchRecommendationRepository
                .findByStudentStudentIdAndEventEventIdOrderByCompatibilityScoreDesc(student.getStudentId(), eventId)
                .stream()
                .map(m -> RecommendationResult.builder()
                        .recommendedStudentId(m.getRecommendedStudent().getStudentId())
                        .name(m.getRecommendedStudent().getName())
                        .compatibilityScore(m.getCompatibilityScore().doubleValue() * 100.0)
                        .explanation(m.getExplanation())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveFeedback(String email, FeedbackRequest req) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Student recommended = studentRepository.findById(req.getRecommendedStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Recommended student not found"));
        Event event = eventRepository.findById(req.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        FeedbackType fType = FeedbackType.Like;
        try {
            fType = FeedbackType.valueOf(req.getFeedbackType());
        } catch (Exception ignored) {}

        MatchFeedback feedback = matchFeedbackRepository
                .findByStudentStudentIdAndEventEventId(student.getStudentId(), event.getEventId())
                .stream()
                .filter(f -> f.getRecommendedStudent().getStudentId().equals(recommended.getStudentId()))
                .findFirst()
                .orElse(MatchFeedback.builder()
                        .student(student)
                        .recommendedStudent(recommended)
                        .event(event)
                        .build());

        feedback.setFeedbackType(fType);
        matchFeedbackRepository.save(feedback);

        aiServiceClient.sendFeedback(req);
    }

    private Map<String, Object> buildStudentProfilePayload(Student s) {
        Map<String, Object> map = new HashMap<>();
        map.put("studentId", s.getStudentId());
        map.put("name", s.getName());
        map.put("bio", s.getBio());
        map.put("preferredRole", s.getPreferredRole());
        map.put("experienceLevel", s.getExperienceLevel() != null ? s.getExperienceLevel().name() : "Intermediate");
        map.put("availability", s.getAvailability() != null ? s.getAvailability().getDbValue() : "Full-time");

        // Skills
        List<Map<String, Object>> skills = studentSkillRepository.findByStudentStudentId(s.getStudentId()).stream()
                .map(ss -> {
                    Map<String, Object> sm = new HashMap<>();
                    sm.put("skillName", ss.getSkill().getSkillName());
                    sm.put("proficiencyLevel", ss.getProficiencyLevel() != null ? ss.getProficiencyLevel().name() : "Intermediate");
                    sm.put("yearsOfExperience", ss.getYearsOfExperience() != null ? ss.getYearsOfExperience().doubleValue() : 1.0);
                    return sm;
                })
                .collect(Collectors.toList());
        map.put("skills", skills);

        // Interests
        List<String> interests = studentInterestRepository.findByStudentStudentId(s.getStudentId()).stream()
                .map(si -> si.getInterest().getInterestName())
                .collect(Collectors.toList());
        map.put("interests", interests);

        // Availability slots
        List<Map<String, Object>> slots = studentAvailabilityRepository.findByStudentStudentId(s.getStudentId()).stream()
                .map(sa -> {
                    Map<String, Object> sl = new HashMap<>();
                    sl.put("dayOfWeek", sa.getDayOfWeek().name());
                    sl.put("startTime", sa.getStartTime().toString());
                    sl.put("endTime", sa.getEndTime().toString());
                    return sl;
                })
                .collect(Collectors.toList());
        map.put("availabilitySlots", slots);

        return map;
    }

    private List<RecommendationResult> computeFallbackRecommendations(Student student, List<Student> candidates, Long eventId, int topK) {
        List<RecommendationResult> results = new ArrayList<>();
        Set<String> mySkills = studentSkillRepository.findByStudentStudentId(student.getStudentId()).stream()
                .map(ss -> ss.getSkill().getSkillName().toLowerCase())
                .collect(Collectors.toSet());

        for (Student c : candidates) {
            if (c.getStudentId().equals(student.getStudentId())) continue;
            if (c.getProfileVisibility() == Student.ProfileVisibility.Private) continue;
            if (teamMemberRepository.findByStudentStudentIdAndEventId(c.getStudentId(), eventId).isPresent()) continue;

            List<String> cSkills = studentSkillRepository.findByStudentStudentId(c.getStudentId()).stream()
                    .map(ss -> ss.getSkill().getSkillName())
                    .collect(Collectors.toList());

            List<String> compSkills = cSkills.stream()
                    .filter(s -> !mySkills.contains(s.toLowerCase()))
                    .collect(Collectors.toList());

            double score = 50.0 + Math.min(compSkills.size() * 15.0, 45.0);
            String exp = String.format("Recommended because %s brings complementary skills: %s.",
                    c.getName(), compSkills.isEmpty() ? "general experience" : String.join(", ", compSkills));

            results.add(RecommendationResult.builder()
                    .recommendedStudentId(c.getStudentId())
                    .name(c.getName())
                    .compatibilityScore(score)
                    .explanation(exp)
                    .complementarySkills(compSkills)
                    .build());
        }

        results.sort((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()));
        return results.stream().limit(topK).collect(Collectors.toList());
    }
}
