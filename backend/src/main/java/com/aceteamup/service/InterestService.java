package com.aceteamup.service;

import com.aceteamup.dto.interest.InterestRequest;
import com.aceteamup.dto.interest.InterestResponse;
import com.aceteamup.entity.Interest;
import com.aceteamup.exception.DuplicateResourceException;
import com.aceteamup.repository.InterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterestService {

    private final InterestRepository interestRepository;

    public InterestService(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    public List<InterestResponse> getAllInterests() {
        return interestRepository.findAll().stream()
                .map(i -> InterestResponse.builder()
                        .interestId(i.getInterestId())
                        .interestName(i.getInterestName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public InterestResponse createInterest(InterestRequest req) {
        if (interestRepository.existsByInterestName(req.getInterestName())) {
            throw new DuplicateResourceException("Interest already exists: " + req.getInterestName());
        }

        Interest interest = Interest.builder()
                .interestName(req.getInterestName())
                .build();

        Interest saved = interestRepository.save(interest);
        return InterestResponse.builder()
                .interestId(saved.getInterestId())
                .interestName(saved.getInterestName())
                .build();
    }
}
