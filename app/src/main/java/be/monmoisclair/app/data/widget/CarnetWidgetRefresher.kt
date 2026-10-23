package be.monmoisclair.app.data.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import be.monmoisclair.app.MainActivity
import be.monmoisclair.app.R
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.CarnetWidgetText
import be.monmoisclair.app.domain.MonthSummaryCalculator
import be.monmoisclair.app.domain.NextUnpaidLine
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarnetWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val instanceDao: InstanceDao,
    private val appMetaDao: AppMetaDao
) : CarnetWidgetSink {

    override suspend fun refresh() {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, CarnetWidgetProvider::class.java)
        )
        if (ids.isEmpty()) return
        val text = load()
        val systemDark = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val night = ThemePreference
            .fromStorage(appMetaDao.get(AppMetaKeys.THEME_PREFERENCE)?.value)
            .resolveNight(systemDark)
        manager.updateAppWidget(ids, render(context, text, night))
    }

    suspend fun load(): CarnetWidgetText {
        return try {
            val locked = appMetaDao.get(AppMetaKeys.BIOMETRIC_ENABLED)?.value == "true"
            val ledgerId = AppClock.ledgerId()
            val incomes = instanceDao.getAllIncomes().filter { it.ledgerId == ledgerId }
            val expenses = instanceDao.getAllExpenses().filter { it.ledgerId == ledgerId }
            CarnetWidgetText.compose(
                locked = locked,
                summary = MonthSummaryCalculator.compute(incomes, expenses),
                next = NextUnpaidLine.pick(expenses, ledgerId),
                expenseCount = expenses.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "lecture du mois pour le widget", e)
            CarnetWidgetText(
                title = "Mon Mois Clair",
                amount = "",
                amountNegative = false,
                line = "Ouvrir le carnet",
                lineOverdue = false,
                locked = true
            )
        }
    }

    companion object {
        private const val TAG = "CarnetWidget"

        fun render(context: Context, text: CarnetWidgetText, night: Boolean): RemoteViews {
            // Alignés A11yTokens Day/Night (ink / inkMuted / rule papier).
            val ink = if (night) 0xFFF6F3E8.toInt() else 0xFF1E1B18.toInt()
            val muted = if (night) 0xFFD1C1B1.toInt() else 0xFF3F3A33.toInt()
            val danger = if (night) 0xFFF4C4B6.toInt() else 0xFF8E3418.toInt()
            val rule = if (night) 0xFF5A534A.toInt() else 0xFFC9C0B0.toInt()
            val views = RemoteViews(context.packageName, R.layout.widget_carnet)
            views.setInt(
                R.id.widget_root,
                "setBackgroundResource",
                if (night) R.drawable.widget_carnet_bg_night else R.drawable.widget_carnet_bg
            )
            views.setTextViewText(R.id.widget_title, text.title)
            views.setTextColor(R.id.widget_title, muted)
            views.setInt(R.id.widget_rule, "setBackgroundColor", rule)
            views.setTextViewText(R.id.widget_line, text.line)
            views.setTextColor(R.id.widget_line, if (text.lineOverdue) danger else muted)
            // Verrouillé : pas de montant. Sinon toujours Reste + filet (pas de trou fade).
            if (text.locked) {
                views.setViewVisibility(R.id.widget_amount, View.GONE)
                views.setViewVisibility(R.id.widget_rule, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_amount, View.VISIBLE)
                views.setViewVisibility(R.id.widget_rule, View.VISIBLE)
                val amount = text.amount.ifBlank { "—" }
                views.setTextViewText(R.id.widget_amount, amount)
                views.setTextColor(
                    R.id.widget_amount,
                    if (text.amountNegative) danger else ink
                )
            }
            val spoken = if (text.amount.isEmpty()) {
                "${text.title}. ${text.line}"
            } else {
                "${text.title} ${text.amount}. ${text.line}"
            }
            views.setContentDescription(R.id.widget_root, spoken)
            val open = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)
            return views
        }
    }
}
