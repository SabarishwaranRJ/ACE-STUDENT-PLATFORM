from enum import Enum

class FeedbackType(str, Enum):
    Like = "Like"
    Dislike = "Dislike"
    Skip = "Skip"

class ProficiencyLevel(str, Enum):
    Beginner = "Beginner"
    Intermediate = "Intermediate"
    Advanced = "Advanced"
    Expert = "Expert"

class AvailabilityType(str, Enum):
    FULL_TIME = "Full-time"
    PART_TIME = "Part-time"
    WEEKENDS = "Weekends"

class ExperienceLevel(str, Enum):
    Beginner = "Beginner"
    Intermediate = "Intermediate"
    Advanced = "Advanced"
