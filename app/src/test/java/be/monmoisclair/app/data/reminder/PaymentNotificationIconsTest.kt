package be.monmoisclair.app.data.reminder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaymentNotificationIconsTest {

    @Test
    fun ic_notification_large_decodes_as_colored_bitmap() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_notification_large
        )
        assertThat(bitmap).isNotNull()
        assertThat(bitmap!!.width).isAtLeast(64)
        assertThat(bitmap.height).isAtLeast(64)
        assertThat(hasNonWhitePixel(bitmap)).isTrue()
    }

    @Test
    fun smallIcon_resource_exists() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = context.getDrawable(R.drawable.ic_notification)
        assertThat(drawable).isNotNull()
    }

    @Test
    fun notification_builder_accepts_largeIcon() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val large = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_notification_large
        )!!
        val notification = NotificationCompat.Builder(context, "paiements")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF0A5254.toInt())
            .setContentTitle("test")
            .setContentText("body")
            .setLargeIcon(large)
            .build()
        assertThat(notification.getLargeIcon()).isNotNull()
    }

    private fun hasNonWhitePixel(bitmap: Bitmap): Boolean {
        val stepX = (bitmap.width / 16).coerceAtLeast(1)
        val stepY = (bitmap.height / 16).coerceAtLeast(1)
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val c = bitmap.getPixel(x, y)
                val a = (c ushr 24) and 0xFF
                val r = (c ushr 16) and 0xFF
                val g = (c ushr 8) and 0xFF
                val b = c and 0xFF
                if (a > 20 && (r < 240 || g < 240 || b < 240)) return true
                x += stepX
            }
            y += stepY
        }
        return false
    }
}
