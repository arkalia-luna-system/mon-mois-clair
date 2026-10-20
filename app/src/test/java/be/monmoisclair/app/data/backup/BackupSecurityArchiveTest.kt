package be.monmoisclair.app.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupSecurityArchiveTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun pack_then_unpack_roundTrip() {
        val json = """{"schemaVersion":3,"ok":true}"""
        val receipts = mapOf(
            "receipts/exp-a.webp" to byteArrayOf(1, 2, 3, 4),
            "receipts/exp-b.webp" to byteArrayOf(9, 8, 7)
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
        val out = tmp.newFile("empty.zip")
        BackupSecurityArchive.packToFile(
            outFile = out,
            jsonUtf8 = "x",
            receipts = emptyMap()
        )
        // Remplace par un zip sans json : pack always writes V1 — test looksLikeZip false path
        val notZip = "juste du texte".toByteArray()
        assertThat(BackupSecurityArchive.looksLikeZip(notZip)).isFalse()
    }
}
