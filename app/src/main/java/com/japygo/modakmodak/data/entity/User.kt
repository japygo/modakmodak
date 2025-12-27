package com.japygo.modakmodak.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val uid: String = "me", // Single user app
    val currentCoin: Int = 0,
    val totalStudyTime: Long = 0, // In seconds
    val streakDays: Int = 0,
    val lastLoginDate: Long = 0, // Timestamp
    val fireExp: Int = 0,
    val fireLevel: Int = 1,
    val fireColor: String = "#FFFF9500"
)
