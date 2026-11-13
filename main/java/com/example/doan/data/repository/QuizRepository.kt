package com.example.doan.data.repository

import com.example.doan.data.model.Question
import com.example.doan.data.model.Quiz
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class QuizRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val quizzes get() = db.collection("quizzes")

    private fun toQuiz(doc: DocumentSnapshot) = Quiz(
        id = doc.id,
        title = doc.getString("title").orEmpty(),
        subject = doc.getString("subject").orEmpty(),
        durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 0,
        questionCount = doc.getLong("questionCount")?.toInt() ?: 0,
        status = doc.getString("status") ?: Quiz.UPCOMING,
        ownerId = doc.getString("ownerId").orEmpty()
    )

    fun getQuizzes(callback: (Result<List<Quiz>>) -> Unit) {
        quizzes.get()
            .addOnSuccessListener { s -> callback(Result.success(s.documents.map(::toQuiz))) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getQuizzesByOwner(ownerId: String, callback: (Result<List<Quiz>>) -> Unit) {
        quizzes.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { s -> callback(Result.success(s.documents.map(::toQuiz))) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getQuiz(id: String, callback: (Result<Quiz>) -> Unit) {
        quizzes.document(id).get()
            .addOnSuccessListener { d ->
                if (d.exists()) callback(Result.success(toQuiz(d)))
                else callback(Result.failure(IllegalStateException("Không tìm thấy đề thi")))
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    /** Tạo mới (id rỗng) hoặc cập nhật đề thi; trả về id. */
    fun saveQuiz(quiz: Quiz, ownerId: String, callback: (Result<String>) -> Unit) {
        val fields = mapOf(
            "title" to quiz.title, "subject" to quiz.subject,
            "durationMinutes" to quiz.durationMinutes, "status" to quiz.status
        )
        if (quiz.id.isBlank()) {
            quizzes.add(fields + mapOf("questionCount" to 0, "ownerId" to ownerId))
                .addOnSuccessListener { callback(Result.success(it.id)) }
                .addOnFailureListener { callback(Result.failure(it)) }
        } else {
            quizzes.document(quiz.id).set(fields, SetOptions.merge())
                .addOnSuccessListener { callback(Result.success(quiz.id)) }
                .addOnFailureListener { callback(Result.failure(it)) }
        }
    }

    fun setStatus(quizId: String, status: String, callback: (Result<Unit>) -> Unit) {
        quizzes.document(quizId).update("status", status)
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun deleteQuiz(quizId: String, callback: (Result<Unit>) -> Unit) {
        val ref = quizzes.document(quizId)
        ref.collection("questions").get()
            .addOnSuccessListener { qs ->
                val batch = db.batch()
                qs.documents.forEach { d ->
                    batch.delete(d.reference)
                    batch.delete(ref.collection("answerKeys").document(d.id))
                }
                batch.delete(ref)
                batch.commit()
                    .addOnSuccessListener { callback(Result.success(Unit)) }
                    .addOnFailureListener { callback(Result.failure(it)) }
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    /** Lấy câu hỏi (đã sắp theo order), kèm đáp án đúng nếu withKey = true. */
    fun getQuestions(quizId: String, withKey: Boolean, callback: (Result<List<Question>>) -> Unit) {
        val ref = quizzes.document(quizId)
        ref.collection("questions").get()
            .addOnSuccessListener { qs ->
                if (!withKey) {
                    callback(Result.success(qs.documents.map { Question.fromDoc(it) }.sortedBy { it.order }))
                    return@addOnSuccessListener
                }
                getAnswerKey(quizId) { keyResult ->
                    keyResult
                        .onSuccess { key ->
                            callback(Result.success(
                                qs.documents.map { Question.fromDoc(it, key[it.id].orEmpty()) }
                                    .sortedBy { it.order }
                            ))
                        }
                        .onFailure { callback(Result.failure(it)) }
                }
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getAnswerKey(quizId: String, callback: (Result<Map<String, String>>) -> Unit) {
        quizzes.document(quizId).collection("answerKeys").get()
            .addOnSuccessListener { s ->
                callback(Result.success(s.documents.associate { it.id to it.getString("correct").orEmpty() }))
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun addQuestion(quizId: String, q: Question, callback: (Result<Unit>) -> Unit) {
        val quizRef = quizzes.document(quizId)
        val qRef = quizRef.collection("questions").document()
        val batch = db.batch()
        batch.set(qRef, q.copy(order = System.currentTimeMillis()).toMap())
        batch.set(quizRef.collection("answerKeys").document(qRef.id), mapOf("correct" to q.correctAnswer))
        batch.update(quizRef, "questionCount", FieldValue.increment(1))
        batch.commit()
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun deleteQuestion(quizId: String, questionId: String, callback: (Result<Unit>) -> Unit) {
        val quizRef = quizzes.document(quizId)
        val batch = db.batch()
        batch.delete(quizRef.collection("questions").document(questionId))
        batch.delete(quizRef.collection("answerKeys").document(questionId))
        batch.update(quizRef, "questionCount", FieldValue.increment(-1))
        batch.commit()
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }
}
