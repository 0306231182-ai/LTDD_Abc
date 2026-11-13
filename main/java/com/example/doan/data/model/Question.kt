package com.example.doan.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Câu hỏi phía giáo viên (có đáp án đúng).
 * Firestore: quizzes/{id}/questions/{qid} (không chứa đáp án)
 *            quizzes/{id}/answerKeys/{qid} { correct: "A".."D" }
 */
data class Question(
    val id: String = "",
    val order: Long = 0L,
    val text: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "A"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "text" to text, "optionA" to optionA, "optionB" to optionB,
        "optionC" to optionC, "optionD" to optionD, "order" to order
    )

    fun option(letter: String): String = when (letter) {
        "A" -> optionA; "B" -> optionB; "C" -> optionC; "D" -> optionD; else -> ""
    }

    companion object {
        fun fromDoc(doc: DocumentSnapshot, correct: String = "") = Question(
            id = doc.id,
            order = doc.getLong("order") ?: 0L,
            text = doc.getString("text").orEmpty(),
            optionA = doc.getString("optionA").orEmpty(),
            optionB = doc.getString("optionB").orEmpty(),
            optionC = doc.getString("optionC").orEmpty(),
            optionD = doc.getString("optionD").orEmpty(),
            correctAnswer = correct
        )
    }
}
