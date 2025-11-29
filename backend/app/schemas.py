from enum import Enum
from typing import List

from pydantic import BaseModel, Field


# --------- User grocery list --------- #

class GroceryItem(BaseModel):
    name: str = Field(..., example="eggs")
    quantity: float = Field(..., gt=0, example=1)
    unit: str = Field(..., example="pieces")


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


class OptimizationResponse(BaseModel):
    mode: OptimizationMode
    total_price: float
    stores: List[str]
    items: List[ItemAssignment]
