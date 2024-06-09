package com.example.mooddiary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MoodRepository
    val allMoods: LiveData<List<MoodEntry>> //представляет собой список всех записей настроений

    init {
        // Получаем DAO для работы с базой данных и создаем репозиторий
        val moodDao = MoodDatabase.getDatabase(application).moodDao()
        repository = MoodRepository(moodDao)

        // Получаем список всех заметок из репозитория
        allMoods = repository.allMoods
    }
    fun insert(moodEntry: MoodEntry) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(moodEntry)
    }
    fun update(moodEntry: MoodEntry) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(moodEntry)
    }
    fun delete(moodEntry: MoodEntry) = viewModelScope.launch(Dispatchers.IO){
        repository.delete(moodEntry)
    }

}
