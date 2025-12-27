package com.onurtas.marktfox.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.onurtas.marktfox.model.ApiBasketItem
import com.onurtas.marktfox.model.ApiError
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

    fun fetchOptimizedBasket(items: List<ApiBasketItem>, isMultiStore: Boolean) {
        val mode = if (isMultiStore) "multi_store" else "single_store"
        val request = OptimizeRequest(items)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.optimizeBasket(mode, request)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    _totalPrice.postValue(result.totalPrice)

                    val optimizedMap = result.items.associate { optimizedItem ->
                        optimizedItem.product to optimizedItem.requiredPackages
                    }
                    _optimizedBasket.postValue(optimizedMap)

                } else {
                    if (response.code() == 404) {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            try {
                                val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                                _errorMessage.postValue(apiError.detail)
                            } catch (e: Exception) {
                                _errorMessage.postValue("Failed to parse error message.")
                            }
                        } else {
                            _errorMessage.postValue("Optimization failed: Store not found (404).")
                        }
                    } else {
                        _errorMessage.postValue("Optimization failed: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.postValue("An error occurred: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
