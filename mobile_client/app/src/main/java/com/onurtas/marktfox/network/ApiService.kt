package com.onurtas.marktfox.network

import com.onurtas.marktfox.model.OptimizeRequest
import com.onurtas.marktfox.model.OptimizeResponse
import com.onurtas.marktfox.model.Product
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>

    @GET("search/products")
    suspend fun searchProducts(@Query("name") query: String): Response<List<Product>>

    @POST("optimize")
    suspend fun optimizeBasket(
        @Query("mode") mode: String = "multi_store",
        @Body request: OptimizeRequest
    ): Response<OptimizeResponse>
}
