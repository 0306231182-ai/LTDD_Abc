package com.example.doan.ui.teacher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.example.doan.data.model.Quiz
import com.example.doan.data.repository.AuthRepository
import com.example.doan.data.repository.QuizRepository
import com.example.doan.ui.Navigator
import com.example.doan.ui.UiKit

class TeacherDashboardActivity : Activity() {

    private val authRepo = AuthRepository()
    private val quizRepo = QuizRepository()
    private lateinit var list: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = UiKit.screen(this, "Đề thi của tôi")
        root.addView(UiKit.button(this, "+ Tạo đề mới") {
            startActivity(Intent(this, QuizFormActivity::class.java))
        })
        root.addView(UiKit.button(this, "Đăng xuất") {
            authRepo.logout()
            Navigator.welcome(this)
        })
        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, UiKit.dp(this@TeacherDashboardActivity, 16), 0, 0)
        }
        root.addView(list)
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        val uid = authRepo.currentUid ?: return
        quizRepo.getQuizzesByOwner(uid) { r ->
            r.onSuccess { render(it) }
             .onFailure { UiKit.toast(this, "Không tải được đề thi: ${it.message}") }
        }
    }

    private fun render(quizzes: List<Quiz>) {
        list.removeAllViews()
        if (quizzes.isEmpty()) list.addView(UiKit.text(this, "Bạn chưa tạo đề nào. Nhấn \"Tạo đề mới\"."))
        quizzes.forEach { q ->
            val card = UiKit.card(this)
            card.addView(UiKit.text(this, q.title, 17f, bold = true))
            card.addView(UiKit.text(this, "${q.subject} • ${q.durationMinutes} phút • ${q.questionCount} câu", 15f))
            card.addView(UiKit.text(this, q.status, 14f, bold = true, color = when (q.status) {
                Quiz.LIVE -> UiKit.GREEN
                Quiz.COMPLETED -> android.graphics.Color.rgb(75, 105, 140)
                else -> android.graphics.Color.rgb(190, 133, 20)
            }))
            card.setOnClickListener { showActions(q) }
            list.addView(card)
        }
    }

    private fun showActions(q: Quiz) {
        AlertDialog.Builder(this)
            .setTitle(q.title)
            .setItems(arrayOf("Sửa thông tin đề", "Quản lý câu hỏi", "Xem kết quả", "Đổi trạng thái", "Xoá đề")) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, QuizFormActivity::class.java).putExtra("QUIZ_ID", q.id))
                    1 -> startActivity(Intent(this, QuestionFormActivity::class.java)
                        .putExtra("QUIZ_ID", q.id).putExtra("QUIZ_TITLE", q.title))
                    2 -> startActivity(Intent(this, TeacherResultsActivity::class.java)
                        .putExtra("QUIZ_ID", q.id).putExtra("QUIZ_TITLE", q.title))
                    3 -> changeStatus(q)
                    4 -> confirmDelete(q)
                }
            }.show()
    }

    private fun changeStatus(q: Quiz) {
        if (q.questionCount == 0) {
            UiKit.toast(this, "Hãy thêm câu hỏi trước khi mở đề")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Trạng thái")
            .setItems(Quiz.STATUSES.toTypedArray()) { _, i ->
                quizRepo.setStatus(q.id, Quiz.STATUSES[i]) { r ->
                    r.onSuccess { load() }.onFailure { UiKit.toast(this, "Lỗi: ${it.message}") }
                }
            }.show()
    }

    private fun confirmDelete(q: Quiz) {
        AlertDialog.Builder(this)
            .setTitle("Xoá đề \"${q.title}\"?")
            .setMessage("Toàn bộ câu hỏi của đề sẽ bị xoá. Kết quả đã nộp vẫn được giữ.")
            .setPositiveButton("Xoá") { _, _ ->
                quizRepo.deleteQuiz(q.id) { r ->
                    r.onSuccess { load() }.onFailure { UiKit.toast(this, "Lỗi: ${it.message}") }
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }
}
