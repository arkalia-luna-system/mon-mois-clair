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
}
