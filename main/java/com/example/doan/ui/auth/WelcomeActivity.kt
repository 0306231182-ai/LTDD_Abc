package com.example.doan.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.example.doan.data.repository.AuthRepository
import com.example.doan.ui.Navigator
import com.example.doan.ui.UiKit

class WelcomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepo = AuthRepository()
        if (authRepo.currentUid != null) {
            authRepo.getCurrentUser { r ->
                r.onSuccess { user -> if (!isFinishing) Navigator.home(this, user) }
            }
        }
        val root = UiKit.screen(this, "Chào mừng đến Quiz App")
        root.gravity = Gravity.CENTER_VERTICAL
        root.addView(UiKit.text(this, "Làm bài thi trắc nghiệm trực tuyến, xem điểm và lịch sử thi ngay trên điện thoại."))
        root.addView(UiKit.button(this, "Đăng nhập") {
            startActivity(Intent(this, LoginActivity::class.java))
        })
        root.addView(UiKit.button(this, "Tạo tài khoản") {
            startActivity(Intent(this, RegisterActivity::class.java))
        })
    }
}
