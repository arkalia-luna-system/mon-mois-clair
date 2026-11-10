package be.monmoisclair.app.ui.health

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.reminder.ReminderHealthChecker
import be.monmoisclair.app.data.reminder.ReminderHealthReport
import be.monmoisclair.app.ui.AsyncLoadCoordinator
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val refreshCoordinator = AsyncLoadCoordinator()

    init {
        refresh()
    }

    fun refresh() {
        val loadId = refreshCoordinator.startLoad()
        viewModelScope.launch {
            _errorMessage.value = null
            val report = healthChecker.check()
            if (refreshCoordinator.isStale(loadId)) return@launch
            _report.value = report
        }
    }

    fun intentIgnoreBattery(): Intent = healthChecker.intentIgnoreBatteryOptimizations()
    fun intentExactAlarm(): Intent = healthChecker.intentExactAlarmSettings()
    fun intentNotifications(): Intent = healthChecker.intentAppNotificationSettings()

    /** Réglages système inaccessibles (ROM / ActivityNotFound). */
    fun reportSettingsIntentFailed() {
        _errorMessage.value =
            "Impossible d’ouvrir les réglages. Réessayez depuis le téléphone."
    }
}
