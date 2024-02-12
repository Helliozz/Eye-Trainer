package com.example.eyetrainer.UI

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eyetrainer.Adapter.NotificationRecyclerViewAdapter
import com.example.eyetrainer.Data.NotificationData
import com.example.eyetrainer.NotificationsApplication
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.AlarmReceiver
import com.example.eyetrainer.ViewModel.NotificationViewModel
import com.example.eyetrainer.ViewModel.NotificationViewModelFactory
import com.example.eyetrainer.databinding.FragmentReminderBinding
import java.util.*

@Suppress("DEPRECATION")
class NotificationFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent

    private lateinit var deleteNotification: (NotificationData) -> Unit
    private lateinit var activateNotification: (NotificationData) -> Unit
    private lateinit var deactivateNotification: (NotificationData) -> Unit
    private lateinit var binding: FragmentReminderBinding

    private val recyclerViewAdapter by lazy { NotificationRecyclerViewAdapter(deleteNotification, activateNotification, deactivateNotification) }
    private val notificationViewModel: NotificationViewModel by activityViewModels {
        NotificationViewModelFactory(
            (activity!!.application as NotificationsApplication).repository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        deleteNotification = {
            notificationViewModel.delete(it)
        }

        activateNotification = {
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context, /*Идентификационный номер, должен быть уникальным (можно заменить id)*/
                    it.id, intent, PendingIntent.FLAG_IMMUTABLE
                )
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                it.time,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
        }

        deactivateNotification = {
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context, /*Идентификационный номер, должен быть уникальным (можно заменить id)*/
                    it.id, intent, PendingIntent.FLAG_IMMUTABLE
                )
            }
            alarmManager.cancel(alarmIntent)
        }

        notificationViewModel.notifications.observe(this) { notifications ->
            notifications.let {
                recyclerViewAdapter.differ.submitList(it)
            }
        }

        recyclerViewAdapter.differ.submitList(notificationViewModel.notifications.value)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = recyclerViewAdapter
        }


        binding.exercise.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_reminderFragment_to_exerciseFragment)
        }
        binding.addRemind.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_reminderFragment_to_createNotificationFragment)
        }


        if (ActivityCompat.checkSelfPermission(
                activity!!, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this.requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                MainActivity.REQUEST_CODE_LOC_NOTIFICATION
            )
            return
        }
    }

    override fun onDestroy() {
        notificationViewModel.notifications.removeObservers(this)
        super.onDestroy()
    }
}


