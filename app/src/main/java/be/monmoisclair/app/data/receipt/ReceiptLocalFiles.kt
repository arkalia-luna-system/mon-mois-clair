package be.monmoisclair.app.data.receipt

import be.monmoisclair.app.data.backup.BackupSecurityArchive
import be.monmoisclair.app.domain.ReceiptPathList
import java.io.File

/** Suppression des copies chiffrées sous `filesDir` (courrier retiré, etc.). */
object ReceiptLocalFiles {
    fun deleteUnder(filesDir: File, relativePathsRaw: String) {
        for (path in ReceiptPathList.parse(relativePathsRaw)) {
            val safe = BackupSecurityArchive.sanitizeReceiptPath(path) ?: continue
            File(filesDir, safe).takeIf { it.exists() }?.delete()
        }
    }
}
