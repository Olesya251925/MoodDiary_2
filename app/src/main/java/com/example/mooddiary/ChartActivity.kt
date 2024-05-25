package com.example.mooddiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mooddiary.databinding.ActivityChartBinding


class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    private lateinit var moodViewModel: MoodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moodViewModel = ViewModelProvider(this).get(MoodViewModel::class.java)

        // Получаем данные из базы данных и строим график
        moodViewModel.allMoods.observe(this, { moodEntries: List<MoodEntry>? ->
            moodEntries?.let {
                val chartView = binding.canvas as ChartView // Здесь мы кастим View к ChartView
                chartView.setMoodEntries(it)
            }
        })
    }
}
