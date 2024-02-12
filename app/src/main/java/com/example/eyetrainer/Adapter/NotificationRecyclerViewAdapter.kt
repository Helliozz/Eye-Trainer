package com.example.eyetrainer.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyetrainer.Data.NotificationData
import com.example.eyetrainer.databinding.ItemRemindActiveBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationRecyclerViewAdapter(
    val deleteNotification: (NotificationData) -> Unit,
    val activateNotification: (NotificationData) -> Unit,
    val deactivateNotification: (NotificationData) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var binding: ItemRemindActiveBinding

    class ItemViewHolder(
        private val binding: ItemRemindActiveBinding,
        private val recycler: NotificationRecyclerViewAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setNotification(item: NotificationData) {
            binding.apply {
                val mDate = Calendar.getInstance()
                mDate.timeInMillis = item.time
                val timeCalendar = mDate.time
                val formatter = SimpleDateFormat("HH:mm")
                val timeText = formatter.format(timeCalendar)

                time.text = timeText
                date.text = item.id.toString()

                delete.setOnClickListener {
                    recycler.deactivateNotification(item)
                    recycler.deleteNotification(item)
                    binding.root.visibility = View.GONE
                    binding.root.isClickable = false
                }

                currentState.isChecked = item.isEnabled
                /*currentState.setOnCheckedChangeListener{v,checked ->
                    item.isEnabled = checked
                    if (checked) {
                        recycler.activateNotification(item)
                    } else {
                        recycler.deactivateNotification(item)
                    }
                }*/
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding =
            ItemRemindActiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).setNotification(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    private val differCallback = object : DiffUtil.ItemCallback<NotificationData>() {
        override fun areItemsTheSame(
            oldItem: NotificationData, newItem: NotificationData
        ): Boolean {
            return oldItem.time == newItem.time && oldItem.days == newItem.days
        }

        override fun areContentsTheSame(
            oldItem: NotificationData, newItem: NotificationData
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}