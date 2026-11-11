import urllib.request
import json

BASE = "http://127.0.0.1:8080/api"

def run_test():
    print("--- 1. Testing Login ---")
    data = json.dumps({"email": "aarav.sharma@example.edu", "password": "password123"}).encode()
    req = urllib.request.Request(f"{BASE}/auth/login", data=data, headers={"Content-Type": "application/json"})
    resp = json.loads(urllib.request.urlopen(req).read().decode())
    assert resp["success"], "Login failed"
    token = resp["data"]["token"]
    student_id = resp["data"]["studentId"]
    print(f"Logged in successfully as {resp['data']['name']} (ID: {student_id})")

    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

    print("\n--- 2. Testing AI Health Endpoint ---")
    req_health = urllib.request.Request(f"{BASE}/recommendations/health", headers=headers)
    health = json.loads(urllib.request.urlopen(req_health).read().decode())
    print("AI Health via Backend:", health["data"])

    print("\n--- 3. Testing AI Teammate Recommendations for Event 1 ---")
    req_rec = urllib.request.Request(f"{BASE}/recommendations/event/1", headers=headers)
    recs = json.loads(urllib.request.urlopen(req_rec).read().decode())
    print(f"Received {len(recs['data'])} recommendations:")
    for r in recs["data"][:5]:
        print(f"  * {r['name']} - Score: {r['compatibilityScore']}%")
        print(f"    Explanation: {r['explanation']}")
        print(f"    Complementary skills: {r.get('complementarySkills')}")
        print(f"    Skill matches: {r.get('skillMatches')}")

    print("\n--- 4. Testing Events Discovery ---")
    req_events = urllib.request.Request(f"{BASE}/events", headers=headers)
    events = json.loads(urllib.request.urlopen(req_events).read().decode())
    print(f"Found {len(events['data'])} events:")
    for e in events["data"]:
        print(f"  * [{e['eventId']}] {e['eventName']} ({e['eventType']}, team size: {e['requiredTeamSize']})")

    print("\n--- 5. Testing Team Optimization Service ---")
    opt_payload = json.dumps({
        "eventId": 1,
        "teamSize": 3,
        "candidateIds": [2, 3, 4, 5, 6, 7, 8],
        "requiredSkills": ["Python", "React", "Docker"],
        "strategy": "BALANCED"
    }).encode()
    req_opt = urllib.request.Request(f"{BASE}/optimize/team", data=opt_payload, headers=headers)
    try:
        opt_res = json.loads(urllib.request.urlopen(req_opt).read().decode())
        print("Team Optimization response:", opt_res)
    except Exception as ex:
        print("Optimization check:", ex)

    print("\nALL BACKEND + AI TESTS COMPLETED!")

if __name__ == "__main__":
    run_test()
