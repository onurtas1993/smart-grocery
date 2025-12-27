package com.onurtas.marktfox.adapter

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.onurtas.marktfox.R
import com.onurtas.marktfox.model.Product
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>,
    private val listener: ProductBasketListener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val basketQuantities = mutableMapOf<Int, Int>()

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.productImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.productTitle)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantityText)
        private val addButton: Button = itemView.findViewById(R.id.addButton)
        private val removeButton: Button = itemView.findViewById(R.id.removeButton)
        val basketQuantity: TextView = itemView.findViewById(R.id.basketQuantity)

        fun bind(
            product: Product,
            currentQuantity: Int,
            listener: ProductBasketListener
        ) {
            imageView.load(product.image) {
                crossfade(true)
                error(R.drawable.ic_launcher_background)
            }

            titleTextView.text = product.title

            val quantityString = if (product.quantity == product.quantity.toInt().toDouble()) {
                String.format("%d %s", product.quantity.toInt(), product.unit)
            } else {
                String.format(Locale.GERMANY, "%.2f %s", product.quantity, product.unit)
            }
            quantityTextView.text = quantityString
            basketQuantity.text = currentQuantity.toString()

            addButton.setOnClickListener { listener.onProductAdded(product) }
            removeButton.setOnClickListener { listener.onProductRemoved(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val currentQuantity = basketQuantities.getOrDefault(product.id, 0)
        holder.bind(product, currentQuantity, listener)
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    fun updateProductQuantity(productId: Int, newQuantity: Int) {
        basketQuantities[productId] = newQuantity
        val index = products.indexOfFirst { it.id == productId }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun getAllProductIds(): List<Int> {
        return products.map { it.id }
    }
}
