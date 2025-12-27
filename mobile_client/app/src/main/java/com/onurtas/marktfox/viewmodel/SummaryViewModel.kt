package com.onurtas.marktfox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onurtas.marktfox.model.ApiBasketItem
import com.onurtas.marktfox.model.OptimizeRequest
import com.onurtas.marktfox.model.Product
import com.onurtas.marktfox.repository.ProductRepository
import kotlinx.coroutines.launch

class SummaryViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _optimizedBasket = MutableLiveData<Map<Product, Int>>()
    val optimizedBasket: LiveData<Map<Product, Int>> = _optimizedBasket

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchOptimizedBasket(items: List<ApiBasketItem>) {
        val mode = "multi_store"
        val request = OptimizeRequest(items)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.optimizeBasket(mode, request)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    _totalPrice.postValue(result.totalPrice)

                    // The logic is now much cleaner.
                    // We directly associate the product with its required package count.
                    val optimizedMap = result.items.associate { optimizedItem ->
                        optimizedItem.product to optimizedItem.requiredPackages
                    }
                    _optimizedBasket.postValue(optimizedMap)

                } else {
                    _errorMessage.postValue("Optimization failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("An error occurred: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
