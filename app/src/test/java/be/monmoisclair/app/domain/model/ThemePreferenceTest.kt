package be.monmoisclair.app.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThemePreferenceTest {

    @Test
    fun resolveNight_system_followsPhone() {
        assertThat(ThemePreference.System.resolveNight(systemDark = true)).isTrue()
        assertThat(ThemePreference.System.resolveNight(systemDark = false)).isFalse()
    }

    @Test
    fun resolveNight_light_alwaysDay() {
        assertThat(ThemePreference.Light.resolveNight(systemDark = true)).isFalse()
        assertThat(ThemePreference.Light.resolveNight(systemDark = false)).isFalse()
    }

    @Test
    fun resolveNight_dark_alwaysNight() {
        assertThat(ThemePreference.Dark.resolveNight(systemDark = true)).isTrue()
        assertThat(ThemePreference.Dark.resolveNight(systemDark = false)).isTrue()
    }

    @Test
    fun fromStorage_defaultsToSystem() {
        assertThat(ThemePreference.fromStorage(null)).isEqualTo(ThemePreference.System)
        assertThat(ThemePreference.fromStorage("")).isEqualTo(ThemePreference.System)
        assertThat(ThemePreference.fromStorage("weird")).isEqualTo(ThemePreference.System)
    }

    @Test
    fun fromStorage_parsesKnownValues() {
        assertThat(ThemePreference.fromStorage("system")).isEqualTo(ThemePreference.System)
        assertThat(ThemePreference.fromStorage("light")).isEqualTo(ThemePreference.Light)
        assertThat(ThemePreference.fromStorage("DARK")).isEqualTo(ThemePreference.Dark)
    }
}
