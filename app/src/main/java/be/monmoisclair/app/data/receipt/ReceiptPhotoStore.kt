package be.monmoisclair.app.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.backup.BackupSecurityArchive
import be.monmoisclair.app.data.backup.SafBackupStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptPhotoStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safBackupStore: SafBackupStore
) {
    fun relativePathFor(expenseId: String) = "receipts/$expenseId.webp"

    suspend fun saveFromUri(expenseId: String, source: Uri): PhotoSaveResult {
        val bytes = context.contentResolver.openInputStream(source)?.use {
            // Plafond lecture (~12 Mo) — le décodage est ensuite sous-échantillonné.
            BackupSecurityArchive.readBounded(it, 12L * 1024L * 1024L)
        } ?: throw BackupFailure.Io("Impossible de lire la photo")
        val bitmap = decodeJpegBytesBounded(bytes)
            ?: throw BackupFailure.Io("Image illisible")
        return saveBitmap(expenseId, bitmap)
    }

    /**
     * Écrit d’abord une copie **locale** (relecture fiable), puis Documents (SAF).
     * Si SAF échoue, la photo reste visible dans l’app ([PhotoSaveResult.documentsCopyOk] = false).
     */
    suspend fun saveBitmap(expenseId: String, bitmap: Bitmap): PhotoSaveResult {
        val webp = compressWebp(bitmap)
        val relative = relativePathFor(expenseId)
        writeLocal(relative, webp)
        val documentsOk = runCatching {
            safBackupStore.writeBinaryRelative(relative, webp)
        }.isSuccess
        return PhotoSaveResult(relativePath = relative, documentsCopyOk = documentsOk)
    }

    suspend fun openUri(relativePath: String): Uri? =
        if (relativePath.isBlank()) null else safBackupStore.uriForRelative(relativePath)

    /** Décode dans l’app : Documents d’abord, sinon copie locale. */
    suspend fun decodeBitmap(relativePath: String): Bitmap? {
        if (relativePath.isBlank()) return null
        openUri(relativePath)?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()?.let { return it }
        }
        return readLocal(relativePath)
    }

    private fun localFile(relativePath: String): File? {
        val safe = BackupSecurityArchive.sanitizeReceiptPath(relativePath) ?: return null
        return File(context.filesDir, safe)
    }

    private fun writeLocal(relativePath: String, bytes: ByteArray) {
        val file = localFile(relativePath)
            ?: throw BackupFailure.Io("Chemin photo invalide")
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
    }

    private fun readLocal(relativePath: String): Bitmap? {
        val file = localFile(relativePath) ?: return null
        if (!file.isFile) return null
        return runCatching {
            file.inputStream().use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }

    companion object {
        /** Plein cadre lisible (IBAN / communication) — pas la miniature système. */
        const val MAX_SIDE_PX = 2048
        const val WEBP_QUALITY = 90

        /** Au-dessus de ~400 Ko : baisser la qualité, sans viser 80 Ko. */
        const val SOFT_MAX_BYTES = 400 * 1024

        data class PhotoSaveResult(
            val relativePath: String,
            val documentsCopyOk: Boolean
        )

        /** Décode un JPEG/PNG déjà lu en mémoire, borné à [MAX_SIDE_PX] (anti-OOM galerie). */
        fun decodeJpegBytesBounded(bytes: ByteArray, maxSide: Int = MAX_SIDE_PX): Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val longest = maxOf(bounds.outWidth, bounds.outHeight)
            var sample = 1
            while (longest / sample > maxSide) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        }

        fun compressWebp(source: Bitmap, maxSide: Int = MAX_SIDE_PX, quality: Int = WEBP_QUALITY): ByteArray {
            val scaled = scaleMax(source, maxSide)
            var q = quality.coerceIn(50, 100)
            var bytes = encodeWebp(scaled, q)
            while (bytes.size > SOFT_MAX_BYTES && q > 55) {
                q -= 5
                bytes = encodeWebp(scaled, q)
            }
            if (scaled !== source) scaled.recycle()
            return bytes
        }

        private fun encodeWebp(scaled: Bitmap, quality: Int): ByteArray {
            val out = ByteArrayOutputStream()
            val ok = if (android.os.Build.VERSION.SDK_INT >= 30) {
                scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, out)
            } else {
                @Suppress("DEPRECATION")
                scaled.compress(Bitmap.CompressFormat.WEBP, quality, out)
            }
            if (!ok) throw BackupFailure.Io("Compression WebP échouée")
            return out.toByteArray()
        }

        fun scaleMax(source: Bitmap, maxSide: Int): Bitmap {
            val longest = maxOf(source.width, source.height)
            if (longest <= maxSide) return source
            val scale = maxSide.toFloat() / longest
            return Bitmap.createScaledBitmap(
                source,
                (source.width * scale).toInt().coerceAtLeast(1),
                (source.height * scale).toInt().coerceAtLeast(1),
                true
            )
        }
    }
}
