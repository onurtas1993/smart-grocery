import os
from datetime import date, timedelta

# Use in-memory sqlite for tests
os.environ["DATABASE_URL"] = "sqlite:///:memory:"

from fastapi.testclient import TestClient
from app.db import SessionLocal, engine, Base
from app.main import app
from app.models import StoreOffer

client = TestClient(app)


def setup_module(module):
    Base.metadata.create_all(bind=engine)


def teardown_module(module):
    Base.metadata.drop_all(bind=engine)


def test_optimize_packages_for_weight():
    db = SessionLocal()
    today = date.today()
    offer = StoreOffer(
        store_name="TestMart",
        product_name="Kartoffeln",
        quantity=2000,
        unit="g",
        price=5.0,
        valid_from=today - timedelta(days=1),
        valid_until=today + timedelta(days=10),
    )
    db.add(offer)
    db.commit()
    db.refresh(offer)
    db.close()

    payload = {
        "items": [
            {"name": "Kartoffeln", "quantity": 6000, "unit": "g"}
        ]
    }

    resp = client.post("/optimize", json=payload)
    assert resp.status_code == 200, resp.text
    data = resp.json()

    # total price should be 3 * 5.0 = 15.0
    assert float(data["total_price"]) == 15.0

    # Response items should be list of AssignedProduct objects
    assert len(data["items"]) == 1
    assigned = data["items"][0]
    product = assigned["product"]

    # product quantity should be the offer package size (2000) and required_packages 3
    assert float(product["quantity"]) == 2000.0
    assert int(assigned["required_packages"]) == 3
    assert product["unit"] == "g"
