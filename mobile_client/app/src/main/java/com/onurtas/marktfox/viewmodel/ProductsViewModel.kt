package com.onurtas.marktfox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurtas.marktfox.model.Product
import com.onurtas.marktfox.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {

    private val repository = ProductRepository()

    // Holds the complete list of products fetched from the API
    private var fullProductList = listOf<Product>()

    // Exposes the filtered list to the UI
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            try {
                val response = repository.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    fullProductList = response.body()!!
                    _products.postValue(fullProductList)
                } else {
                    _errorMessage.postValue("Failed to fetch products: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("An error occurred: ${e.message}")
            }
        }
    }

    /**
     * Filters the product list based on a search query.
     * @param query The text to search for in product titles.
     */
    fun searchProducts(query: String) {
        if (query.isBlank()) {
            // If the query is empty, show the full list
            _products.value = fullProductList
        } else {
            // Otherwise, filter the list
            val filteredList = fullProductList.filter { product ->
                product.title.contains(query, ignoreCase = true)
            }
            _products.value = filteredList
        }
    }
}
