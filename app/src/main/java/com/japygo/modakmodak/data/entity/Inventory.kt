package com.japygo.modakmodak.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class Inventory(
    @PrimaryKey val itemId: String,
    val quantity: Int
)
