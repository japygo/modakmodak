package com.japygo.modakmodak.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.japygo.modakmodak.data.entity.TimerPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerPresetDao {
    @Query("SELECT * FROM timer_preset ORDER BY id ASC")
    fun getAllPresets(): Flow<List<TimerPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TimerPreset)

    @Delete
    suspend fun deletePreset(preset: TimerPreset)

    @Update
    suspend fun updatePreset(preset: TimerPreset)

    @Query("UPDATE timer_preset SET isSelected = 0")
    suspend fun deselectAll()

    @Transaction
    suspend fun selectPreset(presetId: Long) {
        deselectAll()
        // We'll handle selecting the specific ID in the Repository or ViewModel for simplicity
    }

    @Query("UPDATE timer_preset SET isSelected = 1 WHERE id = :id")
    suspend fun setPresetSelected(id: Long)

    @Query("DELETE FROM timer_preset")
    suspend fun deleteAll()
}
