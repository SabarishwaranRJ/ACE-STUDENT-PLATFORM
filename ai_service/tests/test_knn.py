import numpy as np
from services.knn_service import KNNService

def test_knn_candidate_retrieval():
    knn = KNNService()
    req_emb = np.array([1.0, 0.0, 0.0], dtype=np.float32)

    candidates = [
        (1, np.array([1.0, 0.0, 0.0], dtype=np.float32)), # Requester ID 1
        (2, np.array([0.9, 0.1, 0.0], dtype=np.float32)), # Very close
        (3, np.array([0.0, 1.0, 0.0], dtype=np.float32)), # Far
        (4, np.array([0.8, 0.2, 0.0], dtype=np.float32)), # Moderately close
    ]

    results = knn.get_top_k_candidates(req_emb, candidates, top_k=2, requester_id=1)

    assert len(results) == 2
    # Candidate 1 must be excluded
    result_ids = [r["studentId"] for r in results]
    assert 1 not in result_ids
    # Top candidate should be 2
    assert result_ids[0] == 2
