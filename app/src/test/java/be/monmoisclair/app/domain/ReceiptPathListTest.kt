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
    }
}
