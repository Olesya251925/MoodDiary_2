package com.example.mooddiary

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class AddMoodActivity : AppCompatActivity() {

    private val moodViewModel: MoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_mood)

        val editTextDate: EditText = findViewById(R.id.editTextDate)
        val editTextTime: EditText = findViewById(R.id.editTextTime)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription)
        val spinnerMood: Spinner = findViewById(R.id.spinnerMood)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        val moods = arrayOf("Выберите настроение", "Радость", "Виноватое", "Восторженное", "Любопытство", "Грусть", "Стресс", "Раздражительное", "Расслабленное", "Скучное", "Вдохновленное", "Смущенное", "Умиротворенное", "Обиженное", "Удивление", "Гневное")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moods)
        spinnerMood.adapter = adapter

        val modeType = intent.getStringExtra("modeType") ?: "Add"
        val isNewNote = modeType == "Add"
        var id = 0

        if (!isNewNote) {
            id = intent.getIntExtra("id", 0)
            editTextDate.setText(intent.getStringExtra("date"))
            editTextTime.setText(intent.getStringExtra("time"))
            editTextDescription.setText(intent.getStringExtra("description"))
            val mood = intent.getStringExtra("mood")
            val spinnerPosition = adapter.getPosition(mood)
            spinnerMood.setSelection(spinnerPosition)
        }

        buttonSave.setOnClickListener {
            val date = editTextDate.text.toString()
            val time = editTextTime.text.toString()
            val mood = spinnerMood.selectedItem.toString()
            val description = editTextDescription.text.toString()

            if (date.isNotEmpty() && time.isNotEmpty() && mood.isNotEmpty() && description.isNotEmpty()) {
                if (isNewNote) { //новая заметка
                    val newMoodEntry = MoodEntry(date = date, time = time, mood = mood, description = description)
                    moodViewModel.insert(newMoodEntry)

                } else { //обновление заметки
                    val updatedMoodEntry = MoodEntry(id = id, date = date, time = time, mood = mood, description = description)
                    moodViewModel.update(updatedMoodEntry)

                    //в интент добавляется информация об успешном обновлении
                    val resultIntent = Intent()
                    resultIntent.putExtra("isUpdated", true)
                    resultIntent.putExtra("id", id)
                    setResult(RESULT_OK, resultIntent)
                }
                finish()
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

