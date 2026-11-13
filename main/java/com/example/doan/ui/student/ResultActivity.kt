package com.example.doan.ui.student

import android.app.Activity
import android.os.Bundle
import com.example.doan.data.model.ExamResult
import com.example.doan.data.model.Question
import com.example.doan.data.repository.QuizRepository
import com.example.doan.data.repository.ResultRepository
import com.example.doan.ui.UiKit

/** Chi tiết một lượt thi: điểm + từng câu đúng/sai. */
class ResultActivity : Activity() {

    companion object { const val EXTRA_RESULT_ID = "resultId" }

    private val resultRepo = ResultRepository()
    private val quizRepo = QuizRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = UiKit.screen(this, "Chi tiết kết quả")
        val id = intent.getStringExtra(EXTRA_RESULT_ID).orEmpty()
        if (id.isBlank()) { UiKit.toast(this, "Thiếu mã kết quả"); finish(); return }

        resultRepo.getById(id) { r ->
            r.onSuccess { result ->
                root.addView(UiKit.text(this, result.quizTitle, 20f, bold = true))
                root.addView(UiKit.text(this,
                    "Điểm: ${UiKit.formatScore(result.score)} / 10\n" +
                    "Đúng ${result.correctCount} / ${result.totalQuestions} câu\n" +
                    "Nộp lúc ${UiKit.formatDate(result.submittedAt)}" +
                    if (result.timeUp) " (hết giờ)" else ""))
                loadDetails(root, result)
            }.onFailure { UiKit.toast(this, "Không tải được kết quả: ${it.message}") }
        }
    }

    private fun loadDetails(root: android.widget.LinearLayout, result: ExamResult) {
        quizRepo.getQuestions(result.quizId, withKey = true) { r ->
            r.onSuccess { questions -> questions.forEachIndexed { i, q -> root.addView(questionCard(i, q, result)) } }
             .onFailure { UiKit.toast(this, "Không tải được chi tiết câu hỏi: ${it.message}") }
        }
    }

    private fun questionCard(index: Int, q: Question, result: ExamResult): android.widget.LinearLayout {
        val chosen = result.answers[q.id]
        val ok = chosen == q.correctAnswer
        val card = UiKit.card(this)
        card.addView(UiKit.text(this, "Câu ${index + 1}: ${q.text}", bold = true))
        val mine = if (chosen == null) "Bạn chưa trả lời" else "Bạn chọn: $chosen. ${q.option(chosen)}"
        card.addView(UiKit.text(this, mine, 15f, color = if (ok) UiKit.GREEN else UiKit.RED))
        if (!ok) card.addView(UiKit.text(this, "Đáp án đúng: ${q.correctAnswer}. ${q.option(q.correctAnswer)}", 15f, color = UiKit.GREEN))
        return card
    }
}
