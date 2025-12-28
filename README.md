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


  <img src="https://github-production-user-asset-6210df.s3.amazonaws.com/162033294/530534419-d0cba77f-f769-4337-8512-83585ec5be3c.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20251228%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251228T092746Z&X-Amz-Expires=300&X-Amz-Signature=647db9759a25008608ce144083d68405f147dda243a4e7e0a947b4cebcf7aff2&X-Amz-SignedHeaders=host" />
  

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