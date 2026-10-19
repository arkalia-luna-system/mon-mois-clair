package be.monmoisclair.app.data.receipt

import android.graphics.Bitmap
import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReceiptPhotoStoreTest {
    @Test
    fun compress_is_under_200kb() {
        val big = Bitmap.createBitmap(3000, 2000, Bitmap.Config.ARGB_8888)
        big.eraseColor(Color.WHITE)
        assertThat(ReceiptPhotoStore.compressWebp(big).size).isLessThan(200_000)
    }
}
