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

    private var moodEntries: List<MoodEntry> = emptyList()

    fun setMoodEntries(entries: List<MoodEntry>) {
        moodEntries = entries
        invalidate() // Перерисовываем View
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint()
        val dashedPaint = Paint()

        val padding = 100f // Padding для графика
        val startX = padding // начальная позиция X для вертикальной линии
        val startY = padding // начальная позиция Y для горизонтальной линии
        val endY = height - padding // конечная позиция Y для вертикальной линии
        val endX = width - padding // конечная позиция X для горизонтальной линии

        val lineHeight = endY - startY // высота вертикальной линии
        val lineWidth = endX - startX // ширина горизонтальной линии

        val intervalY = lineHeight / 100 // интервал на одно настроение (100 настроений)
        val intervalX = lineWidth / (moodEntries.size + 1) // интервал на одну запись настроения

        // Сортируем список moodEntries по дате
        moodEntries = moodEntries.sortedBy { parseDate(it.date) }

        // Рисуем вертикальную линию (ось Y)
        paint.strokeWidth = 5f
        paint.color = context.getColor(R.color.black)
        canvas.drawLine(startX, startY, startX, endY, paint)

        // Рисуем горизонтальную линию (ось X)
        canvas.drawLine(startX, endY, endX, endY, paint)

        // Настройка шрифта
        paint.textSize = 40f

        // Рисуем метки на вертикальной оси
        val moodLevels = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
        paint.textAlign = Paint.Align.RIGHT
        moodLevels.forEach { moodLevel ->
            val moodY = endY - (moodLevel * intervalY)
            canvas.drawText(moodLevel.toString(), startX - 10, moodY + 10, paint)
        }

        // Настройка краски для линий
        paint.strokeWidth = 3f
        dashedPaint.strokeWidth = 2f
        dashedPaint.style = Paint.Style.STROKE
        dashedPaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        dashedPaint.color = context.getColor(R.color.red)

        var previousX: Float? = null
        var previousY: Float? = null

        // Рисуем оси настроения
        moodEntries.forEachIndexed { index, moodEntry ->
            val moodValue = getMoodValue(moodEntry.mood)
            val moodY = endY - (moodValue * intervalY) // координата Y для точки на графике
            val moodX = startX + (index + 1) * intervalX // координата X для точки на графике

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
            val date = parseDate(moodEntry.date) // Парсим дату из строки
            if (date != null) {
                val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
                val dateText = dateFormat.format(date)
                canvas.drawText(dateText, moodX, endY + 40, paint)
            }
        }
    }

    private fun parseDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

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
            else -> 0 // Если настроение неизвестно, возвращаем 0
        }
    }
}
