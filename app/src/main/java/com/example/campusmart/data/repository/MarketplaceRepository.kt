package com.example.campusmart.data.repository

import com.example.campusmart.data.model.MarketplaceListing
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MarketplaceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val listingsCollection = db.collection("marketplaceListings")

    suspend fun getAllListings(): List<MarketplaceListing> {
        val snapshot = listingsCollection
            .whereEqualTo("status", "available")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(MarketplaceListing::class.java) }
    }

    suspend fun getMyListings(sellerUid: String): List<MarketplaceListing> {
        val snapshot = listingsCollection
            .whereEqualTo("sellerUid", sellerUid)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(MarketplaceListing::class.java) }
    }

    suspend fun addListing(listing: MarketplaceListing): Result<Unit> {
        return try {
            val listingId = listingsCollection.document().id
            val withId = listing.copy(listingId = listingId)
            listingsCollection.document(listingId).set(withId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteListing(listingId: String): Result<Unit> {
        return try {
            listingsCollection.document(listingId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsSold(listingId: String): Result<Unit> {
        return try {
            listingsCollection.document(listingId).update("status", "sold").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}