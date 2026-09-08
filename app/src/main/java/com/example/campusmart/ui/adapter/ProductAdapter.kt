package com.example.campusmart.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmart.data.model.Product
import com.example.campusmart.databinding.ItemProductBinding

class ProductAdapter(
    private val products: MutableList<Product>,
    private val showDelete: Boolean = false,
    private val onDelete: ((Product) -> Unit)? = null,
    private val onAddToCart: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.binding.apply {
            productName.text = product.name
            productDescription.text = product.description
            productPrice.text = "₹${product.price}"
            if (product.imageUrl.isNotBlank()) {
                com.bumptech.glide.Glide.with(holder.binding.root.context)
                    .load(product.imageUrl)
                    .centerCrop()
                    .into(productImage)
            }
            if (showDelete) {
                addButton.text = "Delete"
                addButton.setOnClickListener { onDelete?.invoke(product) }
            } else {
                addButton.text = "Add"
                addButton.setOnClickListener { onAddToCart?.invoke(product) }
            }
        }
    }

    override fun getItemCount() = products.size

    fun removeItem(product: Product) {
        val index = products.indexOf(product)
        if (index != -1) {
            products.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}