package com.example.eyetrainer.UI

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.eyetrainer.Data.Constants.APP_TOAST_NO_DAY_CHOSEN
import com.example.eyetrainer.Model.NotificationData
import com.example.eyetrainer.NotificationsApplication
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.NotificationViewModel
import com.example.eyetrainer.ViewModel.NotificationViewModelFactory
import com.example.eyetrainer.databinding.FragmentCreateNotificationBinding
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
class CreateNotificationFragment : Fragment() {

    private val calendar = Calendar.getInstance()
    private lateinit var alarmManager: AlarmManager
    private lateinit var binding: FragmentCreateNotificationBinding
    private val notificationViewModel: NotificationViewModel by activityViewModels {
        NotificationViewModelFactory(
            (requireActivity().application as NotificationsApplication).repository
        )
    }
    private var checkSum = 127


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val notificationAlreadyExists = (notificationViewModel.getSavedNotification() != null)
        if (notificationAlreadyExists) {
            setupExistingNotification()
        }

        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        binding.save.setOnClickListener {
            if (checkSum == 0) {
                Toast.makeText(requireActivity(), APP_TOAST_NO_DAY_CHOSEN, Toast.LENGTH_SHORT).show()
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
                calendar.set(Calendar.MINUTE, binding.timePicker.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val requestId: Int = if (notificationAlreadyExists) {
                    notificationViewModel.getSavedNotification()!!.id
                } else {
                    notificationViewModel.getPossibleId()
                }

                Log.d("NotificationSample", "onViewCreated:  $checkSum")

                val notificationInfo = NotificationData(
                    id = requestId, time = calendar.timeInMillis, days = checkSum, isEnabled = true
                )

                if (notificationAlreadyExists) {
                    notificationViewModel.cancelNotification(notificationViewModel.getSavedNotification()!!, context, alarmManager)
                }
                notificationViewModel.setNewExactAlarm(notificationInfo, context, alarmManager)

                if (notificationAlreadyExists) {
                    notificationViewModel.update(notificationInfo)
                } else {
                    notificationViewModel.insert(notificationInfo)
                }

                notificationViewModel.clearSavedNotification()
                requireView().findNavController()
                    .navigate(R.id.action_createNotificationFragment_to_reminderFragment)
            }
        }

        binding.back.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_createNotificationFragment_to_reminderFragment)
        }

        setupButtons()
    }

    private fun getButton(day: Int): Button {
        return when(day) {
            0 -> binding.mon
            1 -> binding.tues
            2 -> binding.wedn
            3 -> binding.thurs
            4 -> binding.fri
            5 -> binding.sat
            6 -> binding.sun
            else -> throw (RuntimeException("Attempt to get a button for a non-existing day: day = $day."))
        }
    }

    private fun setupExistingNotification() {
        checkSum = notificationViewModel.getSavedNotification()!!.days
        for (i in 0..6) {
            val btn = getButton(i)
            if ((checkSum and Math.pow(2.0, i.toDouble()).toInt()) == 0) {
                btn.setBackgroundResource(R.drawable.background_button_nonactive)
            } else {
                btn.setBackgroundResource(R.drawable.background_button_active)
            }
        }

        val mCal = Calendar.getInstance()
        mCal.timeInMillis = notificationViewModel.getSavedNotification()!!.time
        binding.timePicker.hour = mCal.get(Calendar.HOUR_OF_DAY)
        binding.timePicker.minute = mCal.get(Calendar.MINUTE)
    }

    private fun setupButtons() {
        for (i in 0..6) {
            setupButton(getButton(i), i)
        }
    }

    private fun setupButton(btn: Button, pow: Int) {
        val sum = Math.pow(2.0, pow.toDouble()).toInt()

        btn.setOnClickListener {
            val prevEnabled = (checkSum and sum) != 0
            if (prevEnabled) {
                checkSum -= sum
                btn.setBackgroundResource(R.drawable.background_button_nonactive)
            } else {
                checkSum += sum
                btn.setBackgroundResource(R.drawable.background_button_active)
            }
        }
    }
}