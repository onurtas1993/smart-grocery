from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base

# TODO: replace user, password, host, port as needed
DATABASE_URL = "postgresql+psycopg2://postgres:password@localhost:5432/smart_grocery_optimizer"

engine = create_engine(DATABASE_URL)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


def get_db():
    """Yield a database session for FastAPI dependencies."""
    from sqlalchemy.orm import Session

    db: Session = SessionLocal()
    try:
        yield db
    finally:
        db.close()
