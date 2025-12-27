package com.onurtas.marktfox.model

data class BasketItem(
    val product: Product,
    val quantity: Int
)

data class Basket(
    val items: List<BasketItem>)
