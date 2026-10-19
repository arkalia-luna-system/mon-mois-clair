package be.monmoisclair.app.domain.usecase

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import be.monmoisclair.app.data.backup.BackupCoordinator
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.backup.BackupFileNames
import be.monmoisclair.app.data.backup.SafBackupStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class ShareBackupResult {
    data class Ready(val intent: Intent) : ShareBackupResult()
    data class Failed(val message: String) : ShareBackupResult()
}

/**
 * T8 : prépare un [Intent.ACTION_SEND] du dernier JSON backup (WhatsApp / Mail / …).
 */
@Singleton
class ShareBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coordinator: BackupCoordinator,
    private val safBackupStore: SafBackupStore
) {

    suspend operator fun invoke(): ShareBackupResult {
        if (!safBackupStore.hasPersistedTree()) {
            return ShareBackupResult.Failed(
                "Choisissez d’abord le dossier de sauvegarde (Documents)."
            )
        }
        return try {
            when (val backup = coordinator.runAutoBackup()) {
                is BackupCoordinator.Result.Failed ->
                    return ShareBackupResult.Failed(backup.failure.message ?: "Sauvegarde impossible.")
                BackupCoordinator.Result.SkippedNotConfigured ->
                    return ShareBackupResult.Failed("Dossier de sauvegarde non configuré.")
                is BackupCoordinator.Result.Success -> Unit
            }
            val json = safBackupStore.readLatestValidBackupUtf8()
            val out = File(context.cacheDir, BackupFileNames.V1)
            out.writeText(json, Charsets.UTF_8)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                out
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                // ClipData : sinon WhatsApp / Mail reçoivent souvent un URI sans permission.
                clipData = ClipData.newRawUri("", uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde Mon Mois Clair")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Copie de sécurité Mon Mois Clair (à garder hors du téléphone)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ShareBackupResult.Ready(Intent.createChooser(send, "Envoyer une copie de sécurité"))
        } catch (e: BackupFailure) {
            ShareBackupResult.Failed(e.message ?: "Partage impossible.")
        } catch (e: Exception) {
            ShareBackupResult.Failed("Partage impossible : ${e.message ?: "erreur inconnue"}")
        }
    }
}
