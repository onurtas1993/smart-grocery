from datetime import date
from typing import Dict, List
import math

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
    # Map product name -> requested quantity and unit
    requested_qty: Dict[str, float] = {item.name: float(item.quantity) for item in payload.items}
    requested_unit: Dict[str, str] = {item.name: item.unit for item in payload.items}

    def _normalize_unit(unit: str) -> str:
        if not unit:
            return unit
        u = unit.strip().lower()
        if u in ("g", "gram", "grams", "gramm", "gramm.", "gramm(s)", "gramm(s)", "gramm"):
            return "g"
        if u in ("kg", "kilogram", "kilograms", "kg."):
            return "kg"
        if u in ("l", "liter", "litre", "liters", "litres", "l."):
            return "l"
        if u in ("ml", "milliliter", "millilitre", "milliliters", "millilitres", "ml."):
            return "ml"
        if u in ("piece", "pieces", "stk", "pcs", "stück"):
            return "piece"
        return u

    def _to_base_qty(qty: float, unit: str):
        u = _normalize_unit(unit)
        if u == "kg":
            return qty * 1000.0, "g"
        if u == "g":
            return qty, "g"
        if u == "l":
            return qty * 1000.0, "ml"
        if u == "ml":
            return qty, "ml"
        # pieces remain as-is
        if u == "piece":
            return qty, "piece"
        # unknown unit: return original and mark base as raw
        return qty, u

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

        # compute offer unit price in a base unit (e.g., grams or pieces) when possible
        try:
            offer_qty_base, offer_base_unit = _to_base_qty(float(offer.quantity), offer.unit)
            offer_unit_price = float(offer.price) / offer_qty_base if offer_qty_base > 0 else float("inf")
        except Exception:
            offer_qty_base, offer_base_unit, offer_unit_price = float(offer.quantity), offer.unit, float(offer.price) / float(offer.quantity) if float(offer.quantity) else float("inf")

        # per-store cheapest (compare by unit price when possible)
        store_entry = store_product_map.setdefault(store, {})
        existing_store_offer = store_entry.get(product)
        if existing_store_offer is None:
            store_entry[product] = offer
        else:
            try:
                ex_qty_base, ex_base_unit = _to_base_qty(float(existing_store_offer.quantity), existing_store_offer.unit)
                ex_unit_price = float(existing_store_offer.price) / ex_qty_base if ex_qty_base > 0 else float("inf")
            except Exception:
                ex_unit_price = float(existing_store_offer.price) / float(existing_store_offer.quantity) if float(existing_store_offer.quantity) else float("inf")

            if offer_unit_price < ex_unit_price:
                store_entry[product] = offer

        # global cheapest per product (compare by unit price)
        existing_global_offer = product_global_cheapest.get(product)
        if existing_global_offer is None:
            product_global_cheapest[product] = offer
        else:
            try:
                ex_qty_base, ex_base_unit = _to_base_qty(float(existing_global_offer.quantity), existing_global_offer.unit)
                ex_unit_price = float(existing_global_offer.price) / ex_qty_base if ex_qty_base > 0 else float("inf")
            except Exception:
                ex_unit_price = float(existing_global_offer.price) / float(existing_global_offer.quantity) if float(existing_global_offer.quantity) else float("inf")

            if offer_unit_price < ex_unit_price:
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
                qty_req = requested_qty.get(name, 1.0)
                req_unit = requested_unit.get(name)

                # convert requested and offer quantities to a common base when possible
                req_qty_base, req_base_unit = _to_base_qty(qty_req, req_unit)
                offer_qty_base, offer_base_unit = _to_base_qty(float(offer.quantity), offer.unit)

                if req_base_unit == offer_base_unit and offer_qty_base > 0:
                    packages = math.ceil(req_qty_base / offer_qty_base)
                    cost = price_f * packages
                else:
                    # fallback: if units strings match, use simple division, else assume price is per unit
                    try:
                        if (req_unit or "").strip().lower() == (offer.unit or "").strip().lower() and float(offer.quantity) > 0:
                            packages = math.ceil(qty_req / float(offer.quantity))
                            cost = price_f * packages
                        else:
                            cost = price_f * qty_req
                    except Exception:
                        cost = price_f * qty_req

                total += cost
                # If units align (same base unit or same normalized unit), return the
                # offer package size as the assignment quantity and set package_count
                # to the required number of packages. Otherwise return the requested
                # quantity and package_count=1.
                normalized_req_unit = _normalize_unit(req_unit or "")
                normalized_offer_unit = _normalize_unit(offer.unit or "")
                if req_base_unit == offer_base_unit or normalized_req_unit == normalized_offer_unit:
                            item = schemas.ItemAssignment(
                                product_name=name,
                                store_name=store,
                                price=price_f,
                                offer_id=offer.id,
                                quantity=float(offer.quantity),
                                unit=offer.unit,
                                valid_from=offer.valid_from,
                                valid_until=offer.valid_until,
                                image=offer.image,
                            )
                            assignments.append(schemas.AssignedProduct(product=item, required_packages=packages))
                else:
                    item = schemas.ItemAssignment(
                        product_name=name,
                        store_name=store,
                        price=price_f,
                        offer_id=offer.id,
                        quantity=qty_req,
                        unit=req_unit or offer.unit,
                        valid_from=offer.valid_from,
                        valid_until=offer.valid_until,
                        image=offer.image,
                    )
                    assignments.append(schemas.AssignedProduct(product=item, required_packages=1))

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
        qty_req = requested_qty.get(name, 1.0)
        req_unit = requested_unit.get(name)

        req_qty_base, req_base_unit = _to_base_qty(qty_req, req_unit)
        offer_qty_base, offer_base_unit = _to_base_qty(float(offer.quantity), offer.unit)

        if req_base_unit == offer_base_unit and offer_qty_base > 0:
            packages = math.ceil(req_qty_base / offer_qty_base)
            cost = price_f * packages
        else:
            try:
                if (req_unit or "").strip().lower() == (offer.unit or "").strip().lower() and float(offer.quantity) > 0:
                    packages = math.ceil(qty_req / float(offer.quantity))
                    cost = price_f * packages
                else:
                    cost = price_f * qty_req
            except Exception:
                cost = price_f * qty_req

        total += cost
        normalized_req_unit = _normalize_unit(req_unit or "")
        normalized_offer_unit = _normalize_unit(offer.unit or "")
        if req_base_unit == offer_base_unit or normalized_req_unit == normalized_offer_unit:
            item = schemas.ItemAssignment(
                product_name=name,
                store_name=offer.store_name,
                price=price_f,
                offer_id=offer.id,
                quantity=float(offer.quantity),
                unit=offer.unit,
                valid_from=offer.valid_from,
                valid_until=offer.valid_until,
                image=offer.image,
            )
            assignments.append(schemas.AssignedProduct(product=item, required_packages=packages))
        else:
            item = schemas.ItemAssignment(
                product_name=name,
                store_name=offer.store_name,
                price=price_f,
                offer_id=offer.id,
                quantity=qty_req,
                unit=req_unit or offer.unit,
                valid_from=offer.valid_from,
                valid_until=offer.valid_until,
                image=offer.image,
            )
            assignments.append(schemas.AssignedProduct(product=item, required_packages=1))
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
    distinct: bool = Query(True, description="Return only distinct rows (uses SQL DISTINCT when true)."),
    db: Session = Depends(get_db),
):
    """
    Search for products containing the given name in their product name.
    Optionally filter results by store name.
    """
    base_where = "WHERE LOWER(product_name) LIKE :pattern"
    params = {"pattern": f"%{name.lower()}%"}

    if distinct:
        # Return one row per product_name. Order by smallest quantity to
        # return the smallest package for each product name.
        sql = f"""
            SELECT DISTINCT ON (product_name)
                product_name, store_name, price, id, quantity, unit, valid_from, valid_until, image
            FROM store_offers
            {base_where}
            ORDER BY product_name, quantity ASC, id
        """
    else:
        sql = f"""
            SELECT product_name, store_name, price, id, quantity, unit, valid_from, valid_until, image
            FROM store_offers
            {base_where}
        """

    sql_query = text(sql)

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
            quantity=float(row["quantity"]),
            unit=row["unit"],
            valid_from=row["valid_from"],
            valid_until=row["valid_until"],
            image=row.get("image"),
        )
        for row in rows
    ]


