package be.monmoisclair.app.data.summary

import java.io.File

/**
 * PDF résumé mois : **clair** pour le lecteur/imprimante du destinataire
 * (MMC1/MRF1 rendrait le fichier illisible hors app).
 *
 * Filet sécu : uniquement sous `cache/share/` (éphémère), jamais `files/` ;
 * [wipeSharePdfs] au verrou biométrique + purge TTL à l’écriture.
 */
object MonthSummaryPdfShare {

    const val SHARE_SUBDIR = "share"
    const val FILE_NAME = MonthSummaryPdfWriter.FILE_NAME

    /** TTL cache clair (aligné aperçus `view/` : 15 min). */
    const val SHARE_TTL_MS = 15L * 60L * 1000L

    fun shareDir(cacheDir: File): File = File(cacheDir, SHARE_SUBDIR)

    fun targetFile(cacheDir: File): File = File(shareDir(cacheDir), FILE_NAME)

    /**
     * Prépare le dossier, purge les PDF share périmés, retourne la cible unique.
     * N’écrit pas encore le PDF — l’appelant (session déjà unlock) appelle le writer.
     */
    fun prepareTarget(cacheDir: File, nowEpochMs: Long = System.currentTimeMillis()): File {
        val dir = shareDir(cacheDir).also { it.mkdirs() }
        purgeStale(dir, nowEpochMs)
        val out = File(dir, FILE_NAME)
        if (out.exists()) out.delete()
        return out
    }

    /** Efface tous les PDF du dossier share (lock biométrique / fin de session). */
    fun wipeSharePdfs(cacheDir: File): Int {
        val dir = shareDir(cacheDir)
        if (!dir.isDirectory) return 0
        var n = 0
        dir.listFiles()?.forEach { f ->
            if (f.isFile && f.name.endsWith(".pdf", ignoreCase = true)) {
                if (f.delete()) n++
            }
        }
        return n
    }

    fun purgeStale(shareDir: File, nowEpochMs: Long = System.currentTimeMillis()): Int {
        if (!shareDir.isDirectory) return 0
        val cutoff = nowEpochMs - SHARE_TTL_MS
        var n = 0
        shareDir.listFiles()?.forEach { f ->
            if (f.isFile && f.name.endsWith(".pdf", ignoreCase = true) && f.lastModified() < cutoff) {
                if (f.delete()) n++
            }
        }
        return n
    }
}
