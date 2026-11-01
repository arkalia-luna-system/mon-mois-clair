package be.monmoisclair.app.data.widget

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * WDG02 / P0 One UI : un `@font/…` dans le layout RemoteViews fait échouer
 * l’inflation chez le host (Samsung → « Impossible d'ajouter le widget. »).
 */
class CarnetWidgetLayoutContractTest {

    @Test
    fun widget_layout_uses_system_fonts_only() {
        val xml = readRes("layout/widget_carnet.xml")
        assertThat(xml).doesNotContain("@font/")
        assertThat(xml).contains("fontFamily=\"serif\"")
        assertThat(xml).contains("fontFamily=\"sans-serif-medium\"")
    }

    @Test
    fun widget_layout_avoids_bare_view_divider() {
        val xml = readRes("layout/widget_carnet.xml")
        // Host One UI (API 31+) : InflateException « Class not allowed … android.view.View »
        assertThat(xml).doesNotContain("<View")
        assertThat(xml).doesNotContain("</View>")
        assertThat(xml).contains("widget_rule")
        assertThat(xml).contains("<ImageView")
    }

    @Test
    fun widget_info_points_at_safe_layout() {
        val xml = readRes("xml/carnet_widget_info.xml")
        assertThat(xml).contains("@layout/widget_carnet")
        assertThat(xml).contains("minResizeWidth")
        assertThat(xml).contains("targetCellWidth")
    }

    @Test
    fun widget_muted_text_atLeast14sp() {
        // P1-1 a11y : titre / ligne muted lisibles senior.
        val xml = readRes("layout/widget_carnet.xml")
        assertThat(xml).contains("android:textSize=\"14sp\"")
        assertThat(xml).contains("android:textSize=\"15sp\"")
        assertThat(xml).doesNotContain("android:textSize=\"11sp\"")
        assertThat(xml).doesNotContain("android:textSize=\"13sp\"")
    }

    private fun readRes(underRes: String): String {
        val candidates = listOf(
            File("src/main/res/$underRes"),
            File("app/src/main/res/$underRes"),
            File("../app/src/main/res/$underRes")
        )
        val file = candidates.firstOrNull { it.isFile }
            ?: error("Introuvable res/$underRes (cwd=${File(".").absolutePath})")
        return file.readText()
    }
}
