package com.example.doan.ui.teacher

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.doan.data.model.Question
import com.example.doan.data.model.Quiz
import com.example.doan.data.repository.QuizRepository
import com.example.doan.ui.UiKit

/** Danh sách câu hỏi của một đề + biểu mẫu thêm câu mới. */
class QuestionFormActivity : Activity() {

    private val quizRepo = QuizRepository()
    private lateinit var quizId: String
    private lateinit var listBox: LinearLayout
    private var questionCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quizId = intent.getStringExtra("QUIZ_ID").orEmpty()
        if (quizId.isBlank()) { UiKit.toast(this, "Thiếu mã đề"); finish(); return }
        val root = UiKit.screen(this, "Câu hỏi: " + intent.getStringExtra("QUIZ_TITLE").orEmpty())

        listBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(listBox)

        root.addView(UiKit.text(this, "Thêm câu hỏi mới", 18f, bold = true).apply {
            setPadding(0, UiKit.dp(this@QuestionFormActivity, 16), 0, 0)
        })
        val etText = UiKit.edit(this, "Nội dung câu hỏi", multiline = true)
        val etA = UiKit.edit(this, "Đáp án A")
        val etB = UiKit.edit(this, "Đáp án B")
        val etC = UiKit.edit(this, "Đáp án C")
        val etD = UiKit.edit(this, "Đáp án D")
        listOf(etText, etA, etB, etC, etD).forEach { root.addView(it) }

        root.addView(UiKit.text(this, "Đáp án đúng", 14f))
        val ids = listOf("A", "B", "C", "D").associateWith { View.generateViewId() }
        val rg = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            ids.forEach { (letter, id) -> addView(RadioButton(this@QuestionFormActivity).apply { text = letter; this.id = id }) }
            check(ids.getValue("A"))
        }
        root.addView(rg)

        root.addView(UiKit.button(this, "Thêm câu hỏi") {
            val fields = listOf(etText, etA, etB, etC, etD)
            val empty = fields.firstOrNull { it.text.toString().isBlank() }
            if (empty != null) { empty.error = "Không được để trống"; empty.requestFocus(); return@button }
            val correct = ids.entries.first { it.value == rg.checkedRadioButtonId }.key
            val q = Question(
                text = etText.text.toString().trim(),
                optionA = etA.text.toString().trim(), optionB = etB.text.toString().trim(),
                optionC = etC.text.toString().trim(), optionD = etD.text.toString().trim(),
                correctAnswer = correct
            )
            quizRepo.addQuestion(quizId, q) { r ->
                r.onSuccess {
                    fields.forEach { it.setText("") }
                    rg.check(ids.getValue("A"))
                    etText.requestFocus()
                    loadQuestions()
                }.onFailure { UiKit.toast(this, "Thêm thất bại: ${it.message}") }
            }
        })

        root.addView(UiKit.button(this, "Hoàn tất & mở đề cho sinh viên thi") {
            if (questionCount == 0) {
                UiKit.toast(this, "Hãy thêm ít nhất một câu hỏi trước")
                return@button
            }
            quizRepo.setStatus(quizId, Quiz.LIVE) { r ->
                r.onSuccess { UiKit.toast(this, "Đề đã mở (Live)"); finish() }
                 .onFailure { UiKit.toast(this, "Lỗi: ${it.message}") }
            }
        })
        loadQuestions()
    }

    private fun loadQuestions() {
        quizRepo.getQuestions(quizId, withKey = true) { r ->
            r.onSuccess { render(it) }
             .onFailure { UiKit.toast(this, "Không tải được câu hỏi: ${it.message}") }
        }
    }

    private fun render(questions: List<Question>) {
        questionCount = questions.size
        listBox.removeAllViews()
        if (questions.isEmpty()) listBox.addView(UiKit.text(this, "Chưa có câu hỏi nào."))
        questions.forEachIndexed { i, q ->
            val card = UiKit.card(this)
            card.addView(UiKit.text(this, "Câu ${i + 1}: ${q.text}", bold = true))
            card.addView(UiKit.text(this, "A. ${q.optionA}\nB. ${q.optionB}\nC. ${q.optionC}\nD. ${q.optionD}", 15f))
            card.addView(UiKit.text(this, "Đáp án đúng: ${q.correctAnswer}", 15f, bold = true, color = UiKit.GREEN))
            card.addView(UiKit.button(this, "Xoá câu này") {
                quizRepo.deleteQuestion(quizId, q.id) { r ->
                    r.onSuccess { loadQuestions() }.onFailure { UiKit.toast(this, "Xoá thất bại: ${it.message}") }
                }
            })
            listBox.addView(card)
        }
    }
}
