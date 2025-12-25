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
                    _products.postValue(response.body())
                } else {
                    _errorMessage.postValue("Failed to fetch products: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("An error occurred: ${e.message}")
            }
        }
    }
}
