package com.onurtas.marktfox.repository

import com.onurtas.marktfox.model.Product
import com.onurtas.marktfox.network.RetrofitInstance
import retrofit2.Response

class ProductRepository {

    private val apiService = RetrofitInstance.api

    suspend fun getProducts(): Response<List<Product>> {
        return apiService.getProducts()
    }

    suspend fun searchProducts(query: String): Response<List<Product>> {
        return apiService.searchProducts(query)
    }
}
