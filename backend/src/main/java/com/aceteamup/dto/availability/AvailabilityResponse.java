package com.aceteamup.dto.availability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {
    private Long availabilityId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
