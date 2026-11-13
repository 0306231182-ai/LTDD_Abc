package com.example.doan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.doan.ui.auth.SplashActivity

/**
 * Màn hình mẫu Compose cũ đã bỏ. Nếu manifest vẫn đặt MainActivity làm launcher
 * thì nó chuyển ngay sang SplashActivity (nơi quyết định đăng nhập hay vào thẳng trang chủ).
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}
