package be.monmoisclair.app.domain.usecase

import android.content.ClipData
import android.content.Context
import android.content.Intent
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
            return ShareBackupResult.Failed(
                "Choisissez d’abord le dossier de sauvegarde (Documents)."
            )
        }
        return try {
            when (val backup = coordinator.runAutoBackup()) {
                is BackupCoordinator.Result.Failed ->
                    return ShareBackupResult.Failed(
                        backup.failure.message ?: "Sauvegarde impossible."
                    )
                BackupCoordinator.Result.SkippedNotConfigured ->
                    return ShareBackupResult.Failed("Dossier de sauvegarde non configuré.")
                is BackupCoordinator.Result.Success -> Unit
            }
            val json = safBackupStore.readLatestValidBackupUtf8()
            val receipts = safBackupStore.listReceiptBinaries()
            val out = File(context.cacheDir, BackupSecurityArchive.SHARE_ZIP_NAME)
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
            ShareBackupResult.Failed(e.message ?: "Partage impossible.")
        } catch (e: Exception) {
            ShareBackupResult.Failed(
                "Partage impossible. Réessayez, ou copiez le dossier Documents à la main."
            )
        }
    }
}
