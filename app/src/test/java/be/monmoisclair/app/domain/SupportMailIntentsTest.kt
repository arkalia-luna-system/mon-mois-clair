package be.monmoisclair.app.domain

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
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
    fun creditorFallback_copiesRecipient() {
        val creditor = CreditorMailComposer.Draft(
            to = "creancier@example.be",
            subject = "Versement",
            body = "Bonjour"
        )
        val chooser = SupportMailIntents.sendFallbackChooser(creditor, "Écrire")
        val inner = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertThat(inner?.getStringArrayExtra(Intent.EXTRA_EMAIL))
            .asList()
            .contains("creancier@example.be")
    }

    @Test
    fun canResolveSendTo_falseWhenNoHandler() {
        val pm = RuntimeEnvironment.getApplication().packageManager
        assertThat(SupportMailIntents.canResolveSendTo(pm, draft)).isFalse()
    }

    @Test
    fun canResolveSendTo_trueWhenHandlerRegistered() {
        val app = RuntimeEnvironment.getApplication()
        val pm = app.packageManager
        val shadow = Shadows.shadowOf(pm)
        val resolve = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.example.mail"
                name = "MailActivity"
            }
        }
        shadow.addResolveInfoForIntent(
            SupportMailIntents.sendToIntent(SupportMailComposer.mailtoUri(draft)),
            resolve
        )
        assertThat(SupportMailIntents.canResolveSendTo(pm, draft)).isTrue()
    }

    @Test
    fun messages_areActionableFrench() {
        assertThat(SupportMailIntents.NO_MAIL_APP).contains("Copiez")
        assertThat(SupportMailIntents.NO_MAIL_APP.lowercase()).doesNotContain("téléphone")
        assertThat(SupportMailIntents.ADDRESS_COPIED).contains("Adresse copiée")
        assertThat(SupportMailIntents.chooserTitle()).contains("Arkalia")
    }
}
