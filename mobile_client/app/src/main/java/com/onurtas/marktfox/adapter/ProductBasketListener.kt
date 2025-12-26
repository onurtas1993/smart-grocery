package com.onurtas.marktfox.adapter

import com.onurtas.marktfox.model.Product

interface ProductBasketListener {
    fun onProductAdded(product: Product)
    fun onProductRemoved(product: Product)
}
