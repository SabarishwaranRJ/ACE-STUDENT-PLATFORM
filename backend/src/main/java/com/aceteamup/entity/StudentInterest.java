package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "student_interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentInterest {

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInterestId implements Serializable {
        @Column(name = "student_id", nullable = false)
        private Long studentId;

        @Column(name = "interest_id", nullable = false)
        private Long interestId;
    }

    @EmbeddedId
    private StudentInterestId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("interestId")
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;
}
