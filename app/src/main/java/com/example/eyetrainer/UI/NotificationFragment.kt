package com.example.eyetrainer.UI

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eyetrainer.Adapter.NotificationRecyclerViewAdapter
import com.example.eyetrainer.Model.NotificationData
import com.example.eyetrainer.NotificationsApplication
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.NotificationViewModel
import com.example.eyetrainer.ViewModel.NotificationViewModelFactory
import com.example.eyetrainer.databinding.FragmentReminderBinding
import java.util.*


@RequiresApi(Build.VERSION_CODES.S)
@Suppress("DEPRECATION")
class NotificationFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager

    private lateinit var editNotification: (NotificationData) -> Unit
    private lateinit var deleteNotification: (NotificationData) -> Unit
    private lateinit var activateNotification: (NotificationData) -> Unit
    private lateinit var deactivateNotification: (NotificationData) -> Unit
    private lateinit var binding: FragmentReminderBinding

    private val recyclerViewAdapter by lazy {
        NotificationRecyclerViewAdapter(editNotification, deleteNotification, activateNotification, deactivateNotification)
    }
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
        setupFunctions()

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
            notificationViewModel.clearSavedNotification()
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

    private fun setupFunctions() {
        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        editNotification = {
            notificationViewModel.saveNotification(it)
            requireView().findNavController().navigate(R.id.action_reminderFragment_to_createNotificationFragment)
        }

        deleteNotification = {
            notificationViewModel.delete(it)
        }

        activateNotification = {
            notificationViewModel.activateNotification(it, context, alarmManager)
        }

        deactivateNotification = {
            notificationViewModel.cancelNotification(it, context, alarmManager)
        }

        notificationViewModel.notifications.observe(this) { notifications ->
            notifications.let {
                recyclerViewAdapter.differ.submitList(it)
                notificationViewModel.performTimerEvent({
                    recyclerViewAdapter.notifyDataSetChanged()
                }, 50L)
            }
        }
    }

    override fun onDestroy() {
        notificationViewModel.notifications.removeObservers(this)
        super.onDestroy()
    }
}


