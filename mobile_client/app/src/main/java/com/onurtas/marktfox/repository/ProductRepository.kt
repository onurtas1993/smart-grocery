package com.onurtas.marktfox.repository

import com.onurtas.marktfox.model.OptimizeRequest
import com.onurtas.marktfox.model.OptimizeResponse
import com.onurtas.marktfox.model.Product
import com.onurtas.marktfox.network.RetrofitInstance
import retrofit2.Response

class ProductRepository {
    suspend fun getProducts(): Response<List<Product>> {
        return RetrofitInstance.api.getProducts()
    }

    suspend fun searchProducts(query: String): Response<List<Product>> {
        return RetrofitInstance.api.searchProducts(query)
    }

    suspend fun optimizeBasket(mode: String, request: OptimizeRequest): Response<OptimizeResponse> {
        return RetrofitInstance.api.optimizeBasket(mode, request)
    }
}