@router.get("/stores", response_model=List[str])
def get_stores(db: Session = Depends(get_db)):
    """
    Get a list of all distinct store names.
    """
    sql_query = text("""
        SELECT DISTINCT store_name
        FROM store_offers
        ORDER BY store_name
    """)

    rows = db.execute(sql_query).fetchall()

    if not rows:
        raise HTTPException(status_code=404, detail="No stores found.")

    return [row[0] for row in rows]


@router.get("/products", response_model=List[schemas.ItemAssignment])
def get_all_products(db: Session = Depends(get_db)):
    """
    Get a list of all distinct products with their details.
    """
    sql_query = text("""
        SELECT DISTINCT ON (product_name)
            product_name, store_name, price, id, quantity, unit, valid_from, valid_until, image
        FROM store_offers
        ORDER BY product_name, quantity ASC, id
    """)

    rows = db.execute(sql_query).mappings().all()

    if not rows:
        raise HTTPException(status_code=404, detail="No products found.")

    return [
        schemas.ItemAssignment(
            product_name=row["product_name"],
            store_name=row["store_name"],
            price=float(row["price"]),
            offer_id=row["id"],
            quantity=float(row["quantity"]),
            unit=row["unit"],
            valid_from=row["valid_from"],
            valid_until=row["valid_until"],
            image=row.get("image"),
        )
        for row in rows
    ]
