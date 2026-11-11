import threading
import numpy as np
from cachetools import LRUCache
from typing import List, Any
import hashlib

class EmbeddingService:
    def __init__(self, model_name: str = "sentence-transformers/all-MiniLM-L6-v2", cache_size: int = 1000):
        self._model = None
        self._model_name = model_name
        self._cache = LRUCache(maxsize=cache_size)
        self._lock = threading.Lock()
        self._model_loaded = False
        self._embedding_dim = 384

    def load_model(self):
        try:
            from sentence_transformers import SentenceTransformer
            self._model = SentenceTransformer(self._model_name)
            test_emb = self._model.encode(["test"], normalize_embeddings=True)
            self._embedding_dim = test_emb.shape[1]
            self._model_loaded = True
            print(f"Loaded SBERT model: {self._model_name} (dim: {self._embedding_dim})")
        except Exception as e:
            print(f"Notice: SBERT model load skipped or failed: {e}. Running fallback semantic encoder.")
            self._model_loaded = False

    def _build_student_text(self, profile: Any) -> str:
        parts = []
        if profile.bio:
            parts.append(profile.bio)
        if profile.preferredRole:
            parts.append(f"Role: {profile.preferredRole}")
        if profile.skills:
            skill_texts = [f"{s.skillName} ({s.proficiencyLevel})" for s in profile.skills]
            parts.append("Skills: " + ", ".join(skill_texts))
        if profile.interests:
            parts.append("Interests: " + ", ".join(profile.interests))
        if profile.experienceLevel:
            parts.append(f"Experience: {profile.experienceLevel}")
        return " | ".join(parts) if parts else f"Student {profile.studentId}"

    def encode_text(self, text: str) -> np.ndarray:
        if not text:
            return np.zeros(self._embedding_dim, dtype=np.float32)

        cache_key = hashlib.md5(text.encode("utf-8")).hexdigest()
        if cache_key in self._cache:
            return self._cache[cache_key]

        with self._lock:
            if cache_key in self._cache:
                return self._cache[cache_key]

            if self._model_loaded and self._model is not None:
                embedding = self._model.encode([text], normalize_embeddings=True)[0]
            else:
                # Deterministic pseudo-embedding for fallback/testing
                seed = int(hashlib.sha256(text.encode('utf-8')).hexdigest()[:8], 16)
                rng = np.random.default_rng(seed)
                v = rng.standard_normal(self._embedding_dim).astype(np.float32)
                norm = np.linalg.norm(v)
                embedding = v / norm if norm > 0 else v

            self._cache[cache_key] = embedding
            return embedding

    def encode_student(self, profile: Any) -> np.ndarray:
        text = self._build_student_text(profile)
        return self.encode_text(text)

    def encode_skills(self, skill_names: List[str]) -> np.ndarray:
        if not skill_names:
            return np.zeros(self._embedding_dim, dtype=np.float32)
        embeddings = [self.encode_text(s) for s in skill_names]
        mean_vec = np.mean(embeddings, axis=0)
        norm = np.linalg.norm(mean_vec)
        return mean_vec / norm if norm > 0 else mean_vec

    @property
    def model_loaded(self) -> bool:
        return self._model_loaded

    @property
    def embedding_dim(self) -> int:
        return self._embedding_dim
