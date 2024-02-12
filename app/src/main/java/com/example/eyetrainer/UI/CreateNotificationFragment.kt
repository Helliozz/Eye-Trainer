package com.example.eyetrainer.UI

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.example.eyetrainer.Data.Constants.APP_KEY_DAY_CHECKSUM
import com.example.eyetrainer.Data.Constants.APP_TOAST_NO_DAY_CHOSEN
import com.example.eyetrainer.Data.NotificationData
import com.example.eyetrainer.NotificationsApplication
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.AlarmReceiver
import com.example.eyetrainer.ViewModel.NotificationViewModel
import com.example.eyetrainer.ViewModel.NotificationViewModelFactory
import com.example.eyetrainer.databinding.FragmentCreateNotificationBinding
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
class CreateNotificationFragment : Fragment() {

    private val calendar = Calendar.getInstance()
    private lateinit var alarmManager: AlarmManager
    private lateinit var binding: FragmentCreateNotificationBinding
    private lateinit var alarmIntent: PendingIntent
    private val notificationViewModel: NotificationViewModel by activityViewModels {
        NotificationViewModelFactory(
            (activity!!.application as NotificationsApplication).repository
        )
    }
    private var checkSum = 127


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        binding.save.setOnClickListener {
            if (checkSum == 0) {
                Toast.makeText(activity!!, APP_TOAST_NO_DAY_CHOSEN, Toast.LENGTH_SHORT).show()
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
                calendar.set(Calendar.MINUTE, binding.timePicker.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val requestId = notificationViewModel.getPossibleId()

                notificationViewModel.insert(
                    NotificationData(
                        id = requestId, time = calendar.timeInMillis, days = checkSum, isEnabled = true
                    )
                )

                Log.d("Notification", "checkSum = $checkSum")
                //переменная room, в неё передаётся id, который потом вставляется в requestcode, ставится время срабатывания

                alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                    intent.putExtra(APP_KEY_DAY_CHECKSUM, checkSum)
                    PendingIntent.getBroadcast(
                        context, requestId, intent, PendingIntent.FLAG_IMMUTABLE

                    )
                }

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    alarmIntent
                )

                Log.d("Notification", "Notification created")
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

    private fun setupButtons() {
        setupButton(binding.mon, 0)
        setupButton(binding.tues, 1)
        setupButton(binding.wedn, 2)
        setupButton(binding.thurs, 3)
        setupButton(binding.fri, 4)
        setupButton(binding.sat, 5)
        setupButton(binding.sun, 6)
    }

    private fun setupButton(btn: Button, pow: Int) {
        val sum = Math.pow(2.0, pow.toDouble()).toInt()

        btn.setOnClickListener {
            val prevEnabled = (checkSum and sum) != 0
            if (prevEnabled) {
                checkSum -= sum
                btn.setBackgroundResource(R.drawable.background_button_disactive)
            } else {
                checkSum += sum
                btn.setBackgroundResource(R.drawable.background_button_active)
            }
        }
    }
}