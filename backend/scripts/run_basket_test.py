import os
from fastapi.testclient import TestClient

# Use test.db
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

resp = client.post('/optimize', json=payload)
print('status', resp.status_code)
print(resp.json())
