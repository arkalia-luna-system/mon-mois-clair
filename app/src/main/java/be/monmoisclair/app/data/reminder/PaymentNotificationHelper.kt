package be.monmoisclair.app.data.reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
        val notification = baseBuilder(title, body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.notification_action_done),
                markDone
            )
            .build()

        notifyIfAllowed(notificationId, notification)
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
        val notification = baseBuilder(
            title = "$label — noté FAIT",
            body = "Touchez Remettre si c’était une erreur."
        )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setTimeoutAfter(10_000)
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.notification_action_undo),
                undo
            )
            .build()
        notifyIfAllowed(notificationId, notification)
    }

    /**
     * smallIcon = silhouette M blanche (barre d’état) ;
     * largeIcon = monogramme mist/teal (bannière Samsung collapsed) ;
     * setColor = accent teal brand.
     */
    private fun baseBuilder(title: String, body: String): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(CONTEXT_TEAL)
            .setContentTitle(title)
            .setContentText(body)
        decodeAppLargeIcon()?.let { builder.setLargeIcon(it) }
        return builder
    }

    private fun decodeAppLargeIcon(): Bitmap? {
        return runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification_large)
                ?: renderDrawableBitmap(R.drawable.ic_notification_large)
        }.getOrNull()
    }

    private fun renderDrawableBitmap(resId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val size = (56f * context.resources.displayMetrics.density).toInt().coerceAtLeast(96)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }

    private fun notifyIfAllowed(notificationId: Int, notification: Notification) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        try {
            manager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission retirée entre le check et l’appel.
        }
    }

    companion object {
        const val CHANNEL_ID = "paiements"
        /** Teal charte — teinte shade / icône (silhouette claire sur fond). */
        private const val CONTEXT_TEAL = 0xFF0A5254.toInt()
        private const val MARK_DONE_REQUEST_OFFSET = 100_000
        private const val UNDO_REQUEST_OFFSET = 200_000
    }
}
