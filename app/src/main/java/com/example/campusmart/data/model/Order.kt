package com.example.campusmart.data.model

data class Order(
    val orderId: String = "",
    val studentUid: String = "",
    val studentName: String = "",
    val vendorFirestoreId: String = "",
    val vendorName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderType: String = "takeaway",
    val deliveryAddress: String = "",
    val status: String = "placed",
    val rating: Int = 0,
    val isPrintOrder: Boolean = false,
    val documentLink: String = "",
    val copies: Int = 0,
    val colorMode: String = "",
    val specialRequest: String = "",
    val createdAt: Long = System.currentTimeMillis()
)