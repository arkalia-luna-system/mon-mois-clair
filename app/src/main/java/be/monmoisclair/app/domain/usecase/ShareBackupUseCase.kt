package be.monmoisclair.app.domain.usecase

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import be.monmoisclair.app.data.backup.BackupCoordinator
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.backup.BackupSecurityArchive
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
 * Prépare un [Intent.ACTION_SEND] : archive ZIP (JSON + photos `receipts/`).
 */
@Singleton
class ShareBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coordinator: BackupCoordinator,
    private val safBackupStore: SafBackupStore
) {

    suspend operator fun invoke(): ShareBackupResult {
        if (!safBackupStore.hasPersistedTree()) {
            return ShareBackupResult.Failed(ShareBackupMessages.FOLDER_REQUIRED)
        }
        return try {
            when (val backup = coordinator.runAutoBackup()) {
                is BackupCoordinator.Result.Failed -> {
                    Log.e(TAG, "Sauvegarde avant partage: ${backup.failure.message}", backup.failure)
                    return ShareBackupResult.Failed(ShareBackupMessages.FAILED)
                }
                BackupCoordinator.Result.SkippedNotConfigured -> {
                    Log.w(TAG, "Partage: dossier non configuré après contrôle")
                    return ShareBackupResult.Failed(ShareBackupMessages.FOLDER_REQUIRED)
                }
                is BackupCoordinator.Result.Success -> Unit
            }
            val json = safBackupStore.readLatestValidBackupUtf8()
            val receipts = safBackupStore.listReceiptBinaries()
            val shareDir = File(context.cacheDir, "share").also { it.mkdirs() }
            val out = File(shareDir, BackupSecurityArchive.SHARE_ZIP_NAME)
            val packed = BackupSecurityArchive.packToFile(out, json, receipts)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                packed.file
            )
            val photoLine = when (packed.photoCount) {
                0 -> "Sans photo de facture pour l’instant."
                1 -> "1 photo de facture incluse."
                else -> "${packed.photoCount} photos de factures incluses."
            }
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("", uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde Mon Mois Clair")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Copie de sécurité Mon Mois Clair (mois + photos). $photoLine " +
                        "À restaurer via Réglages → Restaurer."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ShareBackupResult.Ready(Intent.createChooser(send, "Envoyer une copie de sécurité"))
        } catch (e: BackupFailure) {
            Log.e(TAG, "Partage BackupFailure: ${e.message}", e)
            ShareBackupResult.Failed(ShareBackupMessages.FAILED)
        } catch (e: Exception) {
            Log.e(TAG, "Partage inattendu: ${e.message}", e)
            ShareBackupResult.Failed(ShareBackupMessages.FAILED)
        }
    }

    companion object {
        private const val TAG = "ShareBackup"
    }
}
