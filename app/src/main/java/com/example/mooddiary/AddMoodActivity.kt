package com.example.mooddiary

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        var oldMoodEntry: MoodEntry? = null

        if (!isNewNote) {
            // Если это не новая запись (редактирование существующей).
            id = intent.getIntExtra("id", 0) // Из Intent извлекается ID записи настроения.
            editTextDate.setText(intent.getStringExtra("date"))
            editTextTime.setText(intent.getStringExtra("time"))
            editTextDescription.setText(intent.getStringExtra("description"))
            val mood = intent.getStringExtra("mood")
            val spinnerPosition = adapter.getPosition(mood)
            spinnerMood.setSelection(spinnerPosition)

            //Intent позволяет передать все необходимые данные из одной активности в другую, чтобы пользователь мог
           // редактировать существующую запись настроения, видя все текущие данные этой записи.
        }

        // Устанавливаем слушатель для кнопки "Сохранить".
        buttonSave.setOnClickListener {
            val date = editTextDate.text.toString()
            val time = editTextTime.text.toString()
            val mood = spinnerMood.selectedItem.toString()
            val description = editTextDescription.text.toString()

            //если создние
            if (isNewNote) {
                // Создаём новый объект MoodEntry с полученными данными.
                val newMoodEntry = MoodEntry(date = date, time = time, mood = mood, description = description, status = " ")
                //вставляем в бд
                moodViewModel.insert(newMoodEntry)
                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()
            } else {
                // Если это редактирование существующей записи.
                val updatedMoodEntry = MoodEntry(
                    id = id,
                    date = date,
                    time = time,
                    mood = mood,
                    description = description,
                    // Обновляем статус записи с текущей датой и временем.
                    status = "Обновлено:  ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format( Date() )}"
                )
                moodViewModel.update(updatedMoodEntry)
                Toast.makeText(this, "Данные изменены", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
