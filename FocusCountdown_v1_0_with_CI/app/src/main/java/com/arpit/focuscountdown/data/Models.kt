package com.arpit.focuscountdown.data

data class Goal(
    val id: Long,
    val title: String,
    val startMillis: Long,
    val targetMillis: Long,
    val icon: String = "🎯",
    val archived: Boolean = false
)

data class JournalEntry(
    val id: Long,
    val dateMillis: Long,
    val text: String,
    val studyHours: Float = 0f,
    val mood: String = "🙂",
    val achievement: String = ""
)

data class DailyProgress(
    val dateKey: String,
    val studyHours: Float,
    val tasks: Int,
    val note: String = ""
)
