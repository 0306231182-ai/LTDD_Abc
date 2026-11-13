package com.example.doan.data.model

import com.google.firebase.firestore.DocumentSnapshot

/** Kết quả một lượt thi, lưu ở results/{id}. Điểm theo thang 10. */
data class ExamResult(
    val id: String = "",
    val quizId: String = "",
    val quizTitle: String = "",
    val userId: String = "",
    val userName: String = "",
    val score: Double = 0.0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val timeUp: Boolean = false,
    val submittedAt: Long = 0L,
    val answers: Map<String, String> = emptyMap()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "quizId" to quizId, "quizTitle" to quizTitle,
        "userId" to userId, "userName" to userName,
        "score" to score, "correctCount" to correctCount,
        "totalQuestions" to totalQuestions, "timeUp" to timeUp,
        "submittedAt" to submittedAt, "answers" to answers
    )

    companion object {
        fun fromDoc(doc: DocumentSnapshot) = ExamResult(
            id = doc.id,
            quizId = doc.getString("quizId").orEmpty(),
            quizTitle = doc.getString("quizTitle").orEmpty(),
            userId = doc.getString("userId").orEmpty(),
            userName = doc.getString("userName").orEmpty(),
            score = doc.getDouble("score") ?: 0.0,
            correctCount = doc.getLong("correctCount")?.toInt() ?: 0,
            totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0,
            timeUp = doc.getBoolean("timeUp") ?: false,
            submittedAt = doc.getLong("submittedAt") ?: 0L,
            answers = (doc.get("answers") as? Map<*, *>)
                ?.entries?.associate { it.key.toString() to it.value.toString() }
                ?: emptyMap()
        )
    }
}
