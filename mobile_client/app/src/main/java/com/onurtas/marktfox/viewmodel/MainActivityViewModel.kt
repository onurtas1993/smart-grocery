package com.onurtas.marktfox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onurtas.marktfox.model.Product

class MainActivityViewModel : ViewModel() {

    private val _basket = MutableLiveData<Map<Int, Pair<Product, Int>>>(emptyMap())
    val basket: LiveData<Map<Int, Pair<Product, Int>>> = _basket

    private val _totalCost = MutableLiveData<Double>(0.0)
    val totalCost: LiveData<Double> = _totalCost

    fun addProductToBasket(product: Product) {
        val newBasket = _basket.value?.toMutableMap() ?: mutableMapOf()
        val currentEntry = newBasket[product.id]
        val currentQuantity = currentEntry?.second ?: 0

        newBasket[product.id] = Pair(product, currentQuantity + 1)
        _basket.postValue(newBasket)
        calculateTotalCost(newBasket)
    }

    fun removeProductFromBasket(product: Product) {
        val newBasket = _basket.value?.toMutableMap() ?: mutableMapOf()
        val currentEntry = newBasket[product.id]

        if (currentEntry != null) {
            val newQuantity = currentEntry.second - 1
            if (newQuantity > 0) {
                newBasket[product.id] = Pair(product, newQuantity)
            } else {
                newBasket.remove(product.id)
            }
            _basket.postValue(newBasket)
            calculateTotalCost(newBasket)
        }
    }

    private fun calculateTotalCost(basket: Map<Int, Pair<Product, Int>>) {
        val total = basket.values.sumOf { (product, quantity) ->
            product.price * quantity
        }
        _totalCost.postValue(total)
    }
}
