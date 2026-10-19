package be.monmoisclair.app.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExactAlarmGatewayImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ExactAlarmGateway {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun schedule(requestCode: Int, triggerAtEpochMs: Long, payload: ReminderPayload) {
        val pending = requireNotNull(pendingIntent(requestCode, payload, create = true)) {
            "PendingIntent create a échoué pour requestCode=$requestCode"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtEpochMs,
                pending
            )
        } else {
            @Suppress("DEPRECATION")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtEpochMs, pending)
        }
    }

    override fun cancel(requestCode: Int) {
        val pending = pendingIntent(requestCode, payload = null, create = false) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(
        requestCode: Int,
        payload: ReminderPayload?,
        create: Boolean
    ): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_FIRE
            if (payload != null) {
                putExtra(ReminderReceiver.EXTRA_EXPENSE_ID, payload.expenseInstanceId)
                putExtra(ReminderReceiver.EXTRA_KIND, payload.kind.name)
                putExtra(ReminderReceiver.EXTRA_TITLE, payload.title)
                putExtra(ReminderReceiver.EXTRA_BODY, payload.body)
            }
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (create) {
            PendingIntent.getBroadcast(context, requestCode, intent, flags)
        } else {
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags or PendingIntent.FLAG_NO_CREATE
            )
        }
    }
}
