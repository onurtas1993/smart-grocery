#!/usr/bin/env python3
"""
Drop and recreate tables in the configured DATABASE_URL (will use test.db),
then load the provided JSON dataset into the DB.

WARNING: This script drops tables; ensure DATABASE_URL points to a test DB.
"""
import os
import sys
from pathlib import Path

# Force using test.db to avoid touching production
os.environ["DATABASE_URL"] = "sqlite:///./test.db"

from app.db import engine, Base, SessionLocal
from scripts.load_dataset import load

if __name__ == "__main__":
    print("Resetting database at:", os.environ["DATABASE_URL"])
    # Drop and recreate all tables
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)

    data_path = Path(__file__).resolve().parents[0] / ".." / "data" / "sample_offers.json"
    data_path = data_path.resolve()
    if not data_path.exists():
        print("Data file not found:", data_path)
        sys.exit(1)

    load(data_path)
    print("Done.")
