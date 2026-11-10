package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class WhatsNewCopyTest {

    private fun repoOrModuleFile(pathFromRepoRoot: String, fallbackFromAppModule: String): File {
        return listOf(File(pathFromRepoRoot), File(fallbackFromAppModule))
            .firstOrNull { it.isFile }
            ?: error("Fichier introuvable: $pathFromRepoRoot / $fallbackFromAppModule")
    }

    private val catalog: WhatsNewCatalog.Catalog by lazy {
        val file = repoOrModuleFile(
            pathFromRepoRoot = "app/src/main/assets/whats_new/catalog.json",
            fallbackFromAppModule = "src/main/assets/whats_new/catalog.json"
        )
        WhatsNewCatalog.parse(file.readText())
    }

    private val releases get() = catalog.releases

    @Test
    fun payload_nullWhenAlreadySeen() {
        assertThat(
            WhatsNewCopy.payloadForReleases(147, 147, releases, catalog.genericFallbackBullet)
        ).isNull()
        assertThat(
            WhatsNewCopy.payloadForReleases(200, 147, releases, catalog.genericFallbackBullet)
        ).isNull()
    }

    @Test
    fun payload_skipsInvalidCurrent() {
        assertThat(
            WhatsNewCopy.payloadForReleases(0, 0, releases, catalog.genericFallbackBullet)
        ).isNull()
        assertThat(
            WhatsNewCopy.payloadForReleases(100, -1, releases, catalog.genericFallbackBullet)
        ).isNull()
    }

    @Test
    fun payload_showsOnlyCurrentVersion_evenWhenOlderWereUnseen() {
        val sample = listOf(
            WhatsNewCatalog.Release(
                versionCode = 100,
                bullets = listOf("Ancienne puce — ne doit pas apparaître."),
            ),
            WhatsNewCatalog.Release(
                versionCode = 101,
                bullets = listOf("Puce courante uniquement.", "Deuxième puce courante."),
            ),
        )
        val p = WhatsNewCopy.payloadForReleases(99, 101, sample, catalog.genericFallbackBullet)
        requireNotNull(p)
        assertThat(p.bullets).containsExactly(
            "Puce courante uniquement.",
            "Deuxième puce courante.",
        ).inOrder()
    }

    @Test
    fun payload_doesNotReplayOlderVersions() {
        val sample = listOf(
            WhatsNewCatalog.Release(
                versionCode = 100,
                bullets = listOf("filtres Tous", "ancien écran"),
            ),
            WhatsNewCatalog.Release(
                versionCode = 101,
                bullets = listOf("mise à jour", "nouveau texte"),
            ),
        )
        val p = WhatsNewCopy.payloadForReleases(100, 101, sample, catalog.genericFallbackBullet)
        requireNotNull(p)
        assertThat(p.bullets.joinToString(" ")).contains("mise à jour")
        assertThat(p.bullets.joinToString(" ")).doesNotContain("filtres Tous")
    }

    @Test
    fun payload_limitsCurrentReleaseToSixBullets() {
        val custom = listOf(
            WhatsNewCatalog.Release(
                versionCode = 200,
                bullets = listOf("1", "2", "3", "4", "5", "6", "7")
            )
        )
        val p = WhatsNewCopy.payloadForReleases(199, 200, custom, catalog.genericFallbackBullet)
        requireNotNull(p)
        assertThat(p.bullets).containsExactly("1", "2", "3", "4", "5", "6").inOrder()
    }

    @Test
    fun payload_genericFallbackWhenNoReleaseEntry() {
        val missingVersionCode = releases.maxOf { it.versionCode } + 1
        val p = WhatsNewCopy.payloadForReleases(
            releases.maxOf { it.versionCode },
            missingVersionCode,
            releases,
            catalog.genericFallbackBullet,
        )
        requireNotNull(p)
        assertThat(p.bullets).containsExactly(catalog.genericFallbackBullet)
    }

    @Test
    fun payload_hiddenTechnicalReleaseStaysSilent() {
        val custom = listOf(
            WhatsNewCatalog.Release(
                versionCode = 210,
                bullets = emptyList(),
                userVisible = false
            )
        )
        val p = WhatsNewCopy.payloadForReleases(209, 210, custom, catalog.genericFallbackBullet)
        assertThat(p).isNull()
    }

    @Test
    fun versionLabelFromName() {
        assertThat(WhatsNewCopy.versionLabelFromName("0.1.0-7.42")).isEqualTo("Version 7.42")
        assertThat(WhatsNewCopy.versionLabelFromName(null)).isNull()
    }

    @Test
    fun catalog_hasEntryForShippedVersionCode() {
        val buildFile = repoOrModuleFile(
            pathFromRepoRoot = "app/build.gradle.kts",
            fallbackFromAppModule = "build.gradle.kts"
        ).readText()
        val shippedVersionCode = Regex("""versionCode\s*=\s*(\d+)""")
            .find(buildFile)
            ?.groupValues
            ?.get(1)
            ?.toInt()
        requireNotNull(shippedVersionCode)
        assertThat(releases.map { it.versionCode }).contains(shippedVersionCode)
    }

    @Test
    fun catalog_embeddedRetentionPolicy() {
        val file = repoOrModuleFile(
            pathFromRepoRoot = "app/src/main/assets/whats_new/catalog.json",
            fallbackFromAppModule = "src/main/assets/whats_new/catalog.json"
        )
        val bytes = file.readBytes()
        assertThat(bytes.size).isAtMost(WhatsNewCatalog.MAX_EMBEDDED_CATALOG_BYTES)
        assertThat(releases.size).isAtMost(WhatsNewCatalog.MAX_EMBEDDED_RELEASES)
        val codes = releases.map { it.versionCode }
        assertThat(codes).isEqualTo(codes.sorted())
        assertThat(codes.distinct().size).isEqualTo(codes.size)
    }

    @Test
    fun catalog_parseAssetFile() {
        val file = repoOrModuleFile(
            pathFromRepoRoot = "app/src/main/assets/whats_new/catalog.json",
            fallbackFromAppModule = "src/main/assets/whats_new/catalog.json"
        )
        assertThat(file.isFile).isTrue()
        val parsed = WhatsNewCatalog.parse(file.readText())
        assertThat(parsed.releases).isNotEmpty()
        assertThat(parsed.genericFallbackBullet).isNotEmpty()
    }
}
