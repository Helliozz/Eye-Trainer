package com.example.eyetrainer.ViewModel


import androidx.lifecycle.*
import com.example.eyetrainer.Data.NotificationData
import com.example.eyetrainer.Data.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {
    val notifications: LiveData<List<NotificationData>> = repository.allNotifications.asLiveData()

    fun insert(notificationData: NotificationData) = viewModelScope.launch {
        repository.insertNotification(notificationData)
    }

    fun delete(notificationData: NotificationData) = viewModelScope.launch {
        repository.deleteNotification(notificationData)
    }

    fun getPossibleId(): Int {
        val currentIds: ArrayList<Int> = arrayListOf()
        notifications.value?.forEach {
            currentIds.add(it.id)
        }
        currentIds.sort()

        var newId = 10
        for(e: Int in currentIds){
            if(newId == e){
                newId += 1
            }
        }
        return newId
    }
}

class NotificationViewModelFactory(private val repository: NotificationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return NotificationViewModel(repository) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}