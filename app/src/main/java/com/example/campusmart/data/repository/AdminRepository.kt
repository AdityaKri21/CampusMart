package com.example.campusmart.data.repository

import com.example.campusmart.data.model.RoleRequest
import com.example.campusmart.data.model.Vendor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val db = FirebaseFirestore.getInstance()
    private val roleRequestsCollection = db.collection("roleRequests")
    private val usersCollection = db.collection("users")
    private val vendorsCollection = db.collection("vendors")

    suspend fun getPendingRequests(): List<RoleRequest> {
        val snapshot = roleRequestsCollection
            .whereEqualTo("status", "pending")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(RoleRequest::class.java) }
    }

    suspend fun approveVendorRequest(request: RoleRequest): Result<Unit> {
        return try {
            // 1. Generate a simple vendor ID
            val vendorId = "VEN-${System.currentTimeMillis().toString().takeLast(6)}"

            // 2. Update the user's role
            usersCollection.document(request.uid).update(
                mapOf("role" to "vendor", "vendorId" to vendorId)
            ).await()

            // 3. Create the vendor document
            val vendor = Vendor(
                vendorId = vendorId,
                shopName = request.shopName,
                category = request.category,
                location = request.location,
                rating = 0.0,
                deliveryTimeText = "30-40 min"
            )
            vendorsCollection.document().set(vendor).await()

            // 4. Mark request as approved
            roleRequestsCollection.document(request.requestId).update(
                "status", "approved"
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectRequest(requestId: String): Result<Unit> {
        return try {
            roleRequestsCollection.document(requestId).update(
                "status", "rejected"
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}