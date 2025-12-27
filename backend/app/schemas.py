from enum import Enum
from typing import List, Optional
from datetime import date

from pydantic import BaseModel, Field


# --------- User grocery list --------- #

class GroceryItem(BaseModel):
    name: str = Field(..., example="eggs")
    quantity: float = Field(..., gt=0, example=1)
    unit: str = Field(..., example="pieces")
    image: str = Field(None, example="https://example.com/images/eggs.png")


class GroceryListRequest(BaseModel):
    items: List[GroceryItem]


# --------- Optimization / result models --------- #

class OptimizationMode(str, Enum):
    SINGLE_STORE = "single_store"
    MULTI_STORE = "multi_store"


class ItemAssignment(BaseModel):
    product_name: str
    store_name: str
    price: float
    offer_id: int
    quantity: float
    unit: str
    valid_from: date
    valid_until: date
    image: Optional[str] = Field(None, example="https://example.com/images/eggs.png")
    package_count: int = Field(1, description="Number of offer packages required to fulfill the requested quantity")
    package_size: Optional[float] = Field(None, description="Size/quantity of a single offer package (as stored in DB)")
    package_unit: Optional[str] = Field(None, description="Unit of the offer package (e.g., 'g', 'piece')")


class OptimizationResponse(BaseModel):
    mode: OptimizationMode
    total_price: float
    stores: List[str]
    items: List[ItemAssignment]
