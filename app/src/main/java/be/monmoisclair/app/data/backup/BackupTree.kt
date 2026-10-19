package be.monmoisclair.app.data.backup

/**
 * Abstraction du dossier de backup (SAF en prod, fichiers locaux en tests).
 * Toute écriture doit être syncée sur le support avant fermeture.
 */
interface BackupTree {
    fun exists(fileName: String): Boolean
    fun delete(fileName: String): Boolean
    fun readUtf8(fileName: String): String
    fun writeUtf8Sync(fileName: String, content: String)
    fun rename(fromName: String, toName: String)
}
