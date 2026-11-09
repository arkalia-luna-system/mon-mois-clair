package be.monmoisclair.app.domain

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SupportMailIntentsTest {

    private val draft = SupportMailComposer.Draft(
        subject = "Sujet",
        body = "Corps"
    )

    @Test
    fun sendToChooser_wrapsMailto() {
        val chooser = SupportMailIntents.sendToChooser(draft, "Titre")
        assertThat(chooser.action).isEqualTo(Intent.ACTION_CHOOSER)
        val inner = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertThat(inner?.action).isEqualTo(Intent.ACTION_SENDTO)
        assertThat(inner?.data?.scheme).isEqualTo("mailto")
    }

    @Test
    fun sendFallback_usesRfc822() {
        val chooser = SupportMailIntents.sendFallbackChooser(draft, "Titre")
        val inner = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertThat(inner?.action).isEqualTo(Intent.ACTION_SEND)
        assertThat(inner?.type).isEqualTo("message/rfc822")
        assertThat(inner?.getStringArrayExtra(Intent.EXTRA_EMAIL))
            .asList()
            .contains(SupportMailComposer.SUPPORT_EMAIL)
    }

    @Test
    fun messages_areActionableFrench() {
        assertThat(SupportMailIntents.NO_MAIL_APP).contains("Copiez")
        assertThat(SupportMailIntents.chooserTitle()).contains("Arkalia")
    }
}
