-- Create database (optional — only run if DB does not already exist)
-- CREATE DATABASE smart_grocery_optimizer;
-- \c smart_grocery_optimizer;


-- ============================================
--  TABLE: store_offers
-- ============================================
-- Stores all promotional offers extracted from prospekts.
-- Used read-only by the optimizer endpoint.
--
-- Fields:
--   store_name    : Name of the store ("ALDI", "REWE", "LIDL", ...)
--   product_name  : Product name ("eggs", "milk", "cheese", ...)
--   quantity      : The pack size (12, 400, 1, ...)
--   unit          : The unit ("pieces", "grams", "liter")
--   price         : Price in EUR
--   valid_from    : Optional start date of validity
--   valid_until   : Required expiry date
-- ============================================

CREATE TABLE IF NOT EXISTS store_offers (
    id SERIAL PRIMARY KEY,
    store_name   TEXT NOT NULL,
    product_name TEXT NOT NULL,
    quantity     NUMERIC NOT NULL,
    unit         TEXT NOT NULL,
    price        NUMERIC NOT NULL,
    valid_from   DATE,
    valid_until  DATE NOT NULL
);
