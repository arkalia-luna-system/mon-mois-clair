package be.monmoisclair.app.ui.entry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import java.io.File

/** Sélecteur PDF (courrier mail) — ajout et édition, pas sur l’accueil. */
@Composable
internal fun rememberPdfPicker(
    onBytes: (ByteArray) -> Unit,
    onFailed: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                be.monmoisclair.app.data.backup.BackupSecurityArchive.readBounded(
                    it,
                    ReceiptPhotoStore.MAX_PDF_BYTES.toLong()
                )
            } ?: throw BackupFailure.Io("Impossible de lire le PDF")
            if (!ReceiptPhotoStore.looksLikePdf(bytes)) {
                throw BackupFailure.Io("Ce fichier n’est pas un PDF lisible.")
            }
            onBytes(bytes)
        }.onFailure { e ->
            val raw = e.message.orEmpty()
            val shown = if (raw.contains("trop lourde") || raw.contains("trop lourd")) {
                "Ce courrier est trop lourd."
            } else {
                raw.ifBlank { "Ce PDF n’a pas pu être lu. Réessayez." }
            }
            onFailed(shown)
        }
    }
    return { launcher.launch(arrayOf("application/pdf")) }
}

/** Capture plein cadre (pas la miniature TakePicturePreview). */
@Composable
internal fun rememberFullPhotoCapture(
    onBitmap: (Bitmap) -> Unit,
    onDecodeFailed: () -> Unit = {},
    onCaptureCancelled: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val file = pendingFile
        pendingFile = null
        if (!ok) {
            // E08 : annulation / refus → effacer le jpg temporaire + message.
            file?.delete()
            onCaptureCancelled()
            return@rememberLauncherForActivityResult
        }
        if (file == null || !file.exists()) {
            onCaptureCancelled()
            return@rememberLauncherForActivityResult
        }
        val decoded = runCatching { decodeCameraJpegBounded(file) }.getOrNull()
        file.delete()
        if (decoded != null) onBitmap(decoded) else onDecodeFailed()
    }
    return {
        val cameraDir = File(context.cacheDir, "camera").also { it.mkdirs() }
        val file = File(cameraDir, "receipt_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        pendingFile = file
        launcher.launch(uri)
    }
}

/** Évite OOM sur un JPEG caméra très lourd (borne ~2048 px) + EXIF Samsung. */
internal fun decodeCameraJpegBounded(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (longest / sample > ReceiptPhotoStore.MAX_SIDE_PX) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    val decoded = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
    return ReceiptPhotoStore.applyExifOrientation(decoded, file)
}

