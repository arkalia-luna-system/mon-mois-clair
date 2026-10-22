package be.monmoisclair.app.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
        assertThat(BackupSecurityArchive.sanitizeReceiptPath("receipts/lettre.pdf"))
            .isEqualTo("receipts/lettre.pdf")
        assertThat(BackupSecurityArchive.sanitizeReceiptPath("receipts/secret.bin")).isNull()
        assertThat(BackupSecurityArchive.isPdfReceiptPath("receipts/lettre.pdf")).isTrue()
        assertThat(BackupSecurityArchive.isPdfReceiptPath("receipts/ok.webp")).isFalse()
        assertThat(BackupSecurityArchive.mimeForReceiptPath("receipts/lettre.pdf"))
            .isEqualTo("application/pdf")
        assertThat(BackupSecurityArchive.mimeForReceiptPath("receipts/ok.webp"))
            .isEqualTo("image/webp")
    }

    @Test
    fun pack_then_unpack_includes_pdf() {
        val json = """{"schemaVersion":8,"ok":true}"""
        val receipts = mapOf(
            "receipts/exp-a.webp" to byteArrayOf(1, 2, 3),
            "receipts/exp-b.pdf" to "%PDF-1.4".toByteArray()
        )
        val out = tmp.newFile("with-pdf.zip")
        val packed = BackupSecurityArchive.packToFile(out, json, receipts)
        assertThat(packed.photoCount).isEqualTo(2)
        val unpacked = BackupSecurityArchive.unpack(out.readBytes())
        assertThat(unpacked.receipts).hasSize(2)
        assertThat(unpacked.receipts["receipts/exp-b.pdf"]).isEqualTo("%PDF-1.4".toByteArray())
    }

    @Test
    fun readBounded_rejects_over_limit() {
        val input = ByteArrayInputStream(ByteArray(100) { 1 })
        assertThrows(BackupFailure.Io::class.java) {
            BackupSecurityArchive.readBounded(input, maxBytes = 50)
        }
    }

    @Test
    fun readBounded_accepts_under_limit() {
        val payload = byteArrayOf(1, 2, 3, 4, 5)
        val read = BackupSecurityArchive.readBounded(
            ByteArrayInputStream(payload),
            maxBytes = 50
        )
        assertThat(read).isEqualTo(payload)
    }

}
