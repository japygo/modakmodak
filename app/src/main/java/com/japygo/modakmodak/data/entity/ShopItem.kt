package com.japygo.modakmodak.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "shop_item")
data class ShopItem(
    @PrimaryKey val id: String,
    val name: String,
    val price: Int,
    val type: String, // "EXP", "COSMETIC"
    val value: Int, // e.g., EXP amount
    val description: String,
    val imageUrl: String? = null
)
