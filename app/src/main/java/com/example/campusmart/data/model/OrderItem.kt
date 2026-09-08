package com.example.campusmart.data.model

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)