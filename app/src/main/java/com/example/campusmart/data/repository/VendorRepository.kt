package com.example.campusmart.data.repository

import com.example.campusmart.data.model.Vendor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VendorRepository {

    private val db = FirebaseFirestore.getInstance()
    private val vendorsCollection = db.collection("vendors")

    suspend fun getAllVendors(): List<Vendor> {
        val snapshot = vendorsCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Vendor::class.java)?.apply { firestoreDocId = doc.id }
        }
    }
    suspend fun getVendorByVendorId(vendorId: String): Vendor? {
        val snapshot = vendorsCollection.whereEqualTo("vendorId", vendorId).get().await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.toObject(Vendor::class.java)?.apply { firestoreDocId = doc.id }
    }
    suspend fun addRatingToVendor(vendorFirestoreId: String, newRating: Int): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val vendorRef = vendorsCollection.document(vendorFirestoreId)
                val snapshot = transaction.get(vendorRef)

                val currentRating = snapshot.getDouble("rating") ?: 0.0
                val currentCount = snapshot.getLong("ratingCount")?.toInt() ?: 0

                val newCount = currentCount + 1
                val newAverage = ((currentRating * currentCount) + newRating) / newCount

                transaction.update(vendorRef, mapOf(
                    "rating" to newAverage,
                    "ratingCount" to newCount
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateVendorDetails(
        vendorFirestoreId: String,
        shopName: String,
        category: String,
        location: String,
        imageUrl: String
    ): Result<Unit> {
        return try {
            vendorsCollection.document(vendorFirestoreId).update(
                mapOf(
                    "shopName" to shopName,
                    "category" to category,
                    "location" to location,
                    "imageUrl" to imageUrl
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}