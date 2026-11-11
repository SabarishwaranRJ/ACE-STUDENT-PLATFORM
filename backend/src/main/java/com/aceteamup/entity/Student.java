package com.aceteamup.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for the {@code students} table.
 */
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    public enum AcademicYear {
        FIRST("1st"), SECOND("2nd"), THIRD("3rd"), FOURTH("4th"), ALUMNI("Alumni");

        private final String dbValue;

        AcademicYear(String dbValue) {
            this.dbValue = dbValue;
        }

        @JsonValue
        public String getDbValue() {
            return dbValue;
        }

        @JsonCreator
        public static AcademicYear fromValue(String value) {
            if (value == null) return null;
            for (AcademicYear ay : values()) {
                if (ay.dbValue.equalsIgnoreCase(value) || ay.name().equalsIgnoreCase(value)) {
                    return ay;
                }
            }
            return null;
        }
    }

    public enum ExperienceLevel {
        Beginner, Intermediate, Advanced
    }

    public enum AvailabilityType {
        FULL_TIME("Full-time"), PART_TIME("Part-time"), WEEKENDS("Weekends");

        private final String dbValue;

        AvailabilityType(String dbValue) {
            this.dbValue = dbValue;
        }

        @JsonValue
        public String getDbValue() {
            return dbValue;
        }

        @JsonCreator
        public static AvailabilityType fromValue(String value) {
            if (value == null) return null;
            for (AvailabilityType at : values()) {
                if (at.dbValue.equalsIgnoreCase(value) || at.name().equalsIgnoreCase(value)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum ProfileVisibility {
        Public("Public"), Private("Private"), College_only("College-only");

        private final String dbValue;

        ProfileVisibility(String dbValue) {
            this.dbValue = dbValue;
        }

        @JsonValue
        public String getDbValue() {
            return dbValue;
        }

        @JsonCreator
        public static ProfileVisibility fromValue(String value) {
            if (value == null) return null;
            for (ProfileVisibility pv : values()) {
                if (pv.dbValue.equalsIgnoreCase(value) || pv.name().equalsIgnoreCase(value) || pv.name().replace('_', '-').equalsIgnoreCase(value)) {
                    return pv;
                }
            }
            return null;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false, updatable = false)
    private Long studentId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", referencedColumnName = "college_id")
    private College college;

    @Column(name = "department", length = 255)
    private String department;

    @Convert(converter = AcademicYearConverter.class)
    @Column(name = "academic_year", length = 10)
    private AcademicYear academicYear;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "preferred_role", length = 100)
    private String preferredRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 20)
    private ExperienceLevel experienceLevel;

    @Convert(converter = AvailabilityTypeConverter.class)
    @Column(name = "availability", length = 20)
    private AvailabilityType availability;

    @Column(name = "profile_visibility", nullable = false, length = 20)
    @Builder.Default
    private ProfileVisibility profileVisibility = ProfileVisibility.Public;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Converters
    @Converter(autoApply = true)
    public static class AcademicYearConverter implements AttributeConverter<AcademicYear, String> {
        @Override
        public String convertToDatabaseColumn(AcademicYear attribute) {
            return attribute != null ? attribute.getDbValue() : null;
        }

        @Override
        public AcademicYear convertToEntityAttribute(String dbData) {
            return AcademicYear.fromValue(dbData);
        }
    }

    @Converter(autoApply = true)
    public static class AvailabilityTypeConverter implements AttributeConverter<AvailabilityType, String> {
        @Override
        public String convertToDatabaseColumn(AvailabilityType attribute) {
            return attribute != null ? attribute.getDbValue() : null;
        }

        @Override
        public AvailabilityType convertToEntityAttribute(String dbData) {
            return AvailabilityType.fromValue(dbData);
        }
    }

    @Converter(autoApply = true)
    public static class ProfileVisibilityConverter implements AttributeConverter<ProfileVisibility, String> {
        @Override
        public String convertToDatabaseColumn(ProfileVisibility attribute) {
            return attribute != null ? attribute.getDbValue() : null;
        }

        @Override
        public ProfileVisibility convertToEntityAttribute(String dbData) {
            return ProfileVisibility.fromValue(dbData);
        }
    }
}
