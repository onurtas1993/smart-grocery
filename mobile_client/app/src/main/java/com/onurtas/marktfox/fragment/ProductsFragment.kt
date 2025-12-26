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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.onurtas.marktfox.R
import com.onurtas.marktfox.adapter.ProductAdapter
import com.onurtas.marktfox.viewmodel.ProductsViewModel

class ProductsFragment : Fragment() {

    private val viewModel: ProductsViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var searchInput: TextInputEditText // Add reference for search input

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        // Initialize views
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
        productAdapter = ProductAdapter(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener { text ->
            viewModel.searchProducts(text.toString())
        }
    }

    private fun observeViewModel() {
        progressBar.isVisible = true

        viewModel.products.observe(viewLifecycleOwner) { products ->
            progressBar.isVisible = false
            recyclerView.isVisible = true
            productAdapter.updateProducts(products)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            progressBar.isVisible = false
            errorTextView.isVisible = true
            errorTextView.text = errorMessage
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }
}
