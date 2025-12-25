package com.japygo.modakmodak.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.japygo.modakmodak.data.entity.StudyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyLogDao {
    @Insert
    suspend fun insertLog(log: StudyLog)

    @Query("DELETE FROM study_log")
    suspend fun deleteAll()

    @Query("SELECT * FROM study_log ORDER BY date DESC")
    fun getAllLogs(): Flow<List<StudyLog>>

    @Query("SELECT * FROM study_log WHERE date >= :start AND date <= :end ORDER BY date DESC")
    fun getLogsByDateRange(start: Long, end: Long): Flow<List<StudyLog>>
}
