package com.example.doan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.R
import com.example.doan.data.repository.AuthRepository
import com.example.doan.ui.Navigator

class LoginActivity : AppCompatActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Đã đăng nhập từ trước thì vào thẳng trang chủ, không bắt đăng nhập lại.
        if (authRepo.currentUid != null) {
            authRepo.getCurrentUser { r ->
                r.onSuccess { user -> if (!isFinishing) Navigator.home(this, user) }
            }
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        // Firebase Auth tự ghi nhớ phiên đăng nhập nên cbRemember chỉ giữ để khớp layout.
        findViewById<CheckBox>(R.id.cbRemember)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoRegister = findViewById<TextView>(R.id.tvGoRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty()) {
                etEmail.error = "Please enter your email"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Invalid email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Please enter your password"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            authRepo.login(email, password) { result ->
                btnLogin.isEnabled = true
                result
                    .onSuccess { user -> Navigator.home(this, user) }
                    .onFailure { e ->
                        Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
