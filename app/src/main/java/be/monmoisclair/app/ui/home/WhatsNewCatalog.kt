package be.monmoisclair.app.ui.home

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Source unique versionnée des puces « Nouveautés » ([catalog.json]).
 * Bump `versionCode` + entrée dans le JSON = dialogue auto à l’ouverture.
 */
object WhatsNewCatalog {
    private const val ASSET_PATH = "whats_new/catalog.json"

    /** Entrées `releases` max dans l’asset embarqué (l’app ne montre que la version installée). */
    const val MAX_EMBEDDED_RELEASES = 8

    /** Plafond de taille du JSON embarqué (UTF-8), pour éviter la dérive du fichier. */
    const val MAX_EMBEDDED_CATALOG_BYTES = 5_120

    data class Release(
        val versionCode: Int,
        val bullets: List<String>,
        /** `false` = entrée technique, jamais montrée à l’utilisatrice. */
        val userVisible: Boolean = true,
    )

    data class Catalog(
        val genericFallbackBullet: String,
        val releases: List<Release>,
    )

    @Volatile
    private var cached: Catalog? = null

    fun load(context: Context): Catalog {
        cached?.let { return it }
        val json = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        return parse(json).also { cached = it }
    }

    fun releases(context: Context): List<Release> = load(context).releases

    fun genericFallback(context: Context): String = load(context).genericFallbackBullet

    fun parse(json: String): Catalog {
        val root = JSONObject(json)
        val fallback = root.getString("genericFallbackBullet").trim()
        require(fallback.isNotEmpty()) { "genericFallbackBullet vide" }
        val releasesArray = root.getJSONArray("releases")
        val releases = mutableListOf<Release>()
        for (i in 0 until releasesArray.length()) {
            releases += parseRelease(releasesArray.getJSONObject(i))
        }
        require(releases.isNotEmpty()) { "releases vide" }
        require(releases.size <= MAX_EMBEDDED_RELEASES) {
            "catalog.json : max $MAX_EMBEDDED_RELEASES entrées releases (rétention embarquée)"
        }
        releases.sortBy { it.versionCode }
        releases.forEach {
            require(it.bullets.isNotEmpty() || !it.userVisible) {
                "bullets vides pour ${it.versionCode}"
            }
        }
        val codes = releases.map { it.versionCode }
        require(codes.distinct().size == codes.size) { "versionCode dupliqué dans le catalogue" }
        return Catalog(fallback, releases)
    }

    private fun parseRelease(obj: JSONObject): Release {
        val code = obj.getInt("versionCode")
        val userVisible = obj.optBoolean("userVisible", true)
        val bullets = obj.getJSONArray("bullets").toStringList()
        return Release(
            versionCode = code,
            bullets = bullets,
            userVisible = userVisible,
        )
    }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { i -> getString(i).trim() }.filter { it.isNotEmpty() }
}
