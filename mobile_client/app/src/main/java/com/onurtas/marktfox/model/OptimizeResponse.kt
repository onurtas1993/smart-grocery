package com.onurtas.marktfox.model

import com.google.gson.annotations.SerializedName

// This is the main response object from the /optimize endpoint.
// It will now contain a list of the new OptimizedItem class.
data class OptimizeResponse(
    @SerializedName("total_price")
    val totalPrice: Double,
    val items: List<OptimizedItem>
)

// This new data class perfectly matches the structure of each item in the response list.
// It contains a full, clean "Product" object and the "required_packages" count.
data class OptimizedItem(
    val product: Product,
    @SerializedName("required_packages")
    val requiredPackages: Int
)
