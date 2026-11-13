package com.example.doan.ui.teacher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.doan.data.model.Quiz
import com.example.doan.data.repository.AuthRepository
import com.example.doan.data.repository.QuizRepository
import com.example.doan.ui.UiKit

/** Tạo mới hoặc sửa thông tin đề (nhận QUIZ_ID để sửa). */
class QuizFormActivity : Activity() {

    private val quizRepo = QuizRepository()
    private var existing: Quiz? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quizId = intent.getStringExtra("QUIZ_ID").orEmpty()
        val root = UiKit.screen(this, if (quizId.isBlank()) "Tạo đề thi" else "Sửa đề thi")

        val etTitle = UiKit.edit(this, "Tên đề thi")
        val etSubject = UiKit.edit(this, "Môn học")
        val etDuration = UiKit.edit(this, "Thời gian làm bài (phút)", number = true)
        val spStatus = Spinner(this).apply {
            adapter = ArrayAdapter(this@QuizFormActivity, android.R.layout.simple_spinner_dropdown_item, Quiz.STATUSES)
        }
        listOf(etTitle, etSubject, etDuration).forEach { root.addView(it) }
        root.addView(UiKit.text(this, "Trạng thái", 14f))
        root.addView(spStatus)

        if (quizId.isNotBlank()) {
            quizRepo.getQuiz(quizId) { r ->
                r.onSuccess { q ->
                    existing = q
                    etTitle.setText(q.title)
                    etSubject.setText(q.subject)
                    etDuration.setText(q.durationMinutes.toString())
                    spStatus.setSelection(Quiz.STATUSES.indexOf(q.status).coerceAtLeast(0))
                }.onFailure { UiKit.toast(this, "Không tải được đề: ${it.message}"); finish() }
            }
        }

        root.addView(UiKit.button(this, "Lưu") {
            val title = etTitle.text.toString().trim()
            val subject = etSubject.text.toString().trim()
            val duration = etDuration.text.toString().toIntOrNull() ?: 0
            val status = Quiz.STATUSES[spStatus.selectedItemPosition]
            val questionCount = existing?.questionCount ?: 0

            when {
                title.isEmpty() -> { etTitle.error = "Nhập tên đề"; etTitle.requestFocus(); return@button }
                subject.isEmpty() -> { etSubject.error = "Nhập môn học"; etSubject.requestFocus(); return@button }
                duration <= 0 -> { etDuration.error = "Thời gian phải lớn hơn 0"; etDuration.requestFocus(); return@button }
                status == Quiz.LIVE && questionCount == 0 -> {
                    UiKit.toast(this, "Đề chưa có câu hỏi nên chưa thể mở. Hãy lưu ở trạng thái Upcoming rồi thêm câu hỏi.")
                    return@button
                }
            }
            val uid = AuthRepository().currentUid ?: run { UiKit.toast(this, "Chưa đăng nhập"); return@button }
            val quiz = Quiz(title, subject, duration, questionCount, status, quizId)
            quizRepo.saveQuiz(quiz, uid) { r ->
                r.onSuccess { newId ->
                    UiKit.toast(this, "Đã lưu đề thi")
                    if (quizId.isBlank()) {
                        // Đề mới: chuyển ngay sang thêm câu hỏi.
                        startActivity(Intent(this, QuestionFormActivity::class.java)
                            .putExtra("QUIZ_ID", newId).putExtra("QUIZ_TITLE", title))
                    }
                    finish()
                }
                 .onFailure { UiKit.toast(this, "Lưu thất bại: ${it.message}") }
            }
        })
    }
}
