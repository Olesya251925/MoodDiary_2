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

        moodViewModel = ViewModelProvider(this).get(MoodViewModel::class.java)

        val startDateEditText = binding.editTextStartDate
        val endDateEditText = binding.editTextEndDate
        val buttonShowChart = binding.buttonShowChart

        // Обработчики кликов для полей ввода дат
        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }

        // Обработчик клика для кнопки "Показать график"
        buttonShowChart.setOnClickListener {
            val startDate = dateFormat.parse(startDateEditText.text.toString())
            val endDate = dateFormat.parse(endDateEditText.text.toString())
            if (startDate != null && endDate != null) {
                filterAndDisplayMoods(startDate, endDate)
            }
        }

        // Получаем данные из базы данных и строим график
        moodViewModel.allMoods.observe(this, { moodEntries: List<MoodEntry>? ->
            moodEntries?.let {
                val chartView = binding.canvas as ChartView
                chartView.setMoodEntries(it)
            }
        })
    }

    //календарь
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editText.setText(dateFormat.format(calendar.time))
        }

        // Устанавливаем локаль на русский
        val locale = Locale("ru")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    private fun filterAndDisplayMoods(startDate: Date, endDate: Date) {
        val filteredMoods = moodViewModel.allMoods.value?.filter {
            val date = dateFormat.parse(it.date)
            date != null && date >= startDate && date <= endDate
        }
        val chartView = binding.canvas as ChartView
        chartView.setMoodEntries(filteredMoods ?: listOf())
    }
}
