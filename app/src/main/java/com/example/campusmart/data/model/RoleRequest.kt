package com.example.campusmart.data.model

data class RoleRequest(
    val requestId: String = "",
    val uid: String = "",
    val requestedRole: String = "vendor", // "vendor" | "admin"
    val status: String = "pending", // "pending" | "approved" | "rejected"
    val shopName: String = "",
    val category: String = "",
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis()
)