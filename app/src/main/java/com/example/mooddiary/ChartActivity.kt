package com.example.mooddiary

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mooddiary.databinding.ActivityChartBinding
import java.text.SimpleDateFormat
import java.util.*

class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    private lateinit var moodViewModel: MoodViewModel
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем ViewModel для взаимодействия с данными настроений
        moodViewModel = ViewModelProvider(this).get(MoodViewModel::class.java)

        // Получаем ссылки на элементы интерфейса
        val startDateEditText = binding.editTextStartDate
        val endDateEditText = binding.editTextEndDate
        val buttonShowChart = binding.buttonShowChart

        // Обработчики кликов для полей ввода дат, показывающие диалог выбора даты
        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }

        // Обработчик клика для кнопки "Показать график"
        buttonShowChart.setOnClickListener {
            // Получаем и парсим даты из полей ввода
            val startDate = dateFormat.parse(startDateEditText.text.toString())
            val endDate = dateFormat.parse(endDateEditText.text.toString())
            // Если даты корректны, фильтруем и отображаем данные настроений
            if (startDate != null && endDate != null) {
                filterAndDisplayMoods(startDate, endDate)
            }
        }

        // Наблюдаем за данными настроений и отображаем их на графике
        // Получаем данные из базы данных и строим график
        moodViewModel.allMoods.observe(this, { moodEntries: List<MoodEntry>? ->
            moodEntries?.let {
                // Приводим canvas к типу ChartView и устанавливаем данные настроений
                val chartView = binding.canvas as ChartView
                chartView.setMoodEntries(it)
            }
        })
    }

    // Показывает диалог выбора даты и устанавливает выбранную дату в EditText
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            // Устанавливаем выбранную дату в календарь
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            // Форматируем дату и устанавливаем в EditText
            editText.setText(dateFormat.format(calendar.time))
        }

        // Устанавливаем локаль на русский
        val locale = Locale("ru")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        // Показываем диалог выбора даты с текущей датой по умолчанию
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Фильтрует и отображает записи настроений в заданном диапазоне дат
    private fun filterAndDisplayMoods(startDate: Date, endDate: Date) {
        // Фильтруем данные настроений по диапазону дат
        val filteredMoods = moodViewModel.allMoods.value?.filter {
            // Преобразуем строку даты из MoodEntry в объект Date
            val date = dateFormat.parse(it.date)
            // Проверяем, находится ли дата в заданном диапазоне
            date != null && date >= startDate && date <= endDate
        }

        // Приводим canvas к типу ChartView и устанавливаем отфильтрованные данные настроений
        val chartView = binding.canvas as ChartView
        // Если filteredMoods не является null, устанавливаем его как данные для отображения. В противном случае, устанавливаем пустой список.
        chartView.setMoodEntries(filteredMoods ?: listOf())
    }

}
