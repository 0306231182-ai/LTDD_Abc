package com.example.doan.ui.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.adapter.ExamQuestionAdapter
import com.example.doan.data.model.ExamQuestionUi
import com.example.doan.data.model.User
import com.example.doan.data.repository.AuthRepository
import com.example.doan.data.repository.ResultRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class DoExamActivity : Activity() {

    companion object {
        const val EXTRA_QUIZ_ID = "quizId"
        private const val STATE_DEADLINE = "deadlineMillis"
        private const val STATE_ANSWER_IDS = "answerIds"
        private const val STATE_ANSWER_VALUES = "answerValues"
    }

    private val db = FirebaseFirestore.getInstance()
    private val authRepo = AuthRepository()
    private val resultRepo = ResultRepository()
    private val selectedAnswers = mutableMapOf<String, String>()
    private var questions = emptyList<ExamQuestionUi>()
    private var timer: CountDownTimer? = null
    private var deadlineMillis: Long = 0L
    private var submitted = false
    private lateinit var quizId: String
    private lateinit var tvExamTitle: TextView
    private lateinit var tvTimer: TextView
    private lateinit var rvQuestions: RecyclerView
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_do_exam)

        tvExamTitle = findViewById(R.id.tvExamTitle)
        tvTimer = findViewById(R.id.tvTimer)
        rvQuestions = findViewById(R.id.rvExamQuestions)
        btnSubmit = findViewById(R.id.btnSubmitExam)
        rvQuestions.layoutManager = LinearLayoutManager(this)
        btnSubmit.isEnabled = false
        tvTimer.text = "Đang tải..."

        quizId = intent.getStringExtra(EXTRA_QUIZ_ID).orEmpty()
        if (quizId.isBlank()) {
            Toast.makeText(this, "Thiếu mã đề thi (quizId)", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        deadlineMillis = savedInstanceState?.getLong(STATE_DEADLINE) ?: 0L
        val ids = savedInstanceState?.getStringArrayList(STATE_ANSWER_IDS).orEmpty()
        val values = savedInstanceState?.getStringArrayList(STATE_ANSWER_VALUES).orEmpty()
        ids.zip(values).forEach { (id, value) -> selectedAnswers[id] = value }

        btnSubmit.setOnClickListener { submitExam(timeUp = false) }
        loadExamFromFirestore()
    }

    override fun onResume() {
        super.onResume()
        // Nếu người dùng trở lại từ nền, kiểm tra lại hạn nộp theo thời điểm hết giờ.
        if (questions.isNotEmpty() && deadlineMillis > 0L && !submitted) startCountdown()
    }

    private fun loadExamFromFirestore() {
        val quizRef = db.collection("quizzes").document(quizId)
        quizRef.get()
            .addOnSuccessListener { quiz ->
                if (!quiz.exists()) {
                    showErrorAndClose("Không tìm thấy đề thi")
                    return@addOnSuccessListener
                }
                if (quiz.getString("status") != "Live") {
                    showErrorAndClose("Đề thi chưa mở")
                    return@addOnSuccessListener
                }
                tvExamTitle.text = quiz.getString("title") ?: "Bài thi"
                val durationMinutes = quiz.getLong("durationMinutes") ?: 0L
                if (durationMinutes <= 0L) {
                    showErrorAndClose("Thời gian thi không hợp lệ")
                    return@addOnSuccessListener
                }

                quizRef.collection("questions").get()
                    .addOnSuccessListener { snapshot ->
                        questions = snapshot.documents
                            .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                            .mapNotNull { doc ->
                                val text = doc.getString("text")?.takeIf { it.isNotBlank() }
                                    ?: return@mapNotNull null
                                val a = doc.getString("optionA") ?: return@mapNotNull null
                                val b = doc.getString("optionB") ?: return@mapNotNull null
                                val c = doc.getString("optionC") ?: return@mapNotNull null
                                val d = doc.getString("optionD") ?: return@mapNotNull null
                                ExamQuestionUi(
                                    id = doc.id,
                                    order = doc.getLong("order") ?: 0L,
                                    text = text,
                                    optionA = a,
                                    optionB = b,
                                    optionC = c,
                                    optionD = d
                                )
                            }

                        if (questions.isEmpty()) {
                            showErrorAndClose("Đề chưa có câu hỏi hoặc dữ liệu chưa đúng")
                            return@addOnSuccessListener
                        }
                        rvQuestions.adapter = ExamQuestionAdapter(questions, selectedAnswers)
                        btnSubmit.isEnabled = true
                        if (deadlineMillis == 0L) {
                            deadlineMillis = System.currentTimeMillis() + durationMinutes * 60_000L
                        }
                        startCountdown()
                    }
                    .addOnFailureListener { e ->
                        showErrorAndClose("Không tải được câu hỏi: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showErrorAndClose("Không tải được đề thi: ${e.message}")
            }
    }

    private fun startCountdown() {
        timer?.cancel()
        val remaining = (deadlineMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        if (remaining == 0L) {
            submitExam(timeUp = true)
            return
        }
        updateTimerText(remaining)
        timer = object : CountDownTimer(remaining, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                updateTimerText(0L)
                submitExam(timeUp = true)
            }
        }.start()
    }

    private fun updateTimerText(millis: Long) {
        val totalSeconds = (millis + 999L) / 1_000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun submitExam(timeUp: Boolean) {
        if (submitted || questions.isEmpty()) return
        submitted = true
        btnSubmit.isEnabled = false
        timer?.cancel()

        authRepo.getCurrentUser { userResult ->
            val user = userResult.getOrElse {
                User(uid = authRepo.currentUid.orEmpty())
            }
            resultRepo.submit(
                quizId = quizId,
                quizTitle = tvExamTitle.text.toString(),
                user = user,
                answers = HashMap(selectedAnswers),
                totalQuestions = questions.size,
                timeUp = timeUp
            ) { result ->
                result
                    .onSuccess { r ->
                        startActivity(Intent(this, ExamResultActivity::class.java).apply {
                            putExtra(EXTRA_QUIZ_ID, quizId)
                            putExtra("resultId", r.id)
                            putExtra("totalQuestions", r.totalQuestions)
                            putExtra("answeredCount", selectedAnswers.size)
                            putExtra("correctCount", r.correctCount)
                            putExtra("score", r.score)
                            putExtra("timeUp", timeUp)
                        })
                        finish()
                    }
                    .onFailure { e ->
                        // Cho phép nộp lại nếu lỗi mạng/quyền.
                        submitted = false
                        btnSubmit.isEnabled = true
                        Toast.makeText(this, "Nộp bài thất bại: ${e.message}. Hãy thử lại.", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_DEADLINE, deadlineMillis)
        val entries = selectedAnswers.entries.toList()
        outState.putStringArrayList(
            STATE_ANSWER_IDS, ArrayList(entries.map { it.key })
        )
        outState.putStringArrayList(
            STATE_ANSWER_VALUES, ArrayList(entries.map { it.value })
        )
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
