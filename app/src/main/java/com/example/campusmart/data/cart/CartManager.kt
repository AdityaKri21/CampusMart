package com.example.campusmart.data.cart

import com.example.campusmart.data.model.CartItem
import com.example.campusmart.data.model.Product

object CartManager {

    private val cartItems = mutableListOf<CartItem>()

    fun addToCart(product: Product, vendorFirestoreId: String, vendorName: String): Boolean {
        if (cartItems.isNotEmpty() && cartItems.first().vendorFirestoreId != vendorFirestoreId) {
            return false // signal: different vendor, caller should ask user to confirm clearing cart
        }
        val existing = cartItems.find { it.product.productId == product.productId }
        if (existing != null) {
            existing.quantity += 1
        } else {
            cartItems.add(CartItem(product, vendorFirestoreId, vendorName, 1))
        }
        return true
    }

    fun removeFromCart(productId: String) {
        cartItems.removeAll { it.product.productId == productId }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        val item = cartItems.find { it.product.productId == productId }
        if (item != null) {
            if (newQuantity <= 0) {
                removeFromCart(productId)
            } else {
                item.quantity = newQuantity
            }
        }
    }

    fun getItems(): List<CartItem> = cartItems.toList()

    fun getTotalAmount(): Double = cartItems.sumOf { it.product.price * it.quantity }

    fun getItemCount(): Int = cartItems.sumOf { it.quantity }

    fun clearCart() {
        cartItems.clear()
    }

    fun getVendorFirestoreId(): String? = cartItems.firstOrNull()?.vendorFirestoreId
}