package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SupportMailComposerTest {

    @Test
    fun build_targetsAthalia_andIncludesVersion() {
        val draft = SupportMailComposer.build(
            versionName = "0.1.0-6.39",
            versionCode = 44,
            deviceModel = "SM-S938B",
            monthLabel = "Octobre 2026"
        )
        assertThat(draft.to).isEqualTo("arkalia.luna.system@gmail.com")
        assertThat(draft.subject).contains("0.1.0-6.39")
        assertThat(draft.body).contains("SM-S938B")
        assertThat(draft.body).contains("Octobre 2026")
        assertThat(draft.body).contains("capture")
        val uri = SupportMailComposer.mailtoUri(draft)
        assertThat(uri).startsWith("mailto:arkalia.luna.system@gmail.com?")
        assertThat(uri).contains("subject=")
    }
}
