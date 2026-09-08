package com.example.campusmart.data.model

data class CartItem(
    val product: Product,
    val vendorFirestoreId: String,
    val vendorName: String,
    var quantity: Int = 1
)