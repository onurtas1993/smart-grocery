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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.onurtas.marktfox.R
import com.onurtas.marktfox.adapter.SummaryAdapter
import com.onurtas.marktfox.model.ApiBasketItem
import com.onurtas.marktfox.viewmodel.MainActivityViewModel
import com.onurtas.marktfox.viewmodel.SummaryViewModel
import java.util.Locale

class SummaryFragment : Fragment() {

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: SummaryViewModel by viewModels()

    private lateinit var summaryAdapter: SummaryAdapter
    private lateinit var summaryRecyclerView: RecyclerView
    private lateinit var emptyBasketText: TextView
    private lateinit var totalCostValue: TextView
    private lateinit var totalCostLayout: View
    private lateinit var progressBar: ProgressBar
    private lateinit var modeCheckbox: MaterialCheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_summary, container, false)
        summaryRecyclerView = view.findViewById(R.id.summaryRecyclerView)
        emptyBasketText = view.findViewById(R.id.emptyBasketText)
        totalCostValue = view.findViewById(R.id.totalCostValue)
        totalCostLayout = view.findViewById(R.id.totalCostLayout)
        progressBar = view.findViewById(R.id.progressBar)
        modeCheckbox = view.findViewById(R.id.modeCheckbox)
        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCheckboxListener()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        summaryAdapter = SummaryAdapter(emptyList())
        summaryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = summaryAdapter
        }
    }

    private fun setupCheckboxListener() {
        modeCheckbox.setOnCheckedChangeListener { _, _ ->
            val basket = mainActivityViewModel.basket.value
            if (basket != null && basket.isNotEmpty()) {
                val apiBasketItems = basket.values.map { (product, quantity) ->
                    ApiBasketItem(
                        name = product.title,
                        quantity = product.quantity * quantity,
                        unit = product.unit ?: ""
                    )
                }
                viewModel.fetchOptimizedBasket(apiBasketItems, modeCheckbox.isChecked)
            }
        }
    }

    private fun observeViewModel() {
        mainActivityViewModel.basket.observe(viewLifecycleOwner) { basket ->
            if (basket.isNotEmpty()) {
                val apiBasketItems = basket.values.map { (product, quantity) ->
                    ApiBasketItem(
                        name = product.title,
                        quantity = product.quantity * quantity,
                        unit = product.unit ?: ""
                    )
                }
                viewModel.fetchOptimizedBasket(apiBasketItems, modeCheckbox.isChecked)
            } else {
                summaryAdapter.updateItems(emptyMap())
                totalCostValue.text = ""
                totalCostLayout.isVisible = false
                summaryRecyclerView.isVisible = false
                emptyBasketText.isVisible = true
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            progressBar.isVisible = it
            summaryRecyclerView.isVisible = !it
        }

        viewModel.totalPrice.observe(viewLifecycleOwner) { price ->
            totalCostValue.text = String.format(Locale.GERMANY, "€%.2f", price)
        }

        viewModel.optimizedBasket.observe(viewLifecycleOwner) { optimizedBasket ->
            summaryRecyclerView.isVisible = true
            totalCostLayout.isVisible = true
            emptyBasketText.isVisible = false

            // Convert the map to the type expected by the adapter
            val adapterMap = optimizedBasket.map { (product, quantity) ->
                product.id to Pair(product, quantity)
            }.toMap()

            summaryAdapter.updateItems(adapterMap)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            totalCostLayout.isVisible = false
            summaryRecyclerView.isVisible = false
            emptyBasketText.isVisible = true
            emptyBasketText.text = error
        }
    }
}
