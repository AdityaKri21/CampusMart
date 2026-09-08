package com.example.campusmart.data.repository

import com.example.campusmart.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(name: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User creation failed")

            val newUser = User(uid = uid, name = name, email = email, role = "student")
            usersCollection.document(uid).set(newUser).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserRole(): String? {
        val uid = getCurrentUserId() ?: return null
        val doc = usersCollection.document(uid).get().await()
        return doc.getString("role")
    }

    fun logout() {
        auth.signOut()
    }
    suspend fun getCurrentUser(): User? {
        val uid = getCurrentUserId() ?: return null
        val doc = usersCollection.document(uid).get().await()
        return doc.toObject(User::class.java)
    }

    suspend fun updateProfile(phone: String, roomNo: String, hostelBlock: String): Result<Unit> {
        return try {
            val uid = getCurrentUserId() ?: throw Exception("Not logged in")
            usersCollection.document(uid).update(
                mapOf(
                    "phone" to phone,
                    "roomNo" to roomNo,
                    "hostelBlock" to hostelBlock
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private val roleRequestsCollection = db.collection("roleRequests")

    suspend fun submitVendorRequest(shopName: String, category: String, location: String): Result<Unit> {
        return try {
            val uid = getCurrentUserId() ?: throw Exception("Not logged in")
            val requestId = roleRequestsCollection.document().id
            val request = com.example.campusmart.data.model.RoleRequest(
                requestId = requestId,
                uid = uid,
                requestedRole = "vendor",
                status = "pending",
                shopName = shopName,
                category = category,
                location = location
            )
            roleRequestsCollection.document(requestId).set(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyPendingRequest(): com.example.campusmart.data.model.RoleRequest? {
        val uid = getCurrentUserId() ?: return null
        val snapshot = roleRequestsCollection
            .whereEqualTo("uid", uid)
            .whereEqualTo("status", "pending")
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(com.example.campusmart.data.model.RoleRequest::class.java)
    }
}