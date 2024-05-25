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
    private var moods: List<MoodEntry>,
    private val onDelete: (MoodEntry) -> Unit,
    private val onClick: (MoodEntry) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    private val statusMap: MutableMap<Int, String> = mutableMapOf()

    //Этот метод вызывается RecyclerView тогда, когда ему нужно создать новый элемент для отображения.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mood_item, parent, false)
        return MoodViewHolder(view)
    }

    //Он устанавливает текстовые значения для элементов списка на основе данных, содержащихся в объекте MoodEntry
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position] //получаем объект MoodEntry из списка данных по текущей позиции.
        //устанавливаем текст для соответствующих текстовых полей
        holder.dateTextView.text = mood.date
        holder.timeTextView.text = mood.time
        holder.moodTextView.text = mood.mood
        holder.descriptionTextView.text = mood.description

        val status = statusMap[mood.id] //statusMap - хранит статусы записей, где ключом является идентификатор записи, а значением - строка статуса
        if (!status.isNullOrEmpty()) {
            holder.statusTextView.text = status
            holder.statusTextView.visibility = View.VISIBLE
        } else {
            holder.statusTextView.visibility = View.GONE
        }

        //обработчик события при удалении
        holder.deleteButton.setOnClickListener {
            onDelete(mood) // Вызов метода onDelete при нажатии на кнопку удаления
        }

        // обработчик при нажатии на список
        holder.itemView.setOnClickListener {
            onClick(mood) // нажатие на элемент списка с текущей заметкой
        }
    }

    override fun getItemCount(): Int = moods.size //количество элементов в отображении

    //обновляет ВЕСЬ список настроений в адаптере новыми данными.
    fun updateMoods(newMoods: List<MoodEntry>) {
        moods = newMoods
        notifyDataSetChanged()
    }

    // Обновляет статус одной конкретной записи настроения по её идентификатору
    fun updateStatus(id: Int, status: String) {
        statusMap[id] = status
        notifyDataSetChanged()
    }

    // загружает статусы записей из файлов и обновляет их в адаптере
    fun updateStatuses(newStatusMap: Map<Int, String>) {
        statusMap.clear()
        statusMap.putAll(newStatusMap)
        notifyDataSetChanged()
    }

    //ссылки для отображения, которые будут отображаться в каждой отдельной строке списка RecyclerView
    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val moodTextView: TextView = itemView.findViewById(R.id.moodTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }
}
