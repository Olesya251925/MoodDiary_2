package com.example.mooddiary

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val moodViewModel: MoodViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var adapter: MoodAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: ImageButton
    private lateinit var buttonFilter: ImageButton
    // Инициализация кнопки для отображения графика
    private lateinit var buttonShowChart: FloatingActionButton

    private var isAscending = true

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
        const val CHANNEL_ID = "MoodDiaryChannel"
        private const val CHANNEL_NAME = "Уведомления для Дневника настроения"
        private const val CHANNEL_DESCRIPTION = "Канал для уведомлений приложения Дневник настроения"
    }

    // Обновляет статус записи и сохраняет его в файл после завершения операции добавления или редактирования записи
    private val addMoodLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val id = data?.getIntExtra("id", -1) ?: -1
            val isUpdated = data?.getBooleanExtra("isUpdated", false) ?: false
            if (isUpdated && id != -1) {
                val status = "Обновлено: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())}"
                adapter.updateStatus(id, status) // Вызываем метод у адаптера для обновления статуса заметки в списке
                saveStatusToFile(id, status) // Вызов сохранения в файл
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.plus)
        editTextSearch = findViewById(R.id.editTextSearch)
        buttonSearch = findViewById(R.id.buttonSearch)
        buttonFilter = findViewById(R.id.buttonFilter)

        // Найдем кнопку в макете и инициализируем ее
        buttonShowChart = findViewById(R.id.buttonShowChart)

        adapter = MoodAdapter(listOf(), { mood ->
            moodViewModel.delete(mood) // Вызываем метод для удаления
        }, { mood ->
            onMoodClick(mood) // Вызываем при нажатии на заметку
        }, this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Наблюдатель для обновления списка (если произошло изменение)
        moodViewModel.allMoods.observe(this, { moods ->
            moods?.let { adapter.updateMoods(it) }
        })

        // Для создания заметки
        fab.setOnClickListener {
            val intent = Intent(this, AddMoodActivity::class.java)
            addMoodLauncher.launch(intent)
        }

        buttonSearch.setOnClickListener {
            filterMoods(editTextSearch.text.toString())
        }

        buttonFilter.setOnClickListener {
            toggleFilter()
        }

        buttonShowChart.setOnClickListener {
            val intent = Intent(this, ChartActivity::class.java)
            startActivity(intent)
        }

        // Слушатель текста в поиске
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    resetFilter() // Если текст пустой
                }
            }
        })

        loadStatusesFromFile()

        // Создаем канал уведомлений
        createNotificationChannel()

        // Запускаем уведомления
        startNotifications()
    }

    // Обработчик нажатия на элемент в списке (для редактирования)
    private fun onMoodClick(mood: MoodEntry) {
        val intent = Intent(this, AddMoodActivity::class.java)
        intent.putExtra("modeType", "Edit")
        intent.putExtra("date", mood.date)
        intent.putExtra("time", mood.time)
        intent.putExtra("description", mood.description)
        intent.putExtra("mood", mood.mood)
        intent.putExtra("id", mood.id)
        addMoodLauncher.launch(intent)
    }

    // Для поиска записей в списке
    private fun filterMoods(query: String) {
        val filteredMoods = moodViewModel.allMoods.value?.filter {
            it.mood.contains(query, ignoreCase = true)
        }
        adapter.updateMoods(filteredMoods ?: listOf())
    }

    // Для очистки в поиске
    private fun resetFilter() {
        val allMoods = moodViewModel.allMoods.value
        adapter.updateMoods(allMoods ?: listOf())
    }

    // Переключатель сортировки
    private fun toggleFilter() {
        val sortedMoods = moodViewModel.allMoods.value?.sortedWith(compareBy(
            { parseDate(it.date) },
            { it.time }
        ))

        if (isAscending) {
            adapter.updateMoods(sortedMoods ?: listOf())
        } else {
            adapter.updateMoods(sortedMoods?.reversed() ?: listOf())
        }
        isAscending = !isAscending
    }

    // Строку в дату
    private fun parseDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    // Сохраняем статус в ресурсы файлов, используя идентификатор записи в качестве имени файла
    private fun saveStatusToFile(id: Int, status: String) {
        val fileName = "status_$id.txt"
        val fileContents = status
        FileOutputStream(File(filesDir, fileName)).use {
            it.write(fileContents.toByteArray())
        }
    }

    private fun loadStatusesFromFile() {
        val statusMap: MutableMap<Int, String> = mutableMapOf() // Пустая карта для хранения статусов
        val filesDir = filesDir
        for (file in filesDir.listFiles()) {
            if (file.name.startsWith("status_")) {
                val id = file.name.substringAfter("status_").substringBefore(".txt").toInt()
                val status = file.readText()
                statusMap[id] = status
            }
        }
        adapter.updateStatuses(statusMap)
    }

    // Запускаем уведомления
    private fun startNotifications() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            createAndSendNotification()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
        }
    }

    // Обработка результата запроса разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createAndSendNotification()
                } else {
                    // Обрабатываем отказ в разрешении
                }
            }
        }
    }

    // Создаем и отправляем уведомление
    private fun createAndSendNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications)
            .setContentTitle("Не забудьте сегодня добавить ваше настроение")
            .setContentText("Приглашаем вас заполнить ваше настроение сегодня")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, notification)
    }

    // Создание канала уведомлений
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
