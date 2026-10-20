package be.monmoisclair.app.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream

class BackupSecurityArchiveTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun pack_then_unpack_roundTrip() {
        val json = """{"schemaVersion":3,"ok":true}"""
        val receipts = mapOf(
            "receipts/exp-a.webp" to byteArrayOf(1, 2, 3, 4),
            "receipts/exp-b.webp" to byteArrayOf(9, 8, 7),
            "ignored/not-a-receipt.bin" to byteArrayOf(1)
        )
        val out = tmp.newFile(BackupSecurityArchive.SHARE_ZIP_NAME)
        val packed = BackupSecurityArchive.packToFile(out, json, receipts)
        assertThat(packed.photoCount).isEqualTo(2)
        assertThat(packed.totalBytes).isGreaterThan(0L)
        assertThat(BackupSecurityArchive.looksLikeZip(out.readBytes())).isTrue()

        val unpacked = BackupSecurityArchive.unpack(out.readBytes())
        assertThat(unpacked.jsonUtf8).isEqualTo(json)
        assertThat(unpacked.receipts).hasSize(2)
        assertThat(unpacked.receipts["receipts/exp-a.webp"]).isEqualTo(byteArrayOf(1, 2, 3, 4))
    }

    @Test
    fun unpack_rejects_zip_without_json() {
        val out = tmp.newFile("photos-only.zip")
        ZipOutputStream(FileOutputStream(out)).use { zip ->
            zip.putNextEntry(ZipEntry("receipts/x.webp"))
            zip.write(byteArrayOf(1, 2, 3))
            zip.closeEntry()
        }
        assertThrows(BackupFailure.Io::class.java) {
            BackupSecurityArchive.unpack(out.readBytes())
        }
    }

    @Test
    fun looksLikeZip_false_for_plain_text() {
        assertThat(BackupSecurityArchive.looksLikeZip("juste du texte".toByteArray())).isFalse()
    }

    @Test
    fun sanitizeReceiptPath_rejects_traversal() {
        assertThat(BackupSecurityArchive.sanitizeReceiptPath("receipts/../secret.webp")).isNull()
        assertThat(BackupSecurityArchive.sanitizeReceiptPath("receipts/ok.webp"))
            .isEqualTo("receipts/ok.webp")
    }
}
