package com.example.doan.ui.teacher

import android.app.Activity
import android.os.Bundle
import com.example.doan.data.repository.ResultRepository
import com.example.doan.ui.UiKit

/** Bảng điểm của một đề, xếp từ cao xuống thấp. */
class TeacherResultsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quizId = intent.getStringExtra("QUIZ_ID").orEmpty()
        val root = UiKit.screen(this, "Kết quả: " + intent.getStringExtra("QUIZ_TITLE").orEmpty())
        if (quizId.isBlank()) { UiKit.toast(this, "Thiếu mã đề"); finish(); return }

        ResultRepository().getByQuiz(quizId) { r ->
            r.onSuccess { list ->
                if (list.isEmpty()) {
                    root.addView(UiKit.text(this, "Chưa có sinh viên nào nộp bài."))
                    return@onSuccess
                }
                val avg = list.map { it.score }.average()
                root.addView(UiKit.text(this,
                    "${list.size} lượt nộp • điểm trung bình ${UiKit.formatScore(avg)} • " +
                    "cao nhất ${UiKit.formatScore(list.first().score)}", 15f, bold = true))
                list.forEachIndexed { i, res ->
                    val card = UiKit.card(this)
                    card.addView(UiKit.text(this, "${i + 1}. ${res.userName.ifBlank { "(ẩn danh)" }}", 17f, bold = true))
                    card.addView(UiKit.text(this,
                        "Điểm ${UiKit.formatScore(res.score)}/10 • đúng ${res.correctCount}/${res.totalQuestions}" +
                        if (res.timeUp) " • hết giờ" else "", 15f))
                    card.addView(UiKit.text(this, UiKit.formatDate(res.submittedAt), 13f))
                    root.addView(card)
                }
            }.onFailure { UiKit.toast(this, "Không tải được kết quả: ${it.message}") }
        }
    }
}
