package com.aceteamup.repository;

import com.aceteamup.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link College}.
 */
@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByCollegeName(String collegeName);

    boolean existsByCollegeName(String collegeName);
}

