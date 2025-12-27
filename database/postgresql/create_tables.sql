-- Create database (optional — only run if DB does not already exist)
-- CREATE DATABASE smart_grocery_optimizer;
-- \c smart_grocery_optimizer;


-- Table: public.store_offers

-- DROP TABLE IF EXISTS public.store_offers;

CREATE TABLE IF NOT EXISTS public.store_offers
(
    id integer NOT NULL DEFAULT nextval('store_offers_id_seq'::regclass),
    store_name text COLLATE pg_catalog."default" NOT NULL,
    product_name text COLLATE pg_catalog."default" NOT NULL,
    quantity numeric NOT NULL,
    unit text COLLATE pg_catalog."default" NOT NULL,
    price numeric NOT NULL,
    valid_from date NOT NULL,
    valid_until date NOT NULL,
    image text COLLATE pg_catalog."default",
    CONSTRAINT store_offers_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.store_offers
    OWNER to postgres;
-- Index: ix_store_offers_id

-- DROP INDEX IF EXISTS public.ix_store_offers_id;

CREATE INDEX IF NOT EXISTS ix_store_offers_id
    ON public.store_offers USING btree
    (id ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;