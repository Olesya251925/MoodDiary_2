package com.example.mooddiary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date

@Entity(tableName = "mood_entries")

data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name = "entry_date") val date: String,
    @ColumnInfo(name = "entry_time") val time: String,
    @ColumnInfo(name = "entry_mood") val mood: String,
    @ColumnInfo(name = "entry_description") val description: String
)
