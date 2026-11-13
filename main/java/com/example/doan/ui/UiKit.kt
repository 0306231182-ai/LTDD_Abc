package com.example.doan.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doan.data.model.Quiz
import com.example.doan.data.model.User
import com.example.doan.ui.auth.WelcomeActivity
import com.example.doan.ui.student.ExamIntroActivity
import com.example.doan.ui.student.StudentDashboardActivity
import com.example.doan.ui.teacher.TeacherDashboardActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Dựng nhanh các màn hình không có layout XML. */
object UiKit {

    fun dp(ctx: Context, v: Int): Int = (v * ctx.resources.displayMetrics.density).toInt()

    /** Màn hình cuộn dọc có tiêu đề; trả về LinearLayout để thêm nội dung. */
    fun screen(activity: Activity, title: String): LinearLayout {
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), dp(activity, 16), dp(activity, 16), dp(activity, 24))
        }
        val scroll = ScrollView(activity).apply { isFillViewport = true; addView(root) }
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        activity.setContentView(scroll)
        root.addView(text(activity, title, 24f, bold = true).apply {
            setPadding(0, 0, 0, dp(activity, 12))
        })
        return root
    }

    fun text(ctx: Context, s: String, size: Float = 16f, bold: Boolean = false, color: Int? = null) =
        TextView(ctx).apply {
            text = s
            textSize = size
            if (bold) setTypeface(typeface, Typeface.BOLD)
            if (color != null) setTextColor(color)
        }

    fun edit(ctx: Context, hint: String, number: Boolean = false, multiline: Boolean = false) =
        EditText(ctx).apply {
            this.hint = hint
            inputType = when {
                number -> InputType.TYPE_CLASS_NUMBER
                multiline -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                else -> InputType.TYPE_CLASS_TEXT
            }
            if (multiline) minLines = 2
        }

    fun button(ctx: Context, label: String, onClick: () -> Unit) = Button(ctx).apply {
        text = label
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(ctx, 8) }
    }

    /** Thẻ bo góc, nền xám trong suốt nên dùng được cả giao diện sáng/tối. */
    fun card(ctx: Context) = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12))
        background = GradientDrawable().apply {
            cornerRadius = dp(ctx, 12).toFloat()
            setColor(Color.argb(30, 128, 128, 128))
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(ctx, 10) }
    }

    fun toast(ctx: Context, msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()

    fun formatDate(ms: Long): String =
        if (ms <= 0L) "" else SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ms))

    fun formatScore(score: Double): String = String.format(Locale.US, "%.2f", score)

    val GREEN = Color.rgb(24, 134, 75)
    val RED = Color.rgb(190, 40, 40)
}

object Navigator {
    /** Vào trang chủ theo vai trò và xoá back stack. */
    fun home(activity: Activity, user: User) {
        val target = if (user.isTeacher) TeacherDashboardActivity::class.java
        else StudentDashboardActivity::class.java
        activity.startActivity(
            Intent(activity, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }

    fun welcome(activity: Activity) {
        activity.startActivity(
            Intent(activity, WelcomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }

    fun openExamIntro(activity: Activity, quiz: Quiz) {
        activity.startActivity(Intent(activity, ExamIntroActivity::class.java).apply {
            putExtra("QUIZ_ID", quiz.id)
            putExtra("QUIZ_TITLE", quiz.title)
            putExtra("QUIZ_SUBJECT", quiz.subject)
            putExtra("QUIZ_DURATION", quiz.durationMinutes)
            putExtra("QUIZ_QUESTIONS", quiz.questionCount)
        })
    }
}
