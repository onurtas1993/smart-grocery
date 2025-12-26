package com.onurtas.marktfox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onurtas.marktfox.R
import com.onurtas.marktfox.adapter.SummaryAdapter
import com.onurtas.marktfox.viewmodel.MainActivityViewModel
import java.util.Locale

class SummaryFragment : Fragment() {

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private lateinit var summaryAdapter: SummaryAdapter
    private lateinit var summaryRecyclerView: RecyclerView
    private lateinit var emptyBasketText: TextView
    private lateinit var totalCostValue: TextView
    private lateinit var totalCostLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_summary, container, false)
        summaryRecyclerView = view.findViewById(R.id.summaryRecyclerView)
        emptyBasketText = view.findViewById(R.id.emptyBasketText)
        totalCostValue = view.findViewById(R.id.totalCostValue)
        totalCostLabel = view.findViewById(R.id.totalCostLabel)
        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun setupRecyclerView() {
        summaryAdapter = SummaryAdapter(emptyList())
        summaryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = summaryAdapter
        }
    }

    private fun observeViewModel() {
        mainActivityViewModel.basket.observe(viewLifecycleOwner) { basket ->
            val hasItems = basket.isNotEmpty()
            summaryRecyclerView.isVisible = hasItems
            totalCostLabel.isVisible = hasItems
            totalCostValue.isVisible = hasItems
            emptyBasketText.isVisible = !hasItems
            summaryAdapter.updateItems(basket)
        }

        mainActivityViewModel.totalCost.observe(viewLifecycleOwner) { total ->
            totalCostValue.text = String.format(Locale.GERMANY, "€%.2f", total)
        }
    }
}
