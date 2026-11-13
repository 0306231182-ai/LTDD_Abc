package com.example.doan.data.model

/** Model dùng riêng cho giao diện làm bài, không chứa đáp án đúng. */
data class ExamQuestionUi(
    val id: String,
    val order: Long,
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String
)
