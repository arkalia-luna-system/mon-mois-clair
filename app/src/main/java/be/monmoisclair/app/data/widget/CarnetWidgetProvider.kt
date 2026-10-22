package be.monmoisclair.app.data.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import be.monmoisclair.app.domain.MonthCycleEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Un seul widget : Reste + prochaine facture non payée. */
@AndroidEntryPoint
class CarnetWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var monthCycleEngine: MonthCycleEngine

    @Inject
    lateinit var refresher: CarnetWidgetRefresher

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                monthCycleEngine.ensureCurrentMonthExists()
                refresher.refresh()
            } catch (e: Exception) {
                Log.e(TAG, "mise à jour du widget", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "CarnetWidget"
    }
}
