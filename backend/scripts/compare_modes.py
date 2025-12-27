import os
from fastapi.testclient import TestClient

os.environ['DATABASE_URL'] = 'sqlite:///./test.db'
from app.main import app

client = TestClient(app)

payload = {
    "items": [
        {"name": "Cheese", "quantity": 250, "unit": "g"},
        {"name": "Eggs", "quantity": 6, "unit": "pcs"},
        {"name": "Yogurt", "quantity": 150, "unit": "g"}
    ]
}

for mode in ("single_store", "multi_store"):
    resp = client.post(f"/optimize?mode={mode}", json=payload)
    print("MODE:", mode)
    print("status", resp.status_code)
    print(resp.json())
    print("---")
