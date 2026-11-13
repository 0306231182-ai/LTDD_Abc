package com.example.doan.data.model

data class Quiz (
    val title: String,
    val subject: String,
    val durationMinutes: Int,
    val questionCount: Int,
    val status: String,
    val id: String = "",
    val ownerId: String = ""
) {
    companion object {
        const val LIVE = "Live"
        const val UPCOMING = "Upcoming"
        const val COMPLETED = "Completed"
        val STATUSES = listOf(UPCOMING, LIVE, COMPLETED)
    }
}
