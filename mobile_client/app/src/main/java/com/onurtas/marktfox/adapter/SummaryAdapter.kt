package com.onurtas.marktfox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onurtas.marktfox.R
import com.onurtas.marktfox.model.Product

class SummaryAdapter(private var items: List<Pair<Product, Int>>) :
    RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary_product, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val (product, quantity) = items[position]
        holder.productName.text = product.title
        holder.productQuantity.text = "x $quantity"
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: Map<Int, Pair<Product, Int>>) {
        items = newItems.values.toList()
        notifyDataSetChanged()
    }
}
