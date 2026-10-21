package be.monmoisclair.app.domain

/**
 * Lot C — plusieurs photos sur un dossier.
 * Stockage : chemins `receipts/….webp` séparés par `\n` (le premier = pastille accueil).
 */
object ReceiptPathList {

    fun parse(raw: String): List<String> =
        raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()

    fun encode(paths: List<String>): String =
        paths.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("\n")

    fun primary(raw: String): String = parse(raw).firstOrNull().orEmpty()

    fun append(raw: String, newPath: String): String {
        val path = newPath.trim()
        if (path.isEmpty()) return raw
        return encode(parse(raw) + path)
    }

    fun remove(raw: String, pathToRemove: String): String {
        val target = pathToRemove.trim()
        return encode(parse(raw).filter { it != target })
    }
}
