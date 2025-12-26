from sqlalchemy import Column, Integer, Text, Numeric, Date

from app.db import Base


class StoreOffer(Base):
    __tablename__ = "store_offers"

    id = Column(Integer, primary_key=True, index=True)
    store_name = Column(Text, nullable=False)      # e.g. 'ALDI'
    product_name = Column(Text, nullable=False)    # e.g. 'eggs'
    quantity = Column(Numeric, nullable=False)     # e.g. 12
    unit = Column(Text, nullable=False)            # e.g. 'pieces'
    price = Column(Numeric, nullable=False)        # e.g. 1.0 (EUR)
    valid_from = Column(Date, nullable=False)       # offer start date (can be NULL)
    valid_until = Column(Date, nullable=False)     # offer expiry date
    image = Column(Text, nullable=True)            # optional URL to product image
