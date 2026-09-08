package com.example.campusmart.data.repository

import com.example.campusmart.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    suspend fun placeOrder(order: Order): Result<Unit> {
        return try {
            val orderId = ordersCollection.document().id
            val orderWithId = order.copy(orderId = orderId)
            ordersCollection.document(orderId).set(orderWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersForVendor(vendorFirestoreId: String): List<Order> {
        val snapshot = ordersCollection
            .whereEqualTo("vendorFirestoreId", vendorFirestoreId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
    }

    suspend fun getOrdersForStudent(studentUid: String): List<Order> {
        val snapshot = ordersCollection
            .whereEqualTo("studentUid", studentUid)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getOrdersForVendorByStatus(vendorFirestoreId: String, ongoing: Boolean): List<Order> {
        val allOrders = getOrdersForVendor(vendorFirestoreId)
        return if (ongoing) {
            allOrders.filter { it.status != "completed" && it.status != "cancelled" }
        } else {
            allOrders.filter { it.status == "completed" || it.status == "cancelled" }
        }
    }
    suspend fun submitRating(order: Order, ratingValue: Int, vendorRepository: VendorRepository): Result<Unit> {
        return try {
            ordersCollection.document(order.orderId).update("rating", ratingValue).await()
            vendorRepository.addRatingToVendor(order.vendorFirestoreId, ratingValue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}