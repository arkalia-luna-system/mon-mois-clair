package be.monmoisclair.app.data.receipt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.backup.BackupSecurityArchive
import be.monmoisclair.app.data.backup.SafBackupStore
import be.monmoisclair.app.data.crypto.LocalFileCipher
import be.monmoisclair.app.domain.ReceiptPathList
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptPhotoStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safBackupStore: SafBackupStore
) {
    fun relativePathFor(expenseId: String) = "receipts/$expenseId.webp"

    fun relativePdfPathFor(ownerId: String) = "receipts/$ownerId.pdf"

    /**
     * Un seul chemin `receipts/…` — refuse les chaînes multi-lignes brutes
     * (ex. loyer3 webp+PDF) qui faisaient échouer [sanitizeReceiptPath] / VIEW.
     */
    fun resolvedPath(relativePath: String): String {
        val paths = ReceiptPathList.parse(relativePath)
        if (paths.isEmpty()) return relativePath.trim()
        if (paths.size == 1) return paths[0]
        return paths.firstOrNull { !BackupSecurityArchive.isPdfReceiptPath(it) }
            ?: paths[0]
    }

    fun isPdf(relativePath: String): Boolean =
        BackupSecurityArchive.isPdfReceiptPath(resolvedPath(relativePath))

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

    /**
     * Copie un PDF (courrier reçu par mail) dans `receipts/…pdf`.
     * Pas d’aperçu in-app : [viewIntent] ouvre le lecteur du téléphone.
     */
    suspend fun savePdfBytes(ownerId: String, bytes: ByteArray): PhotoSaveResult {
        if (bytes.isEmpty()) throw BackupFailure.Io("PDF vide")
        if (bytes.size > MAX_PDF_BYTES) {
            throw BackupFailure.Io("Ce PDF est trop lourd (max ${MAX_PDF_BYTES / (1024 * 1024)} Mo).")
        }
        if (!looksLikePdf(bytes)) {
            throw BackupFailure.Io("Ce fichier n’est pas un PDF lisible.")
        }
        val relative = relativePdfPathFor(ownerId)
        writeLocal(relative, bytes)
        val documentsOk = runCatching {
            safBackupStore.writeBinaryRelative(relative, bytes)
        }.isSuccess
        return PhotoSaveResult(relativePath = relative, documentsCopyOk = documentsOk)
    }

    suspend fun savePdfFromUri(ownerId: String, source: Uri): PhotoSaveResult {
        val bytes = context.contentResolver.openInputStream(source)?.use {
            BackupSecurityArchive.readBounded(it, MAX_PDF_BYTES.toLong())
        } ?: throw BackupFailure.Io("Impossible de lire le PDF")
        return savePdfBytes(ownerId, bytes)
    }

    suspend fun openUri(relativePath: String): Uri? {
        val path = resolvedPath(relativePath)
        if (path.isBlank()) return null
        return safBackupStore.uriForRelative(path)
    }

    /**
     * URI pour [Intent.ACTION_VIEW] : déchiffre vers un cache temporaire (FileProvider),
     * car le lecteur PDF/photo du système ne lit pas MRF1.
     */
    suspend fun viewUri(relativePath: String): Uri? {
        val path = resolvedPath(relativePath)
        if (path.isBlank()) return null
        val plain = readLocalBytes(path)
            ?: openUri(path)?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use {
                    LocalFileCipher.open(BackupSecurityArchive.readBounded(it, MAX_PDF_BYTES.toLong()))
                }
            }
            ?: return null
        val safe = BackupSecurityArchive.sanitizeReceiptPath(path) ?: return null
        val viewDir = viewCacheDir().also { it.mkdirs() }
        // Purge aperçus > TTL (évite fuite cache clair).
        purgeStaleViewCache(viewDir)
        val out = File(viewDir, safe.substringAfterLast('/'))
        FileOutputStream(out).use { it.write(plain) }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            out
        )
    }

    /** Efface tout le cache `view/` (ex. au verrouillage biométrique). */
    fun purgeViewCache() {
        viewCacheDir().deleteRecursively()
    }

    private fun viewCacheDir(): File = File(context.cacheDir, VIEW_CACHE_DIR)

    private fun purgeStaleViewCache(viewDir: File) {
        val cutoff = System.currentTimeMillis() - VIEW_CACHE_TTL_MS
        viewDir.listFiles()?.forEach { f ->
            if (f.lastModified() < cutoff) f.delete()
        }
    }

    fun viewIntent(uri: Uri, relativePath: String): Intent {
        val path = resolvedPath(relativePath)
        val mime = BackupSecurityArchive.mimeForReceiptPath(path) ?: "*/*"
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            clipData = android.content.ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Décode dans l’app : **copie locale d’abord** (fiable), puis Documents.
     * Évite un fichier SAF tronqué / non rewindable qui masquait une bonne copie locale.
     * Pas pour un PDF. Tolère une chaîne multi-lignes (photo d’abord).
     */
    suspend fun decodeBitmap(relativePath: String): Bitmap? {
        val path = resolvedPath(relativePath)
        if (path.isBlank() || isPdf(path)) return null
        readLocal(path)?.let { return it }
        openUri(path)?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val raw = BackupSecurityArchive.readBounded(input, MAX_DECODE_BYTES)
                    decodeImageBytes(LocalFileCipher.open(raw))
                }
            }.getOrNull()?.let { return it }
        }
        return null
    }

    private fun localFile(relativePath: String): File? {
        val safe = BackupSecurityArchive.sanitizeReceiptPath(resolvedPath(relativePath))
            ?: return null
        return File(context.filesDir, safe)
    }

    private fun writeLocal(relativePath: String, bytes: ByteArray) {
        val file = localFile(relativePath)
            ?: throw BackupFailure.Io("Chemin photo invalide")
        LocalFileCipher.writeEncrypted(file, bytes)
    }

    private fun readLocalBytes(relativePath: String): ByteArray? {
        val file = localFile(relativePath) ?: return null
        return LocalFileCipher.readDecrypted(file)
    }

    private fun readLocal(relativePath: String): Bitmap? {
        val bytes = readLocalBytes(relativePath) ?: return null
        return runCatching { decodeImageBytes(bytes) }.getOrNull()
    }

    /** Courrier en attente retiré : effacer les copies locales (Documents/S AF inchangés). */
    fun deleteLocalPaths(relativePathsRaw: String) {
        ReceiptLocalFiles.deleteUnder(context.filesDir, relativePathsRaw)
    }

    companion object {
        /** Plein cadre lisible (IBAN / communication) — pas la miniature système. */
        const val MAX_SIDE_PX = 2048
        const val WEBP_QUALITY = 90

        /** Au-dessus de ~400 Ko : baisser la qualité, sans viser 80 Ko. */
        const val SOFT_MAX_BYTES = 400 * 1024

        /** Plafond PDF (mail énergie / huissier) — hors ZIP 45 Mo. */
        const val MAX_PDF_BYTES = 12 * 1024 * 1024

        /** Cache aperçu clair pour lecteur système — TTL agressif (2 min). */
        const val VIEW_CACHE_DIR = "view"
        const val VIEW_CACHE_TTL_MS = 2L * 60L * 1000L

        fun looksLikePdf(bytes: ByteArray): Boolean =
            bytes.size >= 5 &&
                bytes[0] == '%'.code.toByte() &&
                bytes[1] == 'P'.code.toByte() &&
                bytes[2] == 'D'.code.toByte() &&
                bytes[3] == 'F'.code.toByte() &&
                bytes[4] == '-'.code.toByte()

        data class PhotoSaveResult(
            val relativePath: String,
            val documentsCopyOk: Boolean
        )

        /** Plafond lecture photo (OCR / aperçu). Au-delà, on refuse plutôt que de saturer. */
        const val MAX_DECODE_BYTES = 8L * 1024 * 1024

        /**
         * Lit tout le flux puis décode. [BitmapFactory.decodeStream] renvoie null
         * sur un flux Documents sans retour arrière (photo webp déjà enregistrée).
         */
        fun decodeImageStream(input: InputStream, maxBytes: Long = MAX_DECODE_BYTES): Bitmap? =
            runCatching { decodeImageBytes(BackupSecurityArchive.readBounded(input, maxBytes)) }
                .getOrNull()

        /** JPEG, PNG ou WebP en mémoire, borné à [MAX_SIDE_PX]. JPEG : orientation EXIF appliquée. */
        fun decodeImageBytes(bytes: ByteArray, maxSide: Int = MAX_SIDE_PX): Bitmap? {
            if (bytes.isEmpty()) return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val longest = maxOf(bounds.outWidth, bounds.outHeight)
            var sample = 1
            while (longest / sample > maxSide) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null
            return applyExifOrientation(decoded, bytes)
        }

        /** Décode un JPEG/PNG déjà lu en mémoire, borné à [MAX_SIDE_PX] (anti-OOM galerie). */
        fun decodeJpegBytesBounded(bytes: ByteArray, maxSide: Int = MAX_SIDE_PX): Bitmap? =
            decodeImageBytes(bytes, maxSide)

        /**
         * Caméra Samsung / One UI : le JPEG est souvent stocké « couché » + tag EXIF.
         * Sans rotation ici, l’aperçu et le WebP restent à l’envers.
         */
        fun applyExifOrientation(bitmap: Bitmap, jpegFile: File): Bitmap {
            val orientation = runCatching {
                ExifInterface(jpegFile.absolutePath)
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
            return rotateForExif(bitmap, orientation)
        }

        fun applyExifOrientation(bitmap: Bitmap, jpegBytes: ByteArray): Bitmap {
            val orientation = runCatching {
                ExifInterface(ByteArrayInputStream(jpegBytes))
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
            return rotateForExif(bitmap, orientation)
        }

        private fun rotateForExif(bitmap: Bitmap, orientation: Int): Bitmap {
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
                else -> return bitmap
            }
            val out = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (out !== bitmap) bitmap.recycle()
            return out
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
