package be.monmoisclair.app.data.receipt

import android.graphics.Bitmap
import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class ReceiptPhotoStoreTest {
    @Test
    fun looksLikePdf_requires_header() {
        assertThat(ReceiptPhotoStore.looksLikePdf("%PDF-1.4\n".toByteArray())).isTrue()
        assertThat(ReceiptPhotoStore.looksLikePdf("pas un pdf".toByteArray())).isFalse()
        assertThat(ReceiptPhotoStore.looksLikePdf(byteArrayOf())).isFalse()
    }

    @Test
    fun webp_roundTrip_decodesFromBytes() {
        val src = Bitmap.createBitmap(80, 40, Bitmap.Config.ARGB_8888)
        src.eraseColor(Color.rgb(0x1E, 0x1B, 0x18))
        val bytes = ReceiptPhotoStore.compressWebp(src)
        val back = ReceiptPhotoStore.decodeImageBytes(bytes)
        assertThat(back).isNotNull()
        assertThat(back!!.width).isGreaterThan(0)
        assertThat(back.height).isGreaterThan(0)
    }

    @Test
    fun webp_decodesWhenStreamCannotRewind() {
        val src = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
        src.eraseColor(Color.WHITE)
        val bytes = ReceiptPhotoStore.compressWebp(src)
        val stream = object : InputStream() {
            private val bytesIn = ByteArrayInputStream(bytes)
            override fun read(): Int = bytesIn.read()
            override fun read(b: ByteArray, off: Int, len: Int) = bytesIn.read(b, off, len)
            override fun markSupported() = false
        }
        val back = ReceiptPhotoStore.decodeImageStream(stream)
        assertThat(back).isNotNull()
    }

    @Test
    fun compress_is_under_200kb() {
        val big = Bitmap.createBitmap(3000, 2000, Bitmap.Config.ARGB_8888)
        big.eraseColor(Color.WHITE)
        assertThat(ReceiptPhotoStore.compressWebp(big).size).isLessThan(200_000)
    }
}
