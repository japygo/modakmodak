package com.japygo.modakmodak.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_preset")
data class TimerPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String,
    val durationMinutes: Int,
    val isSelected: Boolean = false // New flag for the default preset
)
