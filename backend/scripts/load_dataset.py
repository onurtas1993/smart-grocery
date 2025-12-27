#!/usr/bin/env python3
"""
Simple loader for a JSON dataset of store offers into the project's database.
Usage:
  python scripts/load_dataset.py data/offers.json

Expected JSON format: an array of objects with keys:
  store_name, product_name, quantity, unit, price, valid_from, valid_until, image (optional)
Dates should be YYYY-MM-DD. quantity and price can be numbers or strings.
"""
import sys
import json
from datetime import datetime
from pathlib import Path

from app.db import engine, SessionLocal, Base
from app.models import StoreOffer


def parse_date(s):
    if s is None:
        return None
    if isinstance(s, (datetime,)):
        return s.date()
    return datetime.strptime(str(s), "%Y-%m-%d").date()


def load(path: Path):
    if not path.exists():
        print(f"File not found: {path}")
        return

    with path.open("r", encoding="utf-8") as f:
        data = json.load(f)

    # ensure tables exist
    Base.metadata.create_all(bind=engine)

    db = SessionLocal()
    inserted = 0
    try:
        for obj in data:
            offer = StoreOffer(
                store_name=obj.get("store_name") or obj.get("store") or obj.get("storeName"),
                product_name=obj.get("product_name") or obj.get("product") or obj.get("productName"),
                quantity=obj.get("quantity"),
                unit=obj.get("unit"),
                price=obj.get("price"),
                valid_from=parse_date(obj.get("valid_from") or obj.get("validFrom")),
                valid_until=parse_date(obj.get("valid_until") or obj.get("validUntil")),
                image=obj.get("image"),
            )
            db.add(offer)
            inserted += 1
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()

    print(f"Inserted {inserted} offers into the database.")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python scripts/load_dataset.py path/to/offers.json")
        sys.exit(1)
    p = Path(sys.argv[1])
    load(p)
