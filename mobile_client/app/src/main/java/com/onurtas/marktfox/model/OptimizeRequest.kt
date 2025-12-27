package com.onurtas.marktfox.model

data class OptimizeRequest(
    val items: List<ApiBasketItem>
)

// This data class matches the simple structure required by the API endpoint.
data class ApiBasketItem(
    val name: String,
    val quantity: Double,
    val unit: String
)
