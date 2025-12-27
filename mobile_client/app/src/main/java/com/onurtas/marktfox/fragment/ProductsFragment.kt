package com.onurtas.marktfox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.onurtas.marktfox.R
import com.onurtas.marktfox.adapter.ProductAdapter
import com.onurtas.marktfox.adapter.ProductBasketListener
import com.onurtas.marktfox.model.Product
import com.onurtas.marktfox.viewmodel.MainActivityViewModel
import com.onurtas.marktfox.viewmodel.ProductsViewModel

class ProductsFragment : Fragment(), ProductBasketListener {

    private val viewModel: ProductsViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var searchInput: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.errorTextView)
        searchInput = view.findViewById(R.id.searchInput)
        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupSearch()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList(), this)
        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener { text ->
            viewModel.searchProducts(text.toString())
        }
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            progressBar.isVisible = false
            errorTextView.isVisible = false // This line fixes the bug
            recyclerView.isVisible = true
            productAdapter.updateProducts(products)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                progressBar.isVisible = false
                recyclerView.isVisible = false
                errorTextView.isVisible = true
                errorTextView.text = errorMessage
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        mainActivityViewModel.basket.observe(viewLifecycleOwner) { basket ->
            val productIdsInBasket = basket.keys

            productAdapter.getAllProductIds().forEach { productId ->
                if (productId !in productIdsInBasket) {
                    productAdapter.updateProductQuantity(productId, 0)
                }
            }

            basket.forEach { (productId, entry) ->
                val newQuantity = entry.second
                productAdapter.updateProductQuantity(productId, newQuantity)
            }
        }
    }

    override fun onProductAdded(product: Product) {
        mainActivityViewModel.addProductToBasket(product)
    }

    override fun onProductRemoved(product: Product) {
        mainActivityViewModel.removeProductFromBasket(product)
    }
}
