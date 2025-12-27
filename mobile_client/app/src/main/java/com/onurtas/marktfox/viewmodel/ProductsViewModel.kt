package com.onurtas.marktfox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurtas.marktfox.model.Product
import com.onurtas.marktfox.repository.ProductRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ProductsViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val searchQuery = MutableStateFlow("")

    init {
        fetchProducts()
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        searchQuery
            .debounce(500)
            .filter { it.length > 1 || it.isEmpty() }
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isEmpty()) {
                    fetchProducts()
                } else {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            try {
                // Clear previous errors when fetching all products
                _errorMessage.postValue("")
                val response = repository.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    _products.postValue(response.body())
                } else {
                    _errorMessage.postValue("Failed to fetch products: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("An error occurred: ${e.message}")
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                // Clear previous errors before starting a new search
                _errorMessage.postValue("")
                val response = repository.searchProducts(query)
                if (response.isSuccessful && response.body() != null) {
                    _products.postValue(response.body())
                } else {
                    _errorMessage.postValue("Failed to search products: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("An error occurred: ${e.message}")
            }
        }
    }

    fun searchProducts(query: String) {
        searchQuery.value = query
    }
}
