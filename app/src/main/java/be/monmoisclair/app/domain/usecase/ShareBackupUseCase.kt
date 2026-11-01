package be.monmoisclair.app.domain.usecase

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import be.monmoisclair.app.data.backup.BackupCoordinator
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.backup.BackupSecurityArchive
import be.monmoisclair.app.data.backup.SafBackupStore
import be.monmoisclair.app.data.crypto.BackupEnvelope
import be.monmoisclair.app.data.crypto.ShareSecretVault
import be.monmoisclair.app.domain.ShareFileIntents
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class ShareBackupResult {
    data class Ready(val intent: Intent) : ShareBackupResult()
    data class Failed(val message: String) : ShareBackupResult()
}

/**
 * Prépare un [Intent.ACTION_SEND] : archive ZIP (JSON + photos) puis enveloppe AES-GCM (.mmc).
 * Sans le code de copie, WhatsApp / mail ne peuvent pas lire le fichier.
 */
@Singleton
class ShareBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coordinator: BackupCoordinator,
    private val safBackupStore: SafBackupStore,
    private val shareSecretVault: ShareSecretVault
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
            val zipTmp = File(shareDir, "pack-tmp.zip")
            val packed = BackupSecurityArchive.packToFile(zipTmp, json, receipts)
            val secret = shareSecretVault.getOrCreateSecretChars()
            val mmcOut = File(shareDir, BackupEnvelope.SHARE_MMC_NAME)
            try {
                if (mmcOut.exists()) mmcOut.delete()
                val sealed = BackupEnvelope.seal(packed.file.readBytes(), secret)
                mmcOut.writeBytes(sealed)
            } finally {
                secret.fill('\u0000')
                packed.file.delete()
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                mmcOut
            )
            val photoLine = when (packed.photoCount) {
                0 -> "Sans photo de facture pour l’instant."
                1 -> "1 photo de facture incluse."
                else -> "${packed.photoCount} photos de factures incluses."
            }
            val send = ShareFileIntents.encryptedCopy(
                uri = uri,
                subject = "Copie Mon Mois Clair",
                // P0 ops : rappeler le code SANS l’inclure ; jamais dans ce même message.
                text = "Copie Mon Mois Clair — $photoLine " +
                    "Sans le code de copie (Réglages → Sauvegarde sur le téléphone " +
                    "qui a créé la copie), ce fichier ne peut pas être rouvert. " +
                    "N’envoyez pas le code dans ce message — gardez-le sur papier. " +
                    "À restaurer via Réglages → Restaurer une sauvegarde."
            )
            ShareBackupResult.Ready(
                Intent.createChooser(send, "Envoyer une copie")
            )
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
