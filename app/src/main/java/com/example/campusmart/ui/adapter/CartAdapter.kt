package com.example.campusmart.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmart.data.model.CartItem
import com.example.campusmart.databinding.ItemCartBinding

class CartAdapter(
    private val items: List<CartItem>,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            cartItemName.text = item.product.name
            cartItemPrice.text = "₹${item.product.price} x ${item.quantity}"
            cartItemQty.text = item.quantity.toString()

            increaseButton.setOnClickListener { onIncrease(item) }
            decreaseButton.setOnClickListener { onDecrease(item) }
        }
    }

    override fun getItemCount() = items.size
}