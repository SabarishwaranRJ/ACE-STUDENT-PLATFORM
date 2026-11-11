package com.aceteamup.repository;

import com.aceteamup.entity.StudentInterest;
import com.aceteamup.entity.StudentInterest.StudentInterestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentInterestRepository extends JpaRepository<StudentInterest, StudentInterestId> {
    List<StudentInterest> findByStudentStudentId(Long studentId);
    void deleteByStudentStudentIdAndInterestInterestId(Long studentId, Long interestId);
}
