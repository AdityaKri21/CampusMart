package com.example.campusmart.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student", // "student" | "vendor" | "admin"
    val phone: String = "",
    val roomNo: String = "",
    val hostelBlock: String = "",
    val vendorId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)