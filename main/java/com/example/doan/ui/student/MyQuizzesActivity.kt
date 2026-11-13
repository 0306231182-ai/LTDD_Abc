package com.example.doan.ui.student

import android.app.Activity
import android.os.Bundle
import com.example.doan.data.model.Quiz
import com.example.doan.data.repository.AuthRepository
import com.example.doan.data.repository.QuizRepository
import com.example.doan.data.repository.ResultRepository
import com.example.doan.ui.Navigator
import com.example.doan.ui.UiKit

/** Các đề đang mở (Live) mà sinh viên chưa làm. */
class MyQuizzesActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = UiKit.screen(this, "Đề chưa làm")
        val uid = AuthRepository().currentUid
        if (uid == null) { UiKit.toast(this, "Chưa đăng nhập"); finish(); return }

        ResultRepository().getByUser(uid) { resultsR ->
            val taken = resultsR.getOrNull()?.map { it.quizId }?.toSet().orEmpty()
            QuizRepository().getQuizzes { quizzesR ->
                quizzesR.onSuccess { quizzes ->
                    val todo = quizzes.filter { it.status == Quiz.LIVE && it.id !in taken }
                    if (todo.isEmpty()) root.addView(UiKit.text(this, "Không còn đề nào cần làm."))
                    todo.forEach { q ->
                        val card = UiKit.card(this)
                        card.addView(UiKit.text(this, q.title, 17f, bold = true))
                        card.addView(UiKit.text(this, "${q.subject} • ${q.durationMinutes} phút • ${q.questionCount} câu", 15f))
                        card.setOnClickListener { Navigator.openExamIntro(this, q) }
                        root.addView(card)
                    }
                }.onFailure { UiKit.toast(this, "Không tải được đề thi: ${it.message}") }
            }
        }
    }
}
