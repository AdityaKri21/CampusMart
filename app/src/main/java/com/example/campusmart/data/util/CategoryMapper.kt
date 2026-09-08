package com.example.campusmart.data.util

object CategoryMapper {
    fun mapToCategoryBucket(vendorCategory: String): String {
        val c = vendorCategory.lowercase()
        return when {
            c.contains("food") || c.contains("beverage") -> "Food"
            c.contains("stationery") || c.contains("stationary") -> "Stationery"
            c.contains("print") -> "Printing"
            c.contains("daily") -> "Daily Use"
            else -> "Misc"
        }
    }
}