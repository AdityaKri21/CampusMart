package com.example.campusmart.data.model

data class MarketplaceListing(
    val listingId: String = "",
    val sellerUid: String = "",
    val sellerName: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val condition: String = "used",
    val status: String = "available",
    val imageUrl: String = "",
    val contactInfo: String = "",
    val createdAt: Long = System.currentTimeMillis()
)