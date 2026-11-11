from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "ACE TeamUp AI Service"
    app_version: str = "1.0.0"
    sbert_model: str = "sentence-transformers/all-MiniLM-L6-v2"
    embedding_cache_size: int = 1000
    default_top_k: int = 10

    # Scoring weights (must sum to 1.0)
    weight_skill: float = 0.35
    weight_interest: float = 0.25
    weight_experience: float = 0.15
    weight_availability: float = 0.15
    weight_collaboration: float = 0.10

    # ILP solver time limit in seconds
    ilp_time_limit_seconds: int = 30

    class Config:
        env_file = ".env"


settings = Settings()
