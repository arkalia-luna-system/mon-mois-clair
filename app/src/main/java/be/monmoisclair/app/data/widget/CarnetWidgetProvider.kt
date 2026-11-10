package be.monmoisclair.app.data.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import be.monmoisclair.app.data.device.CredentialStorageGate
import be.monmoisclair.app.domain.CarnetWidgetText
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
        // Coquille immédiate : One UI affiche « Impossible d'ajouter… » si le host
        // n’a que initialLayout en échec d’inflation, ou si le refresh async tarde.
        paintShell(context, appWidgetManager, appWidgetIds)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                if (!CredentialStorageGate.isAvailable(context)) {
                    Log.i(TAG, "widget: CE indisponible — coquille seulement")
                    return@launch
                }
                monthCycleEngine.ensureCurrentMonthExists()
                refresher.refresh()
            } catch (e: Exception) {
                Log.e(TAG, "mise à jour du widget", e)
            } finally {
                pending.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle?
    ) {
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    private fun paintShell(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val systemDark = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val shell = CarnetWidgetRefresher.render(
            context,
            CarnetWidgetText.unavailable(),
            systemDark
        )
        appWidgetManager.updateAppWidget(appWidgetIds, shell)
    }

    private companion object {
        const val TAG = "CarnetWidget"
    }
}
