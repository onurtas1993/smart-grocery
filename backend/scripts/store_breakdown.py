import os
from datetime import date
from app.db import SessionLocal, engine, Base
from app.models import StoreOffer
import math

# Use test.db
os.environ['DATABASE_URL'] = 'sqlite:///./test.db'

# Basket
requested = [
    {"name": "Cheese", "quantity": 250, "unit": "g"},
    {"name": "Eggs", "quantity": 6, "unit": "pcs"},
    {"name": "Yogurt", "quantity": 150, "unit": "g"}
]

# Helpers copied from optimizer
def _normalize_unit(unit: str) -> str:
    if not unit:
        return unit
    u = unit.strip().lower()
    if u in ("g", "gram", "grams", "gramm", "gramm."):
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
    if u == "piece":
        return qty, "piece"
    return qty, u


def compute():
    db = SessionLocal()
    today = date.today()
    names = [i['name'] for i in requested]
    req_qty = {i['name']: float(i['quantity']) for i in requested}
    req_unit = {i['name']: i['unit'] for i in requested}

    offers = (
        db.query(StoreOffer)
        .filter(StoreOffer.product_name.in_(names), StoreOffer.valid_until >= today)
        .all()
    )

    # build per-store map: choose cheapest offer per product in that store by unit price
    store_map = {}
    for off in offers:
        st = off.store_name
        prod = off.product_name
        store_map.setdefault(st, {})
        existing = store_map[st].get(prod)
        # compute per-base-unit price
        try:
            off_qty_base, off_base_unit = _to_base_qty(float(off.quantity), off.unit)
            off_unit_price = float(off.price) / off_qty_base if off_qty_base > 0 else float('inf')
        except Exception:
            off_unit_price = float(off.price) / float(off.quantity) if float(off.quantity) else float('inf')
        if existing is None:
            store_map[st][prod] = off
        else:
            try:
                ex_qty_base, ex_base_unit = _to_base_qty(float(existing.quantity), existing.unit)
                ex_unit_price = float(existing.price) / ex_qty_base if ex_qty_base > 0 else float('inf')
            except Exception:
                ex_unit_price = float(existing.price) / float(existing.quantity) if float(existing.quantity) else float('inf')
            if off_unit_price < ex_unit_price:
                store_map[st][prod] = off

    # compute totals per store where all products present
    results = []
    for st, prods in store_map.items():
        if not all(name in prods for name in names):
            continue
        total = 0.0
        details = []
        for name in names:
            offer = prods[name]
            price_f = float(offer.price)
            qty_req = req_qty[name]
            unit_req = req_unit[name]
            req_qty_base, req_base_unit = _to_base_qty(qty_req, unit_req)
            offer_qty_base, offer_base_unit = _to_base_qty(float(offer.quantity), offer.unit)

            if req_base_unit == offer_base_unit and offer_qty_base > 0:
                packages = math.ceil(req_qty_base / offer_qty_base)
                cost = price_f * packages
            else:
                try:
                    if (unit_req or '').strip().lower() == (offer.unit or '').strip().lower() and float(offer.quantity) > 0:
                        packages = math.ceil(qty_req / float(offer.quantity))
                        cost = price_f * packages
                    else:
                        cost = price_f * qty_req
                        packages = 1
                except Exception:
                    cost = price_f * qty_req
                    packages = 1
            total += cost
            details.append((name, offer.store_name, float(offer.quantity), offer.unit, price_f, packages, cost))
        results.append((st, total, details))

    # sort and print
    results.sort(key=lambda x: x[1])
    for st, total, details in results:
        print(f"Store: {st} -> total: {total:.2f}")
        for d in details:
            print(f"  {d[0]}: offer {d[2]}{d[3]} @ {d[4]} x {d[5]} => {d[6]:.2f}")
        print('')

    if not results:
        print('No single store contains all requested products')

if __name__ == '__main__':
    compute()
