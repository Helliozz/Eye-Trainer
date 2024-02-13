package com.example.eyetrainer.Adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eyetrainer.Data.Constants.APP_NOTIFICATION_DAYS_LIST
import com.example.eyetrainer.Model.NotificationData
import com.example.eyetrainer.databinding.ItemRemindActiveBinding
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
class NotificationRecyclerViewAdapter(
    val editNotification: (NotificationData) -> Unit,
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

                var dateString = ""
                for (i in 0..6) {
                    if ((item.days and Math.pow(2.0, i.toDouble()).toInt()) != 0) {
                        dateString += "${APP_NOTIFICATION_DAYS_LIST[i]} "
                    } else {
                        Log.d("NotificationSample", "Not this day: day = ${APP_NOTIFICATION_DAYS_LIST[i]}, checkSum = ${item.days}")
                    }
                }
                date.text = dateString

                delete.setOnClickListener {
                    recycler.deactivateNotification(item)
                    recycler.deleteNotification(item)
                    binding.root.visibility = View.GONE
                    binding.root.isClickable = false
                }

                currentState.isChecked = item.isEnabled
                currentState.setOnCheckedChangeListener{v,checked ->
                    item.isEnabled = checked
                    if (checked) {
                        recycler.activateNotification(item)
                    } else {
                        recycler.deactivateNotification(item)
                    }
                }

                root.setOnClickListener {
                    recycler.editNotification(item)
                }
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
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: NotificationData, newItem: NotificationData
        ): Boolean {
            return oldItem.time == newItem.time && oldItem.days == newItem.days
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}