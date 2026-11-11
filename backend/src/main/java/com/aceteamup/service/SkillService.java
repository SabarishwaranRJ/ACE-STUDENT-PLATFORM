package com.aceteamup.service;

import com.aceteamup.dto.skill.SkillRequest;
import com.aceteamup.dto.skill.SkillResponse;
import com.aceteamup.entity.Skill;
import com.aceteamup.exception.DuplicateResourceException;
import com.aceteamup.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(s -> SkillResponse.builder()
                        .skillId(s.getSkillId())
                        .skillName(s.getSkillName())
                        .skillCategory(s.getSkillCategory())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SkillResponse> getSkillsByCategory(String category) {
        return skillRepository.findBySkillCategory(category).stream()
                .map(s -> SkillResponse.builder()
                        .skillId(s.getSkillId())
                        .skillName(s.getSkillName())
                        .skillCategory(s.getSkillCategory())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillResponse createSkill(SkillRequest req) {
        if (skillRepository.findBySkillName(req.getSkillName()).isPresent()) {
            throw new DuplicateResourceException("Skill already exists: " + req.getSkillName());
        }

        Skill skill = Skill.builder()
                .skillName(req.getSkillName())
                .skillCategory(req.getSkillCategory())
                .build();

        Skill saved = skillRepository.save(skill);
        return SkillResponse.builder()
                .skillId(saved.getSkillId())
                .skillName(saved.getSkillName())
                .skillCategory(saved.getSkillCategory())
                .build();
    }
}
