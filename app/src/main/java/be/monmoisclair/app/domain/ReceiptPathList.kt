package be.monmoisclair.app.domain

/**
 * Plusieurs pièces jointes (photos WebP + PDF).
 * Stockage : chemins `receipts/…` séparés par `\n`.
 * Sur un modèle : [extraReceiptPaths] = courriers en plus (la photo principale est à part).
 * Sur une facture « une fois » : tout peut vivre dans `receiptRelativePath` (photo d’abord, PDF ensuite).
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

    /** Photo (webp) en premier si présente, sinon premier chemin (souvent un PDF seul). */
    fun primaryPhotoOrFirst(raw: String, isPdf: (String) -> Boolean): String {
        val all = parse(raw)
        return all.firstOrNull { !isPdf(it) } ?: all.firstOrNull().orEmpty()
    }

    fun extrasBesidesPrimary(raw: String, isPdf: (String) -> Boolean): List<String> {
        val primary = primaryPhotoOrFirst(raw, isPdf)
        if (primary.isEmpty()) return emptyList()
        return parse(raw).filter { it != primary }
    }

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
