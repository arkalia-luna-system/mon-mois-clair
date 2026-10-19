package be.monmoisclair.app.ui.health

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.reminder.ReminderHealthChecker
import be.monmoisclair.app.data.reminder.ReminderHealthReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderHealthViewModel @Inject constructor(
    private val healthChecker: ReminderHealthChecker
) : ViewModel() {

    private val _report = MutableStateFlow<ReminderHealthReport?>(null)
    val report: StateFlow<ReminderHealthReport?> = _report.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _report.value = healthChecker.check()
        }
    }

    fun intentIgnoreBattery(): Intent = healthChecker.intentIgnoreBatteryOptimizations()
    fun intentExactAlarm(): Intent = healthChecker.intentExactAlarmSettings()
    fun intentNotifications(): Intent = healthChecker.intentAppNotificationSettings()
}
