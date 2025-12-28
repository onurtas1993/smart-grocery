# NOT READY - IN DEVELOPMENT

<table>
  <tr>
    <td>

<img src="./app_icon.png" width="128"/>
    </td>
    <td>

# Markt Fox

a unified app and backend system that finds the best store combinations for a user’s grocery list by comparing prices, quantities, and offers across multiple supermarkets.
    </td>
  </tr>
</table>

---

## Components

This app consists of 4 components:

- Frontend: Android client
- Backend: RESTful webservice (running on AWS)
- Database: PostgreSQL (running on AWS)
- AI: Extracting texts from market flyers ( in development)

---

## Example Use Case and Screenshots

- **Check the cheapest total price**:  
  User makes a list as: eggs, cheese and milk. User then submits this list to the webservice and the result will be the list of the products with the cheapest offers and total cost for the desired items.

<img width="4980" height="2359" alt="Screenshot_20251228-101040-portrait-imageonline co-merged" src="https://github.com/user-attachments/assets/5e72cd4e-6e28-4743-8262-5f41c6f33ef1" />

---

# Smart Grocery — Backend

A FastAPI backend that optimizes grocery shopping across multiple stores. API docs can be read at:
http://api.marktfox.de/docs

Summary
- Accepts a grocery list and returns an optimized assignment of offers to minimize total cost.
- Supports two modes: `single_store` (all items from one store) and `multi_store` (cheapest offer per item).
- Handles package sizes and unit normalization (g/kg, ml/l, pieces) and computes package-aware total cost.

Key features
- FastAPI endpoints to search products and optimize grocery lists.
- Pydantic schemas and SQLAlchemy models for offers and responses.
- Distinct product search that returns the smallest package per product name.
- Scripts to load a JSON dataset into a local `sqlite` test DB and compare modes.

Quickstart (local)
1. Create and activate a Python venv (project root):
```bash
python3 -m venv .venv
source .venv/bin/activate
```
2. Install dependencies:
```bash
pip install -r app/requirements.txt
```
3. set db connection:
```bash
export DATABASE=xyz
```
4. Run the app locally:
```bash
uvicorn app.main:app --reload
```
---
