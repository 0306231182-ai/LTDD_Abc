package com.example.doan.ui.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.doan.data.repository.AuthRepository
import com.example.doan.ui.Navigator
import com.example.doan.ui.UiKit

class ProfileActivity : Activity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = UiKit.screen(this, "Hồ sơ")

        authRepo.getCurrentUser { r ->
            r.onSuccess { user ->
                root.addView(UiKit.text(this, user.name.ifBlank { "(chưa có tên)" }, 20f, bold = true))
                root.addView(UiKit.text(this, user.email))
                root.addView(UiKit.text(this, "Vai trò: " + if (user.isTeacher) "Giáo viên" else "Sinh viên"))
                if (!user.isTeacher) {
                    root.addView(UiKit.button(this, "Lịch sử thi") {
                        startActivity(Intent(this, ExamHistoryActivity::class.java))
                    })
                    root.addView(UiKit.button(this, "Đề chưa làm") {
                        startActivity(Intent(this, MyQuizzesActivity::class.java))
                    })
                }
            }.onFailure { UiKit.toast(this, "Không tải được hồ sơ: ${it.message}") }
        }

        root.addView(UiKit.button(this, "Đăng xuất") {
            authRepo.logout()
            Navigator.welcome(this)
        })
    }
}
