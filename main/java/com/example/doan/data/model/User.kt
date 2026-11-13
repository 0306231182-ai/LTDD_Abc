package com.example.doan.data.model

import com.google.firebase.firestore.DocumentSnapshot

/** Người dùng lưu ở users/{uid}. role = "student" hoặc "teacher". */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = ROLE_STUDENT
) {
    val isTeacher: Boolean get() = role == ROLE_TEACHER

    fun toMap(): Map<String, Any> = mapOf("name" to name, "email" to email, "role" to role)

    companion object {
        const val ROLE_STUDENT = "student"
        const val ROLE_TEACHER = "teacher"

        fun fromDoc(doc: DocumentSnapshot) = User(
            uid = doc.id,
            name = doc.getString("name").orEmpty(),
            email = doc.getString("email").orEmpty(),
            role = doc.getString("role") ?: ROLE_STUDENT
        )
    }
}
