from fastapi import FastAPI

from app.db import Base, engine
from app import models  # noqa: F401  # Needed so Base.metadata.create_all loads models
from app.api import optimizer

# Dev-only: create tables from SQLAlchemy models.
# In production, use Alembic migrations instead.
Base.metadata.create_all(bind=engine)

app = FastAPI(title="SmartGroceryOptimizer API")

# Register routes
app.include_router(optimizer.router)
