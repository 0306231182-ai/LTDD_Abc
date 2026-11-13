package com.example.doan.ui.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.doan.R

class ExamIntroActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_intro)

        val quizId = intent.getStringExtra("QUIZ_ID") ?: ""
        val title = intent.getStringExtra("QUIZ_TITLE") ?: ""
        val subject = intent.getStringExtra("QUIZ_SUBJECT") ?: ""
        val duration = intent.getIntExtra("QUIZ_DURATION", 0)
        val questionCount = intent.getIntExtra("QUIZ_QUESTIONS", 0)

        findViewById<TextView>(R.id.tvExamTitle).text = title
        findViewById<TextView>(R.id.tvSubject).text = "Môn học: $subject"
        findViewById<TextView>(R.id.tvDuration).text = "Thời gian: $duration phút"
        findViewById<TextView>(R.id.tvQuestionCount).text = "Số câu hỏi: $questionCount câu"

        findViewById<Button>(R.id.btnStartExam).setOnClickListener {
            if (quizId.isBlank()) {
                Toast.makeText(this, "Không tìm thấy mã đề thi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, DoExamActivity::class.java).apply {
                putExtra(DoExamActivity.EXTRA_QUIZ_ID, quizId)
            })
            finish() // tránh quay lại trang giới thiệu khi đang thi
        }
    }
}
