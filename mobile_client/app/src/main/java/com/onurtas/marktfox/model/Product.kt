package com.onurtas.marktfox.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("offer_id")
    val id: Int,

    @SerializedName("product_name")
    val title: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("store_name")
    val store: String,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("unit")
    val unit: String
)
