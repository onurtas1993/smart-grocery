package com.onurtas.marktfox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onurtas.marktfox.R
import com.onurtas.marktfox.model.Product

class ProductAdapter(private var products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Describes an item view and its metadata within the RecyclerView.
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.productTitle)
        private val priceTextView: TextView = itemView.findViewById(R.id.productPrice)

        fun bind(product: Product) {
            titleTextView.text = product.title
            priceTextView.text = String.format("€%.2f", product.price)
        }
    }

    // Creates new views (invoked by the layout manager).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    // Replaces the contents of a view (invoked by the layout manager).
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    // Returns the size of your dataset (invoked by the layout manager).
    override fun getItemCount() = products.size

    // Helper function to update the data in the adapter.
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged() // Notifies the adapter that the data set has changed.
    }
}
