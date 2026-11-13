package com.example.doan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.doan.R
import com.example.doan.data.model.User
import com.example.doan.data.repository.AuthRepository

class RegisterActivity : AppCompatActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoLogin = findViewById<TextView>(R.id.tvGoLogin)

        // Ô chọn loại tài khoản: chèn ngay phía trên nút Register.
        val rbStudent = RadioButton(this).apply { id = View.generateViewId(); text = "Sinh viên"; isChecked = true }
        val rbTeacher = RadioButton(this).apply { id = View.generateViewId(); text = "Giáo viên" }
        val rgRole = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            addView(rbStudent)
            addView(rbTeacher)
        }
        val parent = btnRegister.parent
        val roleInserted = parent is LinearLayout
        if (parent is LinearLayout) {
            val index = parent.indexOfChild(btnRegister)
            val label = TextView(this).apply {
                text = "Loại tài khoản"
                textSize = 14f
                setPadding(0, (12 * resources.displayMetrics.density).toInt(), 0, 0)
            }
            parent.addView(label, index)
            parent.addView(rgRole, index + 1)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty()) {
                etName.error = "Please enter your name"
                etName.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password must contain at least 6 characters"
                etPassword.requestFocus()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            if (roleInserted) {
                val role = if (rbTeacher.isChecked) User.ROLE_TEACHER else User.ROLE_STUDENT
                doRegister(btnRegister, name, email, password, role)
            } else {
                // Layout gốc không phải LinearLayout nên không chèn được ô chọn: hỏi bằng hộp thoại.
                AlertDialog.Builder(this)
                    .setTitle("Loại tài khoản")
                    .setItems(arrayOf("Sinh viên", "Giáo viên")) { _, which ->
                        val role = if (which == 1) User.ROLE_TEACHER else User.ROLE_STUDENT
                        doRegister(btnRegister, name, email, password, role)
                    }
                    .show()
            }
        }

        tvGoLogin.setOnClickListener { finish() }
    }

    private fun doRegister(btn: Button, name: String, email: String, password: String, role: String) {
        btn.isEnabled = false
        authRepo.register(name, email, password, role) { result ->
            btn.isEnabled = true
            result
                .onSuccess {
                    authRepo.logout() // buộc đăng nhập lại như luồng cũ
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .onFailure { e ->
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
