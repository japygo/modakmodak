package com.japygo.modakmodak.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.japygo.modakmodak.data.entity.Inventory
import com.japygo.modakmodak.data.entity.ShopItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_item")
    fun getAllShopItems(): Flow<List<ShopItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopItems(items: List<ShopItem>)

    @Query("SELECT * FROM shop_item WHERE id = :id")
    suspend fun getShopItem(id: String): ShopItem?
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory")
    fun getAllInventory(): Flow<List<Inventory>>

    @Query("SELECT * FROM inventory WHERE itemId = :itemId")
    suspend fun getInventoryItem(itemId: String): Inventory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: Inventory)

    @Update
    suspend fun updateInventory(item: Inventory)

    @Query("DELETE FROM inventory")
    suspend fun deleteAll()
}
