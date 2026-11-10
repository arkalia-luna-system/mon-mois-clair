package be.monmoisclair.app.ui.entry

import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EntryFormCreditorCopyTest {

    private val ctx = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun emptyIban_errors() {
        val r = EntryFormCreditorCopy.copyIban(ctx, "")
        assertThat(r.error).isEqualTo("Aucun IBAN à copier.")
    }

    @Test
    fun invalidChecksum_errors() {
        val r = EntryFormCreditorCopy.copyIban(ctx, "BE00 0000 0000 0000")
        assertThat(r.error).isEqualTo("Vérifiez l’IBAN sur le courrier.")
    }

    @Test
    fun ibanLabelOnly_errorsAsEmpty() {
        val r = EntryFormCreditorCopy.copyIban(ctx, "IBAN")
        assertThat(r.error).isEqualTo("Aucun IBAN à copier.")
    }

    @Test
    fun validIban_prefixedAndLowercase_isNormalizedAndCopied() {
        val r = EntryFormCreditorCopy.copyIban(ctx, "iban be68 5390 0754 7034")

        assertThat(r.copyMessage).isEqualTo(BelgianStructuredCommunication.IBAN_COPIED_OK)
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertThat(clipboard.primaryClip?.getItemAt(0)?.coerceToText(ctx).toString())
            .isEqualTo("BE68539007547034")
    }
}
