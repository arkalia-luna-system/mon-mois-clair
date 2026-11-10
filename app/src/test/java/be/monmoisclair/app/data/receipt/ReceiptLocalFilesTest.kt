package be.monmoisclair.app.data.receipt

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class ReceiptLocalFilesTest {

    @Test
    fun deleteUnder_removesParsedPaths() {
        val dir = createTempDirectory().toFile()
        val a = File(dir, "receipts/a.webp").apply {
            parentFile!!.mkdirs()
            writeBytes(byteArrayOf(1))
        }
        val b = File(dir, "receipts/b.pdf").apply {
            parentFile!!.mkdirs()
            writeBytes(byteArrayOf(2))
        }
        ReceiptLocalFiles.deleteUnder(dir, "receipts/a.webp\nreceipts/b.pdf")
        assertThat(a.exists()).isFalse()
        assertThat(b.exists()).isFalse()
    }

    @Test
    fun deleteUnder_ignoresInvalidPath() {
        val dir = createTempDirectory().toFile()
        ReceiptLocalFiles.deleteUnder(dir, "../escape")
        assertThat(dir.listFiles().orEmpty()).isEmpty()
    }
}
