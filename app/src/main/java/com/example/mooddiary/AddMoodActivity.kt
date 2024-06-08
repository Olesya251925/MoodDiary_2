package com.example.mooddiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddMoodActivity : AppCompatActivity() {

    private val moodViewModel: MoodViewModel by viewModels()
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_mood)

        editTextDate = findViewById(R.id.editTextDate)
        editTextTime = findViewById(R.id.editTextTime)
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

        editTextDate.setOnClickListener {
            showDatePickerDialog()
        }

        editTextTime.setOnClickListener {
            showTimePickerDialog()
        }

        buttonSave.setOnClickListener {
            val date = editTextDate.text.toString()
            val time = editTextTime.text.toString()
            val mood = spinnerMood.selectedItem.toString()
            val description = editTextDescription.text.toString()

            if (isNewNote) {
                val newMoodEntry = MoodEntry(date = date, time = time, mood = mood, description = description, status = " ")
                moodViewModel.insert(newMoodEntry)
                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()
            } else {
                val updatedMoodEntry = MoodEntry(
                    id = id,
                    date = date,
                    time = time,
                    mood = mood,
                    description = description,
                    status = "Обновлено: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())}"
                )
                moodViewModel.update(updatedMoodEntry)
                Toast.makeText(this, "Данные изменены", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        // Устанавливаем локаль на русский
        val locale = Locale("ru")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year)
                editTextDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editTextTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
}
