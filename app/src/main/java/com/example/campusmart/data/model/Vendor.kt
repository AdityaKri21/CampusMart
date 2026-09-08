package com.example.campusmart.data.model

data class Vendor(
    val vendorId: String = "",
    val shopName: String = "",
    val category: String = "",
    val location: String = "",
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val deliveryTimeText: String = "",
    val imageUrl: String = "",
    var firestoreDocId: String = ""
)