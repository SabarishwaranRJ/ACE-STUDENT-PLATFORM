import numpy as np
from sklearn.neighbors import NearestNeighbors
from typing import List, Tuple, Dict, Any

class KNNService:
    def get_top_k_candidates(
        self,
        requester_embedding: np.ndarray,
        candidate_embeddings: List[Tuple[int, np.ndarray]],
        top_k: int,
        requester_id: int
    ) -> List[Dict[str, Any]]:
        if not candidate_embeddings:
            return []

        ids = [c[0] for c in candidate_embeddings]
        embs = np.array([c[1] for c in candidate_embeddings])

        # Exclude requester
        valid_indices = [i for i, sid in enumerate(ids) if sid != requester_id]
        if not valid_indices:
            return []

        embs_filtered = embs[valid_indices]
        ids_filtered = [ids[i] for i in valid_indices]

        actual_k = min(top_k, len(ids_filtered))

        nbrs = NearestNeighbors(
            n_neighbors=actual_k,
            metric="cosine",
            algorithm="brute"
        )
        nbrs.fit(embs_filtered)
        distances, indices = nbrs.kneighbors(requester_embedding.reshape(1, -1))

        results = []
        for dist, idx in zip(distances[0], indices[0]):
            sim = max(0.0, 1.0 - float(dist))
            results.append({
                "studentId": ids_filtered[idx],
                "knnScore": sim
            })

        return results
