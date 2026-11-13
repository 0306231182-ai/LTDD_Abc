package com.example.doan.ui.student

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doan.R
import com.example.doan.adapter.QuizAdapter
import com.example.doan.data.model.Quiz
import com.example.doan.data.repository.AuthRepository
import com.example.doan.data.repository.QuizRepository
import com.example.doan.ui.Navigator
import com.example.doan.ui.UiKit

/** Trang chủ sinh viên: danh sách đề thi thật từ Firestore. */
class StudentDashboardActivity : Activity() {

    private val quizRepo = QuizRepository()
    private lateinit var rvQuizzes: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)
        rvQuizzes = findViewById(R.id.rvQuizzes)
        rvQuizzes.layoutManager = LinearLayoutManager(this)
        rvQuizzes.adapter = QuizAdapter(emptyList()) { }
        addBottomBar()
    }

    /** Thanh nút cố định ở đáy màn hình, không phụ thuộc layout XML. */
    private fun addBottomBar() {
        val content = findViewById<ViewGroup>(android.R.id.content)
        val pad = UiKit.dp(this, 8)
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            elevation = UiKit.dp(this@StudentDashboardActivity, 8).toFloat()
            setPadding(pad, pad, pad, pad)
        }
        fun addButton(label: String, onClick: () -> Unit) {
            bar.addView(Button(this).apply {
                text = label
                setOnClickListener { onClick() }
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { marginStart = pad / 2; marginEnd = pad / 2 }
            })
        }
        addButton("Lịch sử") { startActivity(Intent(this, ExamHistoryActivity::class.java)) }
        addButton("Hồ sơ") { startActivity(Intent(this, ProfileActivity::class.java)) }
        addButton("Đăng xuất") { confirmLogout() }

        content.addView(bar, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))

        // Chừa chỗ để thanh nút không che các thẻ đề thi và không nằm dưới thanh điều hướng hệ thống.
        bar.post {
            val navBottom = ViewCompat.getRootWindowInsets(bar)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0
            bar.setPadding(pad, pad, pad, pad + navBottom)
            rvQuizzes.clipToPadding = false
            rvQuizzes.setPadding(
                rvQuizzes.paddingLeft, rvQuizzes.paddingTop, rvQuizzes.paddingRight,
                rvQuizzes.paddingBottom + UiKit.dp(this, 72) + navBottom
            )
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Đăng xuất?")
            .setMessage("Bạn sẽ cần đăng nhập lại để tiếp tục.")
            .setPositiveButton("Đăng xuất") { _, _ ->
                AuthRepository().logout()
                Navigator.welcome(this)
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadQuizzes()
    }

    private fun loadQuizzes() {
        quizRepo.getQuizzes { result ->
            result
                .onSuccess { quizzes ->
                    rvQuizzes.adapter = QuizAdapter(quizzes) { quiz -> onQuizClick(quiz) }
                    if (quizzes.isEmpty()) {
                        Toast.makeText(this, "Chưa có đề thi nào", Toast.LENGTH_SHORT).show()
                    }
                }
                .onFailure { e ->
                    Toast.makeText(this, "Không tải được đề thi: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun onQuizClick(quiz: Quiz) {
        when (quiz.status) {
            Quiz.LIVE -> {
                if (quiz.id.isBlank()) Toast.makeText(this, "Thiếu mã đề thi", Toast.LENGTH_SHORT).show()
                else Navigator.openExamIntro(this, quiz)
            }
            Quiz.COMPLETED -> startActivity(Intent(this, ExamHistoryActivity::class.java))
            else -> Toast.makeText(this, "Đề chưa mở", Toast.LENGTH_SHORT).show()
        }
    }

    // Menu trên ActionBar (cần theme có ActionBar) để vào các màn hình còn lại.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Đề chưa làm")
        menu.add(0, 2, 1, "Lịch sử thi")
        menu.add(0, 3, 2, "Hồ sơ")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val target = when (item.itemId) {
            1 -> MyQuizzesActivity::class.java
            2 -> ExamHistoryActivity::class.java
            3 -> ProfileActivity::class.java
            else -> return super.onOptionsItemSelected(item)
        }
        startActivity(Intent(this, target))
        return true
    }
}
