package com.onurtas.marktfox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.onurtas.marktfox.R
import com.onurtas.marktfox.model.Product
import java.util.Locale

class SummaryAdapter(private var items: List<Pair<Product, Int>>) :
    RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productTitle: TextView = itemView.findViewById(R.id.productTitle)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val storeName: TextView = itemView.findViewById(R.id.storeName)
        private val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        private val basketQuantity: TextView = itemView.findViewById(R.id.basketQuantity)

        fun bind(item: Pair<Product, Int>) {
            val (product, quantity) = item

            productImage.load(product.image) {
                crossfade(true)
                error(R.drawable.ic_launcher_background)
            }

            productTitle.text = product.title
            productPrice.text = String.format(Locale.GERMANY, "€%.2f", product.price)
            storeName.text = product.store
            basketQuantity.text = "x $quantity"

            val packageDetails = if (product.quantity == product.quantity.toInt().toDouble()) {
                String.format("%d %s", product.quantity.toInt(), product.unit)
            } else {
                String.format(Locale.GERMANY, "%.2f %s", product.quantity, product.unit)
            }
            quantityText.text = packageDetails
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary_product, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: Map<Int, Pair<Product, Int>>) {
        items = newItems.values.toList()
        notifyDataSetChanged()
    }
}
