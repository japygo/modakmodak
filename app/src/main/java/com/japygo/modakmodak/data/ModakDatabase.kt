package com.japygo.modakmodak.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.japygo.modakmodak.data.dao.InventoryDao
import com.japygo.modakmodak.data.dao.ShopDao
import com.japygo.modakmodak.data.dao.StudyLogDao
import com.japygo.modakmodak.data.dao.TimerPresetDao
import com.japygo.modakmodak.data.dao.UserDao
import com.japygo.modakmodak.data.entity.Inventory
import com.japygo.modakmodak.data.entity.ShopItem
import com.japygo.modakmodak.data.entity.StudyLog
import com.japygo.modakmodak.data.entity.TimerPreset
import com.japygo.modakmodak.data.entity.User

@Database(
    entities = [User::class, StudyLog::class, Inventory::class, ShopItem::class, TimerPreset::class],
    version = 6,
    exportSchema = false,
)
abstract class ModakDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun shopDao(): ShopDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun timerPresetDao(): TimerPresetDao

    companion object {
        @Volatile
        private var Instance: ModakDatabase? = null

        fun getDatabase(context: Context): ModakDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ModakDatabase::class.java, "modak_database")
                    .fallbackToDestructiveMigration() // For development
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
