package com.example.doan.ui.auth

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.TextView
import com.example.doan.data.repository.AuthRepository
import com.example.doan.ui.Navigator

class SplashActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "Quiz App"
            textSize = 32f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        handler.postDelayed({ route() }, 1200L)
    }

    private fun route() {
        if (isFinishing) return
        val repo = AuthRepository()
        if (repo.currentUid == null) {
            Navigator.welcome(this)
            return
        }
        repo.getCurrentUser { result ->
            if (isFinishing) return@getCurrentUser
            result
                .onSuccess { Navigator.home(this, it) }
                .onFailure { Navigator.welcome(this) }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
