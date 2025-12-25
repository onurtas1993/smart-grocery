package com.onurtas.marktfox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onurtas.marktfox.R
import com.onurtas.marktfox.adapter.ProductAdapter
import com.onurtas.marktfox.viewmodel.ProductsViewModel

class ProductsFragment : Fragment() {

    private val viewModel: ProductsViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.errorTextView)

        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    private fun observeViewModel() {
        // Show progress bar initially
        progressBar.isVisible = true

        // Observe product list changes
        viewModel.products.observe(viewLifecycleOwner) { products ->
            progressBar.isVisible = false
            recyclerView.isVisible = true
            productAdapter.updateProducts(products)
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            progressBar.isVisible = false
            errorTextView.isVisible = true
            errorTextView.text = errorMessage
            // Optionally, show a Toast for better user feedback
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }
}
