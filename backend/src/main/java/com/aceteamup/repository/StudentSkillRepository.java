package com.aceteamup.repository;

import com.aceteamup.entity.StudentSkill;
import com.aceteamup.entity.StudentSkill.StudentSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSkillRepository extends JpaRepository<StudentSkill, StudentSkillId> {
    List<StudentSkill> findByStudentStudentId(Long studentId);
    void deleteByStudentStudentIdAndSkillSkillId(Long studentId, Long skillId);
}
