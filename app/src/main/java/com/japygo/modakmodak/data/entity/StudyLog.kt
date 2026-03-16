package com.japygo.modakmodak.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_log")
data class StudyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Timestamp
    val durationSeconds: Int,
    val isSuccess: Boolean,
    val earnedCoin: Int,
    val sessionType: String, // "focus" or "break"
    val tag: String? = null, // e.g. "#Coding"
    val isHardcoreMode: Boolean = false
)
