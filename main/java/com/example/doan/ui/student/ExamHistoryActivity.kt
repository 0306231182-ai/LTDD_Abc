package com.example.doan.ui.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.doan.data.repository.AuthRepository
import com.example.doan.data.repository.ResultRepository
import com.example.doan.ui.UiKit

class ExamHistoryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = UiKit.screen(this, "Lịch sử thi")
        val uid = AuthRepository().currentUid
        if (uid == null) { UiKit.toast(this, "Chưa đăng nhập"); finish(); return }

        ResultRepository().getByUser(uid) { r ->
            r.onSuccess { list ->
                if (list.isEmpty()) root.addView(UiKit.text(this, "Bạn chưa làm bài thi nào."))
                list.forEach { res ->
                    val card = UiKit.card(this)
                    card.addView(UiKit.text(this, res.quizTitle, 17f, bold = true))
                    card.addView(UiKit.text(this,
                        "Điểm ${UiKit.formatScore(res.score)}/10 • đúng ${res.correctCount}/${res.totalQuestions}", 15f))
                    card.addView(UiKit.text(this, UiKit.formatDate(res.submittedAt), 13f))
                    card.setOnClickListener {
                        startActivity(Intent(this, ResultActivity::class.java)
                            .putExtra(ResultActivity.EXTRA_RESULT_ID, res.id))
                    }
                    root.addView(card)
                }
            }.onFailure { UiKit.toast(this, "Không tải được lịch sử: ${it.message}") }
        }
    }
}
