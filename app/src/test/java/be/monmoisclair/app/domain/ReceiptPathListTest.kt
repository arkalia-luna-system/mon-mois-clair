package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReceiptPathListTest {

    @Test
    fun append_keepsPrimary_andRemovesWithoutTouchingOthers() {
        val one = ReceiptPathList.append("", "receipts/a.webp")
        assertThat(ReceiptPathList.primary(one)).isEqualTo("receipts/a.webp")
        val two = ReceiptPathList.append(one, "receipts/b.webp")
        assertThat(ReceiptPathList.parse(two)).containsExactly(
            "receipts/a.webp",
            "receipts/b.webp"
        ).inOrder()
        val after = ReceiptPathList.remove(two, "receipts/a.webp")
        assertThat(ReceiptPathList.parse(after)).containsExactly("receipts/b.webp")
        val withPdf = ReceiptPathList.append(after, "receipts/lettre.pdf")
        assertThat(ReceiptPathList.parse(withPdf)).containsExactly(
            "receipts/b.webp",
            "receipts/lettre.pdf"
        ).inOrder()
    }

    @Test
    fun primaryPhotoOrFirst_prefersWebp_overPdf() {
        val isPdf: (String) -> Boolean = { it.endsWith(".pdf", ignoreCase = true) }
        val raw = "receipts/a.webp\nreceipts/lettre.pdf"
        assertThat(ReceiptPathList.primaryPhotoOrFirst(raw, isPdf))
            .isEqualTo("receipts/a.webp")
        assertThat(ReceiptPathList.extrasBesidesPrimary(raw, isPdf))
            .containsExactly("receipts/lettre.pdf")
        assertThat(ReceiptPathList.primaryPhotoOrFirst("receipts/seul.pdf", isPdf))
            .isEqualTo("receipts/seul.pdf")
    }

    @Test
    fun hasReceiptHomeLogic_photoPlusPdf_stillShowsPhotoButton() {
        val isPdf: (String) -> Boolean = { it.endsWith(".pdf", ignoreCase = true) }
        val raw = "receipts/loyer3.webp\nreceipts/loyer3-pdf.pdf"
        // Ancien bug accueil : endsWith(".pdf") sur toute la chaîne → bouton masqué.
        assertThat(raw.endsWith(".pdf", ignoreCase = true)).isTrue()
        val primary = ReceiptPathList.primaryPhotoOrFirst(raw, isPdf)
        val hasReceipt = primary.isNotBlank() && !isPdf(primary)
        assertThat(hasReceipt).isTrue()
        assertThat(primary).isEqualTo("receipts/loyer3.webp")
    }

    @Test
    fun loyer3_multiLine_sanitizeNeedsPrimaryFirst() {
        val raw = "receipts/loyer3.webp\nreceipts/loyer3-pdf.pdf"
        // sanitize refuse la chaîne entière (slash / endsWith .pdf) — ouvrir = primary seul.
        assertThat(
            be.monmoisclair.app.data.backup.BackupSecurityArchive.sanitizeReceiptPath(raw)
        ).isNull()
        val primary = ReceiptPathList.primaryPhotoOrFirst(raw) {
            be.monmoisclair.app.data.backup.BackupSecurityArchive.isPdfReceiptPath(it)
        }
        assertThat(primary).isEqualTo("receipts/loyer3.webp")
        assertThat(
            be.monmoisclair.app.data.backup.BackupSecurityArchive.sanitizeReceiptPath(primary)
        ).isEqualTo("receipts/loyer3.webp")
    }
}
