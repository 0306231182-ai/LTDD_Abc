package com.example.doan.data.repository

import com.example.doan.data.model.ExamResult
import com.example.doan.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class ResultRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val results get() = db.collection("results")

    /** Chấm điểm theo answerKeys rồi lưu results/{id}. Điểm thang 10, làm tròn 2 số lẻ. */
    fun submit(
        quizId: String, quizTitle: String, user: User,
        answers: Map<String, String>, totalQuestions: Int, timeUp: Boolean,
        callback: (Result<ExamResult>) -> Unit
    ) {
        db.collection("quizzes").document(quizId).collection("answerKeys").get()
            .addOnSuccessListener { snap ->
                val key = snap.documents.associate { it.id to it.getString("correct").orEmpty() }
                val correct = answers.count { (qid, ans) -> key[qid] == ans }
                val score = if (totalQuestions > 0)
                    ((correct * 10.0 / totalQuestions) * 100).roundToInt() / 100.0 else 0.0
                val ref = results.document()
                val result = ExamResult(
                    id = ref.id, quizId = quizId, quizTitle = quizTitle,
                    userId = user.uid, userName = user.name.ifBlank { user.email },
                    score = score, correctCount = correct, totalQuestions = totalQuestions,
                    timeUp = timeUp, submittedAt = System.currentTimeMillis(), answers = answers
                )
                ref.set(result.toMap())
                    .addOnSuccessListener { callback(Result.success(result)) }
                    .addOnFailureListener { callback(Result.failure(it)) }
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getById(id: String, callback: (Result<ExamResult>) -> Unit) {
        results.document(id).get()
            .addOnSuccessListener { d ->
                if (d.exists()) callback(Result.success(ExamResult.fromDoc(d)))
                else callback(Result.failure(IllegalStateException("Không tìm thấy kết quả")))
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    /** Sắp xếp phía client (mới nhất trước) để không cần tạo composite index. */
    fun getByUser(userId: String, callback: (Result<List<ExamResult>>) -> Unit) {
        results.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { s ->
                callback(Result.success(s.documents.map(ExamResult::fromDoc).sortedByDescending { it.submittedAt }))
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getByQuiz(quizId: String, callback: (Result<List<ExamResult>>) -> Unit) {
        results.whereEqualTo("quizId", quizId).get()
            .addOnSuccessListener { s ->
                callback(Result.success(s.documents.map(ExamResult::fromDoc).sortedByDescending { it.score }))
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }
}
