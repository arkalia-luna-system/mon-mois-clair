package be.monmoisclair.app.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import be.monmoisclair.app.MainActivity
import be.monmoisclair.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_payments_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_payments_desc)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showPaymentReminder(
        notificationId: Int,
        title: String,
        body: String,
        expenseInstanceId: String
    ) {
        ensureChannel()
        val openApp = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_HIGHLIGHT_EXPENSE_ID, expenseInstanceId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // P3-Q01 : action FAIT sans ouvrir l’app.
        val markDone = PendingIntent.getBroadcast(
            context,
            notificationId + MARK_DONE_REQUEST_OFFSET,
            Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_MARK_DONE
                putExtra(ReminderReceiver.EXTRA_EXPENSE_ID, expenseInstanceId)
                putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(ReminderReceiver.EXTRA_LABEL, title.substringAfter(": ", title))
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .addAction(
                0,
                context.getString(R.string.notification_action_done),
                markDone
            )
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /** E04 : après FAIT depuis la notification, laisser Annuler quelques secondes. */
    fun showMarkedDoneUndo(notificationId: Int, label: String, expenseInstanceId: String) {
        ensureChannel()
        val undo = PendingIntent.getBroadcast(
            context,
            notificationId + UNDO_REQUEST_OFFSET,
            Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_UNDO_DONE
                putExtra(ReminderReceiver.EXTRA_EXPENSE_ID, expenseInstanceId)
                putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle("$label marqué FAIT")
            .setContentText("Touchez Annuler pour remettre à payer.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setTimeoutAfter(10_000)
            .addAction(0, context.getString(R.string.notification_action_undo), undo)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "paiements"
        private const val MARK_DONE_REQUEST_OFFSET = 100_000
        private const val UNDO_REQUEST_OFFSET = 200_000
    }
}
