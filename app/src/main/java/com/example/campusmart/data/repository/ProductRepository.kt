package com.example.campusmart.data.repository

import com.example.campusmart.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getProductsForVendor(vendorId: String): List<Product> {
        val snapshot = db.collection("vendors")
            .document(vendorId)
            .collection("products")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
    }
    suspend fun addProduct(vendorFirestoreId: String, product: Product): Result<Unit> {
        return try {
            val productId = db.collection("vendors").document(vendorFirestoreId)
                .collection("products").document().id
            val productWithId = product.copy(productId = productId)
            db.collection("vendors").document(vendorFirestoreId)
                .collection("products").document(productId)
                .set(productWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(vendorFirestoreId: String, productId: String): Result<Unit> {
        return try {
            db.collection("vendors").document(vendorFirestoreId)
                .collection("products").document(productId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}