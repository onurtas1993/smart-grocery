from datetime import date
from typing import Dict, List

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import and_, text
from sqlalchemy.orm import Session

from app import models, schemas
from app.db import get_db

router = APIRouter(tags=["optimizer"])


@router.post("/optimize", response_model=schemas.OptimizationResponse)
def optimize_grocery_list(
    payload: schemas.GroceryListRequest,
    mode: schemas.OptimizationMode = Query(
        schemas.OptimizationMode.SINGLE_STORE,
        description="single_store = one store with all items; "
                    "multi_store = cheapest combination across stores",
    ),
    db: Session = Depends(get_db),
):
    """
    mode = single_store:
        - pick the single store that can provide ALL requested products
        - minimize total price

    mode = multi_store:
        - for each product, pick the cheapest offer across ALL stores
        - different products can come from different stores
    """
    if not payload.items:
        raise HTTPException(
            status_code=400,
            detail="Grocery list must contain at least one item.",
        )

    today = date.today()
    requested_names = [item.name for item in payload.items]

    # Fetch all offers for requested products that are still valid
    offers = (
        db.query(models.StoreOffer)
        .filter(
            and_(
                models.StoreOffer.product_name.in_(requested_names),
                models.StoreOffer.valid_until >= today,
            )
        )
        .all()
    )

    if not offers:
        raise HTTPException(
            status_code=404,
            detail="No offers found for any requested products.",
        )

    # store -> product -> cheapest offer in that store
    store_product_map: Dict[str, Dict[str, models.StoreOffer]] = {}

    # product -> global cheapest offer (for multi_store)
    product_global_cheapest: Dict[str, models.StoreOffer] = {}

    for offer in offers:
        store = offer.store_name
        product = offer.product_name

        # per-store cheapest
        store_entry = store_product_map.setdefault(store, {})
        existing_store_offer = store_entry.get(product)
        if existing_store_offer is None or offer.price < existing_store_offer.price:
            store_entry[product] = offer

        # global cheapest per product
        existing_global_offer = product_global_cheapest.get(product)
        if existing_global_offer is None or offer.price < existing_global_offer.price:
            product_global_cheapest[product] = offer

    # ---------- SINGLE_STORE mode ---------- #
    if mode == schemas.OptimizationMode.SINGLE_STORE:
        best_store_name = None
        best_total_price = None
        best_assignments: List[schemas.ItemAssignment] = []

        for store, products_map in store_product_map.items():
            # Does this store have all requested products?
            if not all(name in products_map for name in requested_names):
                continue

            total = 0.0
            assignments: List[schemas.ItemAssignment] = []

            for name in requested_names:
                offer = products_map[name]
                price_f = float(offer.price)
                total += price_f
                assignments.append(
                    schemas.ItemAssignment(
                        product_name=name,
                        store_name=store,
                        price=price_f,
                        offer_id=offer.id,
                    )
                )

            if best_total_price is None or total < best_total_price:
                best_total_price = total
                best_store_name = store
                best_assignments = assignments

        if best_store_name is None:
            raise HTTPException(
                status_code=404,
                detail="No single store has all requested products.",
            )

        return schemas.OptimizationResponse(
            mode=mode,
            total_price=best_total_price,
            stores=[best_store_name],
            items=best_assignments,
        )

    # ---------- MULTI_STORE mode ---------- #
    assignments: List[schemas.ItemAssignment] = []
    used_stores: List[str] = []
    total = 0.0

    for name in requested_names:
        offer = product_global_cheapest.get(name)
        if offer is None:
            raise HTTPException(
                status_code=404,
                detail=f"No offers found for product '{name}'.",
            )

        price_f = float(offer.price)
        total += price_f
        assignments.append(
            schemas.ItemAssignment(
                product_name=name,
                store_name=offer.store_name,
                price=price_f,
                offer_id=offer.id,
            )
        )
        if offer.store_name not in used_stores:
            used_stores.append(offer.store_name)

    return schemas.OptimizationResponse(
        mode=mode,
        total_price=total,
        stores=used_stores,
        items=assignments,
    )


@router.get("/search/products", response_model=List[schemas.ItemAssignment])
def search_products(
    name: str = Query(..., description="Search term to find products containing the given name."),
    store: str = Query(None, description="Optional store name to filter results (e.g., ALDI, LIDL)."),
    db: Session = Depends(get_db),
):
    """
    Search for products containing the given name in their product name.
    Optionally filter results by store name.
    """
    sql_query = text("""
        SELECT DISTINCT product_name, store_name, price, id
        FROM store_offers
        WHERE LOWER(product_name) LIKE :pattern
    """)
    params = {"pattern": f"%{name.lower()}%"}

    if store:
        sql_query = text(f"{sql_query} AND LOWER(store_name) = :store")
        params["store"] = store.lower()

    # Use `db.execute()` with `mappings()` to return rows as dictionaries
    rows = db.execute(sql_query, params).mappings().all()

    if not rows:
        raise HTTPException(status_code=404, detail="No products found matching the query.")

    return [
        schemas.ItemAssignment(
            product_name=row["product_name"],
            store_name=row["store_name"],
            price=float(row["price"]),
            offer_id=row["id"],
        )
        for row in rows
    ]
