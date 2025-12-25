package com.onurtas.marktfox.network

import com.onurtas.marktfox.model.Product
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
}
