package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HowItWorksCopyTest {

    @Test
    fun sections_coverFamilyTopics() {
        val titles = HowItWorksCopy.sections.map { it.title }
        assertThat(titles).contains("Marquer FAIT")
        assertThat(titles).contains("Sauvegarde")
        assertThat(titles).contains("Rappels")
        assertThat(titles).contains("Facture papier")
        assertThat(HowItWorksCopy.sections.size).isAtLeast(6)
        HowItWorksCopy.sections.forEach { s ->
            assertThat(s.body.length).isGreaterThan(40)
        }
    }
}
