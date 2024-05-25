package com.example.mooddiary

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MoodDao {
    @Insert
    suspend fun insert(moodEntry: MoodEntry)

    @Update
    suspend fun update(moodEntry: MoodEntry)

    @Delete
    suspend fun delete(moodEntry: MoodEntry)

    @Query("SELECT * FROM mood_entries ORDER BY entry_date DESC, entry_time DESC")
    fun getAll(): LiveData<List<MoodEntry>>
}
