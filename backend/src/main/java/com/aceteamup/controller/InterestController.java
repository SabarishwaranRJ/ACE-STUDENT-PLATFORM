package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.interest.InterestRequest;
import com.aceteamup.dto.interest.InterestResponse;
import com.aceteamup.service.InterestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestService interestService;

    public InterestController(InterestService interestService) {
        this.interestService = interestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterestResponse>>> getAllInterests() {
        return ResponseEntity.ok(ApiResponse.success(interestService.getAllInterests()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InterestResponse>> createInterest(@Valid @RequestBody InterestRequest req) {
        InterestResponse res = interestService.createInterest(req);
        return ResponseEntity.ok(ApiResponse.success("Interest created", res));
    }
}
