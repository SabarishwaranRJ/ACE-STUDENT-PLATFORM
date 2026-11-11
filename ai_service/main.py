import sys
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from config import settings
from services.embedding_service import EmbeddingService
from services.similarity_service import SimilarityService
from services.knn_service import KNNService
from services.scoring_service import ScoringService
from services.optimization_service import TeamOptimizationService
from services.skill_gap_service import SkillGapService
from routers import ai_router

embedding_service = EmbeddingService(settings.sbert_model, settings.embedding_cache_size)
similarity_service = SimilarityService()
knn_service = KNNService()
scoring_service = ScoringService(settings, similarity_service, embedding_service)
optimization_service = TeamOptimizationService(settings, scoring_service, embedding_service)
skill_gap_service = SkillGapService(scoring_service, embedding_service)

# Wire services into router
ai_router.embedding_service = embedding_service
ai_router.similarity_service = similarity_service
ai_router.knn_service = knn_service
ai_router.scoring_service = scoring_service
ai_router.optimization_service = optimization_service
ai_router.skill_gap_service = skill_gap_service

@asynccontextmanager
async def lifespan(app: FastAPI):
    embedding_service.load_model()
    yield

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(ai_router.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
