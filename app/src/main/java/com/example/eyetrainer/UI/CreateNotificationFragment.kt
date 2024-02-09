package com.example.eyetrainer.UI

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.AlarmReceiver
import com.example.eyetrainer.databinding.FragmentCreateNotificationBinding
import java.util.*


class CreateNotificationFragment : Fragment() {

    private val calendar = Calendar.getInstance()
    private lateinit var alarmManager: AlarmManager
    private lateinit var binding: FragmentCreateNotificationBinding
    private lateinit var alarmIntent: PendingIntent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("messageKey", "Hello")
            PendingIntent.getBroadcast(
                context, /*Идентификационный номер, должен быть уникальным (можно заменить id)*/
                0, intent, PendingIntent.FLAG_IMMUTABLE
            )
        }

        binding.save.setOnClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
            calendar.set(Calendar.MINUTE, binding.timePicker.minute)
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmIntent
            )
            requireView().findNavController()
                .navigate(R.id.action_createNotificationFragment_to_reminderFragment)
        }

        binding.back.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_createNotificationFragment_to_reminderFragment)
        }
    }

}