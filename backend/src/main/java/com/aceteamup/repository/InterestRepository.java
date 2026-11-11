package com.aceteamup.repository;

import com.aceteamup.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByInterestName(String interestName);
    boolean existsByInterestName(String interestName);
}
