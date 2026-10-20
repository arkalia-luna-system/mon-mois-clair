package be.monmoisclair.app.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.backup.SafBackupStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptPhotoStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safBackupStore: SafBackupStore
) {
    fun relativePathFor(expenseId: String) = "receipts/$expenseId.webp"

    suspend fun saveFromUri(expenseId: String, source: Uri): String {
        val bytes = context.contentResolver.openInputStream(source)?.use { it.readBytes() }
            ?: throw BackupFailure.Io("Impossible de lire la photo")
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw BackupFailure.Io("Image illisible")
        return saveBitmap(expenseId, bitmap)
    }

    suspend fun saveBitmap(expenseId: String, bitmap: Bitmap): String {
        val webp = compressWebp(bitmap)
        val relative = relativePathFor(expenseId)
        safBackupStore.writeBinaryRelative(relative, webp)
        return relative
    }

    suspend fun openUri(relativePath: String): Uri? =
        if (relativePath.isBlank()) null else safBackupStore.uriForRelative(relativePath)

    companion object {
        /** Plein cadre lisible (IBAN / communication) — pas la miniature système. */
        const val MAX_SIDE_PX = 2048
        const val WEBP_QUALITY = 90

        fun compressWebp(source: Bitmap, maxSide: Int = MAX_SIDE_PX, quality: Int = WEBP_QUALITY): ByteArray {
            val scaled = scaleMax(source, maxSide)
            val out = ByteArrayOutputStream()
            val ok = if (android.os.Build.VERSION.SDK_INT >= 30) {
                scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, out)
            } else {
                @Suppress("DEPRECATION")
                scaled.compress(Bitmap.CompressFormat.WEBP, quality, out)
            }
            if (scaled !== source) scaled.recycle()
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
