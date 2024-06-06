package com.example.mooddiary

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class ChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Список записей настроений
    private var moodEntries: List<MoodEntry> = emptyList()
    // Формат для отображения дат
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Метод для установки записей настроений и сортировки по дате и времени
    fun setMoodEntries(entries: List<MoodEntry>) {
        moodEntries = entries.sortedWith(compareBy(
            { parseDate(it.date) },
            { it.time }
        ))
        invalidate() // Перерисовываем View
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Инициализация объектов Paint для рисования
        val paint = Paint()
        val dashedPaint = Paint()

        // Параметры для отступов и границ графика
        val padding = 100f
        val startX = padding // Начальная позиция X для вертикальной линии
        val startY = padding // Начальная позиция Y для горизонтальной линии
        val endY = height - padding // Конечная позиция Y для вертикальной линии
        val endX = width - padding // Конечная позиция X для горизонтальной линии

        val lineHeight = endY - startY // Высота вертикальной линии
        val lineWidth = endX - startX // Ширина горизонтальной линии

        val intervalY = lineHeight / 100 // Интервал на одно настроение (100 уровней настроения)
        val intervalX = lineWidth / (moodEntries.size + 1) // Интервал на одну запись настроения

        // Рисуем вертикальную линию (ось Y)
        paint.strokeWidth = 5f
        paint.color = context.getColor(R.color.black)
        canvas.drawLine(startX, startY, startX, endY, paint)

        // Рисуем горизонтальную линию (ось X)
        canvas.drawLine(startX, endY, endX, endY, paint)

        // Настройка шрифта для текста
        paint.textSize = 40f

        // Рисуем метки на вертикальной оси (уровни настроения)
        val moodLevels = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
        paint.textAlign = Paint.Align.RIGHT
        moodLevels.forEach { moodLevel ->
            val moodY = endY - (moodLevel * intervalY)
            canvas.drawText(moodLevel.toString(), startX - 10, moodY + 10, paint)
        }

        // Настройка краски для линий
        paint.strokeWidth = 3f
        dashedPaint.strokeWidth = 2f //ширина линии
        dashedPaint.style = Paint.Style.STROKE //контур без заливки
        dashedPaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) //пунктирная линия (длина и отпступы между отрезками)
        dashedPaint.color = context.getColor(R.color.red)

        var previousX: Float? = null //для хранения X-координаты предыдущей точки на график
        var previousY: Float? = null //для хранения Y-координаты предыдущей точки на графике

        // Рисуем точки и линии на графике
        moodEntries.forEachIndexed { index, moodEntry ->
            val moodValue = getMoodValue(moodEntry.mood)
            val moodY = endY - (moodValue * intervalY) // Координата Y для точки на графике
            val moodX = startX + (index + 1) * intervalX // Координата X для точки на графике

            // Рисуем точку на графике
            paint.color = context.getColor(R.color.blue)
            canvas.drawCircle(moodX, moodY, 15f, paint)

            // Рисуем линию между точками
            if (previousX != null && previousY != null) {
                canvas.drawLine(previousX!!, previousY!!, moodX, moodY, paint)
            }
            previousX = moodX
            previousY = moodY

            // Рисуем пунктирную линию от даты к точке
            canvas.drawLine(moodX, endY, moodX, moodY, dashedPaint)

            // Над точкой выводим название настроения
            paint.color = context.getColor(R.color.black)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(moodEntry.mood, moodX, moodY - 20, paint)

            // Выводим дату на пересечении
            val date = parseDate(moodEntry.date)
            if (date != null) {
                val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
                val dateText = dateFormat.format(date)
                canvas.drawText(dateText, moodX, endY + 40, paint)
            }
        }
    }

    // Метод для парсинга даты из строки
    private fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    // Метод для получения значения настроения по названию настроения
    private fun getMoodValue(mood: String): Int {
        return when (mood) {
            "Грусть" -> 5
            "Стресс" -> 10
            "Обиженное" -> 20
            "Раздражительное" -> 30
            "Скучное" -> 40
            "Гневное" -> 50
            "Виноватое" -> 60
            "Смущенное" -> 65
            "Любопытство" -> 70
            "Расслабленное" -> 75
            "Удивление" -> 80
            "Умиротворенное" -> 85
            "Вдохновленное" -> 90
            "Радость" -> 95
            "Восторженное" -> 100
            else -> 0
        }
    }
}
