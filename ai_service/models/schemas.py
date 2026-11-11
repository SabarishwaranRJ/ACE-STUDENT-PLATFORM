from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field

class SkillProfile(BaseModel):
    skillName: str
    proficiencyLevel: str = "Beginner"
    yearsOfExperience: Optional[float] = None

class AvailabilitySlot(BaseModel):
    dayOfWeek: str
    startTime: str
    endTime: str

class StudentProfile(BaseModel):
    studentId: int
    name: str
    bio: Optional[str] = None
    preferredRole: Optional[str] = None
    experienceLevel: Optional[str] = "Intermediate"
    availability: Optional[str] = "Full-time"
    skills: List[SkillProfile] = []
    interests: List[str] = []
    availabilitySlots: List[AvailabilitySlot] = []
    currentTeamEventIds: List[int] = []

class TeamProfile(BaseModel):
    teamId: int
    eventId: int
    existingMemberIds: List[int] = []
    existingSkills: List[str] = []
    requiredSkills: List[str] = []
    maxSize: int = 5
    currentSize: int = 0

# Request Models
class MatchStudentsRequest(BaseModel):
    requestingStudent: StudentProfile
    candidateStudents: List[StudentProfile] = []
    eventDescription: Optional[str] = None
    requiredSkills: List[str] = []
    topK: int = 10

class OptimizeTeamRequest(BaseModel):
    candidateStudents: List[StudentProfile] = []
    requiredSkills: List[str] = []
    minTeamSize: int = 2
    maxTeamSize: int = 5
    eventDescription: Optional[str] = None

class SkillGapRequest(BaseModel):
    team: TeamProfile
    requiredEventSkills: List[str] = []
    candidateStudents: List[StudentProfile] = []

class FeedbackRequest(BaseModel):
    studentId: int
    recommendedStudentId: int
    eventId: int
    feedbackType: str

# Response Models
class RecommendationResult(BaseModel):
    recommendedStudentId: int
    name: str
    compatibilityScore: float
    explanation: str
    skillMatches: List[str] = []
    complementarySkills: List[str] = []
    missingSkills: List[str] = []
    scoreBreakdown: Dict[str, float] = {}

class MatchStudentsResponse(BaseModel):
    recommendations: List[RecommendationResult]
    totalCandidatesEvaluated: int
    processingTimeMs: float

class OptimizeTeamResponse(BaseModel):
    selectedStudentIds: List[int]
    selectedStudents: List[Dict[str, Any]]
    feasible: bool
    explanation: str
    objectiveValue: float
    skillCoverage: float
    processingTimeMs: float

class SkillGapResponse(BaseModel):
    presentSkills: List[str]
    missingSkills: List[str]
    underrepresentedSkills: List[str]
    coveragePercent: float
    recommendedProfiles: List[str]
    candidateMatches: List[Dict[str, Any]]

class HealthResponse(BaseModel):
    status: str
    modelLoaded: bool
    version: str
    embeddingDimension: int
