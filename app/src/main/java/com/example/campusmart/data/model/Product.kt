package com.example.campusmart.data.model

data class Product(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val isAvailable: Boolean = true
)