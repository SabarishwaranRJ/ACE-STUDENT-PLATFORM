import sys
import os

ai_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "ai_service")
sys.path.insert(0, ai_dir)

if __name__ == "__main__":
    import uvicorn
    import main
    uvicorn.run(main.app, host="0.0.0.0", port=8000, log_level="info")

