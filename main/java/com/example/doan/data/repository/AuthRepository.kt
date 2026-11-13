package com.example.doan.data.repository

import com.example.doan.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUid: String? get() = auth.currentUser?.uid

    fun register(
        name: String, email: String, password: String, role: String,
        callback: (Result<User>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { res ->
                val uid = res.user?.uid
                if (uid == null) {
                    callback(Result.failure(IllegalStateException("Không tạo được tài khoản")))
                    return@addOnSuccessListener
                }
                val user = User(uid, name, email, role)
                db.collection("users").document(uid).set(user.toMap())
                    .addOnSuccessListener { callback(Result.success(user)) }
                    .addOnFailureListener { callback(Result.failure(it)) }
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun login(email: String, password: String, callback: (Result<User>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { getCurrentUser(callback) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    /** Lấy hồ sơ người dùng đang đăng nhập. Thiếu hồ sơ thì coi là sinh viên. */
    fun getCurrentUser(callback: (Result<User>) -> Unit) {
        val fbUser = auth.currentUser
        if (fbUser == null) {
            callback(Result.failure(IllegalStateException("Chưa đăng nhập")))
            return
        }
        db.collection("users").document(fbUser.uid).get()
            .addOnSuccessListener { doc ->
                val user = if (doc.exists()) User.fromDoc(doc)
                else User(fbUser.uid, fbUser.displayName ?: fbUser.email.orEmpty(), fbUser.email.orEmpty())
                callback(Result.success(user))
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun logout() = auth.signOut()
}
