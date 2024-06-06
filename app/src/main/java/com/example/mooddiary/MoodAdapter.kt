package com.example.mooddiary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


//Адаптер для RecyclerView, который отображает список настроений
class MoodAdapter(
    private var moods: List<MoodEntry>, //Список объектов настроений
    private val onDelete: (MoodEntry) -> Unit, // Функция удаления настроения
    private val onClick: (MoodEntry) -> Unit, // Функция клика по настроению
    private val context: Context
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    // Обновляем список настроений
    fun updateMoods(newMoods: List<MoodEntry>) {
        moods = newMoods // Обновляем список настроений
        notifyDataSetChanged() // Уведомляем адаптер о необходимости перерисовки
    }

    // Получаем заметку по ID
    fun getMoodById(id: Int): MoodEntry? {
        return moods.find { it.id == id }
    }

    private val statusMap: MutableMap<Int, String> = mutableMapOf()

    // Создаем ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mood_item, parent, false)
        return MoodViewHolder(view)
    }

    // Привязываем данные к ViewHolder
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]
        holder.dateTextView.text = mood.date
        holder.timeTextView.text = mood.time
        holder.moodTextView.text = mood.mood
        holder.descriptionTextView.text = mood.description
        holder.statusTextView.text = mood.status // Устанавливаем значение statusTextView

        // Извлекаем статус из statusMap по id настроения mood.id
        val status = statusMap[mood.id] ?: mood.status

        // Если статус не пустой и не null
        if (!status.isNullOrEmpty()) {
            // Устанавливаем текст статусного TextView равным значению status
            holder.statusTextView.text = status
            // Делаем статусный TextView видимым
            holder.statusTextView.visibility = View.VISIBLE
        } else {
            // Если статус пустой или null, скрываем статусный TextView
            holder.statusTextView.visibility = View.GONE
        }

        holder.deleteButton.setOnClickListener {
            onDelete(mood) // Устанавливаем слушатель для удаления настроения
        }

        holder.itemView.setOnClickListener {
            onClick(mood) // Устанавливаем слушатель для клика по настроению (onMoodClick)
        }
    }

    // Возвращаем количество элементов в списке
    override fun getItemCount(): Int = moods.size

    // Внутренний класс для хранения ссылок на виджеты элемента списка
    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView) // Текстовое поле для даты
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView) // Текстовое поле для времени
        val moodTextView: TextView = itemView.findViewById(R.id.moodTextView) // Текстовое поле для настроения
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView) // Текстовое поле для описания
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView) // Текстовое поле для статуса
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton) // Кнопка для удаления настроения
    }
}
