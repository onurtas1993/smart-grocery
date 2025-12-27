import os
from datetime import date, timedelta

# Use a temporary file-based sqlite for tests to avoid separate in-memory connections
os.environ["DATABASE_URL"] = "sqlite:///./test.db"

from fastapi.testclient import TestClient
from app.db import SessionLocal, engine, Base
from app.main import app
from app.models import StoreOffer

client = TestClient(app)


def setup_module(module):
    Base.metadata.create_all(bind=engine)


def teardown_module(module):
    Base.metadata.drop_all(bind=engine)


def test_single_store_prefers_cheaper_store():
    db = SessionLocal()
    today = date.today()

    # ALDI offers (more expensive)
    db.add(StoreOffer(
        store_name="ALDI",
        product_name="Milk",
        quantity=1,
        unit="l",
        price=2.5,
        valid_from=today - timedelta(days=1),
        valid_until=today + timedelta(days=10),
    ))
    db.add(StoreOffer(
        store_name="ALDI",
        product_name="Bread",
        quantity=1,
        unit="piece",
        price=3.5,
        valid_from=today - timedelta(days=1),
        valid_until=today + timedelta(days=10),
    ))

    # LIDL offers (cheaper total)
    db.add(StoreOffer(
        store_name="LIDL",
        product_name="Milk",
        quantity=1,
        unit="l",
        price=1.8,
        valid_from=today - timedelta(days=1),
        valid_until=today + timedelta(days=10),
    ))
    db.add(StoreOffer(
        store_name="LIDL",
        product_name="Bread",
        quantity=1,
        unit="piece",
        price=2.0,
        valid_from=today - timedelta(days=1),
        valid_until=today + timedelta(days=10),
    ))

    db.commit()
    db.close()

    payload = {
        "items": [
            {"name": "Milk", "quantity": 1, "unit": "l"},
            {"name": "Bread", "quantity": 1, "unit": "piece"},
        ]
    }

    # Request single_store mode explicitly (use lowercase token expected by the API)
    resp = client.post("/optimize?mode=single_store", json=payload)
    assert resp.status_code == 200, resp.text
    data = resp.json()

    # Expect chosen store to be LIDL and total price equal to 1.8 + 2.0 = 3.8
    assert len(data["stores"]) == 1
    assert data["stores"][0] == "LIDL"
    assert abs(float(data["total_price"]) - 3.8) < 1e-6
