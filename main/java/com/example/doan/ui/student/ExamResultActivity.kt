package com.example.doan.ui.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.doan.R
import com.example.doan.ui.UiKit

/** Màn hình ngay sau khi nộp bài: hiển thị điểm vừa chấm. */
class ExamResultActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_result)

        val total = intent.getIntExtra("totalQuestions", 0)
        val answered = intent.getIntExtra("answeredCount", 0)
        val correct = intent.getIntExtra("correctCount", 0)
        val score = intent.getDoubleExtra("score", 0.0)
        val timeUp = intent.getBooleanExtra("timeUp", false)
        val resultId = intent.getStringExtra("resultId").orEmpty()

        findViewById<TextView>(R.id.tvResultHeading).text =
            if (timeUp) "Đã hết giờ làm bài" else "Đã nộp bài"
        findViewById<TextView>(R.id.tvResultSummary).text =
            "Đúng $correct / $total câu (đã trả lời $answered câu)."
        findViewById<TextView>(R.id.tvResultNotice).apply {
            text = "Điểm: ${UiKit.formatScore(score)} / 10\nChạm vào đây để xem chi tiết"
            if (resultId.isNotBlank()) {
                setOnClickListener {
                    startActivity(Intent(this@ExamResultActivity, ResultActivity::class.java)
                        .putExtra(ResultActivity.EXTRA_RESULT_ID, resultId))
                }
            }
        }
        findViewById<Button>(R.id.btnBackHome).setOnClickListener { finish() }
    }
}
