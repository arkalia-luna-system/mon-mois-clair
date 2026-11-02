package be.monmoisclair.app.data.summary

import be.monmoisclair.app.data.crypto.BackupEnvelope
import java.io.File

/**
 * PDF résumé mois : **clair** pour le lecteur/imprimante du destinataire
 * (MMC1/MRF1 rendrait le fichier illisible hors app).
 *
 * Filet sécu : uniquement sous `cache/share/` (éphémère), jamais `files/` ;
 * purge TTL à l’écriture et à l’arrière-plan ([purgeStale]) — **pas** de wipe total
 * sur ON_STOP (sinon FileProvider casse pendant le sheet système).
 * [wipeShareCache] reste pour tests / nettoyage explicite.
 * Le même dossier héberge aussi le `.mmc` temporaire de partage.
 */
object MonthSummaryPdfShare {

    const val SHARE_SUBDIR = "share"
    const val FILE_NAME = MonthSummaryPdfWriter.FILE_NAME

    /** TTL cache clair (aligné aperçus `view/` : 2 min). */
    const val SHARE_TTL_MS = 2L * 60L * 1000L

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

    /**
     * Efface PDF résumé **et** `.mmc` temporaire (lock / ON_STOP).
     * Alias historique : [wipeSharePdfs].
     */
    fun wipeShareCache(cacheDir: File): Int {
        val dir = shareDir(cacheDir)
        if (!dir.isDirectory) return 0
        var n = 0
        dir.listFiles()?.forEach { f ->
            if (!f.isFile) return@forEach
            val name = f.name
            val wipe = name.endsWith(".pdf", ignoreCase = true) ||
                name.endsWith(".mmc", ignoreCase = true) ||
                name.equals(BackupEnvelope.SHARE_MMC_NAME, ignoreCase = true)
            if (wipe && f.delete()) n++
        }
        return n
    }

    /** @deprecated Prefer [wipeShareCache] — conserve le nom pour appels existants. */
    fun wipeSharePdfs(cacheDir: File): Int = wipeShareCache(cacheDir)

    fun purgeStale(shareDir: File, nowEpochMs: Long = System.currentTimeMillis()): Int {
        if (!shareDir.isDirectory) return 0
        val cutoff = nowEpochMs - SHARE_TTL_MS
        var n = 0
        shareDir.listFiles()?.forEach { f ->
            if (!f.isFile) return@forEach
            val stalePdf = f.name.endsWith(".pdf", ignoreCase = true) && f.lastModified() < cutoff
            val staleMmc = f.name.endsWith(".mmc", ignoreCase = true) && f.lastModified() < cutoff
            if ((stalePdf || staleMmc) && f.delete()) n++
        }
        return n
    }
}
