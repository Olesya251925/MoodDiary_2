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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var buttonShowChart: FloatingActionButton

    private var isAscending = true

    // метод отвечает за обработку результата, возвращаемого из активности AddMoodActivity
    private val addMoodLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val id = data?.getIntExtra("id", -1) ?: -1
            val isUpdated = data?.getBooleanExtra("isUpdated", false) ?: false
            if (isUpdated && id != -1) {
                val updatedMood = adapter.getMoodById(id)?.copy()
                if (updatedMood != null) {
                    moodViewModel.update(updatedMood)
                }
            }
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            createNotificationChannel()
            createAndSendNotification()
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
        buttonShowChart = findViewById(R.id.buttonShowChart)

        adapter = MoodAdapter(listOf(), { mood ->
            moodViewModel.delete(mood) // удаляем запись
        }, { mood ->
            onMoodClick(mood) // // Открываем экран редактирования
        }, this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //слушатель для обновления -  обновляем адаптер с новыми данными настроений
        moodViewModel.allMoods.observe(this, { moods ->
            moods?.let { adapter.updateMoods(it) }
        })

        //кнопка добавления нового настроения
        fab.setOnClickListener {
            val intent = Intent(this, AddMoodActivity::class.java)
            addMoodLauncher.launch(intent)
        }

        //обработчик по поиску
        buttonSearch.setOnClickListener {
            filterMoods(editTextSearch.text.toString()) // // Фильтрация списка настроений.
        }

        //сортировка
        buttonFilter.setOnClickListener {
            toggleFilter()
        }

        //кнопка графика
        buttonShowChart.setOnClickListener {
            showChart()
        }

        checkAndStartNotifications()

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
    }

    //редактирование заметки
    private fun onMoodClick(mood: MoodEntry) {
        val intent = Intent(this, AddMoodActivity::class.java).apply {
            // Добавление данных настроения в намерение для передачи в AddMoodActivity.
            putExtra("modeType", "Edit") // Указание режима редактирования.
            putExtra("id", mood.id)
            putExtra("date", mood.date)
            putExtra("time", mood.time)
            putExtra("description", mood.description)
            putExtra("mood", mood.mood)
        }
        // Запуск активности AddMoodActivity с ожиданием результата.
        addMoodLauncher.launch(intent)
    }

    // Сбрасывает фильтр настроений, отображая все записи.
    private fun resetFilter() {
        val allMoods = moodViewModel.allMoods.value // Получаем текущий список настроений из LiveData в ViewModel.
        adapter.updateMoods(allMoods ?: listOf()) // Обновляем адаптер полным списком настроений
    }

    //посиск по названию
    private fun filterMoods(query: String) {
        val filteredMoods = moodViewModel.allMoods.value?.filter { //получаем весь список настроений
            it.mood.contains(query, ignoreCase = true) // Фильтруем список настроений, проверяя, содержит ли настроение строку запроса, игнорируя регистр.
        }
        adapter.updateMoods(filteredMoods ?: listOf()) // Обновляем адаптер отфильтрованным списком
    }

    // Переключение сортировки.
    private fun toggleFilter() {
        val sortedMoods = moodViewModel.allMoods.value?.sortedWith(compareBy(
            { parseDate(it.date) }, // Сначала сортируем по дате.
            { it.time } // Затем сортируем по времени.
        ))

        if (isAscending) { // Проверяем текущий порядок сортировки.
            adapter.updateMoods(sortedMoods ?: listOf()) // Если порядок восходящий, обновляем адаптер отсортированным списком.
        } else {
            adapter.updateMoods(sortedMoods?.reversed() ?: listOf()) // Если порядок нисходящий, обновляем адаптер перевёрнутым отсортированным списком.
        }
        isAscending = !isAscending // Переключаем флаг isAscending.
    }


    // Строку в дату.
    private fun parseDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) // Создаём форматтер для преобразования строки в дату.
        return try {
            dateFormat.parse(dateString) // Преобразуем строку в дату.
        } catch (e: Exception) {
            null // Возвращаем null, если преобразование не удалось.
        }
    }


    private fun showChart() {
        val intent = Intent(this, ChartActivity::class.java)
        startActivity(intent)
    }

    private fun checkAndStartNotifications() {
        // Проверяем, предоставлено ли разрешение на отправку уведомлений.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Если разрешение не предоставлено, запускаем процесс запроса разрешения.
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Если разрешение уже предоставлено, создаем канал уведомлений и отправляем уведомление.
            createNotificationChannel()
            createAndSendNotification()
        }
    }

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

        val notification = NotificationCompat.Builder(this, "MoodDiaryChannel")
            .setSmallIcon(R.drawable.not)
            .setContentTitle("Дневник настроения")
            .setContentText("Добавьте какое настроение у вас сегодня :) ")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        // Проверяем версию ОС, чтобы убедиться, что мы находимся на Android 8.0 (API уровень 26) или выше.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Создаём канал уведомлений.
            val channel = NotificationChannel(
                "MoodDiaryChannel", // Идентификатор канала.
                "Уведомления для Дневника настроения", // Имя канала.
                NotificationManager.IMPORTANCE_DEFAULT // Важность канала.
            ).apply {
                description = "Канал для уведомлений приложения Дневник настроения" // Описание канала.
            }

            // Получаем NotificationManager и создаём канал уведомлений.
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
