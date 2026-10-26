package be.monmoisclair.app.data.crypto

import android.content.Context
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

/**
 * Migration one-shot : `mon_mois_clair.db` claire → SQLCipher, puis suppression de l’ancienne.
 *
 * Détection : marqueur [DbPassphraseVault.isDbEncryptedMarkerPresent].
 * Si aucune DB → marqueur posé après 1ʳᵉ ouverture Room (appelant).
 */
object EncryptedDbMigrator {

    private const val TAG = "EncryptedDbMigrator"

    fun migrateIfNeeded(context: Context, vault: DbPassphraseVault): MigrationOutcome {
        if (vault.isDbEncryptedMarkerPresent()) return MigrationOutcome.AlreadyEncrypted

        val dbFile = context.getDatabasePath(DbPassphraseVault.DB_NAME)
        if (!dbFile.exists()) {
            // Install fraîche : Room créera directement en SQLCipher.
            return MigrationOutcome.NoPlaintextDb
        }

        vault.ensureProvisioned()
        val passphrase = vault.unwrapPassphraseChars()
        val tmpEncrypted = File(dbFile.parentFile, "${DbPassphraseVault.DB_NAME}.migrating")
        try {
            if (tmpEncrypted.exists()) tmpEncrypted.delete()
            deleteSidecars(tmpEncrypted)

            System.loadLibrary("sqlcipher")
            // Ouvre en clair (passphrase vide) puis sqlcipher_export vers fichier chiffré.
            val plain = SQLiteDatabase.openOrCreateDatabase(
                dbFile.absolutePath,
                "",
                null,
                null
            )
            try {
                val keyLiteral = escapeSqlString(String(passphrase))
                plain.rawExecSQL(
                    "ATTACH DATABASE '${tmpEncrypted.absolutePath}' AS encrypted KEY '$keyLiteral';"
                )
                plain.rawExecSQL("SELECT sqlcipher_export('encrypted');")
                plain.rawExecSQL("DETACH DATABASE encrypted;")
            } finally {
                plain.close()
            }

            if (!tmpEncrypted.isFile || tmpEncrypted.length() <= 0L) {
                Log.e(TAG, "export SQLCipher vide")
                tmpEncrypted.delete()
                return MigrationOutcome.Failed
            }

            // Remplace l’ancienne DB (et sidecars WAL/SHM).
            plainCloseAndDelete(dbFile)
            if (!tmpEncrypted.renameTo(dbFile)) {
                // Fallback copie + delete
                tmpEncrypted.copyTo(dbFile, overwrite = true)
                tmpEncrypted.delete()
            }
            deleteSidecars(File(dbFile.parentFile, DbPassphraseVault.DB_NAME))
            vault.markDbEncrypted()
            return MigrationOutcome.Migrated
        } catch (e: Exception) {
            Log.e(TAG, "migration DB échouée — DB claire conservée", e)
            tmpEncrypted.delete()
            deleteSidecars(tmpEncrypted)
            return MigrationOutcome.Failed
        } finally {
            passphrase.fill('\u0000')
        }
    }

    private fun plainCloseAndDelete(dbFile: File) {
        deleteSidecars(dbFile)
        if (dbFile.exists()) dbFile.delete()
    }

    private fun deleteSidecars(dbFile: File) {
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
        File(dbFile.path + "-journal").delete()
    }

    /** Évite les injections dans ATTACH KEY '…'. Passphrase = hex → sûr, mais on escape quand même. */
    private fun escapeSqlString(value: String): String = value.replace("'", "''")

    enum class MigrationOutcome {
        AlreadyEncrypted,
        NoPlaintextDb,
        Migrated,
        Failed
    }
}
